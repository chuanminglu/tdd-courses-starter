# 技术债务AI识别演练

## 🎯 演练目标

内完成一个完整的技术债务AI识别流程，掌握基本的识别方法和处理策略。

## 📋 演练场景

**背景**：你是某电商公司的技术负责人，需要评估订单处理模块的技术债务状况。

**给定代码**：以下是订单处理核心代码

```java
public class OrderProcessor {
    // 魔法数字
    private static final int MAX_ITEMS = 50;
    private static final int VIP_DISCOUNT = 15;
  
    // 命名不规范  
    private PaymentService ps;
    private InventoryService is;
    private NotificationService ns;
  
    // 方法过长，复杂度高
    public OrderResult processOrder(OrderRequest request) {
        // 参数校验缺失
        List<OrderItem> items = request.getItems();
  
        // 深度嵌套逻辑
        if(items != null) {
            if(items.size() <= MAX_ITEMS) {
                double totalAmount = 0;
                for(int i = 0; i < items.size(); i++) {
                    OrderItem item = items.get(i);
                    if(item != null) {
                        // 重复计算逻辑
                        if(is.checkStock(item.getProductId()) >= item.getQuantity()) {
                            double price = item.getPrice();
                            if(request.getCustomer().isVip()) {
                                price = price * (100 - VIP_DISCOUNT) / 100;  // 内联计算
                            }
                            totalAmount += price * item.getQuantity();
                        } else {
                            // 错误处理不统一
                            System.out.println("库存不足: " + item.getProductId());
                            return new OrderResult("failed", "inventory_shortage");
                        }
                    }
                }
  
                // 支付处理
                if(ps.charge(request.getCustomer(), totalAmount)) {
                    // 库存扣减  
                    for(OrderItem item : items) {
                        is.decreaseStock(item.getProductId(), item.getQuantity());
                    }
                    // 通知发送
                    ns.sendOrderConfirmation(request.getCustomer().getEmail());
                    return new OrderResult("success", "order_processed");
                } else {
                    return new OrderResult("failed", "payment_failed");
                }
            }
        }
        return new OrderResult("failed", "invalid_request");
    }
}
```

---

## �️ 第一步：AI工具准备

### 🎯 选择AI分析工具

**可用的AI代码分析工具**：

1. **ChatGPT/Claude/豆包等**：通用代码分析
2. **GitHub Copilot**：集成开发环境分析
3. **SonarQube AI**：专业代码质量分析

### 📝 准备AI提示词模板

**基础分析提示词**：

```
请分析以下Java代码的技术债务：
1. 识别代码质量问题
2. 评估复杂度和可维护性
3. 提供具体的改进建议
4. 估算修复工作量

代码：
[粘贴代码]
```

---

## 🔍 第二步：AI辅助债务识别

### 🤖 使用AI进行代码分析

**操作步骤**：

1. **复制目标代码**到AI工具
2. **输入分析提示词**
3. **获取AI分析结果**
4. **验证和补充分析**

### � AI分析会话示例

**👤 用户输入**：

```
请分析以下订单处理代码的技术债务，重点关注：
- 代码复杂度
- 可维护性问题
- 潜在bug风险
- 性能问题

[OrderProcessor代码]
```

---

## 📊 第三步：AI驱动影响分析

### 🤖 AI影响评估提示词

**提示词模板**：

```
基于以下技术债务问题，请进行影响分析：

已识别问题：
1. 圈复杂度过高（12）
2. 单一职责原则违反
3. 异常处理不统一
4. 命名不规范

请分析：
1. 对业务的潜在影响
2. 对团队开发效率的影响
3. 修复的优先级建议
4. 风险评估等级
```

## 💡 第四步：AI支持决策制定

### 🤖 AI决策分析提示词

**提示词**：

```
基于技术债务分析结果，请制定修复策略：

当前状况：
- 团队规模：5人
- Sprint周期：2周
- 当前迭代任务：新功能开发（8人天）
- 技术债务工时：总计10小时

请提供：
1. 修复策略选择（立即/分批/延后）
2. 具体行动计划
3. 风险控制措施
4. 成功标准定义
```

## ✅ 演练总结

### 🎯 学习收获检查

**请勾选你已掌握的AI辅助技能**：

- ☐ 会编写有效的技术债务分析提示词
- ☐ 能使用AI工具快速识别代码质量问题
- ☐ 会利用AI进行债务影响评估和优先级排序
- ☐ 能通过AI制定可执行的修复计划
- ☐ 理解AI分析结果的验证和补充方法
- ☐ 掌握AI驱动的技术债务管理流程

### 🎲 关键要点回顾

1. **AI提示词要点**：明确分析目标、提供足够上下文、指定输出格式
2. **AI结果验证**：人工验证AI识别结果，补充业务域知识
3. **AI辅助决策**：结合团队情况让AI制定可行的修复计划
4. **持续优化**：将AI工具集成到日常的代码审查和重构流程中

## 📚 延伸阅读

### 📖 技术债务管理经典资料

- 《重构：改善既有代码的设计》- Martin Fowler
- 《代码整洁之道》- Robert C. Martin
- 《技术债务管理实战》- 企业最佳实践

**专业代码分析工具**：

- SonarQube：集成AI的代码质量分析
- CodeClimate：AI驱动的技术债务评估
- DeepCode（现Snyk Code）：AI静态代码分析

**IDE集成工具**：

- IntelliJ IDEA AI Assistant：智能代码检查
- VS Code Copilot：实时编码建议
- CodeGuru Reviewer：AWS AI代码审查

### 🎯 AI提示词模板库

**债务识别模板**：

```
分析以下代码的技术债务，关注：
- 代码复杂度和可维护性
- 设计模式使用是否恰当  
- 潜在的性能问题
- 安全风险评估
请提供：1.问题清单 2.严重程度 3.修复建议 4.工时估算
```

**影响评估模板**：

```
评估技术债务对项目的影响：
当前团队：[规模]人，技术栈：[语言/框架]
债务问题：[具体问题列表]
请分析：1.业务风险 2.开发效率影响 3.优先级建议 4.ROI评估
```

**修复计划模板**：

```
制定技术债务修复计划：
约束条件：Sprint周期[X]周，可用工时[Y]小时
债务清单：[问题列表]
请提供：1.分阶段计划 2.风险控制 3.验收标准 4.回滚策略
```

---
