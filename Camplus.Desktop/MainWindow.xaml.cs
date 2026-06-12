using System;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
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
        private string _dbUsername = "root";
        private string _dbPassword = "123456";

        public MainWindow()
        {
            InitializeComponent();
            _httpClient = new HttpClient { Timeout = TimeSpan.FromSeconds(5) };
            Loaded += MainWindow_Loaded;
            Closing += MainWindow_Closing;
        }

        private async void MainWindow_Loaded(object sender, RoutedEventArgs e)
        {
            UpdateStatus("获取数据库配置...");
            ShowDbCredentialsDialog();
            
            UpdateStatus("初始化WebView2...");
            _cancellationTokenSource = new CancellationTokenSource();
            
            try
            {
                await WebView.EnsureCoreWebView2Async();
                WebView.CoreWebView2.Settings.AreDefaultContextMenusEnabled = true;
                WebView.CoreWebView2.Settings.AreDevToolsEnabled = true;
                WebView.CoreWebView2.Settings.IsStatusBarEnabled = false;
                UpdateStatus("WebView2初始化完成");
                
                await Task.Delay(1000);
                
                UpdateStatus("检查服务状态...");
                if (await IsServiceRunning(_cancellationTokenSource.Token))
                {
                    UpdateStatus("服务运行中，加载页面...");
                    WebView.Source = new Uri(_baseUrl);
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

        private void ShowDbCredentialsDialog()
        {
            var usernameDialog = new InputDialog("MySQL配置", "请输入MySQL用户名:", _dbUsername);
            if (usernameDialog.ShowDialog() == true && !string.IsNullOrEmpty(usernameDialog.InputText))
            {
                _dbUsername = usernameDialog.InputText;
            }

            var passwordDialog = new PasswordDialog("MySQL配置", "请输入MySQL密码:");
            if (passwordDialog.ShowDialog() == true)
            {
                _dbPassword = passwordDialog.Password;
            }
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

                var warPath = Path.Combine(projectRoot, "target", "Camplus.war");
                
                if (!File.Exists(warPath))
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
                    FileName = "cmd.exe",
                    Arguments = $"/c chcp 65001 >nul && title Camplus后端服务 && set DB_USERNAME={_dbUsername} && set DB_PASSWORD={_dbPassword} && java -Dfile.encoding=UTF-8 -jar \"{warPath}\"",
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
                        UpdateStatus("服务启动成功，加载页面...");
                        await Task.Delay(1000);
                        WebView.Source = new Uri(_baseUrl);
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