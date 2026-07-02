// 基础 UI 控制 (复用并修复侧边栏切换逻辑)
// 缓存全局拉取到的贡献列表，供打开模态框时查找原始值
window.cachedContributions = [];

// ========== 后端数据交互 ==========

// 1. 获取待审核列表
async function fetchContributions() {
    try {
        const response = await fetch('/admin/contribution/list');
        const data = await response.json();
        window.cachedContributions = data; // 存入缓存
        renderTable(data);
    } catch (err) {
        console.error("数据加载失败:", err);
    }
}

// 2. 渲染表格内容
function renderTable(list) {
    const body = document.getElementById('contributionBody');
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

// 3. 打开审核弹窗并完成原内容回显
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

// 4. 关闭弹窗
function closeModal() {
    document.getElementById('reviewModal').classList.remove('active');
}

// 5. 组装高级数据传输：适配 ReviewRequestDTO
async function submitReview(status) {
    const contributionId = parseInt(document.getElementById('currentContributionId').value);
    const comment = document.getElementById('modalComment').value.trim();
    const finalQuestion = document.getElementById('modalQuestion').value.trim();
    const finalAnswer = document.getElementById('modalAnswer').value.trim();
    const finalContent = document.getElementById('modalContent').value.trim();
    const finalSourceUrl = document.getElementById('modalSourceUrl').value.trim();
    const categoryId = parseInt(document.getElementById('modalCategoryId').value);

    if (status === 2 && !comment) {
        alert("驳回操作必须填写管理员审核评语/拒绝理由！");
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
            alert(status === 1 ? "审核通过，并已成功联动同步至系统 FAQ 库！" : "已成功驳回该条贡献。");
            closeModal();
            fetchContributions();
        } else {
            alert("操作失败：" + (result.msg || "未知错误"));
        }
    } catch (err) {
        console.error("提交审核发生异常:", err);
        alert("网络请求失败，请检查后端控制器是否正常运行。");
    }
}

// 页面加载完成后自动拉取数据
document.addEventListener('DOMContentLoaded', () => {
    fetchContributions();
});

// ==========================================================================
// 1. 侧边栏菜单点击切换视图控制
// ==========================================================================
// ==========================================================================
// 核心修复：将 switchView 显式绑定到 window 全局，彻底粉碎作用域隔离问题
// ==========================================================================
window.switchView = function(viewName) {
    const panelReview = document.getElementById('panelReview');
    const panelImport = document.getElementById('panelImport');
    const menuReview = document.getElementById('menuReview');
    const menuImport = document.getElementById('menuImport');

    if (!panelReview || !panelImport) {
        console.error("【Camplus 调试错误】: 切换失败！...");
        return;
    }

    if (viewName === 'import') {
        panelReview.style.display = 'none';
        panelImport.style.display = 'block';

        if (menuReview) menuReview.classList.remove('active');
        if (menuImport) menuImport.classList.add('active');

        console.log("已成功切换至：知识导入界面");
    } else if (viewName === 'review') {
        // 切换回贡献审核
        panelReview.style.display = 'block'; // ✅ 修复：直接设置为 block 即可
        panelImport.style.display = 'none';

        if (menuReview) menuReview.classList.add('active');
        if (menuImport) menuImport.classList.remove('active');

        if (typeof fetchContributions === 'function') {
            fetchContributions();
        }
        console.log("已成功切换至：贡献审核界面");
    }
};

// ==========================================================================
// 2. 知识导入模块：统一文件上传 (不作前端内容读取分流)
// ==========================================================================
document.addEventListener("DOMContentLoaded", () => {
    const dropZone = document.getElementById('dropZone');
    const fileInput = document.getElementById('fileInput');
    let selectedFile = null;

    if (!dropZone) return;

    // 点击拖拽框触发文件选择
    dropZone.addEventListener('click', () => fileInput.click());

    // 阻止浏览器默认拖拽打开文件的行为
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => e.preventDefault(), false);
    });

    // 拖拽高亮特效
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.add('highlight'), false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.remove('highlight'), false);
    });

    // 接收拖拽文件
    dropZone.addEventListener('drop', (e) => {
        processFiles(e.dataTransfer.files);
    });

    // 接收点击选择的文件
    fileInput.addEventListener('change', (e) => {
        processFiles(e.target.files);
    });

    // 核心逻辑：不区分类型，一视同仁记录文件
    function processFiles(files) {
        if (files.length === 0) return;
        selectedFile = files[0];

        // 改变拖拽框的提示文字
        const dropText = document.querySelector('#dropZone .drop-text');
        if (dropText) {
            dropText.innerHTML = `当前选中：<span style="color: #40e0d0; font-weight: bold;">${selectedFile.name}</span>`;
        }

        // 显示上传确认面板
        const uploadResult = document.getElementById('uploadResult');
        const fileInfo = document.getElementById('selectedFileInfo');

        if (uploadResult && fileInfo) {
            const sizeKB = (selectedFile.size / 1024).toFixed(2);
            fileInfo.innerHTML = `即将把文件 <b>${selectedFile.name}</b> (${sizeKB} KB) 提交至后端进行落盘解析与向量化。`;
            uploadResult.style.display = 'block';
        }
    }

    // 提交给后端 KnowledgeImportController 的 /upload 接口
    document.getElementById('btnSubmitImport').addEventListener('click', async () => {
        if (!selectedFile) return;

        const btn = document.getElementById('btnSubmitImport');
        const originalText = btn.innerText;
        btn.innerText = '⏳ 正在上传并进行向量化处理 (耗时较长请稍候)...';
        btn.disabled = true;

        // 构造与后端 @RequestParam("file") MultipartFile 匹配的数据
        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            // 对接 KnowledgeImportController[cite: 4]
            const response = await fetch('/admin/knowledge/upload', {
                method: 'POST',
                body: formData
            });
            const result = await response.json();

            // 直接采用后端返回的详细 msg[cite: 4]
            if (result.success) {
                alert(`🎉 导入成功！\n${result.msg}`);

                // 重置前端 UI 状态
                document.getElementById('uploadResult').style.display = 'none';
                selectedFile = null;
                fileInput.value = '';
                const dropText = document.querySelector('#dropZone .drop-text');
                if (dropText) {
                    dropText.innerHTML = `将你的知识库文件拖拽到此处，或 <span>点击选择文件</span>`;
                }
            } else {
                alert('处理失败：' + result.msg);
            }
        } catch (err) {
            console.error(err);
            alert('网络请求异常，无法连接到后端的知识库导入接口。');
        } finally {
            // 恢复按钮状态
            btn.innerText = originalText;
            btn.disabled = false;
        }
    });
});