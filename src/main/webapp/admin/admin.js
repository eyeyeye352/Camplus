window.cachedContributions = [];
window.cachedFaqs = [];

async function fetchContributions() {
    const statusFilter = document.getElementById('reviewStatusFilter')?.value || 'pending';
    try {
        const url = statusFilter === 'all' 
            ? '/admin/contribution/all' 
            : '/admin/contribution/list';
        const response = await fetch(url);
        const data = await response.json();
        window.cachedContributions = data;
        renderContributionTable(data);
    } catch (err) {
        console.error("数据加载失败:", err);
    }
}

function renderContributionTable(list) {
    const body = document.getElementById('contributionBody');
    if (!body) return;
    
    body.innerHTML = '';

    if (!list || list.length === 0) {
        body.innerHTML = `<tr><td colspan="6" style="text-align:center; color:rgba(255,255,255,0.4);">暂无数据</td></tr>`;
        return;
    }

    list.forEach(item => {
        const tr = document.createElement('tr');

        let statusText = "未知";
        let statusColor = "#999";
        if (item.status === 0) { statusText = "待审核"; statusColor = "#ff9800"; }
        if (item.status === 1) { statusText = "已通过"; statusColor = "#4caf50"; }
        if (item.status === 2) { statusText = "已拒绝"; statusColor = "#f44336"; }

        const questionPreview = item.title ? (item.title.length > 30 ? item.title.substring(0, 30) + '...' : item.title) : '无问题';

        tr.innerHTML = `
            <td>${item.contributionId}</td>
            <td>${item.username || '匿名用户'}</td>
            <td>${questionPreview}</td>
            <td style="color: ${statusColor};">${statusText}</td>
            <td>${new Date(item.createTime).toLocaleString()}</td>
            <td>
                ${item.status === 0 ? `<button class="btn-review-trigger" onclick="openReviewModal(${item.contributionId})">去审核</button>` : '-'}
            </td>
        `;
        body.appendChild(tr);
    });
}

function openReviewModal(id) {
    const record = window.cachedContributions.find(c => c.contributionId === id);
    if (!record) return;

    document.getElementById('modalContributionId').innerText = record.contributionId;
    document.getElementById('currentContributionId').value = record.contributionId;

    document.getElementById('modalQuestion').value = record.title || '';
    document.getElementById('modalAnswer').value = record.content || '';
    document.getElementById('modalComment').value = '';

    document.getElementById('reviewModal').classList.add('active');
}

function closeModal() {
    document.getElementById('reviewModal').classList.remove('active');
}

async function submitReview(status) {
    const contributionId = parseInt(document.getElementById('currentContributionId').value);
    const comment = document.getElementById('modalComment').value.trim();
    const finalQuestion = document.getElementById('modalQuestion').value.trim();
    const finalAnswer = document.getElementById('modalAnswer').value.trim();

    if (status === 2 && !comment) {
        showToast("驳回操作必须填写审核评语！", "warning");
        return;
    }

    if (status === 1 && (!finalQuestion || !finalAnswer)) {
        showToast("审核通过时问题和答案不能为空！", "warning");
        return;
    }

    const dtoPayload = {
        contributionId: contributionId,
        status: status,
        comment: comment,
        finalQuestion: finalQuestion,
        finalAnswer: finalAnswer
    };

    try {
        const response = await fetch('/admin/contribution/review', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dtoPayload)
        });

        const result = await response.json();
        if (result.success) {
            showToast(status === 1 ? "审核通过，并已同步至FAQ库！" : "已成功驳回该条贡献。", "success");
            closeModal();
            fetchContributions();
        } else {
            showToast("操作失败：" + (result.msg || "未知错误"), "error");
        }
    } catch (err) {
        console.error("提交审核发生异常:", err);
        showToast("网络请求失败，请检查后端是否正常运行。", "error");
    }
}

async function fetchFaqList() {
    try {
        const response = await fetch('/api/faq/list');
        const data = await response.json();
        if (data.success) {
            window.cachedFaqs = data.data;
            renderFaqTable(data.data);
        }
    } catch (err) {
        console.error("FAQ列表加载失败:", err);
    }
}

function renderFaqTable(list) {
    const body = document.getElementById('faqBody');
    if (!body) return;
    
    body.innerHTML = '';

    if (!list || list.length === 0) {
        body.innerHTML = `<tr><td colspan="6" style="text-align:center; color:rgba(255,255,255,0.4);">暂无FAQ数据</td></tr>`;
        return;
    }

    list.forEach(item => {
        const tr = document.createElement('tr');

        let statusText = "已下架";
        let statusColor = "#f44336";
        if (item.displayStatus === 1) { 
            statusText = "展示中"; 
            statusColor = "#4caf50"; 
        }

        const questionPreview = item.question ? (item.question.length > 30 ? item.question.substring(0, 30) + '...' : item.question) : '无问题';

        tr.innerHTML = `
            <td>${item.faqId}</td>
            <td style="cursor: pointer; text-decoration: underline;" onclick="openFaqDetail(${item.faqId})">${questionPreview}</td>
            <td>${item.hotScore || 0}</td>
            <td>${item.questionCount || 0}</td>
            <td style="color: ${statusColor};">${statusText}</td>
            <td>
                <button class="btn-review-trigger" onclick="toggleFaqStatus(${item.faqId}, ${item.displayStatus})">
                    ${item.displayStatus === 1 ? '下架' : '上架'}
                </button>
            </td>
        `;
        body.appendChild(tr);
    });
}

