const menuButton = document.getElementById('menuButton');
const sideMenu = document.getElementById('sideMenu');
const musicButton = document.getElementById('musicButton');
const bgMusic = document.getElementById('bgMusic');
const avatarCircle = document.getElementById('avatarCircle');
const accountArea = document.getElementById('accountArea');
const accountName = document.getElementById('accountName');
const accountEmail = document.getElementById('accountEmail');
const adminMenuItem = document.getElementById('adminMenuItem');

const username = sessionStorage.getItem('username');

if (!username) {
    alert('请先登录');
    window.location.href = '../login/login.html';
}

function updateLoginUI() {
    const uname = sessionStorage.getItem('username');
    const email = sessionStorage.getItem('email');
    if (uname) {
        accountName.textContent = uname;
        accountEmail.textContent = email || '';
        avatarCircle.classList.add('logged-in');
    } else {
        accountName.textContent = '请先登录';
        accountEmail.textContent = '';
        avatarCircle.classList.remove('logged-in');
    }
}

let isMusicPlaying = false;
const musicSlash = document.querySelector('.music-slash');

musicSlash.style.display = '';
musicButton.style.opacity = '0.5';

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

musicButton.addEventListener('click', (e) => {
    e.stopPropagation();
    toggleMusic();
});

menuButton.addEventListener('click', () => {
    sideMenu.classList.toggle('active');
});

updateLoginUI();

accountArea.addEventListener('click', () => {
    if (sessionStorage.getItem('username')) {
        window.location.href = '../userInfo/userInfo.html';
    } else {
        window.location.href = '../login/login.html';
    }
});

adminMenuItem.addEventListener('click', (e) => {
    if (!sessionStorage.getItem('username')) {
        e.preventDefault();
        alert('请先登录');
        window.location.href = '../login/login.html';
    }
});

document.getElementById('emailValue').textContent =
    sessionStorage.getItem('email') || '—';

const role = sessionStorage.getItem('role');
document.getElementById('roleValue').textContent =
    role === '1' ? '管理员' : '普通用户';

document.getElementById('logoutBtn').addEventListener('click', () => {
    sessionStorage.clear();
    window.location.href = '../login/login.html';
});
