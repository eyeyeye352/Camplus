import { contributionTypes, elements, statusMap } from './state.js';

export function renderLoading() {
    elements.list.innerHTML = '<div class="empty">正在加载...</div>';
}

export function renderLoadError(message) {
    elements.list.innerHTML = `<div class="empty">${escapeHtml(message || '加载失败，请稍后重试')}</div>`;
}

export function renderList(items) {
    if (!items.length) {
        elements.list.innerHTML = '<div class="empty">暂无贡献记录。</div>';
        return;
    }

    elements.list.innerHTML = items.map((item) => {
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

export function setLoginStatus(text) {
    elements.loginStatus.textContent = text;
}

export function showToast(message) {
    elements.toast.textContent = message;
    elements.toast.classList.add('show');
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => elements.toast.classList.remove('show'), 2600);
}

function typeText(type) {
    return contributionTypes[Number(type)] || '未知类型';
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
