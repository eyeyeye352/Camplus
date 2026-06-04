const menuButton = document.getElementById('menuButton');
const sideMenu = document.getElementById('sideMenu');
const menuOverlay = document.getElementById('menuOverlay');
const musicButton = document.getElementById('musicButton');
const bgMusic = document.getElementById('bgMusic');
const menuLoginBtn = document.getElementById('menuLoginBtn');

// 注册相关元素
const loginContent = document.getElementById('loginContent');
const registerSelectContent = document.getElementById('registerSelectContent');
const registerFormContent = document.getElementById('registerFormContent');
const registerLink = document.getElementById('registerLink');
const backToLoginFromSelect = document.getElementById('backToLoginFromSelect');
const backToLoginFromForm = document.getElementById('backToLoginFromForm');
const backToSelect = document.getElementById('backToSelect');
const emailRegisterBtn = document.getElementById('emailRegisterBtn');
const phoneRegisterBtn = document.getElementById('phoneRegisterBtn');
const usernameRegisterBtn = document.getElementById('usernameRegisterBtn');
const registerForm = document.getElementById('registerForm');
const registerInput = document.getElementById('registerInput');
const registerTitle = document.getElementById('registerTitle');

// 当前注册类型
let currentRegisterType = '';

// 跳转到首页
function goToHome() {
    window.location.href = '/home/index.html';
}

// 显示登录界面
function showLogin() {
    loginContent.style.display = 'block';
    registerSelectContent.style.display = 'none';
    registerFormContent.style.display = 'none';
}

// 显示注册选择界面
function showRegisterSelect() {
    loginContent.style.display = 'none';
    registerSelectContent.style.display = 'block';
    registerFormContent.style.display = 'none';
}

// 显示注册表单界面
function showRegisterForm(type, placeholder, title) {
    currentRegisterType = type;
    registerInput.placeholder = placeholder;
    registerTitle.textContent = title;
    registerInput.value = '';
    document.getElementById('registerPassword').value = '';
    loginContent.style.display = 'none';
    registerSelectContent.style.display = 'none';
    registerFormContent.style.display = 'block';
}

// 音乐控制
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
    menuOverlay.classList.toggle('active');
});

menuOverlay.addEventListener('click', () => {
    sideMenu.classList.remove('active');
    menuOverlay.classList.remove('active');
});



// 登录相关按钮点击事件
menuLoginBtn.addEventListener('click', () => {
    // 已在登录页，无需操作
});

// 注册链接点击事件
registerLink.addEventListener('click', (e) => {
    e.preventDefault();
    showRegisterSelect();
});

// 返回登录点击事件
backToLoginFromSelect.addEventListener('click', showLogin);
backToLoginFromForm.addEventListener('click', showLogin);
backToSelect.addEventListener('click', showRegisterSelect);

// 注册类型选择
emailRegisterBtn.addEventListener('click', () => {
    showRegisterForm('email', '请输入邮箱', '邮箱注册');
});

phoneRegisterBtn.addEventListener('click', () => {
    showRegisterForm('phone', '请输入手机号', '手机号注册');
});

usernameRegisterBtn.addEventListener('click', () => {
    showRegisterForm('username', '请输入用户名', '用户名注册');
});


// ========== 登录表单提交【已修改】 ==========
const loginForm = document.getElementById('loginForm');
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const loginAccount = document.getElementById('loginAccount').value.trim();
    const password = document.getElementById('password').value.trim();
    const rememberMe = document.querySelector('input[name="rememberMe"]')?.checked ? 'on' : 'off';

    if (!loginAccount || !password) {
        alert('请填写账号和密码');
        return;
    }

    try {
        // 标准参数构造，自动编码中文/特殊字符
        const params = new URLSearchParams();
        params.append('loginAccount', loginAccount);
        params.append('password', password);
        params.append('rememberMe', rememberMe);

        const res = await fetch('/login', {
            method: 'POST',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params,
            credentials: 'include'
        });

        const result = await res.json();
        if (result.success) {
            alert(result.msg);
            window.location.href = '/home/index.html'; // 绝对路径
        } else {
            alert(result.msg);
        }
    } catch (err) {
        console.error(err);
        alert('请求异常，请稍后重试');
    }
});

// ========== 注册表单提交【已修改】 ==========
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const inputValue = registerInput.value.trim();
    const password = document.getElementById('registerPassword').value.trim();

    if (!inputValue || !password) {
        alert('请填写完整信息');
        return;
    }

    try {
        const params = new URLSearchParams();
        params.append('password', password);

        // 根据注册类型动态追加字段
        if (currentRegisterType === 'email') {
            params.append("email", inputValue);
        } else if (currentRegisterType === 'phone') {
            params.append("phone", inputValue);
        } else if (currentRegisterType === 'username') {
            params.append("username", inputValue);
        }

        const response = await fetch('/register', {
            method: 'POST',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params,
            credentials: 'include'
        });

        const result = await response.json();
        if (result.success) {
            alert(result.msg);
            showLogin();
            registerForm.reset();
        } else {
            alert(result.msg);
        }
    } catch (err) {
        console.error(err);
        alert('请求异常，请稍后重试');
    }
});