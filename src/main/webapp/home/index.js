const fixedTitle = document.querySelector('.fixed-title');
const menuButton = document.getElementById('menuButton');
const sideMenu = document.getElementById('sideMenu');
const menuOverlay = document.getElementById('menuOverlay');
const musicButton = document.getElementById('musicButton');
const bgMusic = document.getElementById('bgMusic');
const userButton = document.getElementById('userButton');
const menuLoginBtn = document.getElementById('menuLoginBtn');
const adminMenuItem = document.getElementById('adminMenuItem');
const activationZone = 200;

// 跳转到登录页面
function goToLogin() {
    window.location.href = '../login/login.html';
}

// 根据登录态更新UI
function updateLoginUI() {
    const username = sessionStorage.getItem('username');
    if (username) {
        // 已登录：显示用户名，不可点击
        menuLoginBtn.textContent = username;
        menuLoginBtn.style.pointerEvents = 'none';
        menuLoginBtn.style.opacity = '1';
        menuLoginBtn.style.cursor = 'default';
    } else {
        // 未登录：显示"登入/注册"
        menuLoginBtn.textContent = '登入/注册';
        menuLoginBtn.style.pointerEvents = 'auto';
        menuLoginBtn.style.opacity = '0.8';
        menuLoginBtn.style.cursor = 'pointer';
    }
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

document.addEventListener('mousemove', (e) => {
    const mouseX = e.clientX;
    const mouseY = e.clientY;
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;

    if (mouseX <= activationZone && mouseY >= windowHeight - activationZone) {
        fixedTitle.classList.add('active');
    } else {
        fixedTitle.classList.remove('active');
    }
});

menuButton.addEventListener('click', () => {
    sideMenu.classList.toggle('active');
    menuOverlay.classList.toggle('active');
});

menuOverlay.addEventListener('click', () => {
    sideMenu.classList.remove('active');
    menuOverlay.classList.remove('active');
});

// 页面加载时初始化登录UI
updateLoginUI();

// 登录按钮/用户名点击事件
menuLoginBtn.addEventListener('click', () => {
    if (!sessionStorage.getItem('username')) {
        goToLogin();
    }
});

// 用户按钮点击：未登录则跳转登录页
userButton.addEventListener('click', () => {
    goToLogin();
});

// admin链接点击：未登录跳转登录页
adminMenuItem.addEventListener('click', (e) => {
    if (!sessionStorage.getItem('username')) {
        e.preventDefault();
        goToLogin();
    }
});
