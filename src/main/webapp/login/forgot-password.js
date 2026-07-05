document.addEventListener('DOMContentLoaded', () => {
    const emailInput = document.getElementById('email');
    const verifyEmailBtn = document.getElementById('verifyEmailBtn');
    const resetFields = document.getElementById('resetFields');
    const sendCodeBtn = document.getElementById('sendCodeBtn');
    const verificationCodeInput = document.getElementById('verificationCode');
    const smtpPasswordInput = document.getElementById('smtpPassword');
    const newPasswordInput = document.getElementById('newPassword');
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');
    const backToLogin = document.getElementById('backToLogin');

    let codeTimer = null;

    backToLogin.addEventListener('click', () => {
        window.location.href = 'login.html';
    });

    verifyEmailBtn.addEventListener('click', async () => {
        const email = emailInput.value.trim();
        if (!email) {
            showToast('请输入邮箱', 'warning');
            return;
        }

        if (!/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(email)) {
            showToast('邮箱格式不正确', 'warning');
            return;
        }

        try {
            const response = await fetch('/user/checkEmail', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `email=${encodeURIComponent(email)}`
            });
            const result = await response.json();
            if (result.success) {
                if (result.data.isAdmin) {
                    showToast('禁止更改管理员账号密码，请重新输入', 'error');
                    emailInput.value = '';
                    emailInput.focus();
                } else {
                    showToast('邮箱验证通过，请继续操作', 'success');
                    resetFields.style.display = 'block';
                }
            } else {
                showToast(result.message || '邮箱不存在', 'error');
            }
        } catch (error) {
            showToast('网络请求失败', 'error');
        }
    });

    sendCodeBtn.addEventListener('click', async () => {
        const email = emailInput.value.trim();
        const smtpPassword = smtpPasswordInput.value.trim();

        if (!smtpPassword) {
            showToast('请输入SMTP授权码', 'warning');
            return;
        }

        sendCodeBtn.disabled = true;
        sendCodeBtn.textContent = '发送中...';

        try {
            const response = await fetch('/sendCode', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `target=${encodeURIComponent(email)}&type=email&smtpPassword=${encodeURIComponent(smtpPassword)}&scene=reset`
            });
            const result = await response.json();
            if (result.success) {
                showToast('验证码发送成功', 'success');
                startCodeTimer();
            } else {
                showToast(result.message || '验证码发送失败', 'error');
                sendCodeBtn.disabled = false;
                sendCodeBtn.textContent = '获取验证码';
            }
        } catch (error) {
            showToast('网络请求失败', 'error');
            sendCodeBtn.disabled = false;
            sendCodeBtn.textContent = '获取验证码';
        }
    });

    function startCodeTimer() {
        let seconds = 60;
        sendCodeBtn.textContent = `${seconds}秒后重发`;
        codeTimer = setInterval(() => {
            seconds--;
            if (seconds <= 0) {
                clearInterval(codeTimer);
                sendCodeBtn.disabled = false;
                sendCodeBtn.textContent = '获取验证码';
            } else {
                sendCodeBtn.textContent = `${seconds}秒后重发`;
            }
        }, 1000);
    }

    forgotPasswordForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = emailInput.value.trim();
        const code = verificationCodeInput.value.trim();
        const newPassword = newPasswordInput.value.trim();

        if (!code) {
            showToast('请输入验证码', 'warning');
            return;
        }

        if (!newPassword) {
            showToast('请输入新密码', 'warning');
            return;
        }

        try {
            const response = await fetch('/user/resetPassword', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}&newPassword=${encodeURIComponent(newPassword)}`
            });
            const result = await response.json();
            if (result.success) {
                showToast('密码重置成功，即将跳转登录', 'success');
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 1500);
            } else {
                showToast(result.message || '密码重置失败', 'error');
            }
        } catch (error) {
            showToast('网络请求失败', 'error');
        }
    });
});