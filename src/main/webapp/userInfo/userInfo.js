document.addEventListener('DOMContentLoaded', () => {
    const username = sessionStorage.getItem('username');

    if (!username) {
        showToast('请先登录', 'warning');
        setTimeout(() => {
            window.location.href = '../login/login.html';
        }, 100);
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