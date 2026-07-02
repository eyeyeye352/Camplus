const menuButton = document.getElementById('menuButton');
const sideMenu = document.getElementById('sideMenu');
const musicButton = document.getElementById('musicButton');
const bgMusic = document.getElementById('bgMusic');
const avatarCircle = document.getElementById('avatarCircle');
const accountName = document.getElementById('accountName');
const menuLoginBtn = document.getElementById('menuLoginBtn');
const adminMenuItem = document.getElementById('adminMenuItem');

const userId = sessionStorage.getItem('userId');
const username = sessionStorage.getItem('username');

if (!username) {
    alert('请先登录');
    window.location.href = '../login/login.html';
}

function goToLogin() {
    window.location.href = '../login/login.html';
}

function updateLoginUI() {
    const uname = sessionStorage.getItem('username');
    if (uname) {
        accountName.textContent = uname;
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

updateLoginUI();

menuLoginBtn.addEventListener('click', () => {
    if (sessionStorage.getItem('username')) {
        sessionStorage.clear();
        location.reload();
    } else {
        goToLogin();
    }
});

adminMenuItem.addEventListener('click', (e) => {
    if (!sessionStorage.getItem('username')) {
        e.preventDefault();
        alert('请先登录');
        goToLogin();
    }
});

document.getElementById('usernameValue').textContent =
    sessionStorage.getItem('username') || '—';

document.getElementById('emailValue').textContent =
    sessionStorage.getItem('email') || '—';

document.getElementById('phoneValue').textContent =
    sessionStorage.getItem('phone') || '—';

const role = sessionStorage.getItem('role');
document.getElementById('roleValue').textContent =
    role === '1' ? '管理员' : '普通用户';

function updateUserInfo(user) {
    sessionStorage.setItem('userId', user.userId);
    sessionStorage.setItem('username', user.username);
    sessionStorage.setItem('email', user.email || '');
    sessionStorage.setItem('phone', user.phone || '');
    sessionStorage.setItem('nickname', user.nickname || '');
    sessionStorage.setItem('avatarUrl', user.avatarUrl || '');
    sessionStorage.setItem('role', user.role || '0');
    sessionStorage.setItem('status', user.status || '1');

    document.getElementById('usernameValue').textContent = user.username || '—';
    document.getElementById('emailValue').textContent = user.email || '—';
    document.getElementById('phoneValue').textContent = user.phone || '—';
    document.getElementById('roleValue').textContent =
        (user.role === 1 || user.role === '1') ? '管理员' : '普通用户';

    accountName.textContent = user.username;
}

document.getElementById('editUsernameForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const newUsername = document.getElementById('newUsername').value.trim();
    if (!newUsername) return;

    try {
        const { data: result } = await axios.post('/user/updateUsername',
            new URLSearchParams({ userId, newUsername }),
            { withCredentials: true });

        if (result.success) {
            updateUserInfo(result.data);
            document.getElementById('newUsername').value = '';
            alert('用户名修改成功！');
        } else {
            alert(result.msg || '修改失败');
        }
    } catch (err) {
        alert('请求异常，请稍后重试');
        console.error(err);
    }
});

document.getElementById('editEmailForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const newEmail = document.getElementById('newEmail').value.trim();
    if (!newEmail) return;

    try {
        const { data: result } = await axios.post('/user/updateEmail',
            new URLSearchParams({ userId, newEmail }),
            { withCredentials: true });

        if (result.success) {
            updateUserInfo(result.data);
            document.getElementById('newEmail').value = '';
            alert('邮箱修改成功！');
        } else {
            alert(result.msg || '修改失败');
        }
    } catch (err) {
        alert('请求异常，请稍后重试');
        console.error(err);
    }
});

document.getElementById('editPhoneForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const newPhone = document.getElementById('newPhone').value.trim();
    if (!newPhone) return;

    try {
        const { data: result } = await axios.post('/user/updatePhone',
            new URLSearchParams({ userId, newPhone }),
            { withCredentials: true });

        if (result.success) {
            updateUserInfo(result.data);
            document.getElementById('newPhone').value = '';
            alert('手机号修改成功！');
        } else {
            alert(result.msg || '修改失败');
        }
    } catch (err) {
        alert('请求异常，请稍后重试');
        console.error(err);
    }
});

document.getElementById('editPasswordForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    if (!oldPassword || !newPassword) return;

    try {
        const { data: result } = await axios.post('/user/updatePassword',
            new URLSearchParams({ userId, oldPassword, newPassword }),
            { withCredentials: true });

        if (result.success) {
            document.getElementById('oldPassword').value = '';
            document.getElementById('newPassword').value = '';
            alert('密码修改成功！');
        } else {
            alert(result.msg || '修改失败');
        }
    } catch (err) {
        alert('请求异常，请稍后重试');
        console.error(err);
    }
});
