import { createContribution, fetchContributions, fetchCurrentUser } from './api.js';
import { elements, pageState } from './state.js';
import { renderList, renderLoadError, renderLoading, setLoginStatus, showToast } from './render.js';

export function bindEvents() {
    bindNavigation();
    bindSubmitForm();
    bindBackHomeButton();
    elements.statusFilter.addEventListener('change', loadContributions);
}

export async function initCurrentUser() {
    try {
        const user = await fetchCurrentUser();
        pageState.currentUser = user;
        setLoginStatus(displayName(user));
    } catch (error) {
        pageState.currentUser = null;
        setLoginStatus('未登录');
    }
}

function bindNavigation() {
    elements.navItems.forEach((item) => {
        item.addEventListener('click', () => {
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
            await createContribution(formData);
            elements.form.reset();
            showToast('提交成功，已进入待审核');
        } catch (error) {
            showToast(error.message || '提交失败，请稍后重试');
        }
    });
}

async function loadContributions() {
    renderLoading();

    try {
        if (!pageState.currentUser) {
            await initCurrentUser();
        }
        if (!pageState.currentUser) {
            throw new Error('请先登录后查看我的贡献');
        }

        const data = await fetchContributions(elements.statusFilter.value);
        renderList(data || []);
        setLoginStatus(displayName(pageState.currentUser));
    } catch (error) {
        renderLoadError(error.message);
        if (!pageState.currentUser) {
            setLoginStatus('未登录');
        }
    }
}

function displayName(user) {
    return user?.nickname || user?.username || user?.email || user?.phone || '用户';
}

// 绑定返回首页按钮功能
function bindBackHomeButton(){
    const backButton=document.querySelector("#backHomeBut");
    backButton.addEventListener("click",(ev)=>{
        ev.preventDefault()
        ev.stopPropagation()

        window.location.href="/home/index.html";
    })
}
