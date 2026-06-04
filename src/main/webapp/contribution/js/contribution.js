const apiBase = `${window.location.origin}${window.location.pathname.split('/module_contribution/')[0]}/contribution`;

const navItems = document.querySelectorAll('.nav-item');
const panels = document.querySelectorAll('.panel');
const form = document.querySelector('#contributionForm');
const list = document.querySelector('#contributionList');
const statusFilter = document.querySelector('#statusFilter');
const toast = document.querySelector('#toast');
const loginStatus = document.querySelector('#loginStatus');

const statusMap = {
    0: ['待审核', 'pending'],
    1: ['已通过', 'approved'],
    2: ['已拒绝', 'rejected']
};

// 给左侧导航栏绑定点击切换面板功能
navItems.forEach((item) => {
    item.addEventListener('click', () => {
        navItems.forEach((nav) => nav.classList.remove('active'));
        panels.forEach((panel) => panel.classList.remove('active'));
        item.classList.add('active');
        document.querySelector(`#${item.dataset.panel}`).classList.add('active');
        if (item.dataset.panel === 'myPanel') {
            loadContributions();
        }
    });
});

// 处理提交贡献，向后端${apiBase}/create发送POST请求
form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    ev.stopPropagation();
    const formData = new FormData(form);
    const title = formData.get('title')?.trim();
    const content = formData.get('content')?.trim();

    if (!title) {
        showToast('请填写标题');
        return;
    }
    if (!content) {
        showToast('请填写贡献内容');
        return;
    }

    try {
        const response = await fetch(`${apiBase}/create`, {
            method: 'POST',
            body: new URLSearchParams(formData)
        });
        const result = await response.json();
        if (!response.ok || !result.success) {
            throw new Error(result.message || '提交失败');
        }
        form.reset();
        showToast('提交成功，已进入待审核');
    } catch (error) {
        showToast(error.message || '提交失败，请稍后重试');
    }
});

statusFilter.addEventListener('change', loadContributions);

// 加载我的贡献
async function loadContributions() {
    const status = statusFilter.value;
    const url = new URL(`${apiBase}/list`);
    if (status !== '') {
        url.searchParams.set('status', status);
    }

    list.innerHTML = '<div class="empty">正在加载...</div>';
    try {
        const response = await fetch(url);
        const result = await response.json();
        if (!response.ok || !result.success) {
            throw new Error(result.message || '加载失败');
        }
        renderList(result.data || []);
        loginStatus.textContent = '已连接用户贡献接口';
    } catch (error) {
        list.innerHTML = `<div class="empty">${escapeHtml(error.message || '加载失败，请稍后重试')}</div>`;
        loginStatus.textContent = '需要登录后使用';
    }
}

// 渲染我的贡献页面
function renderList(items) {
    if (!items.length) {
        list.innerHTML = '<div class="empty">暂无贡献记录。</div>';
        return;
    }
    list.innerHTML = items.map((item) => {
        const [statusText, statusClass] = statusMap[item.status] || ['未知', ''];
        return `
            <article class="contribution-card">
                <h3>${escapeHtml(item.title || '未命名贡献')}</h3>
                <div class="meta">
                    <span class="badge ${statusClass}">${statusText}</span>
                    <span>类型：${typeText(item.contributionType)}</span>
                    <span>提交时间：${escapeHtml(item.createTime || '-')}</span>
                </div>
                <p>${escapeHtml(item.content || '暂无内容摘要')}</p>
            </article>
        `;
    }).join('');
}

// 给类型转换成文本
function typeText(type) {
    return ['新增问题', '答案纠错'][Number(type)] || '未知类型';
}

// 显示一些指定信息，一段时间后消失
function showToast(message) {
    toast.textContent = message;
    toast.classList.add('show');
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => toast.classList.remove('show'), 2600);
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
