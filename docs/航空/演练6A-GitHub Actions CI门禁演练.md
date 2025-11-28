# GitHub Actions CI门禁演练 - 基于银行转账项目

## 演练概述

本演练基于演练5的TDD银行转账项目，演示如何使用GitHub Actions构建完整的CI门禁流水线。通过自动化测试、代码质量检查、安全扫描等环节，确保代码质量和项目稳定性。

**演练目标**：

- 掌握GitHub Actions的基本语法和工作流配置
- 构建多阶段CI门禁流水线
- 集成代码质量检查工具
- 实现自动化测试和报告生成
- 配置门禁规则和分支保护策略
- 学习CI/CD最佳实践

**技术栈**：

- **CI平台**：GitHub Actions
- **项目基础**：Spring Boot + Maven + JUnit 5
- **质量工具**：JaCoCo、SpotBugs、Checkstyle、SonarQube
- **安全扫描**：CodeQL、Dependency Check
- **容器化**：Docker (可选)

**时间安排**：45-60分钟

---

## 项目准备

### 前置条件

1. 完成演练5的TDD银行转账项目
2. 项目已推送到GitHub仓库
3. 具备GitHub仓库的管理员权限

### 项目结构

```
bank-transfer-project/
├── .github/
│   └── workflows/
│       ├── ci.yml                 # 主CI流水线
│       ├── quality-gate.yml       # 质量门禁
│       └── security-scan.yml      # 安全扫描
├── src/
│   ├── main/java/
│   │   └── com/example/
│   │       ├── Account.java
│   │       ├── InvalidAmountException.java
│   │       └── InsufficientFundsException.java
│   └── test/java/
│       └── com/example/
│           └── BankTransferTest.java
├── pom.xml
├── README.md
└── .gitignore
```

---

## 阶段一：基础CI流水线配置

### 1.1 创建主CI工作流

创建 `.github/workflows/ci.yml`：

```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

env:
  JAVA_VERSION: '17'
  MAVEN_OPTS: '-Xmx1024m'

jobs:
  # 阶段1：基础验证
  validation:
    name: 代码验证
    runs-on: ubuntu-latest
    steps:
      - name: 检出代码
        uses: actions/checkout@v4
        with:
          fetch-depth: 0  # 获取完整历史，用于SonarQube分析

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: 验证Maven配置
        run: |
          mvn help:effective-pom
          mvn dependency:resolve

      - name: 编译检查
        run: mvn clean compile

  # 阶段2：单元测试
  unit-tests:
    name: 单元测试
    runs-on: ubuntu-latest
    needs: validation
    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: 运行单元测试
        run: |
          mvn clean test \
            -Dmaven.test.failure.ignore=false \
            -Djacoco.destFile=target/jacoco.exec

      - name: 生成测试报告
        run: mvn surefire-report:report

      - name: 上传测试结果
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: |
            target/surefire-reports/
            target/site/surefire-report.html

      - name: 发布测试结果
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: Maven Tests
          path: target/surefire-reports/*.xml
          reporter: java-junit

  # 阶段3：代码覆盖率
  code-coverage:
    name: 代码覆盖率
    runs-on: ubuntu-latest
    needs: unit-tests
    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: 生成覆盖率报告
        run: |
          mvn clean test jacoco:report

      - name: 检查覆盖率阈值
        run: |
          mvn jacoco:check \
            -Djacoco.haltOnFailure=true

      - name: 上传覆盖率到Codecov
        uses: codecov/codecov-action@v3
        with:
          file: target/site/jacoco/jacoco.xml
          flags: unittests
          name: codecov-umbrella

      - name: 覆盖率评论
        uses: madrapps/jacoco-report@v1.6.1
        with:
          paths: target/site/jacoco/jacoco.xml
          token: ${{ secrets.GITHUB_TOKEN }}
          min-coverage-overall: 80
          min-coverage-changed-files: 80

  # 阶段4：集成测试
  integration-tests:
    name: 集成测试
    runs-on: ubuntu-latest
    needs: code-coverage
    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: 运行集成测试
        run: |
          mvn clean verify \
            -Dspring.profiles.active=test \
            -Dtest.groups=integration

      - name: 上传集成测试结果
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: integration-test-results
          path: target/failsafe-reports/
```

