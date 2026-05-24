<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>

<html lang="zh-CN">
<head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>Camplus - 校园生活百事通</title>
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
        
        .search-shadow {
            box-shadow: 0 10px 25px -5px rgba(43, 92, 158, 0.1), 0 8px 10px -6px rgba(43, 92, 158, 0.1);
        }
        .card-hover:hover {
            transform: translateY(-4px);
            box-shadow: 0 12px 20px -5px rgba(0, 0, 0, 0.1);
        }
    </style>
</head>
<body class="text-slate-800">
<!-- 1. 顶部导航栏 -->
<nav class="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-slate-100">
<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
<div class="flex justify-between h-16 items-center">
<div class="flex items-center space-x-2">
<div class="w-10 h-10 bg-academic-blue rounded-lg flex items-center justify-center">
<iconify-icon class="text-white text-2xl" icon="solar:square-academic-cap-bold-duotone"></iconify-icon>
</div>
<span class="text-2xl font-bold academic-blue tracking-tight">Camplus</span>
</div>
<div class="hidden md:flex items-center space-x-8">
<a class="text-academic-blue font-semibold border-b-2 border-academic-blue pb-1" href="./index.jsp">首页</a>
<a class="text-slate-600 hover:text-academic-blue transition-colors" href="#">知识库</a>
<a class="text-slate-600 hover:text-academic-blue transition-colors" href="./ranking.jsp">排行榜</a>
<a class="text-slate-600 hover:text-academic-blue transition-colors" href="./contribute.jsp">众包贡献</a>
</div>
<div class="flex items-center space-x-4">
<button class="text-slate-600 hover:text-academic-blue font-medium px-4 py-2">登录</button>
<button class="bg-academic-blue text-white px-5 py-2 rounded-full font-medium hover:bg-blue-800 transition-all shadow-md">注册</button>
</div>
</div>
</div>
</nav>
<!-- 2. 搜索主区域 -->
<section class="relative bg-gradient-to-b from-blue-50 via-white to-white py-16 md:py-24 overflow-hidden">
<!-- 装饰背景 -->
<div class="absolute top-0 left-0 w-full h-full opacity-10 pointer-events-none">
<div class="absolute top-10 left-10 w-64 h-64 bg-academic-blue rounded-full blur-3xl"></div>
<div class="absolute bottom-10 right-10 w-96 h-96 bg-vibrant-green rounded-full blur-3xl"></div>
</div>
<div class="max-w-4xl mx-auto px-4 text-center relative z-10">
<h1 class="text-3xl md:text-5xl font-bold text-slate-900 mb-4">Hi, 今天想了解校园的什么？</h1>
<p class="text-slate-500 text-lg mb-10">Camplus 智能问答系统，集成全校官方文档与校友智慧，为你快速排忧解难。</p>
<!-- 搜索框 -->
<div class="relative max-w-3xl mx-auto group">
<div class="absolute inset-y-0 left-0 pl-5 flex items-center pointer-events-none">
<iconify-icon class="text-slate-400 text-2xl group-focus-within:text-academic-blue transition-colors" icon="solar:magnifer-linear"></iconify-icon>
</div>
<input class="block w-full pl-14 pr-32 py-5 bg-white border-0 rounded-2xl search-shadow text-lg focus:ring-2 focus:ring-academic-blue/20 outline-none transition-all placeholder:text-slate-400" placeholder="输入你的问题，如「食堂几点关门？」" type="text"/>
<div class="absolute inset-y-2 right-2 flex">
<button class="bg-academic-blue text-white px-8 rounded-xl font-semibold hover:bg-blue-800 transition-all shadow-lg">
                        搜索
                    </button>
