using System;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using Microsoft.Web.WebView2.Core;

namespace Camplus.Desktop
{
    public partial class MainWindow : Window
    {
        private Process? _springBootProcess;
        private readonly string _baseUrl = "http://localhost:8080";
        private readonly HttpClient _httpClient;
        private CancellationTokenSource? _cancellationTokenSource;

        public MainWindow()
        {
            InitializeComponent();
            _httpClient = new HttpClient { Timeout = TimeSpan.FromSeconds(5) };
            Loaded += MainWindow_Loaded;
            Closing += MainWindow_Closing;
        }

        private async void MainWindow_Loaded(object sender, RoutedEventArgs e)
        {
            UpdateStatus("初始化WebView2...");
            _cancellationTokenSource = new CancellationTokenSource();
            
            try
            {
                var userDataFolder = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "WebView2Cache");
                if (Directory.Exists(userDataFolder))
                {
                    try { Directory.Delete(userDataFolder, true); } catch { }
                }
                
                var env = await CoreWebView2Environment.CreateAsync(null, userDataFolder);
                await WebView.EnsureCoreWebView2Async(env);
                WebView.CoreWebView2.Settings.AreDefaultContextMenusEnabled = true;
                WebView.CoreWebView2.Settings.AreDevToolsEnabled = true;
                WebView.CoreWebView2.Settings.IsStatusBarEnabled = false;
                WebView.CoreWebView2.SetVirtualHostNameToFolderMapping("app", AppDomain.CurrentDomain.BaseDirectory, CoreWebView2HostResourceAccessKind.Allow);
                UpdateStatus("WebView2初始化完成");
                
                await Task.Delay(1000);
                
                UpdateStatus("检查服务状态...");
                if (await IsServiceRunning(_cancellationTokenSource.Token))
                {
                    UpdateStatus("服务运行中，检查数据库连接...");
                    await CheckAndConfigureDatabase();
                }
                else
                {
                    UpdateStatus("服务未运行，启动服务...");
                    await StartSpringBootApp(_cancellationTokenSource.Token);
                }
            }
            catch (Exception ex)
            {
                UpdateStatus($"初始化失败: {ex.Message}");
                MessageBox.Show($"初始化失败: {ex.Message}\n\n{ex.StackTrace}", "错误", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async Task CheckAndConfigureDatabase()
        {
            try
            {
                var response = await _httpClient.GetFromJsonAsync<DbStatusResponse>($"{_baseUrl}/api/db/status");
                if (response != null && response.Connected)
                {
                    UpdateStatus($"数据库连接成功: {response.Username}");
                    WebView.Source = new Uri(_baseUrl);
                    return;
                }
            }
            catch (Exception ex)
            {
                UpdateStatus($"检查数据库状态失败: {ex.Message}");
            }

            UpdateStatus("数据库连接失败，需要配置数据库凭据");
            await ShowDbCredentialsDialogAndUpdate();
        }

        private async Task ShowDbCredentialsDialogAndUpdate()
        {
            bool isValid = false;
            string username = "";
            string password = "";

            while (!isValid)
            {
                var usernameDialog = new InputDialog("MySQL配置", "请输入MySQL用户名:", username);
                if (usernameDialog.ShowDialog() == true && !string.IsNullOrEmpty(usernameDialog.InputText))
                {
                    username = usernameDialog.InputText;
                }
                else
                {
                    Environment.Exit(0);
                    return;
                }

                var passwordDialog = new PasswordDialog("MySQL配置", "请输入MySQL密码:");
                if (passwordDialog.ShowDialog() == true)
                {
                    password = passwordDialog.Password;
                }
                else
                {
                    Environment.Exit(0);
                    return;
                }

                UpdateStatus("正在验证MySQL连接...");
                isValid = await ValidateDatabaseConnection(username, password);
                if (!isValid)
                {
                    MessageBox.Show("MySQL连接失败，请检查用户名和密码是否正确", "连接失败", MessageBoxButton.OK, MessageBoxImage.Error);
                }
                else
                {
                    UpdateStatus("MySQL连接验证成功");
                    WebView.Source = new Uri(_baseUrl);
                }
            }
        }

        private async Task<bool> ValidateDatabaseConnection(string username, string password)
        {
            try
            {
                var response = await _httpClient.PostAsJsonAsync($"{_baseUrl}/api/db/update", new
                {
                    username = username,
                    password = password
                });

                if (response.IsSuccessStatusCode)
                {
                    var result = await response.Content.ReadFromJsonAsync<DbResponse>();
                    return result != null && result.Success;
                }
            }
            catch (Exception ex)
            {
                UpdateStatus($"验证数据库连接异常: {ex.Message}");
            }
            return false;
        }

        private async Task StartSpringBootApp(CancellationToken cancellationToken)
        {
            try
            {
                var baseDir = AppDomain.CurrentDomain.BaseDirectory;
                var projectRoot = FindProjectRoot(baseDir);
                
                if (projectRoot == null)
                {
                    MessageBox.Show("无法找到项目根目录", "错误", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }

                var jarPath = Path.Combine(projectRoot, "target", "Camplus.jar");
                
                if (!File.Exists(jarPath))
                {
                    UpdateStatus("编译项目...");
                    var mavenResult = await RunMavenBuild(projectRoot, cancellationToken);
                    if (!mavenResult)
                    {
                        MessageBox.Show("编译失败", "错误", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }
                }

                var startInfo = new ProcessStartInfo
                {
                    FileName = "java",
                    Arguments = "-Dfile.encoding=UTF-8 -jar \"" + jarPath + "\"",
                    WorkingDirectory = projectRoot,
                    UseShellExecute = true,
                    CreateNoWindow = false
                };

                _springBootProcess = new Process { StartInfo = startInfo };
                _springBootProcess.Start();

                UpdateStatus("等待服务启动...");
                var maxWaitTime = TimeSpan.FromSeconds(120);
                var waitStartTime = DateTime.Now;
                
                while (!cancellationToken.IsCancellationRequested && DateTime.Now - waitStartTime < maxWaitTime)
                {
                    if (await IsServiceRunning(cancellationToken))
                    {
                        UpdateStatus("服务启动成功，检查数据库连接...");
                        await CheckAndConfigureDatabase();
                        return;
                    }
                    await Task.Delay(2000);
                }

                UpdateStatus("服务启动超时");
            }
            catch (Exception ex)
            {
                UpdateStatus($"启动失败: {ex.Message}");
                MessageBox.Show($"启动失败: {ex.Message}", "错误", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async Task<bool> RunMavenBuild(string projectRoot, CancellationToken cancellationToken)
        {
            try
            {
                var mvnCmd = OperatingSystem.IsWindows() ? "mvn.cmd" : "mvn";
                var startInfo = new ProcessStartInfo
                {
                    FileName = mvnCmd,
                    Arguments = "clean package -DskipTests",
                    WorkingDirectory = projectRoot,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };

                using var process = Process.Start(startInfo);
                if (process == null) return false;
                await process.WaitForExitAsync(cancellationToken);
                return process.ExitCode == 0;
            }
            catch { return false; }
        }

        private async Task<bool> IsServiceRunning(CancellationToken cancellationToken)
        {
            try
            {
                var response = await _httpClient.GetAsync(_baseUrl, cancellationToken);
                return response.IsSuccessStatusCode;
            }
            catch { return false; }
        }

        private string? FindProjectRoot(string startPath)
        {
            var currentDir = new DirectoryInfo(startPath);
            while (currentDir != null)
            {
                if (File.Exists(Path.Combine(currentDir.FullName, "pom.xml")))
                    return currentDir.FullName;
                if (Directory.Exists(Path.Combine(currentDir.FullName, "target")))
                    return currentDir.FullName;
                if (Directory.Exists(Path.Combine(currentDir.FullName, "src")))
                    return currentDir.FullName;
                currentDir = currentDir.Parent;
            }
            return null;
        }

        private void UpdateStatus(string status)
        {
            try { Dispatcher.Invoke(() => StatusText.Text = status); }
            catch { }
        }

        private void RefreshBtn_Click(object sender, RoutedEventArgs e)
        {
            WebView.Reload();
        }

        private void DevToolsBtn_Click(object sender, RoutedEventArgs e)
        {
            WebView.CoreWebView2?.OpenDevToolsWindow();
        }

        private void MainWindow_Closing(object? sender, System.ComponentModel.CancelEventArgs e)
        {
            _cancellationTokenSource?.Cancel();
            if (_springBootProcess != null && !_springBootProcess.HasExited)
            {
                try { _springBootProcess.Kill(); _springBootProcess.WaitForExit(5000); }
                catch { }
            }
            _httpClient.Dispose();
            _cancellationTokenSource?.Dispose();
        }

        private class DbStatusResponse
        {
            public bool Connected { get; set; }
            public string Username { get; set; } = "";
        }

        private class DbResponse
        {
            public bool Success { get; set; }
            public string Message { get; set; } = "";
        }
    }

    public class InputDialog : Window
    {
        public string InputText { get; set; }

        public InputDialog(string title, string message, string defaultValue = "")
        {
            Title = title;
            Width = 350;
            Height = 150;
            WindowStartupLocation = WindowStartupLocation.CenterOwner;
            ResizeMode = ResizeMode.NoResize;

            InputText = defaultValue;

            var grid = new Grid();
            grid.Margin = new Thickness(10);
            Content = grid;

            var row1 = new RowDefinition { Height = GridLength.Auto };
            var row2 = new RowDefinition { Height = GridLength.Auto };
            var row3 = new RowDefinition { Height = GridLength.Auto };
            grid.RowDefinitions.Add(row1);
            grid.RowDefinitions.Add(row2);
            grid.RowDefinitions.Add(row3);

            var label = new Label { Content = message, Margin = new Thickness(0, 0, 0, 10) };
            Grid.SetRow(label, 0);
            grid.Children.Add(label);

            var textBox = new TextBox { Text = defaultValue, Margin = new Thickness(0, 0, 0, 10) };
            Grid.SetRow(textBox, 1);
            grid.Children.Add(textBox);
            textBox.KeyDown += (s, e) => { if (e.Key == System.Windows.Input.Key.Enter) { InputText = textBox.Text; DialogResult = true; Close(); } };
            textBox.Focus();

            var buttonPanel = new StackPanel { Orientation = Orientation.Horizontal, HorizontalAlignment = HorizontalAlignment.Right };
            Grid.SetRow(buttonPanel, 2);

            var okButton = new Button { Content = "确定", Width = 70, Margin = new Thickness(0, 0, 5, 0) };
            okButton.Click += (s, e) => { InputText = textBox.Text; DialogResult = true; Close(); };
            buttonPanel.Children.Add(okButton);

            var cancelButton = new Button { Content = "取消", Width = 70 };
            cancelButton.Click += (s, e) => { DialogResult = false; Close(); };
            buttonPanel.Children.Add(cancelButton);

            grid.Children.Add(buttonPanel);
        }
    }

    public class PasswordDialog : Window
    {
        public string Password { get; set; }

        public PasswordDialog(string title, string message)
        {
            Title = title;
            Width = 350;
            Height = 150;
            WindowStartupLocation = WindowStartupLocation.CenterOwner;
            ResizeMode = ResizeMode.NoResize;

            Password = "";

            var grid = new Grid();
            grid.Margin = new Thickness(10);
            Content = grid;

            var row1 = new RowDefinition { Height = GridLength.Auto };
            var row2 = new RowDefinition { Height = GridLength.Auto };
            var row3 = new RowDefinition { Height = GridLength.Auto };
            grid.RowDefinitions.Add(row1);
            grid.RowDefinitions.Add(row2);
            grid.RowDefinitions.Add(row3);

            var label = new Label { Content = message, Margin = new Thickness(0, 0, 0, 10) };
            Grid.SetRow(label, 0);
            grid.Children.Add(label);

            var passwordBox = new PasswordBox { Margin = new Thickness(0, 0, 0, 10) };
            Grid.SetRow(passwordBox, 1);
            grid.Children.Add(passwordBox);
            passwordBox.KeyDown += (s, e) => { if (e.Key == System.Windows.Input.Key.Enter) { Password = passwordBox.Password; DialogResult = true; Close(); } };
            passwordBox.Focus();

            var buttonPanel = new StackPanel { Orientation = Orientation.Horizontal, HorizontalAlignment = HorizontalAlignment.Right };
            Grid.SetRow(buttonPanel, 2);

            var okButton = new Button { Content = "确定", Width = 70, Margin = new Thickness(0, 0, 5, 0) };
            okButton.Click += (s, e) => { Password = passwordBox.Password; DialogResult = true; Close(); };
            buttonPanel.Children.Add(okButton);

            var cancelButton = new Button { Content = "取消", Width = 70 };
            cancelButton.Click += (s, e) => { DialogResult = false; Close(); };
            buttonPanel.Children.Add(cancelButton);

            grid.Children.Add(buttonPanel);
        }
    }
}