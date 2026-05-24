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
<a class="flex items-center space-x-2" href="home.html">
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
<button class="px-6 py-2 bg-white text-academic-blue font-semibold rounded-lg shadow-sm">本日</button>
<button class="px-6 py-2 text-slate-500 font-medium hover:text-academic-blue">本周</button>
<button class="px-6 py-2 text-slate-500 font-medium hover:text-academic-blue">本月</button>
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
<div class="mt-8 space-y-4">
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
<div class="lg:col-span-2 space-y-4">
<!-- 榜单项 -->
<div class="bg-white p-5 rounded-2xl border border-slate-100 card-shadow flex items-center gap-6 hover:border-academic-blue/30 transition-all cursor-pointer group">
<div class="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center text-red-500 font-black text-2xl italic">01</div>
<div class="flex-1">
<div class="flex items-center gap-2 mb-1">
<span class="text-xs font-bold text-red-500 bg-red-50 px-2 py-0.5 rounded uppercase">教务</span>
<span class="text-xs text-slate-400">更新于 1小时前</span>
</div>
<h3 class="text-lg font-bold text-slate-800 group-hover:text-academic-blue transition-colors">如何在线申请缓考或补考？</h3>
<p class="text-slate-500 text-sm mt-1 line-clamp-1">登录教务系统 -&gt; 办事大厅 -&gt; 考试相关 -&gt; 提交申请并上传证明材料...</p>
</div>
<div class="text-right">
<div class="text-xl font-bold text-slate-900">14,203</div>
<div class="text-xs text-slate-400">热度指数</div>
</div>
</div>
<div class="bg-white p-5 rounded-2xl border border-slate-100 card-shadow flex items-center gap-6 hover:border-academic-blue/30 transition-all cursor-pointer group">
<div class="w-12 h-12 bg-orange-50 rounded-xl flex items-center justify-center text-orange-500 font-black text-2xl italic">02</div>
<div class="flex-1">
<div class="flex items-center gap-2 mb-1">
<span class="text-xs font-bold text-orange-500 bg-orange-50 px-2 py-0.5 rounded uppercase">校园卡</span>
<span class="text-xs text-slate-400">更新于 3小时前</span>
</div>
<h3 class="text-lg font-bold text-slate-800 group-hover:text-academic-blue transition-colors">校园卡反向写卡失败怎么办？</h3>
<p class="text-slate-500 text-sm mt-1 line-clamp-1">请携带校园卡前往各食堂入口处的自助圈存机重新写卡，或前往卡务中心...</p>
</div>
<div class="text-right">
<div class="text-xl font-bold text-slate-900">12,840</div>
<div class="text-xs text-slate-400">热度指数</div>
</div>
</div>
<div class="bg-white p-5 rounded-2xl border border-slate-100 card-shadow flex items-center gap-6 hover:border-academic-blue/30 transition-all cursor-pointer group">
<div class="w-12 h-12 bg-yellow-50 rounded-xl flex items-center justify-center text-yellow-500 font-black text-2xl italic">03</div>
<div class="flex-1">
<div class="flex items-center gap-2 mb-1">
<span class="text-xs font-bold text-vibrant-green bg-green-50 px-2 py-0.5 rounded uppercase">图书馆</span>
<span class="text-xs text-slate-400">更新于 5小时前</span>
</div>
<h3 class="text-lg font-bold text-slate-800 group-hover:text-academic-blue transition-colors">图书馆研讨室预约规则？</h3>
<p class="text-slate-500 text-sm mt-1 line-clamp-1">需至少3人组队，提前24小时在微信公众号预约，使用时长不超过4小时...</p>
</div>
<div class="text-right">
<div class="text-xl font-bold text-slate-900">10,560</div>
<div class="text-xs text-slate-400">热度指数</div>
</div>
</div>
<!-- 更多排名项 -->
<div class="bg-white p-5 rounded-2xl border border-slate-100 card-shadow flex items-center gap-6 hover:border-academic-blue/30 transition-all cursor-pointer group">
<div class="w-12 h-12 bg-slate-50 rounded-xl flex items-center justify-center text-slate-400 font-black text-2xl italic">04</div>
<div class="flex-1">
<div class="flex items-center gap-2 mb-1">
<span class="text-xs font-bold text-red-500 bg-red-50 px-2 py-0.5 rounded uppercase">后勤</span>
<span class="text-xs text-slate-400">更新于 昨日</span>
</div>
<h3 class="text-lg font-bold text-slate-800 group-hover:text-academic-blue transition-colors">宿舍空调不制冷如何报修？</h3>
<p class="text-slate-500 text-sm mt-1 line-clamp-1">通过“智慧校园”APP -&gt; 宿舍报修 -&gt; 选择电器类 -&gt; 描述故障并上传照片...</p>
</div>
<div class="text-right">
<div class="text-xl font-bold text-slate-900">9,320</div>
<div class="text-xs text-slate-400">热度指数</div>
</div>
</div>
<div class="bg-white p-5 rounded-2xl border border-slate-100 card-shadow flex items-center gap-6 hover:border-academic-blue/30 transition-all cursor-pointer group">
<div class="w-12 h-12 bg-slate-50 rounded-xl flex items-center justify-center text-slate-400 font-black text-2xl italic">05</div>
<div class="flex-1">
<div class="flex items-center gap-2 mb-1">
<span class="text-xs font-bold text-indigo-500 bg-indigo-50 px-2 py-0.5 rounded uppercase">交通</span>
<span class="text-xs text-slate-400">更新于 昨日</span>
</div>
<h3 class="text-lg font-bold text-slate-800 group-hover:text-academic-blue transition-colors">校车在周末的运行时间表？</h3>
<p class="text-slate-500 text-sm mt-1 line-clamp-1">周末校车仅运行1号线与3号线，首班车08:30，末班车21:30，间隔30分钟...</p>
</div>
<div class="text-right">
<div class="text-xl font-bold text-slate-900">7,840</div>
<div class="text-xs text-slate-400">热度指数</div>
</div>
</div>
<!-- 翻页 -->
<div class="flex justify-center pt-8">
<nav class="flex items-center gap-2">
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50">
<iconify-icon icon="solar:alt-arrow-left-linear"></iconify-icon>
</button>
<button class="w-10 h-10 rounded-lg bg-academic-blue text-white flex items-center justify-center font-bold">1</button>
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50">2</button>
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50">3</button>
<span class="px-2 text-slate-400">...</span>
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50">12</button>
<button class="w-10 h-10 rounded-lg border border-slate-200 flex items-center justify-center hover:bg-slate-50">
<iconify-icon icon="solar:alt-arrow-right-linear"></iconify-icon>
</button>
</nav>
</div>
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
        // 初始化 ECharts 图表
        const chartDom = document.getElementById('chart-pie');
        const myChart = echarts.init(chartDom);
        const option = {
            tooltip: {
                trigger: 'item'
            },
            series: [
                {
                    name: '热度占比',
                    type: 'pie',
                    radius: ['40%', '70%'],
                    avoidLabelOverlap: false,
                    itemStyle: {
                        borderRadius: 10,
                        borderColor: '#fff',
                        borderWidth: 2
                    },
                    label: {
                        show: false
                    },
                    emphasis: {
                        label: {
                            show: false
                        }
                    },
                    labelLine: {
                        show: false
                    },
                    data: [
                        { value: 42, name: '教务服务', itemStyle: {color: '#3B82F6'} },
                        { value: 28, name: '校园卡/生活', itemStyle: {color: '#10B981'} },
                        { value: 15, name: '图书馆', itemStyle: {color: '#F59E0B'} },
                        { value: 15, name: '报修/后勤', itemStyle: {color: '#EF4444'} }
                    ]
                }
            ]
        };
        myChart.setOption(option);

        window.addEventListener('resize', () => {
            myChart.resize();
        });
    </script>
</body>
</html>
