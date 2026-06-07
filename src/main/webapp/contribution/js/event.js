import { createContribution, fetchContributions } from './api.js';
import { elements } from './state.js';
import { renderList, renderLoadError, renderLoading, setLoginStatus, showToast } from './render.js';

export function bindEvents() {
    bindNavigation();
    bindSubmitForm();
    elements.statusFilter.addEventListener('change', loadContributions);
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
        const data = await fetchContributions(elements.statusFilter.value);
        renderList(data || []);
        setLoginStatus('已连接用户贡献接口');
    } catch (error) {
        renderLoadError(error.message);
        setLoginStatus('需要登录后使用');
    }
}
