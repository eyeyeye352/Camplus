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

