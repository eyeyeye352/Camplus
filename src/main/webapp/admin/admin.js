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
function switchView(viewName) {
    const panelReview = document.getElementById('panelReview');
    const panelImport = document.getElementById('panelImport');
    const menuReview = document.getElementById('menuReview');
    const menuImport = document.getElementById('menuImport');

    if (viewName === 'import') {
        // 切换到知识导入：隐藏审核，显示导入
        if (panelReview) panelReview.style.display = 'none';
        if (panelImport) panelImport.style.display = 'block';

        // 菜单高亮切换
        if (menuReview) menuReview.classList.remove('active');
        if (menuImport) menuImport.classList.add('active');
    } else if (viewName === 'review') {
        // 切换回贡献审核：显示审核，隐藏导入
        if (panelReview) panelReview.style.display = 'block';
        if (panelImport) panelImport.style.display = 'none';

        // 菜单高亮切换
        if (menuReview) menuReview.classList.add('active');
        if (menuImport) menuImport.classList.remove('active');

        // 顺便刷新一下原有的审核列表数据
        if (typeof fetchContributions === 'function') {
            fetchContributions();
        }
    }
}

// ==========================================================================
// 2. 知识导入模块：拖拽与前端 CSV 数据解析
// ==========================================================================
document.addEventListener("DOMContentLoaded", () => {
    const dropZone = document.getElementById('dropZone');
    const fileInput = document.getElementById('fileInput');
    let selectedFile = null; // 用于缓存当前拖入的文件

    if (!dropZone) return; // 防止在其他未加载该元素的页面报错

    // 点击拖拽框任意地方，触发隐藏的真正的文件选择器
    dropZone.addEventListener('click', () => fileInput.click());

    // 必须阻止浏览器的默认行为
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => e.preventDefault(), false);
    });

    // 拖进来时高亮
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.add('highlight'), false);
    });

    // 离开或松开时取消高亮
    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.remove('highlight'), false);
    });

    // 监听鼠标松开（放下文件）事件
    dropZone.addEventListener('drop', (e) => {
        processFiles(e.dataTransfer.files);
    });

    // 监听点击选择文件后的变化
    fileInput.addEventListener('change', (e) => {
        processFiles(e.target.files);
    });

    // 读取并解析 CSV 数据进行漂亮的前端表格渲染
    function processFiles(files) {
        if (files.length === 0) return;
        const file = files[0];

        // 后缀校验
        if (!file.name.endsWith('.csv')) {
            alert('文件格式不支持！请拖入标准的 .csv 逗号分隔符文件。');
            return;
        }

        selectedFile = file;

        // 利用 HTML5 FileReader 读取文本内容
        const reader = new FileReader();
        reader.readAsText(file, 'utf-8');
        reader.onload = function (event) {
            const csvContent = event.target.result;
            const rows = csvContent.split('\n');
            const tbody = document.getElementById('previewBody');
            tbody.innerHTML = ''; // 清空历史残留

            let validRowCount = 0;
            // 索引从 1 开始：代表自动跳过 CSV 的第一行表头 (question, answer)
            for (let i = 1; i < rows.length; i++) {
                const rowText = rows[i].trim();
                if (!rowText) continue; // 跳过空白行

                const columns = rowText.split(',');
                if (columns.length >= 2) {
                    validRowCount++;
                    const q = columns[0].replace(/"/g, ''); // 去除可能存在的包裹双引号
                    const a = columns[1].replace(/"/g, '');

                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td style="padding: 10px; color: rgba(255,255,255,0.9); border-bottom: 1px solid rgba(255,255,255,0.05);">${q}</td>
                        <td style="padding: 10px; color: rgba(255,255,255,0.6); border-bottom: 1px solid rgba(255,255,255,0.05);">${a}</td>
                    `;
                    tbody.appendChild(tr);
                }
            }

            if (validRowCount > 0) {
                document.getElementById('uploadResult').style.display = 'block';
            } else {
                alert('未在 CSV 文件中解析到合规的问答数据！格式应为：问题,答案');
                document.getElementById('uploadResult').style.display = 'none';
            }
        };
    }

    // 绑定最终一键导入后端数据库按钮
    document.getElementById('btnSubmitImport').addEventListener('click', async () => {
        if (!selectedFile) return;

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            const response = await fetch('/admin/faq/import', {
                method: 'POST',
                body: formData
            });
            const result = await response.json();

            if (result.success) {
                alert(`🎉 批量导入成功！共计 ${result.count} 条校务知识直接录入系统 FAQ 库！`);
                document.getElementById('uploadResult').style.display = 'none';
                selectedFile = null;
                fileInput.value = '';
            } else {
                alert('导入失败：' + result.msg);
            }
        } catch (err) {
            console.error(err);
            alert('无法连接到后端解析接口，请检查后端服务是否启动。');
        }
    });
});