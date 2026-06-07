const contextPath = window.location.pathname.split('/contribution/')[0];
const apiBase = `${window.location.origin}${contextPath}/contribution`;

async function requestJson(url, options = {}) {
    const response = await fetch(url, options);
    const result = await response.json();
    if (!response.ok || !result.success) {
        throw new Error(result.message || '请求失败');
    }
    return result.data;
}

export function createContribution(formData) {
    return requestJson(`${apiBase}/create`, {
        method: 'POST',
        body: new URLSearchParams(formData)
    });
}

export function fetchContributions(status) {
    const url = new URL(`${apiBase}/list`);
    if (status !== '') {
        url.searchParams.set('status', status);
    }
    return requestJson(url);
}
