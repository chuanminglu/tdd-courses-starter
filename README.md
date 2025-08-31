# AI辅助TDD银行转账演练 - 基础项目

## 项目介绍

这是一个用于AI辅助测试驱动开发(TDD)演练的基础项目，基于Spring Boot框架。通过银行账户转账功能的实现，学习如何结合AI工具进行高效的TDD开发，支持从基础业务逻辑到Web API接口的全栈开发演练。

## 快速开始

### 前置要求

- JDK 17 或更高版本
- Maven 3.8 或更高版本
- IDE (推荐 IntelliJ IDEA)
- AI编程助手 (GitHub Copilot、Cursor 等)

### 项目结构

```
courses_starter/
├── docs/                           # 文档目录
│   └── AI辅助TDD银行转账演练.md      # 演练指南
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/        # 主代码包
│   │   │       ├── BankingApplication.java  # Spring Boot 启动类
│   │   │       └── controller/     # Web控制器层
│   │   └── resources/
│   │       └── application.yml     # Spring Boot 配置
│   └── test/
│       ├── java/                   # 测试代码
│       └── resources/
│           └── application-test.yml # 测试环境配置
├── pom.xml                         # Maven配置文件
└── README.md                       # 项目说明
```

### 环境配置

1. **克隆项目**
   ```bash
   # 进入项目目录
   cd courses_starter
   ```

2. **验证环境**
   ```bash
   # 检查Java版本
   java -version
   
   # 检查Maven版本
   mvn -version
   ```

3. **安装依赖**
   ```bash
   mvn clean compile
   ```

4. **运行测试**
   ```bash
   mvn test
   ```

5. **启动应用**
   ```bash
   # 启动Spring Boot应用
   mvn spring-boot:run
   
   # 访问健康检查接口
   curl http://localhost:8080/health
   
   # 访问H2数据库控制台 (开发环境)
   http://localhost:8080/h2-console
   ```

## 技术栈

### 核心框架
- **Spring Boot**: 3.2.1 (应用框架)
- **Spring Web**: RESTful API开发
- **Spring Data JPA**: 数据持久层
- **Spring Boot Validation**: 数据验证

### 开发环境
- **JDK**: 17+ (推荐使用LTS版本)
- **构建工具**: Maven 3.8+
- **IDE**: IntelliJ IDEA (推荐) / Eclipse / VS Code
- **AI编程助手**: GitHub Copilot / Cursor

### 数据库
- **H2 Database**: 内存数据库 (开发和测试)
- **JPA/Hibernate**: ORM框架

### 测试框架
- **Spring Boot Test**: 集成测试支持
- **JUnit 5**: 单元测试框架
- **AssertJ**: 流畅断言API
- **Mockito**: Mock对象框架
- **MockMvc**: Web层测试
- **TestContainers**: 集成测试容器

### 代码质量工具
- **代码覆盖率**: JaCoCo
- **静态代码分析**: SpotBugs
- **代码规范检查**: Checkstyle
- **构建报告**: Maven Site Plugin

## 演练指南

详细的演练步骤请参考 [AI辅助TDD银行转账演练文档](docs/AI辅助TDD银行转账演练.md)

演练包含四个阶段：
1. **步骤一**: AI辅助生成测试用例文档
2. **步骤二**: Red阶段 - 编写失败的测试  
3. **步骤三**: Green阶段 - 实现最简代码使测试通过
4. **步骤四**: Blue阶段 - AI辅助代码重构和优化

## 开发演练路径

### 🎯 基础业务逻辑 (核心TDD)
- **Account**: 银行账户实体和业务逻辑
- **TransferService**: 转账服务和业务规则
- **单元测试**: 业务逻辑的TDD实现

### 🌐 Web API接口 (全栈TDD)  
- **REST Controller**: 转账API接口
- **DTO设计**: 请求响应数据传输对象
- **接口测试**: Web层的TDD实现
- **集成测试**: 端到端功能验证

### 🗄️ 数据持久化 (数据层TDD)
- **JPA Entity**: 数据实体设计
- **Repository**: 数据访问层
- **数据库测试**: 持久层的TDD实现

## 常用命令

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 启动Spring Boot应用
mvn spring-boot:run

# 生成代码覆盖率报告
mvn jacoco:report

# 打包应用
mvn package

# 运行静态代码分析
mvn spotbugs:check

# 检查代码规范
mvn checkstyle:check

# 生成项目站点报告
mvn site

# 清理项目
mvn clean
```

## 演练目标

### 🎯 **核心技能**
- 掌握AI辅助TDD的四个核心步骤
- 理解Red-Green-Refactor的TDD循环
- 学会利用AI生成高质量的测试用例
- 体验AI在代码重构中的价值

### 💻 **技术技能**
- **Spring Boot**: 现代Java Web开发框架
- **RESTful API**: 设计和实现Web API接口
- **JPA/Hibernate**: 数据持久化和ORM
- **单元测试**: JUnit 5和AssertJ的使用
- **集成测试**: Spring Boot Test和MockMvc
- **金融业务**: BigDecimal精确计算

### 🏗️ **架构技能**
- **分层架构**: Controller-Service-Repository模式
- **数据传输对象**: DTO设计和使用
- **异常处理**: 统一异常处理机制
- **数据验证**: Spring Validation的应用
- **线程安全**: 并发编程的基本概念

### 🔧 **工程技能**
- **代码质量**: 静态分析和代码规范
- **测试覆盖率**: JaCoCo代码覆盖分析
- **重构思维**: 持续改进代码质量
- **AI协作**: 高效利用AI编程助手

## 学习资源

### 📚 **核心框架文档**
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Boot 测试指南](https://spring.io/guides/gs/testing-web/)
- [Spring Data JPA 文档](https://spring.io/projects/spring-data-jpa)
- [H2 Database 文档](http://www.h2database.com/html/main.html)

### 🧪 **测试框架资源**  
- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ 文档](https://assertj.github.io/doc/)
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [TestContainers 文档](https://www.testcontainers.org/)
- [Spring Boot Test 参考](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing)

### 🔧 **开发工具资源**
- [JaCoCo 文档](https://www.jacoco.org/jacoco/trunk/doc/)
- [Maven Spring Boot 插件](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/html/)
- [IntelliJ IDEA Spring Boot 支持](https://www.jetbrains.com/help/idea/spring-boot.html)

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request来改进这个演练项目！
