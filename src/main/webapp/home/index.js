const fixedTitle = document.querySelector('.fixed-title');
const menuButton = document.getElementById('menuButton');
const sideMenu = document.getElementById('sideMenu');
const musicButton = document.getElementById('musicButton');
const bgMusic = document.getElementById('bgMusic');
const avatarCircle = document.getElementById('avatarCircle');
const accountName = document.getElementById('accountName');
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

let heroGuideHidden = false;

menuButton.addEventListener('click', () => {
    sideMenu.classList.toggle('active');
    const heroGuide = document.querySelector('.hero-guide');
    if (heroGuide && !heroGuideHidden && sideMenu.classList.contains('active')) {
        heroGuide.style.opacity = '0';
        heroGuideHidden = true;
    }
});

// 页面加载时初始化登录UI
updateLoginUI();

// 登录按钮/登出点击事件
menuLoginBtn.addEventListener('click', () => {
    if (sessionStorage.getItem('username')) {
        sessionStorage.clear();
        location.reload();
    } else {
        goToLogin();
    }
});

// admin链接点击：未登录跳转登录页，已登录则校验管理员身份
adminMenuItem.addEventListener('click', async (e) => {
    if (!sessionStorage.getItem('username')) {
        e.preventDefault();
        goToLogin();
        return;
    }

    e.preventDefault();
    const userId = sessionStorage.getItem('userId');

    try {
        const { data: result } = await axios.post('/user/getRole',
            new URLSearchParams({ userId }),
            { withCredentials: true });

        if (result.success && (result.data === 1 || result.data === '1')) {
            window.location.href = '../admin/admin.html';
        } else {
            alert('您不是管理员！');
        }
    } catch (err) {
        alert('请求异常，请稍后重试');
        console.error(err);
    }
});