</div>
</div>
<!-- 热门搜索标签 -->
<div class="mt-6 flex flex-wrap justify-center gap-3">
<span class="text-slate-400 text-sm py-1">热门搜索：</span>
<a class="bg-white px-3 py-1 rounded-full text-sm text-slate-600 border border-slate-100 hover:border-academic-blue hover:text-academic-blue transition-all" href="#">课表查询</a>
<a class="bg-white px-3 py-1 rounded-full text-sm text-slate-600 border border-slate-100 hover:border-academic-blue hover:text-academic-blue transition-all" href="#">校园卡充值</a>
<a class="bg-white px-3 py-1 rounded-full text-sm text-slate-600 border border-slate-100 hover:border-academic-blue hover:text-academic-blue transition-all" href="#">图书馆开放时间</a>
<a class="bg-white px-3 py-1 rounded-full text-sm text-slate-600 border border-slate-100 hover:border-academic-blue hover:text-academic-blue transition-all" href="#">四六级报名</a>
<a class="bg-white px-3 py-1 rounded-full text-sm text-slate-600 border border-slate-100 hover:border-academic-blue hover:text-academic-blue transition-all" href="#">宿舍报修</a>
</div>
</div>
</section>
<!-- 3. 服务分类导航 (金刚区) -->
<section class="max-w-7xl mx-auto px-4 -mt-10 relative z-20">
<div class="bg-white rounded-2xl shadow-xl p-8 grid grid-cols-4 md:grid-cols-8 gap-6">
<a class="flex flex-col items-center group" href="#">
<div class="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center group-hover:bg-academic-blue transition-all duration-300">
<iconify-icon class="text-academic-blue text-2xl group-hover:text-white" icon="solar:notebook-linear"></iconify-icon>
</div>
<span class="mt-3 text-sm font-medium text-slate-600 group-hover:text-academic-blue">教务服务</span>
</a>
<a class="flex flex-col items-center group" href="#">
<div class="w-14 h-14 bg-green-50 rounded-2xl flex items-center justify-center group-hover:bg-vibrant-green transition-all duration-300">
<iconify-icon class="text-vibrant-green text-2xl group-hover:text-white" icon="solar:library-linear"></iconify-icon>
</div>
<span class="mt-3 text-sm font-medium text-slate-600 group-hover:text-vibrant-green">图书馆</span>
</a>
<a class="flex flex-col items-center group" href="#">
<div class="w-14 h-14 bg-orange-50 rounded-2xl flex items-center justify-center group-hover:bg-warm-orange transition-all duration-300">
<iconify-icon class="text-warm-orange text-2xl group-hover:text-white" icon="solar:card-linear"></iconify-icon>
</div>
<span class="mt-3 text-sm font-medium text-slate-600 group-hover:text-warm-orange">校园卡</span>
</a>
<a class="flex flex-col items-center group" href="#">
<div class="w-14 h-14 bg-red-50 rounded-2xl flex items-center justify-center group-hover:bg-red-500 transition-all duration-300">
<iconify-icon class="text-red-500 text-2xl group-hover:text-white" icon="solar:settings-linear"></iconify-icon>
</div>
<span class="mt-3 text-sm font-medium text-slate-600 group-hover:text-red-500">报修服务</span>
</a>
<a class="flex flex-col items-center group" href="#">
<div class="w-14 h-14 bg-indigo-50 rounded-2xl flex items-center justify-center group-hover:bg-indigo-500 transition-all duration-300">
<iconify-icon class="text-indigo-500 text-2xl group-hover:text-white" icon="solar:bus-linear"></iconify-icon>
</div>
<span class="mt-3 text-sm font-medium text-slate-600 group-hover:text-indigo-500">校车查询</span>
</a>
<a class="flex flex-col items-center group" href="#">
<div class="w-14 h-14 bg-purple-50 rounded-2xl flex items-center justify-center group-hover:bg-purple-500 transition-all duration-300">
<iconify-icon class="text-purple-500 text-2xl group-hover:text-white" icon="solar:users-group-two-rounded-linear"></iconify-icon>
</div>
<span class="mt-3 text-sm font-medium text-slate-600 group-hover:text-purple-500">社团活动</span>
</a>
<a class="flex flex-col items-center group" href="#">
<div class="w-14 h-14 bg-teal-50 rounded-2xl flex items-center justify-center group-hover:bg-teal-500 transition-all duration-300">
<iconify-icon class="text-teal-500 text-2xl group-hover:text-white" icon="solar:map-point-linear"></iconify-icon>
</div>
<span class="mt-3 text-sm font-medium text-slate-600 group-hover:text-teal-500">校园地图</span>
</a>
<a class="flex flex-col items-center group" href="#">
<div class="w-14 h-14 bg-slate-100 rounded-2xl flex items-center justify-center group-hover:bg-slate-800 transition-all duration-300">
<iconify-icon class="text-slate-500 text-2xl group-hover:text-white" icon="solar:menu-dots-linear"></iconify-icon>
</div>
<span class="mt-3 text-sm font-medium text-slate-600 group-hover:text-slate-800">更多</span>
</a>
</div>
</section>
<!-- 4. 热门问题排行榜 -->
<section class="max-w-7xl mx-auto px-4 py-16">
<div class="flex justify-between items-end mb-8">
<div>
<h2 class="text-2xl font-bold flex items-center gap-2">
<iconify-icon class="text-red-500" icon="solar:fire-bold-duotone"></iconify-icon>
                    热门问题排行榜
                </h2>