async function toggleFaqStatus(faqId, currentStatus) {
    const newStatus = currentStatus === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? '上架' : '下架';
    
    try {
        const response = await fetch('/api/faq/status', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ faqId, displayStatus: newStatus })
        });

        const result = await response.json();
        if (result.success) {
            showToast(`FAQ已${actionText}成功！`, "success");
            fetchFaqList();
        } else {
            showToast("操作失败：" + (result.msg || "未知错误"), "error");
        }
    } catch (err) {
        console.error("操作失败:", err);
        showToast("网络请求失败", "error");
    }
}

async function handleFaqSearch(event) {
    const keyword = event.target.value.trim();
    if (!keyword) {
        fetchFaqList();
        return;
    }
    if (event.key === 'Enter' || keyword.length >= 2) {
        try {
            const response = await fetch(`/api/faq/search?keyword=${encodeURIComponent(keyword)}`);
            const data = await response.json();
            if (data.success) {
                renderFaqTable(data.data);
            }
        } catch (err) {
            console.error("FAQ搜索失败:", err);
        }
    }
}

function openFaqDetail(faqId) {
    const faq = window.cachedFaqs.find(item => item.faqId === faqId);
    if (!faq) return;
    
    document.getElementById('faqDetailId').textContent = faq.faqId;
    document.getElementById('faqDetailQuestion').value = faq.question || '';
    document.getElementById('faqDetailAnswer').value = faq.answer || '';
    document.getElementById('faqDetailHotScore').value = faq.hotScore || 0;
    document.getElementById('faqDetailQuestionCount').value = faq.questionCount || 0;
    
    document.getElementById('faqDetailModal').classList.add('active');
}

function closeFaqDetailModal() {
    document.getElementById('faqDetailModal').classList.remove('active');
}

window.switchView = function(viewName) {
    const panelReview = document.getElementById('panelReview');
    const panelFaq = document.getElementById('panelFaq');
    const panelImport = document.getElementById('panelImport');
    const panelSettings = document.getElementById('panelSettings');
    const menuReview = document.getElementById('menuReview');
    const menuFaq = document.getElementById('menuFaq');
    const menuImport = document.getElementById('menuImport');
    const menuSettings = document.getElementById('menuSettings');

    if (!panelReview || !panelImport || !panelSettings) {
        console.error("切换失败！未找到面板元素");
        return;
    }

    if (viewName === 'faq') {
        panelReview.style.display = 'none';
        panelFaq.style.display = 'block';
        panelImport.style.display = 'none';
        panelSettings.style.display = 'none';

        if (menuReview) menuReview.classList.remove('active');
        if (menuFaq) menuFaq.classList.add('active');
        if (menuImport) menuImport.classList.remove('active');
        if (menuSettings) menuSettings.classList.remove('active');

        fetchFaqList();
    } else if (viewName === 'import') {
        panelReview.style.display = 'none';
        panelFaq.style.display = 'none';
        panelImport.style.display = 'block';
        panelSettings.style.display = 'none';

        if (menuReview) menuReview.classList.remove('active');
        if (menuFaq) menuFaq.classList.remove('active');
        if (menuImport) menuImport.classList.add('active');
        if (menuSettings) menuSettings.classList.remove('active');
    } else if (viewName === 'settings') {
        panelReview.style.display = 'none';
        panelFaq.style.display = 'none';
        panelImport.style.display = 'none';
        panelSettings.style.display = 'block';

        if (menuReview) menuReview.classList.remove('active');
        if (menuFaq) menuFaq.classList.remove('active');
        if (menuImport) menuImport.classList.remove('active');
        if (menuSettings) menuSettings.classList.add('active');

        fetchAdminList();
    } else {
        panelReview.style.display = 'block';
        panelFaq.style.display = 'none';
        panelImport.style.display = 'none';
        panelSettings.style.display = 'none';

        if (menuReview) menuReview.classList.add('active');
        if (menuFaq) menuFaq.classList.remove('active');
        if (menuImport) menuImport.classList.remove('active');
        if (menuSettings) menuSettings.classList.remove('active');

        if (typeof fetchContributions === 'function') {
            fetchContributions();
        }
    }
};

