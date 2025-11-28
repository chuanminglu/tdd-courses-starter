# BankAccount 类实现文档

## 📋 概述

`BankAccount` 是一个线程安全的银行账户类，实现了基本的账户操作和转账功能。该类使用 `BigDecimal` 确保金额计算的精度，并采用银行家舍入法则处理小数。

---

## 🎯 核心特性

### 1. 精确的金额计算

- 使用 `BigDecimal` 类型存储余额，避免浮点数精度问题
- 所有金额保留2位小数
- 采用银行家舍入法则（`RoundingMode.HALF_EVEN`）

### 2. 线程安全

- 使用 `volatile` 关键字修饰 `balance` 字段，确保可见性
- 使用 `synchronized` 同步块保护临界区
- 转账操作使用有序锁定机制，避免死锁

### 3. 严格的参数验证

- 账户号不能为空或null
- 初始余额不能为负数
- 转账金额必须大于0
- 目标账户不能为null或自身

### 4. 自定义异常

- `InvalidAmountException`: 金额无效异常
- `InsufficientFundsException`: 余额不足异常

---

## 🏗️ 类结构

### 包声明

```java
package main.java;
```

### 字段定义

| 字段              | 类型           | 修饰符               | 说明                       |
| ----------------- | -------------- | -------------------- | -------------------------- |
| `accountNumber` | `String`     | `private final`    | 账户号，不可变             |
| `balance`       | `BigDecimal` | `private volatile` | 账户余额，支持多线程可见性 |
| `lock`          | `Object`     | `private final`    | 同步锁对象                 |

---

## 📚 构造函数

### 1. 带初始余额的构造函数

```java
public BankAccount(String accountNumber, BigDecimal initialBalance)
```

**参数**:

- `accountNumber`: 账户号，不能为null或空字符串
- `initialBalance`: 初始余额，不能为null或负数

**异常**:

- `IllegalArgumentException`: 参数验证失败时抛出

**验证规则**:

1. 账户号不能为null
2. 账户号不能为空字符串（去除空格后）
3. 初始余额不能为null
4. 初始余额不能小于0

**实现细节**:

```java
this.accountNumber = accountNumber;
this.balance = initialBalance.setScale(2, RoundingMode.HALF_EVEN);
```

### 2. 零余额构造函数

```java
public BankAccount(String accountNumber)
```

**说明**: 创建初始余额为0的账户，内部调用主构造函数

---

## 🔧 核心方法

### 1. getAccountNumber()

```java
public String getAccountNumber()
```

**功能**: 获取账户号

**返回值**: 账户号字符串

**特点**: 账户号是不可变的（final字段）

---

### 2. getBalance()

```java
public BigDecimal getBalance()
```

**功能**: 获取当前账户余额

**返回值**: 当前余额（保留2位小数）

**线程安全**: 使用 `synchronized` 同步块

**实现**:

```java
synchronized (lock) {
    return balance.setScale(2, RoundingMode.HALF_EVEN);
}
```

---

### 3. deposit() - 存款

```java
public void deposit(BigDecimal amount) throws InvalidAmountException
```

**功能**: 向账户存入指定金额

**参数**:

- `amount`: 存款金额，必须大于0

**异常**:

- `InvalidAmountException`: 存款金额无效（≤0或null）

**验证规则**:

1. 金额不能为null
2. 金额必须大于0

**线程安全**: 使用 `synchronized` 同步块保护余额更新

**实现逻辑**:

```java
synchronized (lock) {
    balance = balance.add(amount).setScale(2, RoundingMode.HALF_EVEN);
}
```

---

### 4. withdraw() - 取款

```java
public void withdraw(BigDecimal amount) 
    throws InvalidAmountException, InsufficientFundsException
```

**功能**: 从账户取出指定金额

**参数**:

- `amount`: 取款金额，必须大于0且不超过当前余额

**异常**:

- `InvalidAmountException`: 取款金额无效（≤0或null）
- `InsufficientFundsException`: 余额不足

**验证规则**:

1. 金额不能为null
2. 金额必须大于0
3. 余额必须大于等于取款金额

**线程安全**: 使用 `synchronized` 同步块保护余额检查和更新

**实现逻辑**:

```java
synchronized (lock) {
    if (balance.compareTo(amount) < 0) {
        throw new InsufficientFundsException("余额不足...");
    }
    balance = balance.subtract(amount).setScale(2, RoundingMode.HALF_EVEN);
}
```

---

### 5. transferTo() - 转账

```java
public void transferTo(BankAccount targetAccount, BigDecimal amount) 
    throws InvalidAmountException, InsufficientFundsException
```

**功能**: 向目标账户转账指定金额

**参数**:

- `targetAccount`: 目标账户对象，不能为null或自身
- `amount`: 转账金额，必须大于0且不超过当前余额

**异常**:

- `IllegalArgumentException`: 目标账户为null或为自身账户
- `InvalidAmountException`: 转账金额无效（≤0或null）
- `InsufficientFundsException`: 余额不足

**验证规则**:

1. 目标账户不能为null
2. 目标账户不能是自己
3. 转账金额不能为null
4. 转账金额必须大于0
5. 当前余额必须大于等于转账金额

**线程安全 - 避免死锁的有序锁定机制**:

```java
// 根据账户号字典序确定锁定顺序
BankAccount firstLock = this.accountNumber.compareTo(targetAccount.accountNumber) < 0 
    ? this : targetAccount;
BankAccount secondLock = this.accountNumber.compareTo(targetAccount.accountNumber) < 0 
    ? targetAccount : this;

// 按顺序获取锁
synchronized (firstLock.lock) {
    synchronized (secondLock.lock) {
        // 执行转账操作
    }
}
```

**为什么这样设计？**

在多线程环境下，如果两个线程同时执行：

- 线程A: 账户1 → 账户2 转账
- 线程B: 账户2 → 账户1 转账

如果不使用有序锁定，可能发生：

- 线程A锁定账户1，等待账户2
- 线程B锁定账户2，等待账户1
- **死锁发生！**

使用有序锁定（按账户号字典序），确保所有线程以相同顺序获取锁，避免循环等待。

**原子性操作**:

```java
// 扣款
this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_EVEN);
// 入账
targetAccount.balance = targetAccount.balance.add(amount).setScale(2, RoundingMode.HALF_EVEN);
```

---

## 🔍 重写方法

### 1. toString()

```java
@Override
public String toString()
```

**功能**: 返回账户的字符串表示

**返回格式**:

```
BankAccount{accountNumber='ACC001', balance=1000.00}
```

---

### 2. equals()

```java
@Override
public boolean equals(Object obj)
```

**功能**: 判断两个账户是否相等

**判断依据**: 仅根据账户号判断（账户号相同则视为同一账户）

**实现逻辑**:

1. 如果是同一对象引用，返回true
2. 如果对象为null或类型不同，返回false
3. 比较账户号是否相等

---

### 3. hashCode()

```java
@Override
public int hashCode()
```

**功能**: 返回账户的哈希码

**实现**: 使用账户号的哈希码

**重要性**: 与 `equals()` 配合使用，确保在HashMap等集合中正确工作

---

## ⚠️ 自定义异常

### 1. InvalidAmountException

```java
public static class InvalidAmountException extends Exception
```

**用途**: 表示金额无效（≤0或null）

**使用场景**:

- 存款金额无效
- 取款金额无效
- 转账金额无效

**示例**:

```java
throw new InvalidAmountException("存款金额必须大于0");
```

---

### 2. InsufficientFundsException

```java
public static class InsufficientFundsException extends Exception
```

**用途**: 表示账户余额不足

**使用场景**:

- 取款时余额不足
- 转账时余额不足

**示例**:

```java
throw new InsufficientFundsException("余额不足，当前余额: 100.00，取款金额: 200.00");
```

---

## 🎓 使用示例

### 示例1: 创建账户并存取款

```java
// 创建账户
BankAccount account = new BankAccount("ACC001", new BigDecimal("1000.00"));

// 存款
account.deposit(new BigDecimal("500.00"));
System.out.println("存款后余额: " + account.getBalance()); // 1500.00

// 取款
account.withdraw(new BigDecimal("200.00"));
System.out.println("取款后余额: " + account.getBalance()); // 1300.00
```

---

### 示例2: 转账操作