<p class="text-slate-500 mt-1">基于全校同学的高频查询实时生成</p>
</div>
<a class="text-academic-blue font-medium flex items-center gap-1 hover:underline" href="#">
                查看完整榜单 <iconify-icon icon="solar:alt-arrow-right-linear"></iconify-icon>
</a>
</div>
<div class="grid md:grid-cols-2 gap-6">
<!-- 左侧榜单 -->
<div class="space-y-3">
<div class="flex items-center p-4 bg-white rounded-xl border border-slate-100 card-hover transition-all cursor-pointer">
<span class="w-8 text-xl font-bold text-red-500 italic">01</span>
<div class="flex-1 ml-2">
<h3 class="font-semibold text-slate-800">如何查询本学期课表？</h3>
<div class="flex items-center gap-4 mt-1">
<span class="text-xs text-slate-400 flex items-center gap-1">
<iconify-icon icon="solar:eye-linear"></iconify-icon> 12,840 次查询
                            </span>
<span class="text-xs text-vibrant-green bg-green-50 px-2 py-0.5 rounded">教务服务</span>
</div>
</div>
<iconify-icon class="text-slate-300" icon="solar:alt-arrow-right-linear"></iconify-icon>
</div>
<div class="flex items-center p-4 bg-white rounded-xl border border-slate-100 card-hover transition-all cursor-pointer">
<span class="w-8 text-xl font-bold text-orange-500 italic">02</span>
<div class="flex-1 ml-2">
<h3 class="font-semibold text-slate-800">校园卡丢失如何补办？</h3>
<div class="flex items-center gap-4 mt-1">
<span class="text-xs text-slate-400 flex items-center gap-1">
<iconify-icon icon="solar:eye-linear"></iconify-icon> 9,320 次查询
                            </span>
<span class="text-xs text-orange-500 bg-orange-50 px-2 py-0.5 rounded">校园卡</span>
</div>
</div>
<iconify-icon class="text-slate-300" icon="solar:alt-arrow-right-linear"></iconify-icon>
</div>
<div class="flex items-center p-4 bg-white rounded-xl border border-slate-100 card-hover transition-all cursor-pointer">
<span class="w-8 text-xl font-bold text-yellow-500 italic">03</span>
<div class="flex-1 ml-2">
<h3 class="font-semibold text-slate-800">图书馆周末开放时间？</h3>
<div class="flex items-center gap-4 mt-1">
<span class="text-xs text-slate-400 flex items-center gap-1">
<iconify-icon icon="solar:eye-linear"></iconify-icon> 8,105 次查询
                            </span>
<span class="text-xs text-vibrant-green bg-green-50 px-2 py-0.5 rounded">图书馆</span>
</div>
</div>
<iconify-icon class="text-slate-300" icon="solar:alt-arrow-right-linear"></iconify-icon>
</div>
<div class="flex items-center p-4 bg-white rounded-xl border border-slate-100 card-hover transition-all cursor-pointer">
<span class="w-8 text-xl font-bold text-slate-300 italic">04</span>
<div class="flex-1 ml-2">
<h3 class="font-semibold text-slate-800">宿舍报修流程是什么？</h3>
<div class="flex items-center gap-4 mt-1">
<span class="text-xs text-slate-400 flex items-center gap-1">
<iconify-icon icon="solar:eye-linear"></iconify-icon> 6,420 次查询
                            </span>
<span class="text-xs text-red-500 bg-red-50 px-2 py-0.5 rounded">报修服务</span>
</div>
</div>
<iconify-icon class="text-slate-300" icon="solar:alt-arrow-right-linear"></iconify-icon>
</div>
</div>
<!-- 右侧榜单 -->
<div class="space-y-3">
<div class="flex items-center p-4 bg-white rounded-xl border border-slate-100 card-hover transition-all cursor-pointer">
<span class="w-8 text-xl font-bold text-slate-300 italic">05</span>
<div class="flex-1 ml-2">
<h3 class="font-semibold text-slate-800">四六级考试报名时间？</h3>
<div class="flex items-center gap-4 mt-1">
<span class="text-xs text-slate-400 flex items-center gap-1">
<iconify-icon icon="solar:eye-linear"></iconify-icon> 5,880 次查询
                            </span>
