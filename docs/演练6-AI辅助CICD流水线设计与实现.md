# 演练6 - AI辅助CI流水线设计与实现

## 🎯 演练目标

### 学习目标

- 掌握使用AI工具设计完整的CI流水线
- 学会AI辅助的持续集成配置生成
- 理解代码质量检查和自动化测试

### 技能要求

- 基础的Docker容器知识
- 了解Git工作流和代码管理
- 熟悉常见的CI工具(Jenkins、GitLab CI、GitHub Actions等)
- 具备基本的代码质量检查工具使用经验

## 🛠️ 演练场景设定

### 业务背景

基于演练的**航空货运管理系统**，需要设计完整的CI流水线，实现从代码提交到构建打包的全自动化流程。

### 技术要求

```yaml
应用架构:
  - 前端: React + TypeScript
  - 后端: Spring Boot + Java
  - 数据库: PostgreSQL + Redis
  - 消息队列: Apache Kafka

构建环境:
  - 开发环境: Docker Compose本地构建
  - 测试环境: Docker容器化打包

质量要求:
  - 代码覆盖率: >80%
  - 安全扫描: 通过SAST检查
  - 构建时间: <10分钟
  - 代码质量: 通过SonarQube检查
```

## 📋 演练任务清单

### 阶段1：CI流水线架构设计

#### 任务1.1：AI辅助流水线设计(必做)

**使用AI工具**：ChatGPT/Claude + Cursor/GitHub Copilot
**具体任务**：

```markdown
1. 向AI描述项目需求和技术栈
2. 生成完整的CI流水线架构图
3. 确定各阶段的工具选型和配置
4. 设计分支策略和构建策略
```

**AI提示词示例**：

```
我需要为一个航空货运管理系统设计CI流水线：
- 前端：React + TypeScript
- 后端：Spring Boot + Java
- 数据库：PostgreSQL + Redis
- 消息队列：Apache Kafka
- 构建：Docker容器化

请帮我设计：
1. 完整的CI流水线架构
2. 各阶段的具体步骤
3. 工具选型建议
4. 配置文件模板
```

#### 任务1.2：分支策略设计

**设计要求**：

- Git Flow或GitHub Flow策略选择
- 分支保护规则配置
- Code Review流程设计
- 构建触发条件规划

### 阶段2：代码质量检查实现

#### 任务2.1：静态代码分析配置(必做)

**AI辅助生成配置**：

```yaml
静态代码分析:
  - Java: SpotBugs
  - TypeScript: ESLint + Prettier

代码规范检查:
  - 代码风格统一
  - 代码复杂度检查
  - 重复代码检测
  - 安全漏洞扫描
```

#### 任务2.2：自动化测试集成

**测试配置**：

```yaml
单元测试:
  - 后端: JUnit 5 + Mockito
  - 前端: Jest + React Testing Library
  - 覆盖率: JaCoCo + Istanbul

集成测试:
  - API测试: RestAssured
  - 数据库测试: TestContainers
  - 组件测试: 前端组件测试
```

### 阶段3：容器化构建实现

#### 任务3.1：Dockerfile优化

**多阶段构建配置**：

```dockerfile
# 示例：AI辅助生成的多阶段构建Dockerfile
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci --production=false
COPY frontend/ .
RUN npm run build

FROM openjdk:17-jdk-slim AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml .
COPY backend/src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jre-slim
COPY --from=backend-build /app/backend/target/*.jar app.jar
COPY --from=frontend-build /app/frontend/dist /static
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### 任务3.2：CI配置文件编写

**GitHub Actions配置示例**：

```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
  
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
  
    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
  
    - name: Run tests
      run: |
        # 后端测试
        cd backend && ./mvnw test
        # 前端测试
        cd frontend && npm test
  
    - name: Build Docker image
      run: docker build -t cargo-system:${{ github.sha }} .
```

### AI提示词模板

#### CI流水线设计提示词

```
角色：你是一位资深的DevOps工程师
任务：设计CI流水线
项目：[项目描述]
技术栈：[具体技术栈]
要求：
1. 流水线各阶段设计
2. 工具选型建议
3. 配置文件示例
4. 最佳实践建议
5. 常见问题和解决方案
```

#### 配置生成提示词

```
请为以下需求生成完整的CI配置文件：
平台：GitHub Actions / GitLab CI
应用：Java Spring Boot + React前端
需求：
- 代码质量检查
- 单元测试和集成测试
- Docker镜像构建
- 安全扫描
要求：包含最佳实践和错误处理
```

## 📊 成果交付标准

### 必须交付物

1. **CI流水线架构设计图** - 完整的流水线架构和流程图
2. **配置文件集合** - 所有必要的配置文件(YAML、Dockerfile等)
3. **质量检查配置** - 代码质量和安全检查配置
4. **测试配置** - 单元测试和集成测试配置
5. **构建脚本** - Docker构建和镜像管理脚本

## 💡 最佳实践建议

### CI设计原则

1. **自动化优先** - 尽可能减少人工干预
2. **快速反馈** - 及时发现和解决问题
3. **可重复性** - 相同输入产生相同输出
4. **可观测性** - 全程监控和日志记录
5. **安全第一** - 集成安全检查和验证
