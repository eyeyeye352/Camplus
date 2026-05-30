const fixedTitle = document.querySelector('.fixed-title');
const menuButton = document.getElementById('menuButton');
const sideMenu = document.getElementById('sideMenu');
const menuOverlay = document.getElementById('menuOverlay');
const musicButton = document.getElementById('musicButton');
const bgMusic = document.getElementById('bgMusic');
const userAvatar = document.getElementById('userAvatar');
const userButton = document.getElementById('userButton');
const menuLoginBtn = document.getElementById('menuLoginBtn');
const activationZone = 200;

// 跳转到登录页面
function goToLogin() {
    window.location.href = '../login/login.html';
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

// 登录相关按钮点击事件
userAvatar.addEventListener('click', () => {
    goToLogin();
});

userButton.addEventListener('click', () => {
    goToLogin();
});

menuLoginBtn.addEventListener('click', () => {
    goToLogin();
});
