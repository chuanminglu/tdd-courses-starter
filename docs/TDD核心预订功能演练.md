# TDD核心预订功能演练：航班预订系统

## 📋 演练概述

### 演练目标

- 掌握TDD的Red-Green-Refactor完整循环
- 体验AI辅助的测试驱动开发过程
- 理解测试先行的设计思维
- 完成航班预订系统的核心预订功能

### 技术栈

- **语言**: Java 17
- **框架**: Spring Boot 3.x
- **测试框架**: JUnit 5 + Mockito
- **构建工具**: Maven
- **AI工具**: GitHub Copilot / Claude / ChatGPT

### 演练时长

**总计：90分钟**

- 环境准备：15分钟
- 第一阶段TDD开发：35分钟
- 第二阶段功能扩展：25分钟
- 总结回顾：15分钟

## 🎯 业务需求说明

**功能**：实现航班预订系统的核心预订功能

**业务规则**：

1. 单次预订最多9名乘客
2. 起飞前2小时停止预订
3. 需要验证乘客基本信息
4. 支持简单的价格计算

**技术约束**：

- 采用简单的Service层架构
- 使用Spring Boot框架
- 暂不涉及数据库和外部服务集成

## 🚀 第一阶段：需求分析与测试设计

### Step 1: AI辅助需求分析

**提示词模板**：

```
我需要实现一个航班预订系统的核心预订功能。

业务需求：
1. 用户可以预订航班座位
2. 需要提供乘客信息和联系方式
3. 支持信用卡支付
4. 预订成功后发送确认邮件

业务约束：
- 单次最多预订9人
- 起飞前2小时停止预订
- 支付超时30分钟自动取消

请帮我分析这个需求，识别关键的业务场景和边界条件，并设计测试用例结构。
```

**AI分析结果示例**：

```
基于需求分析，我识别出以下关键测试场景：

1. 正常流程测试
   - 单人预订成功
   - 多人预订成功
   - 不同舱位预订

2. 边界条件测试
   - 最大人数(9人)预订
   - 起飞前2小时01分预订
   - 支付倒计时最后1秒完成

3. 异常情况测试
   - 超过最大人数限制
   - 起飞前2小时内预订
   - 航班已满
   - 支付失败
   - 网络异常

4. 业务规则测试
   - 乘客信息验证
   - 重复预订检查
   - 价格计算正确性

建议的测试类结构：
- FlightBookingServiceTest (核心业务逻辑)
- BookingValidationTest (输入验证)
- PaymentProcessingTest (支付流程)
- NotificationServiceTest (通知功能)
```

### Step 2: 测试驱动的接口设计

#### 2.1 先写测试，定义接口

```java
/**
 * 第一个测试：定义最基本的预订接口
 * 这个测试会失败，因为还没有实现
 */
@Test
@DisplayName("单人预订航班成功")
public void shouldBookFlightSuccessfullyForSinglePassenger() {
    // Given: 准备预订数据
    List<PassengerInfo> passengers = Arrays.asList(
        createValidPassenger("张三")
    );
  
    BookingRequest request = BookingRequest.builder()
        .flightId("CA1234-20241225")
        .passengers(passengers)
        .contactInfo(createValidContact())
        .paymentInfo(createValidPayment())
        .build();
  
    // When: 执行预订
    BookingResult result = bookingService.bookFlight(request);
  
    // Then: 验证预订成功
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getBookingId()).isNotNull();
    assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    assertThat(result.getPassengerCount()).isEqualTo(1);
}

private PassengerInfo createValidPassenger(String name) {
    return PassengerInfo.builder()
        .name(name)
        .idType("身份证")
        .idNumber("110101199001011234")
        .phone("13800138000")
        .birthDate(LocalDate.of(1990, 1, 1))
        .build();
}

private ContactInfo createValidContact() {
    return ContactInfo.builder()
        .name("张三")
        .phone("13800138000")
        .email("zhangsan@example.com")
        .build();
}

private PaymentInfo createValidPayment() {
    return PaymentInfo.builder()
        .paymentMethod("信用卡")
        .cardNumber("4111111111111111")
        .cardHolderName("ZHANG SAN")
        .expiryDate("12/25")
        .cvv("123")
        .build();
}
```

#### 2.2 让AI生成最小接口定义

**AI提示词**：

```
基于上面的测试用例，帮我生成对应的接口和数据结构定义。
要求：
1. 只生成测试需要的最小接口
2. 使用Builder模式构建请求对象
3. 返回结果要包含必要的状态信息
```

**AI生成的接口**：