### 1.2 配置Maven插件

更新 `pom.xml` 添加必要的插件：

```xml
<project>
    <!-- 现有配置... -->
  
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <junit.version>5.10.1</junit.version>
        <jacoco.version>0.8.8</jacoco.version>
        <surefire.version>3.0.0-M9</surefire.version>
    </properties>

    <build>
        <plugins>
            <!-- Surefire插件 - 单元测试 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>${surefire.version}</version>
                <configuration>
                    <includes>
                        <include>**/*Test.java</include>
                        <include>**/*Tests.java</include>
                    </includes>
                    <reportFormat>xml</reportFormat>
                    <systemPropertyVariables>
                        <jacoco.destFile>${project.build.directory}/jacoco.exec</jacoco.destFile>
                    </systemPropertyVariables>
                </configuration>
            </plugin>

            <!-- JaCoCo插件 - 代码覆盖率 -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>check</id>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>BUNDLE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>0.80</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- Failsafe插件 - 集成测试 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>${surefire.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 阶段二：代码质量门禁

### 2.1 创建质量门禁工作流

创建 `.github/workflows/quality-gate.yml`：

```yaml
name: Quality Gate

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  # 代码风格检查
  checkstyle:
    name: 代码风格检查
    runs-on: ubuntu-latest
    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: 运行Checkstyle
        run: mvn checkstyle:check

      - name: 上传Checkstyle报告
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: checkstyle-report
          path: target/checkstyle-result.xml

  # 静态代码分析
  spotbugs:
    name: 静态代码分析
    runs-on: ubuntu-latest
    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: 运行SpotBugs
        run: |
          mvn clean compile
          mvn spotbugs:check

      - name: 上传SpotBugs报告
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: spotbugs-report
          path: target/spotbugsXml.xml

  # PMD代码质量检查
  pmd:
    name: PMD代码质量检查
    runs-on: ubuntu-latest
    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: 运行PMD
        run: mvn pmd:check

      - name: 上传PMD报告
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: pmd-report
          path: target/pmd.xml

  # SonarQube分析
  sonarqube:
    name: SonarQube分析
    runs-on: ubuntu-latest
    if: github.event_name == 'push' || github.event.pull_request.head.repo.full_name == github.repository
    steps:
      - name: 检出代码
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: 缓存SonarQube包
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar

      - name: 运行测试和SonarQube分析
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          mvn clean verify sonar:sonar \
            -Dsonar.projectKey=bank-transfer-project \
            -Dsonar.organization=your-org \
            -Dsonar.host.url=https://sonarcloud.io \
            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml

  # 质量门禁汇总
  quality-gate:
    name: 质量门禁汇总
    runs-on: ubuntu-latest
    needs: [checkstyle, spotbugs, pmd, sonarqube]
    if: always()
    steps:
      - name: 检查质量门禁状态
        run: |
          echo "质量检查结果："
          echo "Checkstyle: ${{ needs.checkstyle.result }}"
          echo "SpotBugs: ${{ needs.spotbugs.result }}"
          echo "PMD: ${{ needs.pmd.result }}"
          echo "SonarQube: ${{ needs.sonarqube.result }}"
        
          if [[ "${{ needs.checkstyle.result }}" != "success" ]] || \
             [[ "${{ needs.spotbugs.result }}" != "success" ]] || \
             [[ "${{ needs.pmd.result }}" != "success" ]]; then
            echo "❌ 质量门禁检查失败！"
            exit 1
          else
            echo "✅ 质量门禁检查通过！"
          fi
