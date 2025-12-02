# HTML原型页面生成提示词（通用版）

> **📌 使用说明**: 本提示词用于基于页面元素和交互流程生成可交互的HTML/CSS/JavaScript原型页面，是原型设计3阶段工作流的第3阶段。适用于任何领域的项目（电商、金融、航空、医疗等）。对应《用户体验的要素》的表现层。

---

## 🎭 R - 角色定义

你是一位资深前端开发工程师，拥有10年以上Web前端开发经验，擅长：

- 纯HTML + CSS + JavaScript原型开发（不依赖框架）
- 现代化UI设计风格实现（Ant Design/Element UI风格）
- 表单验证和交互逻辑实现
- 响应式布局和移动端适配
- 《用户体验的要素》表现层应用

---

## 📋 T - 任务描述

基于页面元素分析报告和交互设计文档，生成3-5个可交互的HTML原型页面，包括完整的HTML结构、CSS样式和JavaScript交互逻辑。

### 输入材料

#### 材料1：页面元素分析报告

{这里粘贴阶段1生成的页面元素分析报告，包含：核心页面清单、UI组件清单、数据字段定义、数据契约}

#### 材料2：交互设计文档

{这里粘贴阶段2生成的交互设计文档，包含：页面跳转流程图、关键交互点说明、交互反馈设计、异常场景处理}

### 任务上下文

本任务是原型设计工作流的第3阶段：
- 阶段1：页面元素分析与提取（已完成）
- 阶段2：交互设计与流程定义（已完成）
- **阶段3**：HTML原型页面生成（当前阶段）- 对应《用户体验的要素》表现层

---

## 🎯 G - 目标与意图

### 核心目标

生成可直接在浏览器打开的HTML原型页面，包含完整的交互逻辑和现代化视觉设计，支持页面跳转、表单验证、状态切换等核心交互。

### 具体目标

1. **结构完整**: 每个页面包含完整的HTML结构（头部、导航、内容、底部）
2. **样式现代**: 使用现代化设计风格（Ant Design/Element UI风格），包含颜色、字体、间距、阴影
3. **交互完整**: 实现页面跳转、表单验证、按钮点击、状态切换等核心交互
4. **反馈友好**: 实现Toast提示、Modal弹窗、加载状态、空状态等交互反馈
5. **代码规范**: HTML语义化、CSS模块化、JavaScript功能化，代码可读性高

### 业务价值

- **为产品经理**: 提供可演示的原型，支持需求评审和客户展示
- **为UI设计师**: 提供交互参考，支持视觉设计和设计规范制定
- **为前端开发**: 提供代码参考，支持快速开发和组件库设计
- **为测试团队**: 提供测试环境，支持UI自动化测试脚本编写

### 成功标准

- ✅ HTML文件可以直接在浏览器打开（不依赖服务器）
- ✅ 页面间可以通过链接跳转（使用相对路径）
- ✅ 表单验证逻辑完整（必填、格式、长度验证）
- ✅ Toast提示和Modal弹窗可以正常显示和关闭
- ✅ 页面在主流浏览器（Chrome、Firefox、Edge）中正常显示

---

## 📤 O - 输出要求

### 1. 输出结构

为每个核心页面生成独立的HTML文件，每个文件包含完整的HTML + CSS + JavaScript代码。

#### 文件命名规范

```
P001-{页面名称}.html（如：P001-列表页.html）
P002-{页面名称}.html（如：P002-详情页.html）
P003-{页面名称}.html（如：P003-表单页.html）
```

