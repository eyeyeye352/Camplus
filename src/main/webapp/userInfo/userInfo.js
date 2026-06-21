// ========== 初始化 ==========
const menuButton = document.getElementById('menuButton');
const sideMenu = document.getElementById('sideMenu');
const menuOverlay = document.getElementById('menuOverlay');
const musicButton = document.getElementById('musicButton');
const bgMusic = document.getElementById('bgMusic');
const menuLoginBtn = document.getElementById('menuLoginBtn');
const adminMenuItem = document.getElementById('adminMenuItem');

const username = sessionStorage.getItem('username');

// ========== 未登录则重定向 ==========
if (!username) {
    alert('请先登录');
    window.location.href = '../login/login.html';
}

// ========== 菜单控制 ==========
menuButton.addEventListener('click', () => {
    sideMenu.classList.add('active');
    menuOverlay.classList.add('active');
});

menuOverlay.addEventListener('click', () => {
    sideMenu.classList.remove('active');
    menuOverlay.classList.remove('active');
});

// ========== 音乐控制 ==========
let isMusicPlaying = false;
musicButton.addEventListener('click', () => {
    if (isMusicPlaying) {
        bgMusic.pause();
        document.querySelector('.music-slash').style.display = 'block';
        isMusicPlaying = false;
    } else {
        bgMusic.play().catch(() => {});
        document.querySelector('.music-slash').style.display = 'none';
        isMusicPlaying = true;
    }
});

// ========== 菜单登录按钮 ==========
if (sessionStorage.getItem('username')) {
    menuLoginBtn.textContent = sessionStorage.getItem('username');
    menuLoginBtn.style.pointerEvents = 'none';
} else {
    menuLoginBtn.addEventListener('click', () => {
        window.location.href = '../login/login.html';
    });
}

// ========== 管理员入口拦截 ==========
adminMenuItem.addEventListener('click', (e) => {
    if (!sessionStorage.getItem('username')) {
        e.preventDefault();
        alert('请先登录');
        window.location.href = '../login/login.html';
    }
});

// ========== 加载用户信息 ==========
document.getElementById('usernameValue').textContent =
    sessionStorage.getItem('username') || '—';

document.getElementById('emailValue').textContent =
    sessionStorage.getItem('email') || '—';

document.getElementById('phoneValue').textContent =
    sessionStorage.getItem('phone') || '—';

const role = sessionStorage.getItem('role');
document.getElementById('roleValue').textContent =
    role === '1' ? '管理员' : '普通用户';

// ========== 表单提交（暂时禁止默认行为，后续接入后端） ==========
document.getElementById('editUsernameForm').addEventListener('submit', (e) => {
    e.preventDefault();
    alert('修改用户名功能待接入后端');
});

document.getElementById('editPasswordForm').addEventListener('submit', (e) => {
    e.preventDefault();
    alert('修改密码功能待接入后端');
});

document.getElementById('editEmailForm').addEventListener('submit', (e) => {
    e.preventDefault();
    alert('修改邮箱功能待接入后端');
});

document.getElementById('editPhoneForm').addEventListener('submit', (e) => {
    e.preventDefault();
    alert('修改手机号功能待接入后端');
});
