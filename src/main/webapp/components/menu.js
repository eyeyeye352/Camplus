function initMenu() {
    const menuButton = document.getElementById('menuButton');
    const sideMenu = document.getElementById('sideMenu');
    const musicButton = document.getElementById('musicButton');
    const bgMusic = document.getElementById('bgMusic');
    const accountArea = document.getElementById('accountArea');
    const accountName = document.getElementById('accountName');
    const accountEmail = document.getElementById('accountEmail');
    const avatarCircle = document.getElementById('avatarCircle');
    const adminMenuItem = document.getElementById('adminMenuItem');

    if (!menuButton || !sideMenu) {
        console.error('菜单组件初始化失败：未找到菜单DOM元素');
        return;
    }

    function updateLoginUI() {
        if (!accountName || !accountEmail || !avatarCircle) return;
        
        const username = sessionStorage.getItem('username');
        const email = sessionStorage.getItem('email');
        if (username) {
            accountName.textContent = username;
            accountEmail.textContent = email || '';
            avatarCircle.classList.add('logged-in');
            accountName.classList.add('logged-in');
        } else {
            accountName.textContent = '请先登录';
            accountEmail.textContent = '';
            avatarCircle.classList.remove('logged-in');
            accountName.classList.remove('logged-in');
        }
    }

    updateLoginUI();

    function showMenu() {
        sideMenu.style.transition = 'none';
        sideMenu.style.opacity = '1';
        sideMenu.style.pointerEvents = 'auto';
        sideMenu.classList.add('active');
        const header = sideMenu.querySelector('.menu-header');
        const content = sideMenu.querySelector('.menu-content');
        const footer = sideMenu.querySelector('.menu-footer');
        if (header) { header.style.transition = 'none'; header.style.visibility = 'visible'; header.style.opacity = '1'; header.style.pointerEvents = 'auto'; }
        if (content) { content.style.transition = 'none'; content.style.visibility = 'visible'; content.style.opacity = '1'; content.style.pointerEvents = 'auto'; }
        if (footer) { footer.style.transition = 'none'; footer.style.visibility = 'visible'; footer.style.opacity = '1'; footer.style.pointerEvents = 'auto'; }
        setTimeout(() => {
            sideMenu.style.transition = 'opacity 0.4s ease';
            if (header) header.style.transition = 'visibility 0s linear 0s, opacity 0.4s ease';
            if (content) content.style.transition = 'visibility 0s linear 0s, opacity 0.4s ease';
            if (footer) footer.style.transition = 'visibility 0s linear 0s, opacity 0.4s ease';
        }, 50);
    }

    function hideMenu() {
        sideMenu.style.opacity = '0';
        sideMenu.style.pointerEvents = 'none';
        sideMenu.classList.remove('active');
        const header = sideMenu.querySelector('.menu-header');
        const content = sideMenu.querySelector('.menu-content');
        const footer = sideMenu.querySelector('.menu-footer');
        if (header) { header.style.visibility = 'hidden'; header.style.opacity = '0'; header.style.pointerEvents = 'none'; }
        if (content) { content.style.visibility = 'hidden'; content.style.opacity = '0'; content.style.pointerEvents = 'none'; }
        if (footer) { footer.style.visibility = 'hidden'; footer.style.opacity = '0'; footer.style.pointerEvents = 'none'; }
    }

    menuButton.addEventListener('click', (e) => {
        e.stopPropagation();
        if (sideMenu.classList.contains('active')) {
            hideMenu();
        } else {
            showMenu();
        }
    });

    document.addEventListener('click', (e) => {
        if (!sideMenu.contains(e.target) && !menuButton.contains(e.target)) {
            hideMenu();
        }
    });

    if (accountArea) {
        accountArea.addEventListener('click', () => {
            if (sessionStorage.getItem('username')) {
                window.location.href = '../userInfo/userInfo.html';
            } else {
                window.location.href = '../login/login.html';
            }
        });
    }

    if (adminMenuItem) {
        adminMenuItem.addEventListener('click', (e) => {
            const role = sessionStorage.getItem('role');
            if (!sessionStorage.getItem('username')) {
                e.preventDefault();
                alert('请先登录');
                window.location.href = '../login/login.html';
            } else if (role !== '1') {
                e.preventDefault();
                alert('您不是管理员，无法访问此页面');
            }
        });
    }

    let isMusicPlaying = false;
    const musicSlash = document.querySelector('.music-slash');

    if (musicSlash) {
        musicSlash.style.display = '';
    }
    if (musicButton) {
        musicButton.style.opacity = '0.5';
    }

    function toggleMusic() {
        if (!bgMusic) return;
        
        if (isMusicPlaying) {
            bgMusic.pause();
            if (musicSlash) {
                musicSlash.style.display = '';
            }
            musicButton.style.opacity = '0.5';
        } else {
            bgMusic.play().then(() => {
                if (musicSlash) {
                    musicSlash.style.display = 'none';
                }
                musicButton.style.opacity = '1';
            }).catch(err => {
                console.log('需要用户交互才能播放音乐', err);
            });
        }
        isMusicPlaying = !isMusicPlaying;
    }

    if (musicButton) {
        musicButton.addEventListener('click', (e) => {
            e.stopPropagation();
            toggleMusic();
        });
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initMenu);
} else {
    initMenu();
}