```java
// 预订服务接口
public interface FlightBookingService {
    BookingResult bookFlight(BookingRequest request);
}

// 预订请求对象
@Data
@Builder
public class BookingRequest {
    private String flightId;
    private List<PassengerInfo> passengers;
    private ContactInfo contactInfo;
    private PaymentInfo paymentInfo;
}

// 预订结果对象
@Data
@Builder
public class BookingResult {
    private boolean success;
    private String bookingId;
    private BookingStatus status;
    private int passengerCount;
    private String errorMessage;
    private String errorCode;
}

// 其他支持对象
@Data
@Builder
public class PassengerInfo {
    private String name;
    private String idType;
    private String idNumber;
    private String phone;
    private LocalDate birthDate;
}

@Data 
@Builder
public class ContactInfo {
    private String name;
    private String phone;
    private String email;
}

@Data
@Builder  
public class PaymentInfo {
    private String paymentMethod;
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
}

public enum BookingStatus {
    PENDING, CONFIRMED, FAILED, CANCELLED, TIMEOUT
}
```

### Step 3: 第一次红-绿-重构循环

#### 🔴 RED阶段：确认测试失败

```bash
# 运行测试，确认失败
mvn test -Dtest=FlightBookingServiceTest#shouldBookFlightSuccessfullyForSinglePassenger

# 预期输出：测试失败，因为还没有实现
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
[ERROR] shouldBookFlightSuccessfullyForSinglePassenger - CompilationFailureException: 
FlightBookingService class not found
```

#### 🟢 GREEN阶段：AI生成最小实现

**AI提示词**：

```
现在测试失败了，请为我生成一个最简单的实现，让测试通过。
要求：
1. 只关注让测试通过，不要过度设计
2. 可以使用硬编码的方式
3. 先不考虑数据库和外部依赖
```

**AI生成的最小实现**：

```java
@Service
public class FlightBookingServiceImpl implements FlightBookingService {
  
    @Override
    public BookingResult bookFlight(BookingRequest request) {
        // 最简实现：直接返回成功结果
        return BookingResult.builder()
            .success(true)
            .bookingId("BK" + System.currentTimeMillis())
            .status(BookingStatus.CONFIRMED)
            .passengerCount(request.getPassengers().size())
            .build();
    }
}
```

**运行测试确认通过**：

```bash
mvn test -Dtest=FlightBookingServiceTest#shouldBookFlightSuccessfullyForSinglePassenger
# 预期输出：测试通过 ✅
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

#### 🔵 REFACTOR阶段：暂时跳过

由于代码很简单，暂时不需要重构。

## 🚀 第二阶段：迭代增加功能

### Step 4: 添加输入验证测试

#### 4.1 增加边界条件测试

```java
@Test
@DisplayName("预订人数超过9人应该失败")
public void shouldFailWhenPassengerCountExceedsLimit() {
    // Given: 准备10人的预订请求
    List<PassengerInfo> passengers = IntStream.range(1, 11)
        .mapToObj(i -> createValidPassenger("乘客" + i))
        .collect(Collectors.toList());
  
    BookingRequest request = BookingRequest.builder()
        .flightId("CA1234-20241225")
        .passengers(passengers)
        .contactInfo(createValidContact())
        .paymentInfo(createValidPayment())
        .build();
  
    // When: 执行预订
    BookingResult result = bookingService.bookFlight(request);
  
    // Then: 应该失败
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorCode()).isEqualTo("EXCEED_MAX_PASSENGERS");
    assertThat(result.getErrorMessage()).contains("最多只能预订9名乘客");
}

@Test
@DisplayName("起飞前2小时内预订应该失败")
public void shouldFailWhenBookingTooCloseToDepature() {
    // Given: 航班在1小时后起飞
    String flightId = "CA1234-" + LocalDate.now().toString();
  
    BookingRequest request = BookingRequest.builder()
        .flightId(flightId)
        .passengers(Arrays.asList(createValidPassenger("张三")))
        .contactInfo(createValidContact())
        .paymentInfo(createValidPayment())
        .build();
  
    // Mock航班信息：1小时后起飞
    Flight flight = Flight.builder()
        .flightId(flightId)
        .departureTime(LocalDateTime.now().plusHours(1))
        .build();
  
    when(flightService.getFlightInfo(flightId)).thenReturn(flight);
  
    // When: 执行预订
    BookingResult result = bookingService.bookFlight(request);
  
    // Then: 应该失败
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorCode()).isEqualTo("BOOKING_TOO_LATE");
    assertThat(result.getErrorMessage()).contains("起飞前2小时停止预订");
}
```

#### 4.2 🔴 确认测试失败

```bash
mvn test -Dtest=FlightBookingServiceTest
# 预期：新增测试失败，因为当前实现没有验证逻辑
```

#### 4.3 🟢 AI生成验证逻辑

**AI提示词**：

```
现在需要添加业务规则验证：
1. 乘客人数验证，最多9人
2. 起飞时间验证，起飞前2小时停止预订
请修改bookFlight方法，添加这些验证逻辑。
要求：
1. 保持之前的测试仍然通过
2. 添加输入验证
3. 返回合适的错误信息和错误代码
```

**AI生成的改进实现**：

```java
@Service
public class FlightBookingServiceImpl implements FlightBookingService {
  
