package main.java;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 银行账户类 - 支持基本的账户操作和转账功能
 * 
 * 业务规则：
 * 1. 账户号不能为空，且创建后不可修改
 * 2. 账户余额使用BigDecimal确保精度，保留2位小数
 * 3. 初始余额不能为负数
 * 4. 支持线程安全的转账操作
 * 5. 所有金额计算采用银行家舍入法则
 * 
 * 异常处理：
 * - InvalidAmountException: 转账金额无效（≤0）
 * - InsufficientFundsException: 余额不足
 * - IllegalArgumentException: 参数验证失败
 */
public class BankAccount {
    private final String accountNumber;
    private volatile BigDecimal balance;
    private final Object lock = new Object();
    
    /**
     * 构造函数 - 创建银行账户
     * @param accountNumber 账户号，不能为null或空字符串
     * @param initialBalance 初始余额，不能为负数
     * @throws IllegalArgumentException 如果参数无效
     */
    public BankAccount(String accountNumber, BigDecimal initialBalance) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("账户号不能为空");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("初始余额不能为负数");
        }
        
        this.accountNumber = accountNumber;
        this.balance = initialBalance.setScale(2, RoundingMode.HALF_EVEN);
    }
    
    /**
     * 构造函数 - 创建零余额账户
     * @param accountNumber 账户号
     */
    public BankAccount(String accountNumber) {
        this(accountNumber, BigDecimal.ZERO);
    }
    
    /**
     * 获取账户号
     * @return 账户号
     */
    public String getAccountNumber() {
        return accountNumber;
    }
    
    /**
     * 获取账户余额
     * @return 当前余额
     */
    public BigDecimal getBalance() {
        synchronized (lock) {
            return balance.setScale(2, RoundingMode.HALF_EVEN);
        }
    }
    
    /**
     * 存款操作
     * @param amount 存款金额
     * @throws InvalidAmountException 如果金额无效
     */
    public void deposit(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("存款金额必须大于0");
        }
        
        synchronized (lock) {
            balance = balance.add(amount).setScale(2, RoundingMode.HALF_EVEN);
        }
   
    
    /**
     * 取款操作
     * @param amount 取款金额
     * @throws InvalidAmountException 如果金额无效
     * @throws InsufficientFundsException 如果余额不足
     */
    public void withdraw(BigDecimal amount) throws InvalidAmountException, InsufficientFundsException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("取款金额必须大于0");
        }
        
        synchronized (lock) {
            if (balance.compareTo(amount) < 0) {
                throw new InsufficientFundsException("余额不足，当前余额: " + balance + "，取款金额: " + amount);
            }
            balance = balance.subtract(amount).setScale(2, RoundingMode.HALF_EVEN);
        }
    }
    
    /**
     * 转账到其他账户
     * @param targetAccount 目标账户
     * @param amount 转账金额
     * @throws InvalidAmountException 如果金额无效
     * @throws InsufficientFundsException 如果余额不足
     * @throws IllegalArgumentException 如果目标账户无效
     */
    public void transferTo(BankAccount targetAccount, BigDecimal amount) 
            throws InvalidAmountException, InsufficientFundsException {
        if (targetAccount == null) {
            throw new IllegalArgumentException("目标账户不能为null");
        }
        if (targetAccount == this) {
            throw new IllegalArgumentException("不能转账到自己的账户");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("转账金额必须大于0");
        }
        
        // 使用有序锁定避免死锁
        BankAccount firstLock = this.accountNumber.compareTo(targetAccount.accountNumber) < 0 ? this : targetAccount;
        BankAccount secondLock = this.accountNumber.compareTo(targetAccount.accountNumber) < 0 ? targetAccount : this;
        
        synchronized (firstLock.lock) {
            synchronized (secondLock.lock) {
                if (this.balance.compareTo(amount) < 0) {
                    throw new InsufficientFundsException("余额不足，当前余额: " + this.balance + "，转账金额: " + amount);
                }
                
                this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_EVEN);
                targetAccount.balance = targetAccount.balance.add(amount).setScale(2, RoundingMode.HALF_EVEN);
            }
        }
    }
    
    @Override
    public String toString() {
        return "BankAccount{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + getBalance() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BankAccount that = (BankAccount) obj;
        return accountNumber.equals(that.accountNumber);
    }
    
    @Override
    public int hashCode() {
        return accountNumber.hashCode();
    }
    
    /**
     * 自定义异常 - 无效金额异常
     */
    public static class InvalidAmountException extends Exception {
        public InvalidAmountException(String message) {
            super(message);
        }
    }
    
    /**
     * 自定义异常 - 余额不足异常
     */
    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}  