```

### 2.2 配置质量检查插件

在 `pom.xml` 中添加质量检查插件：

```xml
<build>
    <plugins>
        <!-- 现有插件... -->
      
        <!-- Checkstyle插件 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.2.0</version>
            <configuration>
                <configLocation>checkstyle.xml</configLocation>
                <includeTestSourceDirectory>true</includeTestSourceDirectory>
                <failOnViolation>true</failOnViolation>
                <violationSeverity>warning</violationSeverity>
            </configuration>
            <executions>
                <execution>
                    <id>validate</id>
                    <phase>validate</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- SpotBugs插件 -->
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.7.3.6</version>
            <configuration>
                <effort>Max</effort>
                <threshold>Medium</threshold>
                <failOnError>true</failOnError>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- PMD插件 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <version>3.19.0</version>
            <configuration>
                <failOnViolation>true</failOnViolation>
                <rulesets>
                    <ruleset>/category/java/bestpractices.xml</ruleset>
                    <ruleset>/category/java/codestyle.xml</ruleset>
                    <ruleset>/category/java/design.xml</ruleset>
                    <ruleset>/category/java/errorprone.xml</ruleset>
                    <ruleset>/category/java/performance.xml</ruleset>
                    <ruleset>/category/java/security.xml</ruleset>
                </rulesets>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- SonarQube插件 -->
        <plugin>
            <groupId>org.sonarsource.scanner.maven</groupId>
            <artifactId>sonar-maven-plugin</artifactId>
            <version>3.9.1.2184</version>
        </plugin>
    </plugins>
</build>
```

---

## 阶段三：安全扫描

### 3.1 创建安全扫描工作流

创建 `.github/workflows/security-scan.yml`：

```yaml
name: Security Scan

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 2 * * 1'  # 每周一凌晨2点运行

jobs:
  # CodeQL代码安全分析
  codeql:
    name: CodeQL安全分析
    runs-on: ubuntu-latest
    permissions:
      actions: read
      contents: read
      security-events: write
    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 初始化CodeQL
        uses: github/codeql-action/init@v2
        with:
          languages: java

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: 编译项目
        run: mvn clean compile

      - name: 执行CodeQL分析
        uses: github/codeql-action/analyze@v2

  # 依赖漏洞扫描
  dependency-check:
    name: 依赖漏洞扫描
    runs-on: ubuntu-latest
    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 设置Java环境
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: 运行依赖检查
        run: |
          mvn org.owasp:dependency-check-maven:check \
            -DfailBuildOnCVSS=7

      - name: 上传依赖检查报告
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: dependency-check-report
          path: target/dependency-check-report.html

  # Snyk安全扫描
  snyk:
    name: Snyk安全扫描
    runs-on: ubuntu-latest
    if: github.event_name != 'schedule'
    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 运行Snyk扫描
        uses: snyk/actions/maven@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        with:
          args: --severity-threshold=medium

      - name: 上传Snyk结果到GitHub
        uses: github/codeql-action/upload-sarif@v2
        if: always()
        with:
          sarif_file: snyk.sarif

  # 密钥扫描
  secret-scan:
    name: 密钥扫描
    runs-on: ubuntu-latest
    steps:
      - name: 检出代码
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: 运行GitLeaks
        uses: gitleaks/gitleaks-action@v2
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## 阶段四：完整CI门禁配置

### 4.1 创建完整的门禁工作流

创建 `.github/workflows/gate.yml`：

