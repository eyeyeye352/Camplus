// 已登录则直接跳转首页
if (sessionStorage.getItem('username')) {
    window.location.href = '/home/index.html';
}

const menuButton = document.getElementById('menuButton');
const sideMenu = document.getElementById('sideMenu');
const musicButton = document.getElementById('musicButton');
const bgMusic = document.getElementById('bgMusic');
const avatarCircle = document.getElementById('avatarCircle');
const accountName = document.getElementById('accountName');
const menuLoginBtn = document.getElementById('menuLoginBtn');
const adminMenuItem = document.getElementById('adminMenuItem');

function goToLogin() {
}

function updateLoginUI() {
    const username = sessionStorage.getItem('username');
    if (username) {
        accountName.textContent = username;
        avatarCircle.classList.add('logged-in');
        menuLoginBtn.textContent = '登出';
        menuLoginBtn.classList.add('logged-in');
    } else {
        accountName.textContent = '游客';
        avatarCircle.classList.remove('logged-in');
        menuLoginBtn.textContent = '登入/注册';
        menuLoginBtn.classList.remove('logged-in');
    }
}

const loginContent = document.getElementById('loginContent');
const registerFormContent = document.getElementById('registerFormContent');
const registerLink = document.getElementById('registerLink');
const backToLoginFromForm = document.getElementById('backToLoginFromForm');
const registerForm = document.getElementById('registerForm');
const registerInput = document.getElementById('registerInput');
const verificationCodeGroup = document.getElementById('verificationCodeGroup');
const verificationCode = document.getElementById('verificationCode');
const sendCodeBtn = document.getElementById('sendCodeBtn');
const smtpPassword = document.getElementById('smtpPassword');
const visitorLoginLink = document.getElementById('visitorLoginLink');

let countdownTimer = null;

function showLogin() {
    loginContent.style.display = 'block';
    registerFormContent.style.display = 'none';
    clearCountdown();
}

function showRegisterForm() {
    registerInput.value = '';
    verificationCode.value = '';
    smtpPassword.value = '';
    document.getElementById('registerPassword').value = '';
    
    sendCodeBtn.disabled = true;
    sendCodeBtn.classList.add('disabled');
    sendCodeBtn.textContent = '获取验证码';
    
    clearCountdown();
    
    loginContent.style.display = 'none';
    registerFormContent.style.display = 'block';
}

function clearCountdown() {
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
}

function startCountdown() {
    let seconds = 60;
    sendCodeBtn.disabled = true;
    sendCodeBtn.classList.add('disabled');
    
    countdownTimer = setInterval(() => {
        seconds--;
        sendCodeBtn.textContent = `${seconds}秒后重新获取`;
        
        if (seconds <= 0) {
            clearCountdown();
            sendCodeBtn.disabled = false;
            sendCodeBtn.classList.remove('disabled');
            sendCodeBtn.textContent = '获取验证码';
        }
    }, 1000);
}

let isMusicPlaying = true;
const musicSlash = document.querySelector('.music-slash');

function toggleMusic() {
    if (isMusicPlaying) {
        bgMusic.pause();
        musicSlash.style.display = '';
        musicButton.style.opacity = '0.5';
    } else {
        bgMusic.play().then(() => {
            musicSlash.style.display = 'none';
            musicButton.style.opacity = '1';
        }).catch(err => {
            console.log('需要用户交互才能播放音乐', err);
        });
    }
    isMusicPlaying = !isMusicPlaying;
}

document.body.addEventListener('click', function playMusicOnFirstClick() {
    if (!isMusicPlaying) {
        bgMusic.play().then(() => {
            musicSlash.style.display = 'none';
            musicButton.style.opacity = '1';
            isMusicPlaying = true;
        }).catch(err => {
            console.log('需要用户交互', err);
        });
    }
    document.body.removeEventListener('click', playMusicOnFirstClick);
}, { once: true });

bgMusic.play().catch(err => {
    console.log('等待用户交互后播放', err);
    isMusicPlaying = false;
    musicSlash.style.display = '';
    musicButton.style.opacity = '0.5';
});

musicButton.addEventListener('click', (e) => {
    e.stopPropagation();
    toggleMusic();
});

menuButton.addEventListener('click', () => {
    sideMenu.classList.toggle('active');
});