    private static final int MAX_PASSENGERS = 9;
    private static final int BOOKING_CUTOFF_HOURS = 2;
  
    private final FlightService flightService;
  
    public FlightBookingServiceImpl(FlightService flightService) {
        this.flightService = flightService;
    }
  
    @Override
    public BookingResult bookFlight(BookingRequest request) {
        // 输入验证
        ValidationResult validation = validateBookingRequest(request);
        if (!validation.isValid()) {
            return BookingResult.builder()
                .success(false)
                .errorCode(validation.getErrorCode())
                .errorMessage(validation.getErrorMessage())
                .build();
        }
    
        // 航班时间验证
        Flight flight = flightService.getFlightInfo(request.getFlightId());
        ValidationResult timeValidation = validateBookingTime(flight);
        if (!timeValidation.isValid()) {
            return BookingResult.builder()
                .success(false)
                .errorCode(timeValidation.getErrorCode())
                .errorMessage(timeValidation.getErrorMessage())
                .build();
        }
    
        // 预订成功
        return BookingResult.builder()
            .success(true)
            .bookingId("BK" + System.currentTimeMillis())
            .status(BookingStatus.CONFIRMED)
            .passengerCount(request.getPassengers().size())
            .build();
    }
  
    private ValidationResult validateBookingRequest(BookingRequest request) {
        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            return ValidationResult.invalid("MISSING_PASSENGERS", "至少需要一名乘客");
        }
    
        if (request.getPassengers().size() > MAX_PASSENGERS) {
            return ValidationResult.invalid("EXCEED_MAX_PASSENGERS", 
                "最多只能预订" + MAX_PASSENGERS + "名乘客");
        }
    
        return ValidationResult.valid();
    }
  
    private ValidationResult validateBookingTime(Flight flight) {
        if (flight == null) {
            return ValidationResult.invalid("FLIGHT_NOT_FOUND", "航班不存在");
        }
    
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = flight.getDepartureTime().minusHours(BOOKING_CUTOFF_HOURS);
    
        if (now.isAfter(cutoffTime)) {
            return ValidationResult.invalid("BOOKING_TOO_LATE", "起飞前2小时停止预订");
        }
    
        return ValidationResult.valid();
    }
}

// 验证结果辅助类
@Data
@AllArgsConstructor
public class ValidationResult {
    private boolean valid;
    private String errorCode;
    private String errorMessage;
  
    public static ValidationResult valid() {
        return new ValidationResult(true, null, null);
    }
  
    public static ValidationResult invalid(String errorCode, String errorMessage) {
        return new ValidationResult(false, errorCode, errorMessage);
    }
}

// 航班服务接口
public interface FlightService {
    Flight getFlightInfo(String flightId);
}

// 航班实体
@Data
@Builder
public class Flight {
    private String flightId;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int availableSeats;
    private BigDecimal basePrice;
}
```

#### 4.4 运行所有测试确认通过

```bash
mvn test -Dtest=FlightBookingServiceTest
# 预期：所有测试都通过 ✅
```

#### 4.5 🔵 重构阶段：提取验证器

**AI提示词**：

```
现在验证逻辑混在service里，请帮我重构，提取出独立的验证器类。
要求：
1. 创建BookingRequestValidator类
2. 使用策略模式，方便扩展验证规则
3. 保持所有测试通过
```

**AI生成的重构代码**：

```java
// 验证器接口
public interface BookingRequestValidator {
    ValidationResult validate(BookingRequest request);
}

// 组合验证器
@Component
public class CompositeBookingRequestValidator implements BookingRequestValidator {
  
    private final List<BookingRequestValidator> validators;
  
    public CompositeBookingRequestValidator() {
        this.validators = Arrays.asList(
            new PassengerCountValidator(),
            new PassengerInfoValidator(),
            new ContactInfoValidator(),
            new PaymentInfoValidator()
        );
    }
  
    @Override
    public ValidationResult validate(BookingRequest request) {
        for (BookingRequestValidator validator : validators) {
            ValidationResult result = validator.validate(request);
            if (!result.isValid()) {
                return result;
            }
        }
        return ValidationResult.valid();
    }
}

// 乘客人数验证器
public class PassengerCountValidator implements BookingRequestValidator {
    private static final int MAX_PASSENGERS = 9;
  
    @Override
    public ValidationResult validate(BookingRequest request) {
        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            return ValidationResult.invalid("MISSING_PASSENGERS", "至少需要一名乘客");
        }
    
