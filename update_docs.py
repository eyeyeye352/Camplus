# -*- coding: utf-8 -*-
import zipfile
import shutil
import os
import re


def read_docx_text(path):
    with zipfile.ZipFile(path) as z:
        return z.read('word/document.xml').decode('utf-8')


def write_docx(path, new_document_xml):
    import tempfile
    tmpfd, tmppath = tempfile.mkstemp(suffix='.docx')
    os.close(tmpfd)
    zin = zipfile.ZipFile(path, 'r')
    zout = zipfile.ZipFile(tmppath, 'w')
    for item in zin.infolist():
        if item.filename == 'word/document.xml':
            zout.writestr(item, new_document_xml)
        else:
            zout.writestr(item, zin.read(item.filename))
    zin.close()
    zout.close()
    shutil.move(tmppath, path)


def modify_usecase_spec():
    """Login用例规约.docx - use case specification"""
    src = r'c:\Users\Lenovo\IdeaProjects\Camplus\Login用例规约.docx'
    dst = r'c:\Users\Lenovo\IdeaProjects\Camplus\Login用例规约.docx'
    doc = read_docx_text(src)

    # 1. 2.1.9 系统返回登录成功结果与用户基本信息 → 增加会话写入 & 完整用户信息
    doc = doc.replace(
        '2.1.9 系统返回登录成功结果与用户基本信息。',
        '2.1.9 后端通过 HttpServletRequest.getSession().setAttribute("user", user) 将完整用户对象写入服务端会话。'
    )
    doc = doc.replace(
        '2.1.9 后端通过 HttpServletRequest.getSession().setAttribute("user", user) 将完整用户对象写入服务端会话。',
        '2.1.9 后端通过 HttpServletRequest.getSession().setAttribute("user", user) 将完整用户对象写入服务端会话；'
        '系统返回登录成功结果，包含 userId、username、email、phone、nickname、avatarUrl、role、status 全部用户字段。'
    )

    # 2. 登录页 "保持用户登录会话" → 强化说明
    doc = doc.replace(
        '页面提示登录成功，自动跳转至平台首页，保持用户登录会话。',
        '后端创建会话并保存完整用户对象，前端接收返回的用户信息后将各字段写入 sessionStorage；'
        '页面提示登录成功，自动跳转至平台首页，首页通过读取 sessionStorage 识别已登录状态。'
    )

    # 3. 2.2.4 账号锁定 → "永久拒绝" → "30分钟后自动解除锁定"
    doc = doc.replace(
        '后续再次登录时，系统检测到锁定状态，直接拒绝登录并给出异常提示。',
        '后续再次登录时，系统检测到锁定状态，若距离锁定时间未超过 30 分钟则拒绝登录并给出异常提示；'
        '若已超过 30 分钟，则自动解除锁定状态（清除 lockTime 并将 loginErrorCount 重置为 0），允许重新尝试登录。'
    )

    # 4. 后置条件 → "账号锁定：系统写入锁定时间，在未解锁前永久拒绝该账号登录请求"
    doc = doc.replace(
        '账号锁定：系统写入锁定时间，在未解锁前永久拒绝该账号登录请求。',
        '账号锁定：系统写入锁定时间，自锁定起 30 分钟内拒绝该账号登录请求；'
        '超过 30 分钟后首次登录时自动清除锁定标记、重置错误计数，账号恢复可用。'
    )

    # 5. 扩展点 6.1 → "可扩展实现账号超时自动解锁功能" 已经实现，改为"账号超时自动解锁已实现（30分钟）"
    doc = doc.replace(
        '可扩展实现账号超时自动解锁功能，无需管理员手动解锁。',
        '账号超时自动解锁功能已实现（连续密码错误达到上限后锁定账号，30 分钟后自动解除锁定），无需管理员手动解锁。'
    )

    # 6. 活动图中的锁定分支
    doc = doc.replace(
        '校验失败 → 判断错误类型 → 提示对应文案 / 累加错误次数 / 锁定账号 → 停留登录页',
        '校验失败 → 判断错误类型 → 提示对应文案 / 累加错误次数 / 锁定账号（锁定 30 分钟后自动解除） → 停留登录页'
    )

    # 7. 修订历史 - 增加新版本记录
    new_revision = (
        '<w:p><w:pPr><w:rPr></w:rPr></w:pPr><w:r><w:t>14/06/2026</w:t></w:r><w:r><w:tab/></w:r>'
        '<w:r><w:t>1.1</w:t></w:r><w:r><w:tab/></w:r>'
        '<w:r><w:t>更新登录成功会话写入说明、账号锁定30分钟自动解除、用户信息字段扩展</w:t></w:r><w:r><w:tab/></w:r>'
        '<w:r><w:t>游竣予</w:t></w:r></w:p>'
    )
    # 在"初次完成用户登录用例规约编写"段落之后插入新版本
    if '初次完成用户登录用例规约编写' in doc:
        doc = doc.replace(
            '初次完成用户登录用例规约编写',
            '初次完成用户登录用例规约编写</w:t></w:r></w:p>' + new_revision + '<w:p><w:r><w:t>'
        )

    write_docx(dst, doc)
    print('[OK] Login用例规约.docx updated')


