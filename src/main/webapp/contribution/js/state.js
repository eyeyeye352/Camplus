export const elements = {
    navItems: document.querySelectorAll('.nav-item'),
    panels: document.querySelectorAll('.panel'),
    form: document.querySelector('#contributionForm'),
    userIdInput: document.querySelector('#userIdInput'),
    list: document.querySelector('#contributionList'),
    statusFilter: document.querySelector('#statusFilter'),
    toast: document.querySelector('#toast'),
    loginStatus: document.querySelector('#loginStatus')
};

export const statusMap = {
    0: ['待审核', 'pending'],
    1: ['已通过', 'approved'],
    2: ['已拒绝', 'rejected']
};

export const contributionTypes = ['新增问题', '答案纠错'];
