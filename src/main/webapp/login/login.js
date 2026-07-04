// 已登录则直接跳转首页
if (sessionStorage.getItem('username')) {
    window.location.href = '/home/index.html';
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



registerLink.addEventListener('click', (e) => {
    e.preventDefault();
    showRegisterForm();
});

backToLoginFromForm.addEventListener('click', showLogin);

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
        showToast('请先填写SMTP授权码', 'warning');
        return;
    }
    
    try {
        const { data: result } = await axios.post('/sendCode',
            new URLSearchParams({ target, type: 'email', smtpPassword: smtpPass }),
            { withCredentials: true });
        
        if (result.success) {
            showToast('验证码已发送', 'success');
            startCountdown();
        } else {
            showToast(result.msg, 'error');
        }
    } catch (err) {
        console.error(err);
        showToast('请求异常，请稍后重试', 'error');
    }
});

const loginForm = document.getElementById('loginForm');
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const loginAccount = document.getElementById('loginAccount').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!loginAccount || !password) {
        showToast('请填写账号和密码', 'warning');
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
            showToast(result.msg, 'success');
            setTimeout(() => {
                window.location.href = '/home/index.html';
            }, 500);
        } else {
            showToast(result.msg, 'error');
        }
    } catch (err) {
        console.error(err);
        showToast('请求异常，请稍后重试', 'error');
    }
});

registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const inputValue = registerInput.value.trim();
    const password = document.getElementById('registerPassword').value.trim();
    const code = verificationCode.value.trim();
    const smtpPass = smtpPassword.value.trim();

    if (!inputValue || !password) {
        showToast('请填写完整信息', 'warning');
        return;
    }
    if (!code) {
        showToast('请输入验证码', 'warning');
        return;
    }
    if (!smtpPass) {
        showToast('请填写SMTP授权码', 'warning');
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
            showToast(result.msg, 'success');
            setTimeout(() => {
                window.location.href = '/home/index.html';
            }, 500);
        } else {
            showToast(result.msg, 'error');
        }
    } catch (err) {
        console.error(err);
        showToast('请求异常，请稍后重试', 'error');
    }
});