def modify_implementation_spec():
    """Login实现规约.docx - implementation specification"""
    src = r'c:\Users\Lenovo\IdeaProjects\Camplus\Login实现规约.docx'
    dst = r'c:\Users\Lenovo\IdeaProjects\Camplus\Login实现规约.docx'
    doc = read_docx_text(src)

    # 1. User 实体类字段扩展 + userId Long 类型
    doc = doc.replace(
        '封装用户属性：userId、username、passwordHash、email、phone、role、status、loginErrorCount、lockTime、lastLoginTime 等。',
        '封装用户属性：userId（Long 类型，映射数据库 BIGINT）、username、passwordHash、email、phone、nickname、'
        'avatarUrl、role、status、loginErrorCount、lockTime、lastLoginTime 等字段。'
    )

    # 2. MyBatisUtil 位置变更
    doc = doc.replace(
        'MyBatisUtil（MyBatis 会话工具）',
        'MyBatisUtil（位于 com.camplus.util 包，提供 MyBatis 会话工具）'
    )

    # 3. UserMapper 方法参数类型更新
    doc = doc.replace(
        '定义方法：selectByUsername、selectByEmail、selectByPhone、updateLoginErrorCount、updateLockTime、updateLoginSuccessInfo。',
        '定义方法：selectByUsername、selectByEmail、selectByPhone、'
        'updateLoginErrorCount(userId, count)、updateLockTime(userId, lockTime)、updateLoginSuccessInfo(userId, lastLoginTime)；'
        '所有涉及 userId 的方法参数类型均为 Long，与数据库 users 表 BIGINT 字段对齐。'
    )

    # 4. UserServiceImpl 业务逻辑补充 - 30分钟自动解锁
    doc = doc.replace(
        '核心业务：多方式账号查询、账号锁定 / 禁用状态校验、密码比对、登录错误次数累加、自动锁定账号、更新最后登录时间。',
        '核心业务：多方式账号查询、账号锁定 / 禁用状态校验（lockTime 存在且距当前不超过 30 分钟则拒绝）、'
        '密码比对（MD5 加密后比对密文）、登录错误次数累加、达到上限自动锁定账号、'
        '锁定超过 30 分钟自动解锁（清除 lockTime 并重置 loginErrorCount）、更新最后登录时间。'
    )

    # 5. LoginController 补充 - HttpServletRequest & 会话写入
    doc = doc.replace(
        '接收前端 loginAccount、password、rememberMe 参数；调用业务层；封装统一 JSON 返回结果（status、msg、用户信息）。',
        '方法签名：login(String loginAccount, String password, String rememberMe, HttpServletRequest request)；'
        '接收前端 loginAccount、password、rememberMe 参数；调用业务层 login()；'
        '登录成功后通过 request.getSession().setAttribute("user", user) 写入完整用户对象至服务端会话；'
        '封装 JSON 返回结果，包含 success、msg、userId、username、email、phone、nickname、avatarUrl、role、status 字段。'
    )

    # 6. 类依赖关系补充
    doc = doc.replace(
        'LoginController 依赖 UserService',
        'LoginController 依赖 UserService，并通过 Spring Boot 自动注入；方法接收 HttpServletRequest 以访问 HttpSession'
    )

    doc = doc.replace(
        'UserServiceImpl 依赖 UserMapper、MD5Util、MyBatisUtil',
        'UserServiceImpl 依赖 UserMapper、MD5Util（通过 Spring 自动注入，不再依赖 MyBatisUtil 直接构建 SqlSession）'
    )

    # 7. 顺序图/交互流程 - 补充会话逻辑
    doc = doc.replace(
        'LoginController 封装 JSON 结果返回前端',
        'LoginController 将 user 对象写入 HttpSession，然后封装 JSON 结果返回前端'
    )

    doc = doc.replace(
        '密码错误：累加 loginErrorCount，超阈值则写入 lockTime 锁定账号',
        '密码错误：累加 loginErrorCount，超阈值则写入 lockTime 锁定账号（30 分钟后自动解除）'
    )

    # 8. 派生需求 - 补充会话与配置
    doc = doc.replace(
        '输入参数自动去空格、跨域配置、会话保持等非功能需求，在设计和编码阶段必须一并实现。',
        '输入参数自动去空格、跨域配置、会话保持（HttpSession 写入 user 对象，前端 sessionStorage 读取）等非功能需求，'
        '在设计和编码阶段必须一并实现。'
        'MyBatis 扫描路径通过 application.properties 中 mybatis.mapper-locations 配置项生效，'
        '数据库连接参数（url、username、password、driver-class-name）在同一文件中统一配置。'
    )

    # 9. 注册功能补充（如果有相关描述）
    if '注册' not in doc[:5000]:
        # 如果文档前面没注册相关，在 LoginController 描述后加一段
        doc = doc.replace(
            '封装 JSON 返回结果，包含 success、msg、userId、username、email、phone、nickname、avatarUrl、role、status 字段。',
            '封装 JSON 返回结果，包含 success、msg、userId、username、email、phone、nickname、avatarUrl、role、status 字段。'
            'RegisterController 接收 username / email / phone（三选一）、password 参数，'
            '调用 UserService.registerAndReturnUser() 完成注册并返回完整用户信息，成功后同样将 user 对象写入 HttpSession。'
        )

    write_docx(dst, doc)
    print('[OK] Login实现规约.docx updated')


if __name__ == '__main__':
    modify_usecase_spec()
    modify_implementation_spec()
    print('Done.')
