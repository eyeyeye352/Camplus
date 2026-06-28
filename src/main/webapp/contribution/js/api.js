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

export function fetchContributions(userId, status, page, pageSize) {
    const url = new URL(`${apiBase}/list`);
    url.searchParams.set('userId', userId);
    url.searchParams.set('page', page);
    url.searchParams.set('pageSize', pageSize);
    if (status !== '') {
        url.searchParams.set('status', status);
    }
    return requestJson(url);
}

export function fetchContributionDetail(userId, contributionId) {
    const url = new URL(`${apiBase}/detail`);
    url.searchParams.set('userId', userId);
    url.searchParams.set('contribution_id', contributionId);
    return requestJson(url);
}

export function updateContribution(formData, userId, contributionId) {
    const params = new URLSearchParams(formData);
    params.set('userId', userId);
    params.set('contribution_id', contributionId);
    return requestJson(`${apiBase}/update`, {
        method: 'POST',
        body: params
    });
}