```yaml
name: Quality Gate

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  # 第一道门：基础检查
  basic-checks:
    name: 基础检查
    uses: ./.github/workflows/ci.yml

  # 第二道门：质量检查
  quality-checks:
    name: 质量检查
    needs: basic-checks
    uses: ./.github/workflows/quality-gate.yml

  # 第三道门：安全检查
  security-checks:
    name: 安全检查
    needs: quality-checks
    uses: ./.github/workflows/security-scan.yml

  # 最终门禁决策
  final-gate:
    name: 最终门禁
    runs-on: ubuntu-latest
    needs: [basic-checks, quality-checks, security-checks]
    if: always()
    steps:
      - name: 评估门禁结果
        run: |
          echo "🚦 CI门禁结果评估"
          echo "==================="
          echo "基础检查: ${{ needs.basic-checks.result }}"
          echo "质量检查: ${{ needs.quality-checks.result }}"
          echo "安全检查: ${{ needs.security-checks.result }}"
        
          # 计算通过率
          total=3
          passed=0
        
          [[ "${{ needs.basic-checks.result }}" == "success" ]] && ((passed++))
          [[ "${{ needs.quality-checks.result }}" == "success" ]] && ((passed++))
          [[ "${{ needs.security-checks.result }}" == "success" ]] && ((passed++))
        
          pass_rate=$((passed * 100 / total))
          echo "通过率: ${pass_rate}%"
        
          if [[ $pass_rate -ge 100 ]]; then
            echo "🟢 门禁通过！代码可以合并"
            echo "gate-status=passed" >> $GITHUB_OUTPUT
          elif [[ $pass_rate -ge 67 ]]; then
            echo "🟡 门禁警告！建议修复问题后合并"
            echo "gate-status=warning" >> $GITHUB_OUTPUT
          else
            echo "🔴 门禁失败！禁止合并"
            echo "gate-status=failed" >> $GITHUB_OUTPUT
            exit 1
          fi

      - name: 创建门禁报告
        if: always()
        run: |
          cat > gate-report.md << EOF
          # 🚦 CI门禁报告
        
          ## 检查结果
        
          | 检查项目 | 状态 | 结果 |
          |---------|------|------|
          | 基础检查 | ${{ needs.basic-checks.result }} | ${{ needs.basic-checks.result == 'success' && '✅' || '❌' }} |
          | 质量检查 | ${{ needs.quality-checks.result }} | ${{ needs.quality-checks.result == 'success' && '✅' || '❌' }} |
          | 安全检查 | ${{ needs.security-checks.result }} | ${{ needs.security-checks.result == 'success' && '✅' || '❌' }} |
        
          ## 建议
        
          ${{ steps.evaluate.outputs.gate-status == 'passed' && '🎉 所有检查通过，代码质量优秀！' || '' }}
          ${{ steps.evaluate.outputs.gate-status == 'warning' && '⚠️ 存在一些问题，建议修复后合并。' || '' }}
          ${{ steps.evaluate.outputs.gate-status == 'failed' && '🚫 存在严重问题，禁止合并！' || '' }}
          EOF

      - name: 评论PR
        if: github.event_name == 'pull_request'
        uses: actions/github-script@v6
        with:
          script: |
            const fs = require('fs');
            const report = fs.readFileSync('gate-report.md', 'utf8');
          
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: report
            });
```

### 4.2 配置分支保护规则

在GitHub仓库设置中配置分支保护：

```yaml
# 通过GitHub CLI配置分支保护
gh api repos/:owner/:repo/branches/main/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"contexts":["CI Pipeline","Quality Gate","Security Scan"]}' \
  --field enforce_admins=true \
  --field required_pull_request_reviews='{"required_approving_review_count":1,"dismiss_stale_reviews":true}' \
  --field restrictions=null
```

---

## 阶段五：监控和通知

### 5.1 配置失败通知

添加失败通知到主工作流：

```yaml
  # 添加到ci.yml的最后
  notify-failure:
    name: 失败通知
    runs-on: ubuntu-latest
    needs: [validation, unit-tests, code-coverage, integration-tests]
    if: failure()
    steps:
      - name: 发送Slack通知
        uses: 8398a7/action-slack@v3
        with:
          status: failure
          channel: '#ci-alerts'
          fields: repo,message,commit,author,action,eventName,ref,workflow
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}

      - name: 发送邮件通知
        uses: dawidd6/action-send-mail@v3
        with:
          server_address: smtp.gmail.com
          server_port: 587
          username: ${{ secrets.EMAIL_USERNAME }}
          password: ${{ secrets.EMAIL_PASSWORD }}
          subject: "CI Pipeline Failed: ${{ github.repository }}"
          body: |
            CI Pipeline failed for commit ${{ github.sha }}
          
            Repository: ${{ github.repository }}
            Branch: ${{ github.ref }}
            Author: ${{ github.actor }}
          
            Please check the logs: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}
          to: team@example.com
          from: ci@example.com
```