async function fetchAdminList() {
    const body = document.getElementById('adminListBody');
    if (!body) return;
    
    body.innerHTML = '<tr><td colspan="5" style="text-align:center; color:rgba(255,255,255,0.4);">正在加载...</td></tr>';

    try {
        const response = await fetch('/admin/user/listAdmin');
        const result = await response.json();
        
        if (!result.success) {
            body.innerHTML = '<tr><td colspan="5" style="text-align:center; color:rgba(255,255,255,0.4);">加载失败</td></tr>';
            return;
        }

        const adminList = result.data;
        if (!adminList || adminList.length === 0) {
            body.innerHTML = '<tr><td colspan="3" style="text-align:center; color:rgba(255,255,255,0.4);">暂无管理员</td></tr>';
            return;
        }

        body.innerHTML = adminList.map(user => `
            <tr>
                <td>${user.userId}</td>
                <td>${user.username}</td>
                <td>${user.email || '-'}</td>
            </tr>
        `).join('');
    } catch (err) {
        console.error("加载管理员列表失败:", err);
        body.innerHTML = '<tr><td colspan="3" style="text-align:center; color:rgba(255,255,255,0.4);">加载失败</td></tr>';
    }
}

async function addAdmin() {
    const username = document.getElementById('newAdminUsername').value.trim();
    if (!username) {
        showToast("请输入要设为管理员的用户名", "warning");
        return;
    }

    try {
        const response = await fetch('/admin/user/addAdmin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username: username })
        });

        const result = await response.json();
        if (result.success) {
            showToast("设置成功！用户 " + username + " 已成为管理员", "success");
            document.getElementById('newAdminUsername').value = '';
            fetchAdminList();
        } else {
            showToast("操作失败：" + (result.msg || "未知错误"), "error");
        }
    } catch (err) {
        console.error("设置管理员失败:", err);
        showToast("网络请求失败", "error");
    }
}

function updateLoginStatus() {
    const loginStatus = document.getElementById('loginStatus');
    const username = sessionStorage.getItem('username');
    const role = sessionStorage.getItem('role');
    
    if (loginStatus) {
        if (username && role === '1') {
            loginStatus.textContent = `管理员: ${username}`;
        } else {
            loginStatus.textContent = '未登录';
            setTimeout(() => {
                window.location.href = '../login/login.html';
            }, 1000);
        }
    }
}

document.addEventListener("DOMContentLoaded", () => {
    updateLoginStatus();
    fetchContributions();

    const dropZone = document.getElementById('dropZone');
    const fileInput = document.getElementById('fileInput');
    let selectedFile = null;

    if (!dropZone) return;

    dropZone.addEventListener('click', () => fileInput.click());

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => e.preventDefault(), false);
    });

    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.add('highlight'), false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.remove('highlight'), false);
    });

    dropZone.addEventListener('drop', (e) => {
        processFiles(e.dataTransfer.files);
    });

    fileInput.addEventListener('change', (e) => {
        processFiles(e.target.files);
    });

    function processFiles(files) {
        if (files.length === 0) return;
        
        const validExts = ['.txt', '.pdf', '.docx'];
        const file = files[0];
        const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
        
        if (!validExts.includes(ext)) {
            showToast('不支持的文件类型！仅支持 .txt, .pdf, .docx', 'warning');
            return;
        }
        
        selectedFile = file;

        const dropText = document.querySelector('#dropZone .drop-text');
        if (dropText) {
            dropText.innerHTML = `当前选中：<span style="color: #40e0d0; font-weight: bold;">${selectedFile.name}</span>`;
        }

        const uploadResult = document.getElementById('uploadResult');
        const fileInfo = document.getElementById('selectedFileInfo');

        if (uploadResult && fileInfo) {
            const sizeKB = (selectedFile.size / 1024).toFixed(2);
            fileInfo.innerHTML = `即将把文件 <b>${selectedFile.name}</b> (${sizeKB} KB) 提交至后端进行落盘解析与向量化。`;
            uploadResult.style.display = 'block';
        }
    }

    document.getElementById('btnSubmitImport').addEventListener('click', async () => {
        if (!selectedFile) return;

        const btn = document.getElementById('btnSubmitImport');
        const originalText = btn.innerText;
        btn.innerText = '⏳ 正在上传并进行向量化处理 (耗时较长请稍候)...';
        btn.disabled = true;

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            const response = await fetch('/admin/knowledge/upload', {
                method: 'POST',
                body: formData
            });
            const result = await response.json();

            if (result.success) {
                showToast(`导入成功！\n${result.msg}`, "success");

                document.getElementById('uploadResult').style.display = 'none';
                selectedFile = null;
                fileInput.value = '';
                const dropText = document.querySelector('#dropZone .drop-text');
                if (dropText) {
                    dropText.innerHTML = `将知识库文件拖拽到此处，或 <span>点击选择文件</span>`;
                }
            } else {
                showToast('处理失败：' + result.msg, "error");
            }
        } catch (err) {
            console.error(err);
            showToast('网络请求异常，无法连接到后端的知识库导入接口。', "error");
        } finally {
            btn.innerText = originalText;
            btn.disabled = false;
        }
    });
});