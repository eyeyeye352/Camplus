(function() {
    const contextPath = window.location.pathname.split('/contribution/')[0];
    const apiBase = `${window.location.origin}${contextPath}/contribution`;

    async function requestJson(url, options = {}) {
        const response = await fetch(url, {
            credentials: 'include',
            ...options
        });
        const result = await response.json();
        if (!response.ok || !result.success) {
            throw new Error(result.message || '请求失败');
        }
        return result.data;
    }

    function createContribution(formData, userId) {
        const params = new URLSearchParams(formData);
        params.set('userId', userId);
        return requestJson(`${apiBase}/create`, {
            method: 'POST',
            body: params
        });
    }

    function fetchContributions(userId, status, page, pageSize) {
        const url = new URL(`${apiBase}/list`);
        url.searchParams.set('userId', userId);
        url.searchParams.set('page', page);
        url.searchParams.set('pageSize', pageSize);
        if (status !== '') {
            url.searchParams.set('status', status);
        }
        return requestJson(url);
    }

    function fetchContributionDetail(userId, contributionId) {
        const url = new URL(`${apiBase}/detail`);
        url.searchParams.set('userId', userId);
        url.searchParams.set('contribution_id', contributionId);
        return requestJson(url);
    }

    function updateContribution(formData, userId, contributionId) {
        const params = new URLSearchParams(formData);
        params.set('userId', userId);
        params.set('contribution_id', contributionId);
        return requestJson(`${apiBase}/update`, {
            method: 'POST',
            body: params
        });
    }

    const elements = {};

    function initElements() {
        elements.navItems = document.querySelectorAll('.nav-item');
        elements.panels = document.querySelectorAll('.panel');
        elements.form = document.querySelector('#submitForm');
        elements.list = document.querySelector('#contributionList');
        elements.statusFilter = document.querySelector('#statusFilter');
        elements.toast = document.querySelector('#toast');
        elements.loginStatus = document.querySelector('#loginStatus');
        elements.previousPage = document.querySelector('#previousPage');
        elements.nextPage = document.querySelector('#nextPage');
        elements.pageInfo = document.querySelector('#pageInfo');
        elements.dialog = document.querySelector('#contributionDialog');
        elements.closeDialog = document.querySelector('#closeDialog');
        elements.detailView = document.querySelector('#detailView');
        elements.detailStatus = document.querySelector('#detailStatus');
        elements.detailType = document.querySelector('#detailType');
        elements.detailCreateTime = document.querySelector('#detailCreateTime');
        elements.detailTitle = document.querySelector('#detailTitle');
        elements.detailContent = document.querySelector('#detailContent');
        elements.reviewBlock = document.querySelector('#reviewBlock');
        elements.reviewComment = document.querySelector('#reviewComment');
        elements.editContribution = document.querySelector('#editContribution');
        elements.editForm = document.querySelector('#editContributionForm');
        elements.editContributionType = document.querySelector('#editType');
        elements.editTitle = document.querySelector('#editTitle');
        elements.editContent = document.querySelector('#editContent');
        elements.cancelEdit = document.querySelector('#cancelEdit');
    }

    const pageState = {
        currentUser: null,
        currentPage: 1,
        pageSize: 5,
        totalPages: 1,
        currentDetail: null
    };

    const statusMap = {
        0: ['审核中', 'pending'],
        1: ['已通过', 'approved'],
        2: ['已拒绝', 'rejected']
    };

    const contributionTypes = ['新增问题', '答案纠错'];

    function renderLoading() {
        if (elements.list) {
            elements.list.innerHTML = '<div class="empty">正在加载...</div>';
        }
    }

    function renderLoadError(message) {
        if (elements.list) {
            elements.list.innerHTML = `<div class="empty">${escapeHtml(message || '加载失败，请稍后重试')}</div>`;
        }
    }

    function renderList(items) {
        if (!elements.list) return;
        
        if (!items || !items.length) {
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

    function renderPagination(pageData) {
        if (!elements.pageInfo || !elements.previousPage || !elements.nextPage) return;
        
        const page = Number(pageData.page) || 1;
        const totalPages = Number(pageData.totalPages) || 1;
        const total = Number(pageData.total) || 0;

        elements.pageInfo.textContent = `第 ${page} / ${totalPages} 页，共 ${total} 条`;
        elements.previousPage.disabled = page <= 1;
        elements.nextPage.disabled = page >= totalPages;
    }

    function renderDetail(item) {
        if (!elements.detailStatus || !elements.detailType || !elements.detailCreateTime || !elements.detailTitle || !elements.detailContent) return;
        
        const [statusText, statusClass] = statusMap[item.status] || ['未知', ''];
        elements.detailStatus.className = `badge ${statusClass}`;
        elements.detailStatus.textContent = statusText;
        elements.detailType.textContent = `类型：${typeText(item.contributionType)}`;
        elements.detailCreateTime.textContent = `提交时间：${item.createTime || '-'}`;
        elements.detailTitle.textContent = item.title || '未命名贡献';
        elements.detailContent.textContent = item.content || '暂无内容';

        const rejected = Number(item.status) === 2;
        if (elements.reviewBlock) elements.reviewBlock.hidden = !rejected;
        if (elements.reviewComment) elements.reviewComment.textContent = item.reviewComment || '管理员未填写拒绝原因';
        if (elements.editContribution) elements.editContribution.hidden = ![0, 2].includes(Number(item.status));

        showDetailView();
    }

    function showEditForm(item) {
        if (!elements.editContributionType || !elements.editTitle || !elements.editContent || !elements.detailView || !elements.editForm) return;
        
        elements.editContributionType.value = String(item.contributionType);
        elements.editTitle.value = item.title || '';
        elements.editContent.value = item.content || '';
        elements.detailView.hidden = true;
        elements.editForm.hidden = false;
    }

    function showDetailView() {
        if (!elements.editForm || !elements.detailView) return;
        
        elements.editForm.hidden = true;
        elements.detailView.hidden = false;
    }

    function setLoginStatus(text) {
        const loginStatus = document.querySelector('#loginStatus');
        if (loginStatus) {
            loginStatus.textContent = text;
        }
    }

    function showToast(message) {
        if (!elements.toast) return;
        
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

    function bindEvents() {
        bindNavigation();
        bindSubmitForm();
        bindListEvents();
        bindDialogEvents();
        bindPagination();
        bindBackHomeButton();

        if (elements.statusFilter) {
            elements.statusFilter.addEventListener('change', () => {
                pageState.currentPage = 1;
                loadContributions();
            });
        }
    }

    function initCurrentUser() {
        const userId = sessionStorage.getItem('userId');
        if (!userId) {
            pageState.currentUser = null;
            setLoginStatus('未登录');
            return;
        }

        pageState.currentUser = {
            userId: Number(userId),
            username: sessionStorage.getItem('username'),
            email: sessionStorage.getItem('email')
        };
        setLoginStatus(displayName(pageState.currentUser));
    }

    function bindNavigation() {
        if (!elements.navItems || elements.navItems.length === 0) return;
        
        elements.navItems.forEach((item) => {
            item.addEventListener('click', (event) => {
                if (!item.dataset.panel) {
                    return;
                }

                event.preventDefault();
                event.stopPropagation();

                elements.navItems.forEach((nav) => nav.classList.remove('active'));
                elements.panels.forEach((panel) => panel.classList.remove('active'));

                item.classList.add('active');
                const targetPanel = document.querySelector(`#${item.dataset.panel}`);
                if (targetPanel) {
                    targetPanel.classList.add('active');
                }

                if (item.dataset.panel === 'myPanel') {
                    loadContributions();
                }
            });
        });
    }

    function bindSubmitForm() {
        if (!elements.form) return;
        
        elements.form.addEventListener('submit', async (event) => {
            event.preventDefault();
            event.stopPropagation();

            const formData = new FormData(elements.form);
            const title = formData.get('title')?.trim();
            const content = formData.get('content')?.trim();

            if (!pageState.currentUser) {
                showToast('请先登录后再提交贡献');
                return;
            }
            if (!title) {
                showToast('请填写标题');
                return;
            }
            if (!content) {
                showToast('请填写贡献内容');
                return;
            }

            try {
                await createContribution(formData, pageState.currentUser.userId);
                elements.form.reset();
                showToast('提交成功，已进入审核中');
            } catch (error) {
                showToast(error.message || '提交失败，请稍后重试');
            }
        });
    }

    function bindListEvents() {
        if (!elements.list) return;
        
        elements.list.addEventListener('click', (event) => {
            const card = event.target.closest('[data-contribution-id]');
            if (card) {
                openContributionDetail(Number(card.dataset.contributionId));
            }
        });
    }

    function bindDialogEvents() {
        if (!elements.closeDialog || !elements.dialog || !elements.editContribution || !elements.cancelEdit || !elements.editForm) return;
        
        elements.closeDialog.addEventListener('click', () => elements.dialog.close());

        elements.dialog.addEventListener('click', (event) => {
            if (event.target === elements.dialog) {
                elements.dialog.close();
            }
        });

        elements.dialog.addEventListener('close', () => {
            pageState.currentDetail = null;
            showDetailView();
        });

        elements.editContribution.addEventListener('click', () => {
            if (pageState.currentDetail) {
                showEditForm(pageState.currentDetail);
            }
        });

        elements.cancelEdit.addEventListener('click', showDetailView);
        elements.editForm.addEventListener('submit', submitContributionUpdate);
    }

    function bindPagination() {
        if (!elements.previousPage || !elements.nextPage) return;
        
        elements.previousPage.addEventListener('click', () => {
            if (pageState.currentPage > 1) {
                pageState.currentPage -= 1;
                loadContributions();
            }
        });

        elements.nextPage.addEventListener('click', () => {
            if (pageState.currentPage < pageState.totalPages) {
                pageState.currentPage += 1;
                loadContributions();
            }
        });
    }

    async function openContributionDetail(contributionId) {
        if (!pageState.currentUser) {
            showToast('请先登录后查看贡献详情');
            return;
        }

        try {
            const detail = await fetchContributionDetail(
                pageState.currentUser.userId,
                contributionId
            );
            pageState.currentDetail = detail;
            renderDetail(detail);
            if (elements.dialog) {
                elements.dialog.showModal();
            }
        } catch (error) {
            showToast(error.message || '详情加载失败');
        }
    }

    async function submitContributionUpdate(event) {
        event.preventDefault();

        if (!pageState.currentUser || !pageState.currentDetail) {
            showToast('当前贡献信息已失效，请重新打开');
            return;
        }

        const formData = new FormData(elements.editForm);
        const title = formData.get('title')?.trim();
        const content = formData.get('content')?.trim();
        if (!title || !content) {
            showToast('标题和贡献内容不能为空');
            return;
        }

        try {
            await updateContribution(
                formData,
                pageState.currentUser.userId,
                pageState.currentDetail.contributionId
            );
            if (elements.dialog) {
                elements.dialog.close();
            }
            showToast('修改成功，贡献已重新进入审核中');
            await loadContributions();
        } catch (error) {
            showToast(error.message || '修改失败，请稍后重试');
        }
    }

    async function loadContributions() {
        renderLoading();

        try {
            if (!pageState.currentUser) {
                initCurrentUser();
            }
            if (!pageState.currentUser) {
                throw new Error('请先登录后查看我的贡献');
            }

            if (!elements.statusFilter) {
                throw new Error('无法获取状态筛选器');
            }

            const pageData = await fetchContributions(
                pageState.currentUser.userId,
                elements.statusFilter.value,
                pageState.currentPage,
                pageState.pageSize
            );
            pageState.currentPage = pageData.page;
            pageState.totalPages = pageData.totalPages;
            renderList(pageData.items || []);
            renderPagination(pageData);
            setLoginStatus(displayName(pageState.currentUser));
        } catch (error) {
            renderLoadError(error.message);
            renderPagination({ page: 1, totalPages: 1, total: 0 });
            if (!pageState.currentUser) {
                setLoginStatus('未登录');
            }
        }
    }

    function displayName(user) {
        return user?.username || user?.email || '用户';
    }

    function bindBackHomeButton() {
        const backButton = document.querySelector('#backHomeBut');
        if (backButton) {
            backButton.addEventListener('click', (event) => {
                event.preventDefault();
                event.stopPropagation();
                window.location.href = '/home/index.html';
            });
        }
    }

    function initApp() {
        initElements();
        initCurrentUser();
        bindEvents();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initApp);
    } else {
        initApp();
    }
})();