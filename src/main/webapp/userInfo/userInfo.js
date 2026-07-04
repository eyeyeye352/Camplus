document.addEventListener('DOMContentLoaded', () => {
    const username = sessionStorage.getItem('username');

    if (!username) {
        alert('请先登录');
        window.location.href = '../login/login.html';
        return;
    }

    document.getElementById('emailValue').textContent =
        sessionStorage.getItem('email') || '—';

    const role = sessionStorage.getItem('role');
    document.getElementById('roleValue').textContent =
        role === '1' ? '管理员' : '普通用户';

    document.getElementById('logoutBtn').addEventListener('click', () => {
        sessionStorage.clear();
        window.location.href = '../login/login.html';
    });
});