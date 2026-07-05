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

    document.getElementById('deleteAccountBtn').addEventListener('click', async () => {
        const userId = sessionStorage.getItem('userId');
        if (!userId) {
            showToast('请先登录', 'warning');
            return;
        }

        if (!confirm('确定要注销账号吗？此操作不可撤销，所有数据将被删除！')) {
            return;
        }

        try {
            const response = await fetch('/user/delete', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `userId=${userId}`
            });
            const result = await response.json();
            if (result.success) {
                showToast('账号注销成功', 'success');
                sessionStorage.clear();
                setTimeout(() => {
                    window.location.href = '../login/login.html';
                }, 1500);
            } else {
                showToast(result.message || '注销失败', 'error');
            }
        } catch (error) {
            showToast('网络请求失败', 'error');
        }
    });
});