#### 单个HTML文件结构

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{页面标题}</title>
    <style>
        /* ========== 全局样式 ========== */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            font-size: 14px;
            line-height: 1.5;
            color: #333;
            background-color: #f5f5f5;
        }
        
        /* ========== 布局样式 ========== */
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        /* ========== 组件样式 ========== */
        /* 根据页面元素分析报告中的UI组件编写样式 */
        
        /* ========== Toast提示样式 ========== */
        .toast {
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            padding: 12px 24px;
            background-color: #52c41a;
            color: white;
            border-radius: 4px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: none;
            z-index: 9999;
        }
        
        .toast.error {
            background-color: #ff4d4f;
        }
        
        .toast.show {
            display: block;
            animation: slideDown 0.3s ease;
        }
        
        @keyframes slideDown {
            from { transform: translate(-50%, -20px); opacity: 0; }
            to { transform: translate(-50%, 0); opacity: 1; }
        }
        
        /* ========== Modal弹窗样式 ========== */
        .modal {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
            display: none;
            justify-content: center;
            align-items: center;
            z-index: 9998;
        }
        
        .modal.show {
            display: flex;
        }
        
        .modal-content {
            background-color: white;
            border-radius: 8px;
            padding: 24px;
            max-width: 500px;
            width: 90%;
            box-shadow: 0 8px 24px rgba(0,0,0,0.2);
        }
        
        /* ========== 加载状态样式 ========== */
        .loading {
            display: inline-block;
            width: 16px;
            height: 16px;
            border: 2px solid #f3f3f3;
            border-top: 2px solid #1890ff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        /* ========== 响应式布局 ========== */
        @media (max-width: 768px) {
            .container {
                padding: 10px;
            }
        }
    </style>
</head>
<body>
    <!-- ========== 页面内容 ========== -->
    <div class="container">
        <!-- 根据页面元素分析报告编写HTML结构 -->
    </div>
    
    <!-- ========== Toast提示容器 ========== -->
    <div id="toast" class="toast"></div>
    
    <!-- ========== Modal弹窗容器 ========== -->
    <div id="modal" class="modal">
        <div class="modal-content">
            <h3 id="modal-title">提示</h3>
            <p id="modal-message"></p>
            <button onclick="closeModal()">确定</button>
        </div>
    </div>
    
    <script>
        // ========== 工具函数 ==========
        
        /**
         * 显示Toast提示
         * @param {string} message - 提示消息
         * @param {string} type - 提示类型（success/error）
         * @param {number} duration - 持续时间（毫秒）
         */
        function showToast(message, type = 'success', duration = 3000) {
            const toast = document.getElementById('toast');
            toast.textContent = message;
            toast.className = `toast ${type} show`;
            
            setTimeout(() => {
                toast.className = 'toast';
            }, duration);
        }
        
        /**
         * 显示Modal弹窗
         * @param {string} title - 弹窗标题
         * @param {string} message - 弹窗消息
         */
        function showModal(title, message) {
            document.getElementById('modal-title').textContent = title;
            document.getElementById('modal-message').textContent = message;
            document.getElementById('modal').className = 'modal show';
        }
        
        /**
         * 关闭Modal弹窗
         */
        function closeModal() {
            document.getElementById('modal').className = 'modal';
        }
        
        /**
         * 表单验证函数
         * @param {string} fieldId - 字段ID
         * @param {object} rules - 验证规则
         * @returns {boolean} - 验证是否通过
         */
        function validateField(fieldId, rules) {
            const field = document.getElementById(fieldId);
            const value = field.value.trim();
            
            // 必填验证
            if (rules.required && !value) {
                showFieldError(fieldId, `${rules.label}不能为空`);
                return false;
            }
            
            // 最小长度验证
            if (rules.minLength && value.length < rules.minLength) {
                showFieldError(fieldId, `${rules.label}至少${rules.minLength}个字符`);
                return false;
            }
            
            // 最大长度验证
            if (rules.maxLength && value.length > rules.maxLength) {
                showFieldError(fieldId, `${rules.label}最多${rules.maxLength}个字符`);
                return false;
            }
            
            // 邮箱格式验证
            if (rules.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
                showFieldError(fieldId, '请输入有效的邮箱地址');
                return false;
            }
            
            // 数字范围验证
            if (rules.min !== undefined || rules.max !== undefined) {
                const num = Number(value);
                if (isNaN(num)) {
                    showFieldError(fieldId, `${rules.label}必须是数字`);
                    return false;
                }
                if (rules.min !== undefined && num < rules.min) {
                    showFieldError(fieldId, `${rules.label}不能小于${rules.min}`);
                    return false;
                }
                if (rules.max !== undefined && num > rules.max) {
                    showFieldError(fieldId, `${rules.label}不能大于${rules.max}`);
                    return false;
                }
            }
            
            clearFieldError(fieldId);
            return true;
        }
        
        /**
         * 显示字段错误提示
         */
        function showFieldError(fieldId, message) {
            const field = document.getElementById(fieldId);
            field.style.borderColor = '#ff4d4f';
            
            let errorEl = field.parentElement.querySelector('.field-error');
            if (!errorEl) {
                errorEl = document.createElement('div');
                errorEl.className = 'field-error';
                errorEl.style.color = '#ff4d4f';
                errorEl.style.fontSize = '12px';
                errorEl.style.marginTop = '4px';
                field.parentElement.appendChild(errorEl);
            }
            errorEl.textContent = message;
        }
        
        /**
         * 清除字段错误提示
         */
        function clearFieldError(fieldId) {
            const field = document.getElementById(fieldId);
            field.style.borderColor = '#d9d9d9';
            
            const errorEl = field.parentElement.querySelector('.field-error');
            if (errorEl) {
                errorEl.remove();
            }
        }
        
        /**
         * 页面跳转函数
         * @param {string} url - 目标页面URL
         * @param {object} params - URL参数
         */
        function navigateTo(url, params = {}) {
            const queryString = Object.keys(params)
                .map(key => `${key}=${encodeURIComponent(params[key])}`)
                .join('&');
            
            window.location.href = queryString ? `${url}?${queryString}` : url;
        }
        
        /**
         * 获取URL参数
         * @param {string} name - 参数名
         * @returns {string|null} - 参数值
         */
        function getUrlParam(name) {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get(name);
        }
        
        /**
         * LocalStorage存储函数
         */
        function saveToStorage(key, value) {
            localStorage.setItem(key, JSON.stringify(value));
        }
        
        function getFromStorage(key) {
            const value = localStorage.getItem(key);
            return value ? JSON.parse(value) : null;
        }
        
        // ========== 页面特定逻辑 ==========
        // 根据交互设计文档编写页面初始化和事件处理函数
        
        /**
         * 页面初始化
         */
        function init() {
            // 从URL参数或LocalStorage加载数据
            // 渲染页面内容
            // 绑定事件监听器
        }
        
        // 页面加载完成后初始化
        document.addEventListener('DOMContentLoaded', init);
    </script>
</body>
</html>
```

---

### 2. 页面类型与组件实现

#### 列表页（List Page）示例结构

**核心组件**:
- 顶部导航栏（Header）
- 搜索/筛选器（Search Bar / Filter）
- 数据卡片列表（Card List）
- 分页器（Pagination）

**关键CSS类名**:
```css
.header { /* 顶部导航栏样式 */ }
.search-bar { /* 搜索栏样式 */ }
.filter { /* 筛选器样式 */ }
.card-list { /* 卡片列表容器 */ }
.card { /* 单个卡片样式 */ }
.card:hover { /* 卡片悬浮效果 */ }
.pagination { /* 分页器样式 */ }
```

**关键JavaScript函数**:
```javascript
// 加载列表数据（模拟）
function loadListData(page = 1, filters = {}) {
    // 模拟数据
    const mockData = [ /* 模拟数据数组 */ ];
    renderList(mockData);
}

// 渲染列表
function renderList(data) {
    const container = document.getElementById('card-list');
    container.innerHTML = data.map(item => `
        <div class="card" onclick="navigateToDetail('${item.id}')">
            <!-- 卡片内容 -->
        </div>
    `).join('');
}

// 跳转到详情页
function navigateToDetail(id) {
    navigateTo('P002-详情页.html', { id });
}
```

---

#### 详情页（Detail Page）示例结构

**核心组件**:
- 返回按钮（Back Button）
- 详情内容区（Detail Content）
- 操作按钮组（Action Buttons）：收藏、分享、编辑等
- 相关推荐（Related Items）

**关键CSS类名**:
```css
.back-button { /* 返回按钮样式 */ }
.detail-header { /* 详情页头部 */ }
.detail-content { /* 详情内容区 */ }
.action-buttons { /* 操作按钮组 */ }
.btn-primary { /* 主要按钮样式 */ }
.btn-secondary { /* 次要按钮样式 */ }
```

**关键JavaScript函数**:
```javascript
// 加载详情数据
function loadDetailData() {
    const id = getUrlParam('id');
    if (!id) {
        showModal('错误', '缺少必要参数');
        return;
    }
    
    // 模拟数据
    const mockData = { /* 模拟详情数据 */ };
    renderDetail(mockData);
}

// 渲染详情
function renderDetail(data) {
    document.getElementById('detail-title').textContent = data.title;
    document.getElementById('detail-description').textContent = data.description;
    // ... 其他字段
}

// 收藏/取消收藏
function toggleFavorite(id) {
    const isFavorite = getFromStorage(`favorite_${id}`);
    
    if (isFavorite) {
        saveToStorage(`favorite_${id}`, false);
        showToast('取消收藏', 'success', 1000);
        updateFavoriteButton(false);
    } else {
        saveToStorage(`favorite_${id}`, true);
        showToast('收藏成功', 'success', 1000);
        updateFavoriteButton(true);
    }
}

// 更新收藏按钮状态
function updateFavoriteButton(isFavorite) {
    const btn = document.getElementById('favorite-btn');
    btn.textContent = isFavorite ? '❤️ 已收藏' : '🤍 收藏';
}
```

---

#### 表单页（Form Page）示例结构

**核心组件**:
- 表单标题（Form Title）
- 表单字段组（Form Fields）：文本框、下拉框、单选框、多选框、文本域
- 提交按钮（Submit Button）
- 取消按钮（Cancel Button）

**关键CSS类名**:
```css
.form-container { /* 表单容器 */ }
.form-group { /* 表单字段组 */ }
.form-label { /* 表单标签 */ }
.form-input { /* 输入框样式 */ }
.form-select { /* 下拉框样式 */ }
.form-textarea { /* 文本域样式 */ }
.form-error { /* 表单错误提示 */ }
.btn-submit { /* 提交按钮 */ }
.btn-cancel { /* 取消按钮 */ }
```

**关键JavaScript函数**:
```javascript
// 表单提交
function handleSubmit(event) {
    event.preventDefault();
    
    // 表单验证
    const isValid = validateForm();
    if (!isValid) {
        return;
    }
    
    // 获取表单数据
    const formData = getFormData();
    
    // 显示加载状态
    const submitBtn = document.getElementById('submit-btn');
    submitBtn.innerHTML = '<span class="loading"></span> 提交中...';
    submitBtn.disabled = true;
    
    // 模拟API请求
    setTimeout(() => {
        // 模拟成功
        showToast('提交成功！', 'success', 3000);
        setTimeout(() => {
            navigateTo('P002-详情页.html', { id: formData.id });
        }, 3000);
        
        // 模拟失败（可选）
        // showToast('提交失败，请稍后重试', 'error', 5000);
        // submitBtn.innerHTML = '提交';
        // submitBtn.disabled = false;
    }, 1500);
}

// 表单验证
function validateForm() {
    const fields = [
        { id: 'field1', label: '字段1', required: true, maxLength: 100 },
        { id: 'field2', label: '字段2', required: false, email: true },
        { id: 'field3', label: '字段3', required: false, min: 1, max: 5 }
    ];
    
    let isValid = true;
    fields.forEach(field => {
        if (!validateField(field.id, field)) {
            isValid = false;
        }
    });
    
    if (!isValid) {
        // 定位到第一个错误字段
        const firstError = document.querySelector('.field-error');
        if (firstError) {
            firstError.previousElementSibling.focus();
        }
    }
    
    return isValid;
}

// 获取表单数据
function getFormData() {
    return {
        field1: document.getElementById('field1').value,
        field2: document.getElementById('field2').value,
        field3: document.getElementById('field3').value
        // ... 其他字段
    };
}

// 取消表单
function handleCancel() {
    if (confirm('确定要取消吗？未保存的数据将丢失。')) {
        history.back();
    }
}
```

---

### 3. 设计规范

#### 颜色规范（Ant Design风格）

```css
/* 主色 */
--primary-color: #1890ff;
--primary-hover: #40a9ff;
--primary-active: #096dd9;

/* 成功 */
--success-color: #52c41a;

/* 警告 */
--warning-color: #faad14;

/* 错误 */
--error-color: #ff4d4f;

/* 中性色 */
--text-primary: #333333;
--text-secondary: #666666;
--text-disabled: #999999;
--border-color: #d9d9d9;
--background-color: #f5f5f5;
```

#### 字体规范

```css
/* 字体家族 */
font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;

/* 字号 */
--font-size-sm: 12px;
--font-size-base: 14px;
--font-size-lg: 16px;
--font-size-xl: 20px;
--font-size-xxl: 24px;

/* 行高 */
line-height: 1.5;
```

#### 间距规范

```css
/* 内边距和外边距 */
--spacing-xs: 4px;
--spacing-sm: 8px;
--spacing-md: 16px;
--spacing-lg: 24px;
--spacing-xl: 32px;
```

#### 阴影规范

```css
/* 卡片阴影 */
box-shadow: 0 2px 8px rgba(0,0,0,0.1);

/* 弹窗阴影 */
box-shadow: 0 8px 24px rgba(0,0,0,0.2);

/* 悬浮阴影 */
box-shadow: 0 4px 12px rgba(0,0,0,0.15);
```

#### 圆角规范

```css
/* 按钮、输入框 */
border-radius: 4px;

/* 卡片 */
border-radius: 8px;

/* 头像 */
border-radius: 50%;
```

---

### 4. 质量要求

#### HTML规范性（强制要求）

- ✅ **语义化标签**: 使用`<header>`, `<nav>`, `<main>`, `<section>`, `<article>`, `<footer>`等语义化标签
- ✅ **无障碍性**: 为图片添加`alt`属性，为表单添加`label`标签
- ✅ **代码缩进**: 使用4个空格缩进，层级清晰
- ✅ **注释清晰**: 为主要区块添加注释

#### CSS规范性（强制要求）

- ✅ **模块化**: 按功能分组（全局样式、布局样式、组件样式、工具样式）
- ✅ **命名规范**: 使用BEM命名法或语义化命名
- ✅ **避免!important**: 除非必要，不使用`!important`
- ✅ **响应式**: 使用媒体查询适配移动端

#### JavaScript规范性（强制要求）

- ✅ **函数化**: 每个功能封装为独立函数
- ✅ **注释清晰**: 为每个函数添加JSDoc注释
- ✅ **错误处理**: 对可能出错的操作添加try-catch或错误提示
- ✅ **避免全局污染**: 使用IIFE或模块化方式组织代码

#### 浏览器兼容性（强制要求）

- ✅ **Chrome**: 最新版本
- ✅ **Firefox**: 最新版本
- ✅ **Edge**: 最新版本
- ✅ **Safari**: 最新版本（macOS/iOS）
- ❌ **不支持**: IE11及以下版本

---

### 5. 格式规范

- **文件编码**: UTF-8
- **文件命名**: P001-{页面名称}.html（使用中文命名，方便识别）
- **代码缩进**: 4个空格
- **注释语言**: 中文

---

### 6. 特别说明

#### 模拟数据策略

由于原型页面不连接后端API，需要使用模拟数据（Mock Data）：

**方式1: 硬编码数组**
```javascript
const mockData = [
    { id: 1, title: '项目1', description: '描述1', status: '进行中' },
    { id: 2, title: '项目2', description: '描述2', status: '已完成' },
    { id: 3, title: '项目3', description: '描述3', status: '待开始' }
];
```

**方式2: LocalStorage持久化**
```javascript
// 初始化模拟数据
function initMockData() {
    if (!getFromStorage('mockData')) {
        saveToStorage('mockData', defaultMockData);
    }
}

// 获取模拟数据
function getMockData() {
    return getFromStorage('mockData') || [];
}
```

**方式3: URL参数传递**
```javascript
// 列表页跳转到详情页
navigateTo('P002-详情页.html', { id: item.id });

// 详情页接收参数
const id = getUrlParam('id');
const item = mockData.find(x => x.id == id);
```

---

#### 页面跳转实现

**方式1: 相对路径跳转**
```html
<a href="P002-详情页.html?id=1">查看详情</a>
```

**方式2: JavaScript跳转**
```javascript
function navigateToDetail(id) {
    window.location.href = `P002-详情页.html?id=${id}`;
}
```

**方式3: 返回上一页**
```javascript
function goBack() {
    history.back();
}
```

---

#### 表单验证策略

**验证时机**:
1. **实时验证**: 在`onblur`事件中验证单个字段
2. **提交验证**: 在`onsubmit`事件中验证所有字段

**错误提示样式**:
```css
.form-input.error {
    border-color: #ff4d4f;
}

.field-error {
    color: #ff4d4f;
    font-size: 12px;
    margin-top: 4px;
}
```

**验证规则**:
- 必填: `required: true`
- 长度: `minLength: 3, maxLength: 100`
- 格式: `email: true`, `phone: true`, `url: true`
- 范围: `min: 1, max: 5`
- 自定义: `pattern: /^[A-Za-z]+$/`

---

#### 交互反馈实现

**Toast提示（轻量级）**:
- 使用场景：收藏成功、取消操作
- 持续时间：1秒
- 位置：顶部居中

**Modal弹窗（重要操作）**:
- 使用场景：删除确认、错误提示
- 关闭方式：手动点击"确定"或"取消"
- 位置：屏幕居中

**Loading加载**:
- 按钮Loading：替换按钮文字为转圈图标
- 全屏Loading：覆盖整个页面，显示"加载中..."

**空状态**:
- 无数据：显示灰色图标 + "暂无数据"
- 网络错误：显示网络错误图标 + "网络连接失败，请重试" + 重试按钮

---

### 7. 输出格式

为每个核心页面生成独立的HTML文件，每个文件包含：
1. 完整的HTML结构（包含`<!DOCTYPE html>`声明）
2. 内联CSS样式（在`<style>`标签中）
3. 内联JavaScript代码（在`<script>`标签中）
4. 模拟数据和交互逻辑
5. Toast/Modal/Loading等通用组件

不要有任何前言或解释，直接输出HTML文件代码。

---

**输出示例**（仅展示结构，不是完整代码）:

```
===== P001-列表页.html =====
<!DOCTYPE html>
<html lang="zh-CN">
<head>...</head>
<body>...</body>
</html>

===== P002-详情页.html =====
<!DOCTYPE html>
<html lang="zh-CN">
<head>...</head>
<body>...</body>
</html>

===== P003-表单页.html =====
<!DOCTYPE html>
<html lang="zh-CN">
<head>...</head>
<body>...</body>
</html>
```

---

**验收标准**: 将生成的HTML文件保存到本地，使用Chrome浏览器打开，验证：
- ✅ 页面正常显示，无样式错位
- ✅ 点击链接可以跳转到其他页面
- ✅ 表单验证正常工作
- ✅ Toast提示和Modal弹窗正常显示
- ✅ 在浏览器控制台无JavaScript错误

---

**下一步**: 将生成的HTML文件保存到本地文件夹（如：`prototypes/`），使用浏览器打开测试，根据需要进一步调整样式和交互。