<span class="text-xs text-vibrant-green bg-green-50 px-2 py-0.5 rounded">教务服务</span>
</div>
</div>
<iconify-icon class="text-slate-300" icon="solar:alt-arrow-right-linear"></iconify-icon>
</div>
<div class="flex items-center p-4 bg-white rounded-xl border border-slate-100 card-hover transition-all cursor-pointer">
<span class="w-8 text-xl font-bold text-slate-300 italic">06</span>
<div class="flex-1 ml-2">
<h3 class="font-semibold text-slate-800">校车时刻表在哪里查？</h3>
<div class="flex items-center gap-4 mt-1">
<span class="text-xs text-slate-400 flex items-center gap-1">
<iconify-icon icon="solar:eye-linear"></iconify-icon> 4,920 次查询
                            </span>
<span class="text-xs text-indigo-500 bg-indigo-50 px-2 py-0.5 rounded">校车查询</span>
</div>
</div>
<iconify-icon class="text-slate-300" icon="solar:alt-arrow-right-linear"></iconify-icon>
</div>
<div class="flex items-center p-4 bg-white rounded-xl border border-slate-100 card-hover transition-all cursor-pointer">
<span class="w-8 text-xl font-bold text-slate-300 italic">07</span>
<div class="flex-1 ml-2">
<h3 class="font-semibold text-slate-800">奖学金评定标准？</h3>
<div class="flex items-center gap-4 mt-1">
<span class="text-xs text-slate-400 flex items-center gap-1">
<iconify-icon icon="solar:eye-linear"></iconify-icon> 4,560 次查询
                            </span>
<span class="text-xs text-vibrant-green bg-green-50 px-2 py-0.5 rounded">教务服务</span>
</div>
</div>
<iconify-icon class="text-slate-300" icon="solar:alt-arrow-right-linear"></iconify-icon>
</div>
<div class="flex items-center p-4 bg-white rounded-xl border border-slate-100 card-hover transition-all cursor-pointer">
<span class="w-8 text-xl font-bold text-slate-300 italic">08</span>
<div class="flex-1 ml-2">
<h3 class="font-semibold text-slate-800">校园网如何登录与续费？</h3>
<div class="flex items-center gap-4 mt-1">
<span class="text-xs text-slate-400 flex items-center gap-1">
<iconify-icon icon="solar:eye-linear"></iconify-icon> 3,890 次查询
                            </span>
<span class="text-xs text-slate-500 bg-slate-100 px-2 py-0.5 rounded">生活服务</span>
</div>
</div>
<iconify-icon class="text-slate-300" icon="solar:alt-arrow-right-linear"></iconify-icon>
</div>
</div>
</div>
</section>
<!-- 5. 用户众包贡献模块 -->
<section class="bg-slate-50 py-20">
<div class="max-w-7xl mx-auto px-4">
<div class="text-center mb-12">
<h2 class="text-3xl font-bold text-slate-900">💬 校园问答共建</h2>
<p class="text-slate-500 mt-3">知识源于分享，Camplus 的成长离不开每一位同学的贡献</p>
</div>
<div class="grid md:grid-cols-3 gap-8">
<!-- 我也想问 -->
<div class="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm card-hover transition-all">
<div class="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mb-6">
<iconify-icon class="text-academic-blue text-3xl" icon="solar:chat-line-bold-duotone"></iconify-icon>
</div>
<h3 class="text-xl font-bold mb-3">我也想问</h3>
<p class="text-slate-500 mb-6">遇到了没搜到的问题？发布悬赏，让热心的学长学姐来帮你解答，支持匿名提问。</p>
<button class="w-full py-3 bg-academic-blue text-white rounded-xl font-semibold hover:bg-blue-800 transition-all shadow-md">
                        发起提问
                    </button>
</div>
<!-- 我来回答 -->
<div class="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm card-hover transition-all">
<div class="w-16 h-16 bg-green-50 rounded-2xl flex items-center justify-center mb-6">
<iconify-icon class="text-vibrant-green text-3xl" icon="solar:pen-bold-duotone"></iconify-icon>
</div>
<h3 class="text-xl font-bold mb-3">我来回答</h3>
<p class="text-slate-500 mb-6">你是校园活地图？快去解答学弟学妹的疑惑。帮助他人还能赚取积分兑换校园文创。</p>
<button class="w-full py-3 bg-vibrant-green text-white rounded-xl font-semibold hover:bg-green-600 transition-all shadow-md">
                        浏览待办
                    </button>
