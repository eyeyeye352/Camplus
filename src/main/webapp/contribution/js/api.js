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

export function createContribution(formData, userId) {
    const params = new URLSearchParams(formData);
    params.set('userId', userId);
    return requestJson(`${apiBase}/create`, {
        method: 'POST',
        body: params
    });
}

export function fetchContributions(userId, status) {
    const url = new URL(`${apiBase}/list`);
    url.searchParams.set('userId', userId);
    if (status !== '') {
        url.searchParams.set('status', status);
    }
    return requestJson(url);
}
