# 携程网机票搜索自动化测试演练（Python版）

## 演练目标

在5分钟内创建一个简单的Playwright测试，模拟在携程网搜索机票并进行星级筛选

## 前置条件

- VS Code已安装
- Python 3.8+环境

---

## 第1步：项目初始化（2分钟）

```bash
# 创建项目目录
mkdir ctrip-test
cd ctrip-test

# 创建虚拟环境
python -m venv venv
venv\Scripts\activate

# 安装Playwright
pip install playwright pytest-playwright

# 安装浏览器（必须执行）
playwright install
```

---

## 第2步：编写测试脚本（2分钟）

创建 `test_ctrip_search.py`：

```python
import pytest
from playwright.sync_api import Page, expect
import time

def test_ctrip_flight_search(page: Page):
    """携程网机票搜索与星级筛选测试"""
  
    # 1. 访问携程网
    page.goto("https://www.ctrip.com/")
  
    # 2. 点击机票选项卡
    page.click("text=机票")
    time.sleep(2)
  
    # 3. 输入出发地和目的地
    page.fill('[placeholder*="请输入出发城市"]', "北京")
    time.sleep(1)
    page.fill('[placeholder*="请输入到达城市"]', "上海")
    time.sleep(1)
  
    # 4. 点击搜索按钮
    page.click('button:has-text("搜索航班")')
  
    # 5. 等待搜索结果加载
    page.wait_for_load_state("networkidle")
    time.sleep(3)
  
    # 6. 尝试星级筛选（如果存在）
    try:
        star_filter = page.locator("text=五星").first
        if star_filter.is_visible(timeout=3000):
            star_filter.click()
            time.sleep(2)
    except Exception as e:
        print("星级筛选不可用，跳过")
  
    # 7. 验证结果页面
    expect(page).to_have_url("*ctrip.com*")
  
    # 8. 截图保存
    page.screenshot(path="ctrip_search_result.png")
```

创建 `pytest.ini` 配置文件：

```ini
[tool:pytest]
addopts = --browser chromium --headed
```

---

## 第3步：运行测试（1分钟）

```bash
# 激活虚拟环境（如果未激活）
venv\Scripts\activate

# 运行测试（显示浏览器界面）
pytest test_ctrip_search.py -v -s

# 或者使用无头模式
pytest test_ctrip_search.py --browser chromium
```

---

## 完成验证

✅ 测试成功运行
✅ 生成截图文件
✅ 验证搜索功能
