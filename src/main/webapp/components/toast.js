function initToast() {
    if (document.getElementById('toastContainer')) {
        return;
    }

    const container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';

    const box = document.createElement('div');
    box.id = 'toastBox';
    box.className = 'toast-box';

    const icon = document.createElement('div');
    icon.id = 'toastIcon';
    icon.className = 'toast-icon info';

    const message = document.createElement('div');
    message.id = 'toastMessage';
    message.className = 'toast-message';

    const btn = document.createElement('button');
    btn.id = 'toastConfirmBtn';
    btn.className = 'toast-confirm-btn';
    btn.textContent = '确定';

    box.appendChild(icon);
    box.appendChild(message);
    box.appendChild(btn);
    container.appendChild(box);
    document.body.appendChild(container);

    btn.addEventListener('click', hideToast);

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && box.classList.contains('active')) {
            hideToast();
        }
    });
}

function showToast(message, type = 'info') {
    initToast();

    const box = document.getElementById('toastBox');
    const icon = document.getElementById('toastIcon');
    const msg = document.getElementById('toastMessage');

    if (!box || !icon || !msg) {
        console.error('Toast elements not found');
        return;
    }

    const icons = {
        info: 'ℹ️',
        success: '✅',
        error: '❌',
        warning: '⚠️'
    };

    icon.textContent = icons[type] || icons.info;
    icon.className = `toast-icon ${type}`;
    msg.textContent = message;

    box.classList.add('active');
}

function hideToast() {
    const box = document.getElementById('toastBox');
    if (!box) return;
    box.classList.remove('active');
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initToast);
} else {
    initToast();
}
