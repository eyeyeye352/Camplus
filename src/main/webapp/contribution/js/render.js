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
            <button type="button" class="contribution-card" data-contribution-id="${item.contributionId}"
                    aria-label="查看${escapeHtml(item.title || '未命名贡献')}详情">
                <h3>${escapeHtml(item.title || '未命名贡献')}</h3>
                <div class="meta">
                    <span class="badge ${statusClass}">${statusText}</span>
                    <span>类型：${typeText(item.contributionType)}</span>
                    <span>提交时间：${escapeHtml(item.createTime || '-')}</span>
                </div>
                <p>${escapeHtml(item.content || '暂无内容摘要')}</p>
                <span class="card-action">查看详情</span>
            </button>
        `;
    }).join('');
}

export function renderPagination(pageData) {
    const page = Number(pageData.page) || 1;
    const totalPages = Number(pageData.totalPages) || 1;
    const total = Number(pageData.total) || 0;

    elements.pageInfo.textContent = `第 ${page} / ${totalPages} 页，共 ${total} 条`;
    elements.previousPage.disabled = page <= 1;
    elements.nextPage.disabled = page >= totalPages;
}

export function renderDetail(item) {
    const [statusText, statusClass] = statusMap[item.status] || ['未知', ''];
    elements.detailStatus.className = `badge ${statusClass}`;
    elements.detailStatus.textContent = statusText;
    elements.detailType.textContent = `类型：${typeText(item.contributionType)}`;
    elements.detailCreateTime.textContent = `提交时间：${item.createTime || '-'}`;
    elements.detailTitle.textContent = item.title || '未命名贡献';
    elements.detailContent.textContent = item.content || '暂无内容';

    const rejected = Number(item.status) === 2;
    elements.reviewBlock.hidden = !rejected;
    elements.reviewComment.textContent = item.reviewComment || '管理员未填写拒绝原因';
    elements.editContribution.hidden = ![0, 2].includes(Number(item.status));

    showDetailView();
}

export function showEditForm(item) {
    elements.editContributionType.value = String(item.contributionType);
    elements.editTitle.value = item.title || '';
    elements.editContent.value = item.content || '';
    elements.detailView.hidden = true;
    elements.editForm.hidden = false;
}

export function showDetailView() {
    elements.editForm.hidden = true;
    elements.detailView.hidden = false;
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