```java
// 创建两个账户
BankAccount account1 = new BankAccount("ACC001", new BigDecimal("1000.00"));
BankAccount account2 = new BankAccount("ACC002", new BigDecimal("500.00"));

// 从账户1向账户2转账300元
account1.transferTo(account2, new BigDecimal("300.00"));

System.out.println("账户1余额: " + account1.getBalance()); // 700.00
System.out.println("账户2余额: " + account2.getBalance()); // 800.00
```

---

### 示例3: 异常处理

```java
BankAccount account = new BankAccount("ACC001", new BigDecimal("100.00"));

try {
    // 尝试取款超过余额
    account.withdraw(new BigDecimal("200.00"));
} catch (InsufficientFundsException e) {
    System.err.println("错误: " + e.getMessage());
    // 输出: 错误: 余额不足，当前余额: 100.00，取款金额: 200.00
}

try {
    // 尝试存入负数
    account.deposit(new BigDecimal("-50.00"));
} catch (InvalidAmountException e) {
    System.err.println("错误: " + e.getMessage());
    // 输出: 错误: 存款金额必须大于0
}
```

---

### 示例4: 多线程转账（演示线程安全）

```java
BankAccount account1 = new BankAccount("ACC001", new BigDecimal("1000.00"));
BankAccount account2 = new BankAccount("ACC002", new BigDecimal("1000.00"));

// 线程1: 账户1 → 账户2 转账100元（循环100次）
Thread t1 = new Thread(() -> {
    for (int i = 0; i < 100; i++) {
        try {
            account1.transferTo(account2, new BigDecimal("1.00"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
});

// 线程2: 账户2 → 账户1 转账100元（循环100次）
Thread t2 = new Thread(() -> {
    for (int i = 0; i < 100; i++) {
        try {
            account2.transferTo(account1, new BigDecimal("1.00"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
});

t1.start();
t2.start();
t1.join();
t2.join();

// 总金额应保持不变：2000.00
System.out.println("账户1余额: " + account1.getBalance());
System.out.println("账户2余额: " + account2.getBalance());
System.out.println("总金额: " + 
    account1.getBalance().add(account2.getBalance())); // 应为2000.00
```

---

## 🔐 线程安全分析

### 1. 为什么使用 volatile？

```java
private volatile BigDecimal balance;
```

**作用**:

- 确保 `balance` 字段的可见性（一个线程修改后，其他线程立即可见）
- 禁止指令重排序优化

**适用场景**:

- 多线程读取余额
- 配合synchronized使用

---

### 2. 为什么使用 synchronized？

```java
synchronized (lock) {
    // 临界区代码
}
```

**作用**:

- 确保操作的原子性（多个操作作为一个整体执行）
- 防止竞态条件

**保护的操作**:

- 余额查询（getBalance）
- 余额更新（deposit、withdraw）
- 余额检查和更新（withdraw、transferTo）

---

### 3. 有序锁定机制详解

**问题**: 两个账户互相转账可能导致死锁

**解决方案**: 按账户号字典序获取锁

```java
// 确定锁的顺序
BankAccount firstLock = 
    this.accountNumber.compareTo(targetAccount.accountNumber) < 0 
    ? this : targetAccount;
  
BankAccount secondLock = 
    this.accountNumber.compareTo(targetAccount.accountNumber) < 0 
    ? targetAccount : this;

// 按顺序获取锁
synchronized (firstLock.lock) {
    synchronized (secondLock.lock) {
        // 转账操作
    }
}
```

**为什么有效？**

| 场景             | 线程A操作        | 线程B操作        | 锁定顺序               |
| ---------------- | ---------------- | ---------------- | ---------------------- |
| ACC001 → ACC002 | transferTo       | -                | 先锁ACC001，再锁ACC002 |
| ACC002 → ACC001 | -                | transferTo       | 先锁ACC001，再锁ACC002 |
| 同时执行         | ACC001 → ACC002 | ACC002 → ACC001 | 都按相同顺序锁定       |

所有线程都按照相同的顺序（字典序）获取锁，避免循环等待，从而避免死锁。

---

## 💡 最佳实践

### 1. BigDecimal使用注意事项

❌ **错误**:

```java
BigDecimal amount = new BigDecimal(0.1); // 可能产生精度问题
```

✅ **正确**:

```java
BigDecimal amount = new BigDecimal("0.1"); // 使用字符串构造
```

---

### 2. 金额比较

❌ **错误**:

```java
if (balance == amount) { ... } // 错误：使用 == 比较
```

✅ **正确**:

```java
if (balance.compareTo(amount) == 0) { ... } // 使用compareTo
```

---

### 3. 异常处理

✅ **推荐**:

```java
try {
    account.withdraw(amount);
} catch (InvalidAmountException e) {
    // 处理金额无效
    logger.error("金额无效: " + e.getMessage());
} catch (InsufficientFundsException e) {
    // 处理余额不足
    logger.error("余额不足: " + e.getMessage());
}
```

---

### 4. 线程安全使用

✅ **线程安全的操作**:

```java
// 所有公共方法都是线程安全的
account.deposit(amount);
account.withdraw(amount);
account.transferTo(target, amount);
BigDecimal balance = account.getBalance();
```

---

## 📊 性能考虑

### 1. 锁的粒度

- **优点**: 使用对象级别的锁（`lock`），不影响其他账户的操作
- **缺点**: 转账需要同时锁定两个账户，可能降低并发性

### 2. 优化建议

**场景1**: 高并发读取余额

```java
// 当前实现已经使用synchronized，确保一致性
// 如果读多写少，可以考虑使用ReadWriteLock
```

**场景2**: 批量转账

```java
// 可以实现批量转账方法，减少锁的获取次数
public void batchTransfer(List<Transfer> transfers) {
    // 按账户号排序所有涉及的账户
    // 一次性获取所有锁
    // 执行所有转账
    // 释放所有锁
}
```

---

## 🐛 已知限制

### 1. 转账原子性

**当前实现**: 转账操作在同步块内完成，确保原子性

**限制**: 如果系统崩溃，可能导致数据不一致（需要事务机制）

### 2. 审计日志

**当前实现**: 无操作日志记录

**建议**: 添加审计日志，记录所有账户操作

```java
public void deposit(BigDecimal amount) throws InvalidAmountException {
    // 验证...
    synchronized (lock) {
        BigDecimal oldBalance = balance;
        balance = balance.add(amount).setScale(2, RoundingMode.HALF_EVEN);
        // 记录日志
        auditLog.log("DEPOSIT", accountNumber, amount, oldBalance, balance);
    }
}
```

### 3. 并发性能

**限制**: 转账时需要锁定两个账户，可能成为性能瓶颈

**优化方向**: 使用乐观锁或版本号机制

---

## 🧪 测试建议

### 1. 单元测试用例

```java
@Test
public void testDeposit() throws Exception {
    BankAccount account = new BankAccount("ACC001", new BigDecimal("100.00"));
    account.deposit(new BigDecimal("50.00"));
    assertEquals(new BigDecimal("150.00"), account.getBalance());
}

@Test(expected = InvalidAmountException.class)
public void testDepositNegativeAmount() throws Exception {
    BankAccount account = new BankAccount("ACC001", new BigDecimal("100.00"));
    account.deposit(new BigDecimal("-50.00"));
}

@Test(expected = InsufficientFundsException.class)
public void testWithdrawInsufficientFunds() throws Exception {
    BankAccount account = new BankAccount("ACC001", new BigDecimal("100.00"));
    account.withdraw(new BigDecimal("200.00"));
}
```

### 2. 并发测试

```java
@Test
public void testConcurrentTransfer() throws Exception {
    BankAccount account1 = new BankAccount("ACC001", new BigDecimal("1000.00"));
    BankAccount account2 = new BankAccount("ACC002", new BigDecimal("1000.00"));
  
    // 创建100个线程同时转账
    // 验证总金额保持不变
    // 验证无死锁发生
}
```

---

## 📝 总结

`BankAccount` 类是一个设计良好的银行账户实现，具有以下优点：

✅ **精确性**: 使用BigDecimal和银行家舍入法则，确保金额计算精确
✅ **安全性**: 严格的参数验证，防止非法操作
✅ **线程安全**: volatile + synchronized + 有序锁定，确保多线程环境下的正确性
✅ **可维护性**: 清晰的代码结构，详细的注释文档
✅ **可扩展性**: 自定义异常，便于错误处理和扩展

**适用场景**: 银行系统、支付系统、电子钱包等需要精确金额计算和线程安全的场景。

---

**文档版本**: v1.0
**创建日期**: 2025-11-27
**作者**: 系统生成
