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

// 跳转到首页
function goToHome() {
    window.location.href = '../home/index.html';
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

// 菜单点击事件
const menuItems = document.querySelectorAll('.menu-item');
menuItems.forEach(item => {
    if (item.textContent === '首页') {
        item.addEventListener('click', () => {
            goToHome();
        });
    }
});

// 登录相关按钮点击事件
userAvatar.addEventListener('click', () => {
    // 已经在登录页面了
});

userButton.addEventListener('click', () => {
    // 已经在登录页面了
});

menuLoginBtn.addEventListener('click', () => {
    // 已经在登录页面了
});

// 原有登录表单逻辑
const form = document.getElementById('loginForm');

// 表单提交（对接后端接口）
form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();

    // 简单前端校验
    if (!email || !password) {
        alert('请填写邮箱和密码');
        return;
    }

    // TODO: 这里替换成你后端的登录接口地址
    const apiUrl = '/login/loginApi';

    try {
        const response = await fetch(apiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include', // 带 Cookie，维持 Session
            body: JSON.stringify({
                username: email,
                password: password
            })
        });

        const result = await response.json();
        if (result.code === 200) {
            alert('登录成功');
            // 登录成功后跳转到首页
            window.location.href = '../home/index.html';
        } else {
            alert(result.msg || '登录失败');
        }
    } catch (err) {
        console.error(err);
        alert('请求异常，请稍后重试');
    }
});