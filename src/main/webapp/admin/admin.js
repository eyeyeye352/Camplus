window.cachedContributions = [];

async function fetchContributions() {
    try {
        const response = await fetch('/admin/contribution/list');
        const data = await response.json();
        window.cachedContributions = data;
        renderTable(data);
    } catch (err) {
        console.error("数据加载失败:", err);
    }
}

function renderTable(list) {
    const body = document.getElementById('contributionBody');
    if (!body) return;
    
    body.innerHTML = '';

    if (!list || list.length === 0) {
        body.innerHTML = `<tr><td colspan="6" style="text-align:center; color:rgba(255,255,255,0.4);">暂无待审核数据</td></tr>`;
        return;
    }

    list.forEach(item => {
        const tr = document.createElement('tr');

        let typeText = "未知";
        if (item.contributionType === 0) typeText = "💡 新增问题";
        if (item.contributionType === 1) typeText = "🔧 答案纠错";
        if (item.contributionType === 2) typeText = "📚 资料贡献";

        tr.innerHTML = `
            <td>${item.contributionId}</td>
            <td>${item.username || '匿名用户'}</td>
            <td style="color: #40e0d0;">${typeText}</td>
            <td>${item.title || '（无标题）'}</td>
            <td>${new Date(item.createTime).toLocaleString()}</td>
            <td>
                <button class="btn-review-trigger" onclick="openReviewModal(${item.contributionId})">去审核</button>
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
    document.getElementById('currentContributionType').value = record.contributionType;

    document.getElementById('modalQuestion').value = record.question || record.title || '';
    document.getElementById('modalAnswer').value = record.answer || '';
    document.getElementById('modalContent').value = record.content || '';
    document.getElementById('modalSourceUrl').value = record.sourceUrl || '';

    document.getElementById('modalComment').value = (record.contributionType === 0) ? "内容准确，准予问答库入库" : "纠错属实，修改入库";
    document.getElementById('modalCategoryId').value = "1";

    if (record.contributionType === 2) {
        document.getElementById('questionGroup').style.display = 'none';
        document.getElementById('answerGroup').style.display = 'none';
    } else {
        document.getElementById('questionGroup').style.display = 'block';
        document.getElementById('answerGroup').style.display = 'block';
    }

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
    const finalContent = document.getElementById('modalContent').value.trim();
    const finalSourceUrl = document.getElementById('modalSourceUrl').value.trim();
    const categoryId = parseInt(document.getElementById('modalCategoryId').value);

    if (status === 2 && !comment) {
        showToast("驳回操作必须填写管理员审核评语/拒绝理由！", "warning");
        return;
    }

    const dtoPayload = {
        contributionId: contributionId,
        status: status,
        comment: comment,
        finalQuestion: finalQuestion,
        finalAnswer: finalAnswer,
        finalContent: finalContent,
        finalSourceUrl: finalSourceUrl,
        categoryId: categoryId
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
            showToast(status === 1 ? "审核通过，并已成功联动同步至系统 FAQ 库！" : "已成功驳回该条贡献。", "success");
            closeModal();
            fetchContributions();
        } else {
            showToast("操作失败：" + (result.msg || "未知错误"), "error");
        }
    } catch (err) {
        console.error("提交审核发生异常:", err);
        showToast("网络请求失败，请检查后端控制器是否正常运行。", "error");
    }
}

window.switchView = function(viewName) {
    const panelReview = document.getElementById('panelReview');
    const panelImport = document.getElementById('panelImport');
    const panelSettings = document.getElementById('panelSettings');
    const menuReview = document.getElementById('menuReview');
    const menuImport = document.getElementById('menuImport');
    const menuSettings = document.getElementById('menuSettings');

    if (!panelReview || !panelImport || !panelSettings) {
        console.error("切换失败！未找到面板元素");
        return;
    }

    if (viewName === 'import') {
        panelReview.style.display = 'none';
        panelImport.style.display = 'block';
        panelSettings.style.display = 'none';

        if (menuReview) menuReview.classList.remove('active');
        if (menuImport) menuImport.classList.add('active');
        if (menuSettings) menuSettings.classList.remove('active');
    } else if (viewName === 'settings') {
        panelReview.style.display = 'none';
        panelImport.style.display = 'none';
        panelSettings.style.display = 'block';

        if (menuReview) menuReview.classList.remove('active');
        if (menuImport) menuImport.classList.remove('active');
        if (menuSettings) menuSettings.classList.add('active');

        fetchAdminList();
    } else {
        panelReview.style.display = 'block';
        panelImport.style.display = 'none';
        panelSettings.style.display = 'none';

        if (menuReview) menuReview.classList.add('active');
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
        selectedFile = files[0];

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
                    dropText.innerHTML = `将你的知识库文件拖拽到此处，或 <span>点击选择文件</span>`;
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