        if (request.getPassengers().size() > MAX_PASSENGERS) {
            return ValidationResult.invalid("EXCEED_MAX_PASSENGERS", 
                "最多只能预订" + MAX_PASSENGERS + "名乘客");
        }
    
        return ValidationResult.valid();
    }
}

// 重构后的服务类
@Service
public class FlightBookingServiceImpl implements FlightBookingService {
  
    private final FlightService flightService;
    private final BookingRequestValidator validator;
  
    public FlightBookingServiceImpl(FlightService flightService, 
                                  BookingRequestValidator validator) {
        this.flightService = flightService;
        this.validator = validator;
    }
  
    @Override
    public BookingResult bookFlight(BookingRequest request) {
        // 输入验证
        ValidationResult validation = validator.validate(request);
        if (!validation.isValid()) {
            return createFailureResult(validation);
        }
    
        // 航班时间验证
        Flight flight = flightService.getFlightInfo(request.getFlightId());
        ValidationResult timeValidation = validateBookingTime(flight);
        if (!timeValidation.isValid()) {
            return createFailureResult(timeValidation);
        }
    
        // 预订成功
        return BookingResult.builder()
            .success(true)
            .bookingId("BK" + System.currentTimeMillis())
            .status(BookingStatus.CONFIRMED)
            .passengerCount(request.getPassengers().size())
            .build();
    }
  
    private BookingResult createFailureResult(ValidationResult validation) {
        return BookingResult.builder()
            .success(false)
            .errorCode(validation.getErrorCode())
            .errorMessage(validation.getErrorMessage())
            .build();
    }
  
    private ValidationResult validateBookingTime(Flight flight) {
        if (flight == null) {
            return ValidationResult.invalid("FLIGHT_NOT_FOUND", "航班不存在");
        }
    
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = flight.getDepartureTime().minusHours(2);
    
        if (now.isAfter(cutoffTime)) {
            return ValidationResult.invalid("BOOKING_TOO_LATE", "起飞前2小时停止预订");
        }
    
        return ValidationResult.valid();
    }
}
```

## 演练总结与收获

### 技能掌握检查清单

#### ✅ TDD核心技能

- [ ] 理解Red-Green-Refactor循环
- [ ] 能够编写失败的测试用例
- [ ] 能够实现最小可行代码
- [ ] 能够在测试保护下安全重构

#### ✅ AI辅助开发

- [ ] 掌握AI提示词的编写技巧
- [ ] 能够让AI生成测试用例
- [ ] 能够让AI生成实现代码
- [ ] 能够让AI提供重构建议

#### ✅ 业务理解

- [ ] 理解航班预订基本业务流程
- [ ] 掌握输入验证的重要性
- [ ] 理解业务规则的实现方式
- [ ] 掌握错误处理基本方式

### AI工具使用效果统计

| 开发环节       | 传统开发耗时      | AI辅助耗时       | 效率提升      |
| -------------- | ----------------- | ---------------- | ------------- |
| 测试用例设计   | 25分钟            | 8分钟            | 68%           |
| 接口定义       | 15分钟            | 5分钟            | 67%           |
| 最小实现       | 20分钟            | 7分钟            | 65%           |
| 业务逻辑完善   | 30分钟            | 12分钟           | 60%           |
| 重构优化       | 20分钟            | 8分钟            | 60%           |
| **总计** | **110分钟** | **40分钟** | **64%** |

### 关键收获总结

1. **TDD思维转换**：从"先写代码后写测试"转变为"测试驱动设计"
2. **AI赋能效果**：大幅提升开发效率，减少思考时间
3. **代码质量提升**：测试覆盖完整，重构信心充足
4. **业务理解深化**：通过测试用例更好地理解业务需求

## 🏠 实践作业

### 作业1：功能完善（必做）

基于现有代码，使用TDD方式完善以下基础功能：

1. **乘客信息验证增强**

   - 验证身份证号码格式
   - 验证手机号码格式
   - 验证乘客年龄限制（3-80岁）
2. **预订信息展示**

   - 生成预订确认信息
   - 计算并显示总价
   - 显示乘客列表

**要求**：

- 遵循完整的TDD循环（Red-Green-Refactor）
- 测试覆盖率达到90%以上
- 使用AI工具辅助开发
- 记录每次循环的过程和心得

### 作业2：边界条件测试（推荐）

为现有功能补充更完整的测试：

1. **异常场景测试**

   - 空输入处理
   - 非法参数处理
   - 边界值测试
2. **业务规则测试**

   - 各种组合场景
   - 时间边界测试
   - 数量限制测试

### 作业3：代码重构练习（可选）

使用AI工具帮助重构现有代码：

1. 提取更多的验证器类
2. 优化错误处理机制
3. 改善代码可读性
4. 添加适当的日志记录

---

**演练完成标志：基础预订功能完整实现，所有测试通过，代码清晰易懂** ✅
