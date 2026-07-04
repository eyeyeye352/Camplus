import {
    createContribution,
    fetchContributionDetail,
    fetchContributions,
    updateContribution
} from './api.js';
import { elements, pageState } from './state.js';
import {
    renderDetail,
    renderList,
    renderLoadError,
    renderLoading,
    renderPagination,
    setLoginStatus,
    showDetailView,
    showEditForm,
    showToast
} from './render.js';

export function bindEvents() {
    bindNavigation();
    bindSubmitForm();
    bindListEvents();
    bindDialogEvents();
    bindPagination();
    bindBackHomeButton();

    elements.statusFilter.addEventListener('change', () => {
        pageState.currentPage = 1;
        loadContributions();
    });
}

export function initCurrentUser() {
    const userId = sessionStorage.getItem('userId');
    if (!userId) {
        pageState.currentUser = null;
        setLoginStatus('未登录');
        return;
    }

    pageState.currentUser = {
        userId: Number(userId),
        username: sessionStorage.getItem('username'),
        nickname: sessionStorage.getItem('nickname'),
        email: sessionStorage.getItem('email'),
        phone: sessionStorage.getItem('phone')
    };
    setLoginStatus(displayName(pageState.currentUser));
}

function bindNavigation() {
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
            document.querySelector(`#${item.dataset.panel}`)?.classList.add('active');

            if (item.dataset.panel === 'myPanel') {
                loadContributions();
            }
        });
    });
}

function bindSubmitForm() {
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
    elements.list.addEventListener('click', (event) => {
        const card = event.target.closest('[data-contribution-id]');
        if (card) {
            openContributionDetail(Number(card.dataset.contributionId));
        }
    });
}

function bindDialogEvents() {
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
        elements.dialog.showModal();
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
        elements.dialog.close();
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
    return user?.nickname || user?.username || user?.email || user?.phone || '用户';
}

function bindBackHomeButton() {
    const backButton = document.querySelector('#backHomeBut');
    backButton.addEventListener('click', (event) => {
        event.preventDefault();
        event.stopPropagation();
        window.location.href = '/home/index.html';
    });
}
