export const elements = {};

export function initElements() {
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

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initElements);
} else {
    initElements();
}

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

export const contributionTypes = ['新增问题', '答案纠错'];
