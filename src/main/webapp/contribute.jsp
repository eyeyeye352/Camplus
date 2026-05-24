<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>

<html lang="zh-CN">
<head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>众包贡献 - Camplus 校园生活百事通</title>
<!-- Tailwind CSS -->
<script src="https://modao.cc/agent-py/static/source/js/tailwindcss.js"></script>
<!-- Iconify -->
<script src="https://modao.cc/agent-py/static/source/js/iconify-icon.min.js"></script>
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
        
        .tab-active {
            @apply border-academic-blue text-academic-blue bg-blue-50;
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
<a class="text-slate-600 hover:text-academic-blue transition-colors" href="./ranking.jsp">排行榜</a>
<a class="text-academic-blue font-semibold border-b-2 border-academic-blue pb-1" href="./contribute.jsp">众包贡献</a>
</div>
<div class="flex items-center space-x-4">
<div class="flex items-center gap-2 mr-4">
<span class="text-xs font-bold bg-academic-blue/10 text-academic-blue px-2 py-1 rounded">贡献分: 1250</span>
</div>
<img alt="Avatar" class="w-8 h-8 rounded-full border border-slate-200" src="/agent-py/static/source/images/placeholder.svg"/>
</div>
</div>
</div>
</nav>
<main class="max-w-7xl mx-auto px-4 py-12">
<div class="grid lg:grid-cols-4 gap-8">
<!-- 左侧侧边栏：分类与状态 -->
<div class="lg:col-span-1 space-y-6">
<div class="bg-white rounded-2xl p-6 border border-slate-100 shadow-sm">
<h2 class="font-bold text-lg mb-4">贡献中心</h2>
<nav class="space-y-1">
<a class="flex items-center justify-between p-3 rounded-xl bg-blue-50 text-academic-blue font-semibold" href="#">
<span class="flex items-center gap-3">
<iconify-icon icon="solar:question-square-bold-duotone"></iconify-icon> 我要提问
                            </span>
</a>
<a class="flex items-center justify-between p-3 rounded-xl text-slate-600 hover:bg-slate-50 transition-all" href="#">
<span class="flex items-center gap-3">
<iconify-icon icon="solar:pen-new-square-linear"></iconify-icon> 等我回答
                            </span>
<span class="bg-red-500 text-white text-[10px] px-1.5 py-0.5 rounded-full">12</span>
</a>
<a class="flex items-center justify-between p-3 rounded-xl text-slate-600 hover:bg-slate-50 transition-all" href="#">
<span class="flex items-center gap-3">
<iconify-icon icon="solar:document-add-linear"></iconify-icon> 知识分享
                            </span>
</a>
<a class="flex items-center justify-between p-3 rounded-xl text-slate-600 hover:bg-slate-50 transition-all" href="#">
<span class="flex items-center gap-3">
<iconify-icon icon="solar:history-linear"></iconify-icon> 我的贡献
                            </span>
</a>
</nav>
</div>
<div class="bg-gradient-to-br from-academic-blue to-blue-800 rounded-2xl p-6 text-white">
<h3 class="font-bold mb-2">贡献等级：白银学长</h3>
<div class="w-full bg-white/20 h-2 rounded-full mb-4">
<div class="bg-white h-2 rounded-full w-3/4"></div>
</div>
<p class="text-xs text-blue-100">再获得 250 积分即可升级为“黄金导师”，解锁专属勋章！</p>
</div>
</div>
<!-- 右侧主要内容区 -->
<div class="lg:col-span-3 space-y-8">
<!-- 1. 提交新问题表单 -->
<section class="bg-white rounded-2xl p-8 border border-slate-100 shadow-sm">
<h2 class="text-2xl font-bold mb-6">发布新提问</h2>
<form class="space-y-6">
<div>
<label class="block text-sm font-medium text-slate-700 mb-2">问题标题</label>
<input class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-academic-blue/20 focus:border-academic-blue outline-none transition-all" placeholder="用一句话描述你的问题，如「如何在假期预约校内实验室？」" type="text"/>
</div>
<div class="grid md:grid-cols-2 gap-6">
<div>
<label class="block text-sm font-medium text-slate-700 mb-2">所属分类</label>
<select class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-academic-blue/20 outline-none transition-all appearance-none bg-no-repeat bg-[right_1rem_center] bg-[length:1em]">
<option>请选择分类</option>
<option>教务服务</option>
<option>校园生活</option>
<option>图书馆</option>
<option>后勤报修</option>
<option>社团活动</option>
</select>
</div>
<div>
<label class="block text-sm font-medium text-slate-700 mb-2">期望回答者</label>
<select class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-academic-blue/20 outline-none transition-all">
<option>所有人</option>
<option>本专业学长学姐</option>
<option>管理员/官方老师</option>
</select>
</div>
</div>
<div>
<label class="block text-sm font-medium text-slate-700 mb-2">详细描述 (可选)</label>
<textarea class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-academic-blue/20 outline-none transition-all" placeholder="可以补充具体背景，方便他人更准确地回答..." rows="4"></textarea>
</div>
<div class="flex items-center justify-between">
<label class="flex items-center gap-2 cursor-pointer">
<input class="w-4 h-4 text-academic-blue rounded border-slate-300 focus:ring-academic-blue" type="checkbox"/>
<span class="text-sm text-slate-500">匿名提问</span>
</label>
<button class="bg-academic-blue text-white px-10 py-3 rounded-xl font-bold hover:bg-blue-800 transition-all shadow-lg" type="button">
                                立即发布
                            </button>
</div>
</form>
</section>
<!-- 2. 待回答问题广场 -->
<section>
<div class="flex items-center justify-between mb-6">
<h2 class="text-xl font-bold">待回答问题广场</h2>
<a class="text-sm text-academic-blue font-medium hover:underline" href="#">查看全部</a>
</div>
<div class="grid md:grid-cols-2 gap-4">
<!-- 问题卡片 -->
<div class="bg-white p-5 rounded-2xl border border-slate-100 hover:border-vibrant-green/30 transition-all group">
<div class="flex justify-between items-start mb-3">
<span class="text-[10px] font-bold text-vibrant-green bg-green-50 px-2 py-0.5 rounded">悬赏 50 积分</span>
<span class="text-[10px] text-slate-400">10分钟前</span>
</div>
<h3 class="font-bold text-slate-800 mb-2 group-hover:text-vibrant-green transition-colors">考研自习室的空调开放时间有规定吗？</h3>
<p class="text-xs text-slate-500 line-clamp-2 mb-4">最近在西区图书馆考研教室学习，感觉空调晚上10点就关了，想确认一下官方时间...</p>
<div class="flex items-center justify-between">
<span class="text-xs text-slate-400">3人已围观</span>
<button class="text-xs font-bold text-vibrant-green hover:underline flex items-center gap-1">
                                    我来回答 <iconify-icon icon="solar:pen-2-linear"></iconify-icon>
</button>
</div>
</div>
<div class="bg-white p-5 rounded-2xl border border-slate-100 hover:border-vibrant-green/30 transition-all group">
<div class="flex justify-between items-start mb-3">
<span class="text-[10px] font-bold text-academic-blue bg-blue-50 px-2 py-0.5 rounded">急需解答</span>
<span class="text-[10px] text-slate-400">2小时前</span>
</div>
<h3 class="font-bold text-slate-800 mb-2 group-hover:text-academic-blue transition-colors">如何申请校内勤工助学的岗位？</h3>
<p class="text-xs text-slate-500 line-clamp-2 mb-4">大一新生，想在课余时间找一份校内兼职，请问是在哪个公众号或者网站申请？</p>
<div class="flex items-center justify-between">
<span class="text-xs text-slate-400">15人已围观</span>
<button class="text-xs font-bold text-vibrant-green hover:underline flex items-center gap-1">
                                    我来回答 <iconify-icon icon="solar:pen-2-linear"></iconify-icon>
</button>
</div>
</div>
</div>
</section>
<!-- 3. 知识分享动态 -->
<section>
<h2 class="text-xl font-bold mb-6">热门知识分享</h2>
<div class="space-y-4">
<div class="bg-white p-6 rounded-2xl border border-slate-100 flex gap-6 items-start">
<img alt="Cover" class="w-24 h-24 rounded-xl object-cover" src="/agent-py/static/source/images/placeholder.svg"/>
<div class="flex-1">
<div class="flex items-center gap-2 mb-1">
<span class="text-xs text-slate-400">由 **学姐 分享</span>
<span class="text-slate-200">|</span>
<span class="text-xs text-slate-400">获赞 1.2k</span>
</div>
<h3 class="text-lg font-bold text-slate-800 mb-2">避坑指南：校园内最全的打印店价格与速度测评</h3>
<p class="text-sm text-slate-500 line-clamp-2">期末考周到了，大家都在忙着打印复习资料。我跑遍了南北校区共8家打印店，整理了这篇价格对比表...</p>
</div>
<button class="text-slate-300 hover:text-academic-blue">
<iconify-icon class="text-2xl" icon="solar:bookmark-linear"></iconify-icon>
</button>
</div>
</div>
</section>
</div>
</div>
</main>
<!-- 页脚 -->
<footer class="bg-white border-t border-slate-100 py-12">
<div class="max-w-7xl mx-auto px-4 text-center">
<p class="text-slate-400 text-sm">© 2026 Camplus 校园生活百事通. 为智慧校园而生.</p>
</div>
</footer>
</body>
</html>