menuLoginBtn.addEventListener('click', () => {
    if (sessionStorage.getItem('username')) {
        sessionStorage.clear();
        location.reload();
    }
});

adminMenuItem.addEventListener('click', async (e) => {
    e.preventDefault();
    alert('请先登录');
});

updateLoginUI();

registerLink.addEventListener('click', (e) => {
    e.preventDefault();
    showRegisterForm();
});

backToLoginFromForm.addEventListener('click', showLogin);

visitorLoginLink.addEventListener('click', (e) => {
    e.preventDefault();
    sessionStorage.setItem('userId', '-1');
    sessionStorage.setItem('username', '游客');
    sessionStorage.setItem('email', '');
    sessionStorage.setItem('phone', '');
    sessionStorage.setItem('nickname', '游客');
    sessionStorage.setItem('avatarUrl', '');
    sessionStorage.setItem('role', '0');
    sessionStorage.setItem('status', '1');
    alert('欢迎，游客！');
    window.location.href = '/home/index.html';
});

registerInput.addEventListener('input', () => {
    const value = registerInput.value.trim();
    const isValid = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(value);
    if (isValid) {
        sendCodeBtn.disabled = false;
        sendCodeBtn.classList.remove('disabled');
    } else {
        sendCodeBtn.disabled = true;
        sendCodeBtn.classList.add('disabled');
    }
});

sendCodeBtn.addEventListener('click', async () => {
    const target = registerInput.value.trim();
    const smtpPass = smtpPassword.value.trim();
    
    if (!smtpPass) {
        alert('请先填写SMTP授权码');
        return;
    }
    
    try {
        const { data: result } = await axios.post('/sendCode',
            new URLSearchParams({ target, type: 'email', smtpPassword: smtpPass }),
            { withCredentials: true });
        
        if (result.success) {
            alert('验证码已发送');
            startCountdown();
        } else {
            alert(result.msg);
        }
    } catch (err) {
        console.error(err);
        alert('请求异常，请稍后重试');
    }
});

const loginForm = document.getElementById('loginForm');
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const loginAccount = document.getElementById('loginAccount').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!loginAccount || !password) {
        alert('请填写账号和密码');
        return;
    }

    try {
        const { data: result } = await axios.post('/login',
            new URLSearchParams({ loginAccount, password }),
            { withCredentials: true });

        if (result.success) {
            const user = result.data;
            sessionStorage.setItem('userId', user.userId);
            sessionStorage.setItem('username', user.username);
            sessionStorage.setItem('email', user.email || '');
            sessionStorage.setItem('phone', user.phone || '');
            sessionStorage.setItem('nickname', user.nickname || '');
            sessionStorage.setItem('avatarUrl', user.avatarUrl || '');
            sessionStorage.setItem('role', user.role || '0');
            sessionStorage.setItem('status', user.status || '1');
            alert(result.msg);
            window.location.href = '/home/index.html';
        } else {
            alert(result.msg);
        }
    } catch (err) {
        console.error(err);
        alert('请求异常，请稍后重试');
    }
});

registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const inputValue = registerInput.value.trim();
    const password = document.getElementById('registerPassword').value.trim();
    const code = verificationCode.value.trim();
    const smtpPass = smtpPassword.value.trim();

    if (!inputValue || !password) {
        alert('请填写完整信息');
        return;
    }
    if (!code) {
        alert('请输入验证码');
        return;
    }
    if (!smtpPass) {
        alert('请填写SMTP授权码');
        return;
    }

    const params = new URLSearchParams({ email: inputValue, password, code, smtpPassword: smtpPass });

    try {
        const { data: result } = await axios.post('/register', params, { withCredentials: true });

        if (result.success) {
            const user = result.data;
            sessionStorage.setItem('userId', user.userId);
            sessionStorage.setItem('username', user.username);
            sessionStorage.setItem('email', user.email || '');
            sessionStorage.setItem('phone', user.phone || '');
            sessionStorage.setItem('nickname', user.nickname || '');
            sessionStorage.setItem('avatarUrl', user.avatarUrl || '');
            sessionStorage.setItem('role', user.role || '0');
            sessionStorage.setItem('status', user.status || '1');
            alert(result.msg);
            window.location.href = '/home/index.html';
        } else {
            alert(result.msg);
        }
    } catch (err) {
        console.error(err);
        alert('请求异常，请稍后重试');
    }
});