</div>
<!-- 知识分享 -->
<div class="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm card-hover transition-all">
<div class="w-16 h-16 bg-orange-50 rounded-2xl flex items-center justify-center mb-6">
<iconify-icon class="text-warm-orange text-3xl" icon="solar:lightbulb-bold-duotone"></iconify-icon>
</div>
<h3 class="text-xl font-bold mb-3">知识分享</h3>
<p class="text-slate-500 mb-6">发现宝藏自习室、食堂隐藏菜单或高效选课攻略？写成小知识分享给全校同学。</p>
<button class="w-full py-3 bg-warm-orange text-white rounded-xl font-semibold hover:bg-orange-600 transition-all shadow-md">
                        立即分享
                    </button>
</div>
</div>
<!-- 最近动态展示 -->
<div class="mt-16 bg-white rounded-2xl p-6 border border-slate-100 shadow-sm">
<h4 class="text-sm font-bold text-slate-400 uppercase tracking-wider mb-4">社区实时动态</h4>
<div class="space-y-4">
<div class="flex items-center justify-between text-sm py-2 border-b border-slate-50">
<div class="flex items-center gap-3">
<img alt="User Avatar" class="w-8 h-8 rounded-full" src="/agent-py/static/source/images/placeholder.svg"/>
<span class="text-slate-700">**同学 回答了 <span class="font-medium">「西区体育馆几点闭馆？」</span></span>
</div>
<span class="text-slate-400">2分钟前</span>
</div>
<div class="flex items-center justify-between text-sm py-2 border-b border-slate-50">
<div class="flex items-center gap-3">
<div class="w-8 h-8 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 font-bold text-xs">J</div>
<span class="text-slate-700">**君 提问了 <span class="font-medium">「考研自习室如何预约？」</span></span>
</div>
<span class="text-slate-400">15分钟前</span>
</div>
<div class="flex items-center justify-between text-sm py-2">
<div class="flex items-center gap-3">
<img alt="User Avatar" class="w-8 h-8 rounded-full" src="/agent-py/static/source/images/placeholder.svg"/>
<span class="text-slate-700">**学姐 分享了 <span class="font-medium">「三食堂二楼新品测评」</span></span>
</div>
<span class="text-slate-400">1小时前</span>
</div>
</div>
</div>
</div>
</section>
<!-- 6. 页脚 -->
<footer class="bg-white border-t border-slate-100 py-12">
<div class="max-w-7xl mx-auto px-4">
<div class="flex flex-col md:flex-row justify-between items-center gap-6">
<div class="flex items-center space-x-2">
<div class="w-8 h-8 bg-academic-blue rounded flex items-center justify-center">
<iconify-icon class="text-white text-lg" icon="solar:square-academic-cap-bold-duotone"></iconify-icon>
</div>
<span class="text-xl font-bold academic-blue">Camplus</span>
</div>
<div class="flex flex-wrap justify-center gap-8 text-sm text-slate-500">
<a class="hover:text-academic-blue transition-colors" href="#">关于我们</a>
<a class="hover:text-academic-blue transition-colors" href="#">使用指南</a>
<a class="hover:text-academic-blue transition-colors" href="#">知识库协议</a>
<a class="hover:text-academic-blue transition-colors" href="#">隐私政策</a>
<a class="hover:text-academic-blue transition-colors" href="#">联系管理员</a>
</div>
<div class="flex items-center space-x-4">
<a class="w-10 h-10 bg-slate-50 rounded-full flex items-center justify-center text-slate-400 hover:text-academic-blue hover:bg-blue-50 transition-all" href="#">
<iconify-icon class="text-xl" icon="solar:share-circle-linear"></iconify-icon>
</a>
<a class="w-10 h-10 bg-slate-50 rounded-full flex items-center justify-center text-slate-400 hover:text-academic-blue hover:bg-blue-50 transition-all" href="#">
<iconify-icon class="text-xl" icon="solar:bell-linear"></iconify-icon>
</a>
</div>
</div>
<div class="mt-8 pt-8 border-t border-slate-50 text-center">
<p class="text-slate-400 text-sm">© 2026 Camplus 校园生活百事通. 为智慧校园而生.</p>
</div>
</div>
</footer>
<script>
        // 搜索框交互增强
        const searchInput = document.querySelector('input[type="text"]');
        searchInput.addEventListener('focus', () => {
            searchInput.parentElement.classList.add('scale-[1.02]');
        });
        searchInput.addEventListener('blur', () => {
            searchInput.parentElement.classList.remove('scale-[1.02]');
        });

        // 模拟搜索推荐逻辑
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                alert('正在为您识别意图并检索知识库...');
            }
        });
    </script>
</body>
</html>
