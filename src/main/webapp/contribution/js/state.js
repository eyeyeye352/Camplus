export const elements = {
    navItems: document.querySelectorAll('.nav-item'),
    panels: document.querySelectorAll('.panel'),
    form: document.querySelector('#contributionForm'),
    list: document.querySelector('#contributionList'),
    statusFilter: document.querySelector('#statusFilter'),
    toast: document.querySelector('#toast'),
    loginStatus: document.querySelector('#loginStatus'),
    previousPage: document.querySelector('#previousPage'),
    nextPage: document.querySelector('#nextPage'),
    pageInfo: document.querySelector('#pageInfo'),
    dialog: document.querySelector('#contributionDialog'),
    closeDialog: document.querySelector('#closeDialog'),
    detailView: document.querySelector('#detailView'),
    detailStatus: document.querySelector('#detailStatus'),
    detailType: document.querySelector('#detailType'),
    detailCreateTime: document.querySelector('#detailCreateTime'),
    detailTitle: document.querySelector('#detailTitle'),
    detailContent: document.querySelector('#detailContent'),
    reviewBlock: document.querySelector('#reviewBlock'),
    reviewComment: document.querySelector('#reviewComment'),
    editContribution: document.querySelector('#editContribution'),
    editForm: document.querySelector('#editContributionForm'),
    editContributionType: document.querySelector('#editContributionType'),
    editTitle: document.querySelector('#editTitle'),
    editContent: document.querySelector('#editContent'),
    cancelEdit: document.querySelector('#cancelEdit')
};

export const pageState = {
    currentUser: null,
    currentPage: 1,
    pageSize: 5,
    totalPages: 1,
    currentDetail: null
};

export const statusMap = {
    0: ['审核中', 'pending'],
    1: ['已通过', 'approved'],
    2: ['已拒绝', 'rejected']
};

export const contributionTypes = ['新增问题'];