### 5.2 配置成功度量

添加成功度量收集：

```yaml
  # 添加到gate.yml的最后
  collect-metrics:
    name: 收集度量
    runs-on: ubuntu-latest
    needs: final-gate
    if: always()
    steps:
      - name: 收集CI度量
        run: |
          echo "收集CI流水线度量数据..."
        
          # 计算构建时间
          start_time="${{ github.event.head_commit.timestamp }}"
          end_time=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
        
          # 发送到度量系统
          curl -X POST "https://metrics.example.com/api/ci-metrics" \
            -H "Content-Type: application/json" \
            -d '{
              "repository": "${{ github.repository }}",
              "branch": "${{ github.ref }}",
              "commit": "${{ github.sha }}",
              "build_status": "${{ job.status }}",
              "start_time": "'$start_time'",
              "end_time": "'$end_time'",
              "tests_passed": "${{ needs.basic-checks.outputs.tests-passed }}",
              "coverage_percent": "${{ needs.basic-checks.outputs.coverage-percent }}"
            }'
```

---

## 最佳实践和优化

### 6.1 缓存优化

```yaml
      # Maven依赖缓存
      - name: 缓存Maven依赖
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2

      # SonarQube缓存
      - name: 缓存SonarQube
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar
```

### 6.2 并行执行优化

```yaml
  # 使用矩阵策略并行测试
  test-matrix:
    name: 矩阵测试
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java-version: [11, 17, 21]
        test-group: [unit, integration, performance]
    steps:
      - name: 测试 Java ${{ matrix.java-version }} - ${{ matrix.test-group }}
        run: |
          mvn test -Dgroups=${{ matrix.test-group }}
```

### 6.3 环境隔离

```yaml
  # 使用容器进行环境隔离
  test-in-container:
    name: 容器化测试
    runs-on: ubuntu-latest
    container:
      image: openjdk:17-jdk-slim
      env:
        MAVEN_OPTS: -Xmx1024m
    services:
      postgres:
        image: postgres:13
        env:
          POSTGRES_PASSWORD: testpass
```

---

## 演练总结

### 实施检查清单

- [ ] 创建基础CI工作流 (.github/workflows/ci.yml)
- [ ] 配置质量门禁工作流 (.github/workflows/quality-gate.yml)
- [ ] 设置安全扫描工作流 (.github/workflows/security-scan.yml)
- [ ] 配置完整门禁工作流 (.github/workflows/gate.yml)
- [ ] 更新Maven配置 (pom.xml)
- [ ] 设置分支保护规则
- [ ] 配置必要的Secrets
- [ ] 测试工作流执行
- [ ] 配置通知机制
- [ ] 优化性能和缓存

### 关键收获

1. **多阶段门禁**：基础检查 → 质量检查 → 安全检查 → 最终决策
2. **自动化质量保证**：代码覆盖率、静态分析、安全扫描
3. **失败快速反馈**：早期发现问题，快速失败
4. **可观测性**：完整的报告和度量收集
5. **团队协作**：PR评论、通知机制

### 进阶优化方向

1. **性能优化**：缓存策略、并行执行、增量构建
2. **环境管理**：多环境部署、容器化、基础设施即代码
3. **高级安全**：签名验证、供应链安全、合规检查
4. **智能化**：AI辅助代码审查、自动修复建议
5. **度量分析**：质量趋势、性能基准、团队效率分析

通过这个演练，你将掌握构建企业级CI门禁流水线的完整技能，确保代码质量和项目稳定性。
