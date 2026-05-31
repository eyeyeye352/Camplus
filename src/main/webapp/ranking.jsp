<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>

<html lang="zh-CN">
<head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>排行榜 - Camplus 校园生活百事通</title>
<!-- Tailwind CSS -->
<script src="https://modao.cc/agent-py/static/source/js/tailwindcss.js"></script>
<!-- Iconify -->
<script src="https://modao.cc/agent-py/static/source/js/iconify-icon.min.js"></script>
<!-- ECharts -->
<script src="https://modao.cc/agent-py/static/source/js/echarts.min.js"></script>
<style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
        body {
            font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
            background-color: #F8FAFC;
        }
        .academic-blue { color: #2B5C9E; }
        .bg-academic-blue { background-color: #2B5C9E; }
        .vibrant-green { color: #4CAF50; }
        .bg-vibrant-green { background-color: #4CAF50; }
        .warm-orange { color: #FF9800; }
        .bg-warm-orange { background-color: #FF9800; }
        
        .card-shadow {
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
        }
    </style>
</head>
<body class="text-slate-800">
<!-- 导航栏 -->
<nav class="sticky top-0 z-50 bg-white border-b border-slate-100">
<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
<div class="flex justify-between h-16 items-center">
<div class="flex items-center space-x-2">
<a class="flex items-center space-x-2" href="index.jsp">
<div class="w-10 h-10 bg-academic-blue rounded-lg flex items-center justify-center">
<iconify-icon class="text-white text-2xl" icon="solar:square-academic-cap-bold-duotone"></iconify-icon>
</div>
<span class="text-2xl font-bold academic-blue tracking-tight">Camplus</span>
</a>
</div>
<div class="hidden md:flex items-center space-x-8">
<a class="text-slate-600 hover:text-academic-blue transition-colors" href="./index.jsp">首页</a>
<a class="text-slate-600 hover:text-academic-blue transition-colors" href="#">知识库</a>
<a class="text-academic-blue font-semibold border-b-2 border-academic-blue pb-1" href="./ranking.jsp">排行榜</a>
<a class="text-slate-600 hover:text-academic-blue transition-colors" href="./contribute.jsp">众包贡献</a>
</div>
<div class="flex items-center space-x-4">
<button class="text-slate-600 hover:text-academic-blue font-medium px-4 py-2">登录</button>
<button class="bg-academic-blue text-white px-5 py-2 rounded-full font-medium hover:bg-blue-800 transition-all shadow-md">注册</button>
</div>
</div>
</div>
</nav>
<!-- 头部区域 -->
<header class="bg-white py-12 border-b border-slate-100">
<div class="max-w-7xl mx-auto px-4">
<div class="flex flex-col md:flex-row md:items-center justify-between gap-8">
<div>
<h1 class="text-3xl font-bold text-slate-900 mb-2">常见问题排行榜</h1>
<p class="text-slate-500">汇聚全校关注热点，实时更新高频查询词与热门问答。</p>
</div>
<div class="flex bg-slate-100 p-1 rounded-xl">
<button class="px-6 py-2 bg-white text-academic-blue font-semibold rounded-lg shadow-sm" id="todayBtn" onclick="switchTimeRange('today')">本日</button>
<button class="px-6 py-2 text-slate-500 font-medium hover:text-academic-blue" id="weekBtn" onclick="switchTimeRange('week')">本周</button>
<button class="px-6 py-2 text-slate-500 font-medium hover:text-academic-blue" id="monthBtn" onclick="switchTimeRange('month')">本月</button>
</div>
</div>
</div>
</header>
<main class="max-w-7xl mx-auto px-4 py-12">
<div class="grid lg:grid-cols-3 gap-8">
<!-- 左侧：趋势分析图表 -->
<div class="lg:col-span-1">
<div class="bg-white p-6 rounded-2xl border border-slate-100 card-shadow h-full">
<h2 class="text-lg font-bold mb-6 flex items-center gap-2">
<iconify-icon class="text-academic-blue" icon="solar:chart-2-bold-duotone"></iconify-icon>
                    问答分类热度
                </h2>
<div class="w-full h-64" id="chart-pie"></div>
<div class="mt-8 space-y-4" id="category-stats">
<div class="flex items-center justify-between">
<span class="text-sm text-slate-500 flex items-center gap-2">
<span class="w-3 h-3 rounded-full bg-blue-500"></span> 教务服务
                        </span>
<span class="text-sm font-bold">42%</span>
</div>
<div class="flex items-center justify-between">
<span class="text-sm text-slate-500 flex items-center gap-2">
<span class="w-3 h-3 rounded-full bg-green-500"></span> 校园卡/生活
                        </span>
<span class="text-sm font-bold">28%</span>
</div>
<div class="flex items-center justify-between">
<span class="text-sm text-slate-500 flex items-center gap-2">
<span class="w-3 h-3 rounded-full bg-orange-500"></span> 图书馆
                        </span>
<span class="text-sm font-bold">15%</span>
</div>
<div class="flex items-center justify-between">
<span class="text-sm text-slate-500 flex items-center gap-2">
<span class="w-3 h-3 rounded-full bg-red-500"></span> 报修/后勤
                        </span>
<span class="text-sm font-bold">15%</span>
</div>
</div>
</div>
</div>
<!-- 右侧：榜单列表 -->
<div class="lg:col-span-2 space-y-4" id="ranking-list">
<!-- 榜单项将通过 JS 动态加载 -->
</div>
<!-- 翻页 -->
<div class="flex justify-center pt-8" id="pagination-container" style="display: none;">
<nav class="flex items-center gap-2">
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50" onclick="prevPage()">
<iconify-icon icon="solar:alt-arrow-left-linear"></iconify-icon>
</button>
<button class="w-10 h-10 rounded-lg bg-academic-blue text-white flex items-center justify-center font-bold" id="page-1">1</button>
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50" id="page-2">2</button>
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50" id="page-3">3</button>
<span class="px-2 text-slate-400">...</span>
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50" id="page-12">12</button>
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50" onclick="nextPage()">
<iconify-icon icon="solar:alt-arrow-right-linear"></iconify-icon>
</button>
</nav>
</div>
</div>
</main>
<!-- 页脚 -->
<footer class="bg-white border-t border-slate-100 py-12 mt-12">
<div class="max-w-7xl mx-auto px-4 text-center">
<p class="text-slate-400 text-sm">© 2026 Camplus 校园生活百事通. 为智慧校园而生.</p>
</div>
</footer>
<script>
        let currentTimeRange = 'today';
        let currentPage = 1;
        const pageSize = 10;

        const chartDom = document.getElementById('chart-pie');
        let myChart = null;

        function initChart() {
            myChart = echarts.init(chartDom);
            const option = {
                tooltip: { trigger: 'item' },
                series: [{
                    name: '热度占比',
                    type: 'pie',
                    radius: ['40%', '70%'],
                    avoidLabelOverlap: false,
                    itemStyle: {
                        borderRadius: 10,
                        borderColor: '#fff',
                        borderWidth: 2
                    },
                    label: { show: false },
                    emphasis: { label: { show: false } },
                    labelLine: { show: false },
                    data: [
                        { value: 42, name: '教务服务', itemStyle: {color: '#3B82F6'} },
                        { value: 28, name: '校园卡/生活', itemStyle: {color: '#10B981'} },
                        { value: 15, name: '图书馆', itemStyle: {color: '#F59E0B'} },
                        { value: 15, name: '报修/后勤', itemStyle: {color: '#EF4444'} }
                    ]
                }]
            };
            myChart.setOption(option);
        }

        window.addEventListener('resize', () => {
            if (myChart) myChart.resize();
        });

        async function loadRankings() {
            try {
                const response = await fetch('/api/faq/hot?limit=' + pageSize);
                const result = await response.json();
                if (result.success && result.data) {
                    renderRankings(result.data);
                }
            } catch (error) {
                console.error('加载排行榜失败:', error);
            }
        }

        function renderRankings(faqs) {
            const container = document.getElementById('ranking-list');
            const rankColors = ['text-red-500', 'text-orange-500', 'text-yellow-500'];
            const rankBgColors = ['bg-red-50', 'bg-orange-50', 'bg-yellow-50'];
            
            container.innerHTML = faqs.map((faq, index) => {
                const displayIndex = index + 1;
                const colorClass = rankColors[index] || 'text-slate-300';
                const bgClass = rankBgColors[index] || 'bg-slate-50';
                
                return `
                    <div class="bg-white p-5 rounded-2xl border border-slate-100 card-shadow flex items-center gap-6 hover:border-academic-blue/30 transition-all cursor-pointer group" onclick="handleFaqClick(${faq.faqId}, '${escapeHtml(faq.question)}')">
                        <div class="w-12 h-12 ${bgClass} rounded-xl flex items-center justify-center ${colorClass} font-black text-2xl italic">${String(displayIndex).padStart(2, '0')}</div>
                        <div class="flex-1">
                            <div class="flex items-center gap-2 mb-1">
                                <span class="text-xs text-slate-400">更新于 刚刚</span>
                            </div>
                            <h3 class="text-lg font-bold text-slate-800 group-hover:text-academic-blue transition-colors">${escapeHtml(faq.question)}</h3>
                            <p class="text-slate-500 text-sm mt-1 line-clamp-1">${faq.answer ? escapeHtml(faq.answer.substring(0, 50)) : '点击查看回答'}...</p>
                        </div>
                        <div class="text-right">
                            <div class="text-xl font-bold text-slate-900">${faq.hotScore || faq.questionCount * 10}</div>
                            <div class="text-xs text-slate-400">热度指数</div>
                        </div>
                    </div>
                `;
            }).join('');
        }

        async function handleFaqClick(faqId, question) {
            try {
                await fetch('/api/faq/click', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'faqId=' + faqId
                });
                
                const forwardResponse = await fetch('/api/faq/qa/forward', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'faqId=' + faqId + '&question=' + encodeURIComponent(question)
                });
                const result = await forwardResponse.json();
                
                if (result.success && result.data) {
                    alert('问答系统回复：' + result.data.qaResult);
                }
            } catch (error) {
                console.error('处理点击失败:', error);
            }
        }

        function switchTimeRange(range) {
            currentTimeRange = range;
            
            document.getElementById('todayBtn').className = range === 'today' ? 
                'px-6 py-2 bg-white text-academic-blue font-semibold rounded-lg shadow-sm' : 
                'px-6 py-2 text-slate-500 font-medium hover:text-academic-blue';
            document.getElementById('weekBtn').className = range === 'week' ? 
                'px-6 py-2 bg-white text-academic-blue font-semibold rounded-lg shadow-sm' : 
                'px-6 py-2 text-slate-500 font-medium hover:text-academic-blue';
            document.getElementById('monthBtn').className = range === 'month' ? 
                'px-6 py-2 bg-white text-academic-blue font-semibold rounded-lg shadow-sm' : 
                'px-6 py-2 text-slate-500 font-medium hover:text-academic-blue';
            
            loadRankings();
        }

        function prevPage() {
            if (currentPage > 1) {
                currentPage--;
                loadRankings();
            }
        }

        function nextPage() {
            currentPage++;
            loadRankings();
        }

        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        document.addEventListener('DOMContentLoaded', () => {
            initChart();
            loadRankings();
        });
    </script>
</body>
</html>
