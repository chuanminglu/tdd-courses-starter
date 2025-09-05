# Jenkins CI门禁演练 - 基于银行转账项目

## 演练概述

本演练基于演练5的TDD银行转账项目，演示如何使用Jenkins构建企业级CI门禁流水线。通过Pipeline as Code、多阶段质量检查、自动化部署等环节，实现完整的DevOps流水线。

**演练目标**：
- 掌握Jenkins Pipeline语法和最佳实践
- 构建多阶段企业级CI/CD流水线
- 集成企业级质量工具链
- 实现自动化测试和部署
- 配置复杂的门禁规则和审批流程
- 学习Jenkins在企业环境中的应用

**技术栈**：
- **CI平台**：Jenkins + Pipeline Plugin
- **项目基础**：Spring Boot + Maven + JUnit 5
- **质量工具**：SonarQube Enterprise、Nexus、Artifactory
- **安全扫描**：Fortify、Checkmarx、WhiteSource
- **部署工具**：Docker、Kubernetes、Ansible

**时间安排**：60-90分钟

---

## 环境准备

### 前置条件
1. 完成演练5的TDD银行转账项目
2. Jenkins 2.400+ 已安装并配置
3. 安装必要的Jenkins插件
4. 配置必要的工具集成

### 必需的Jenkins插件
```bash
# 核心Pipeline插件
Pipeline
Pipeline: Stage View
Pipeline: Groovy
Blue Ocean

# 工具集成插件
Maven Integration
SonarQube Scanner
Artifactory
Docker Pipeline
Kubernetes

# 质量和安全插件
Checkstyle
SpotBugs
JaCoCo
OWASP Dependency Check

# 通知和报告插件
Email Extension
Slack Notification
HTML Publisher
Test Results Analyzer
```

### Jenkins工具配置
```groovy
// 在Jenkins全局工具配置中设置
// Manage Jenkins > Global Tool Configuration

// JDK配置
JDK installations:
- Name: JDK-17
  JAVA_HOME: /usr/lib/jvm/java-17-openjdk

// Maven配置  
Maven installations:
- Name: Maven-3.9
  MAVEN_HOME: /opt/maven

// SonarQube Scanner配置
SonarQube Scanner installations:
- Name: SonarQube-Scanner
  Install automatically: true

// Docker配置
Docker installations:
- Name: Docker
  Install automatically: true
```

---

## 阶段一：基础Pipeline配置

### 1.1 创建Jenkinsfile

在项目根目录创建 `Jenkinsfile`：

```groovy
@Library('shared-library@main') _

pipeline {
    agent {
        kubernetes {
            yaml """
                apiVersion: v1
                kind: Pod
                spec:
                  containers:
                  - name: maven
                    image: maven:3.9-openjdk-17
                    command:
                    - cat
                    tty: true
                    volumeMounts:
                    - mountPath: /root/.m2
                      name: maven-cache
                  - name: docker
                    image: docker:dind
                    securityContext:
                      privileged: true
                  volumes:
                  - name: maven-cache
                    persistentVolumeClaim:
                      claimName: maven-cache-pvc
            """
        }
    }
    
    environment {
        // 环境变量配置
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
        SONAR_PROJECT_KEY = 'bank-transfer-project'
        ARTIFACTORY_URL = credentials('artifactory-url')
        SONAR_TOKEN = credentials('sonar-token')
        
        // 版本管理
        BUILD_VERSION = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
        RELEASE_VERSION = "${params.RELEASE_VERSION ?: '1.0.0'}-${BUILD_VERSION}"
    }
    
    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['none', 'dev', 'test', 'staging', 'prod'],
            description: '选择部署环境'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: '跳过测试阶段'
        )
        string(
            name: 'RELEASE_VERSION',
            defaultValue: '',
            description: '发布版本号 (可选)'
        )
    }
    
    triggers {
        // 触发器配置
        pollSCM('H/15 * * * *')  // 每15分钟检查代码变化
        cron('H 2 * * 1-5')      // 工作日凌晨2点定时构建
    }
    
    options {
        // Pipeline选项
        buildDiscarder(logRotator(
            numToKeepStr: '30',
            daysToKeepStr: '30',
            artifactNumToKeepStr: '10'
        ))
        timeout(time: 60, unit: 'MINUTES')
        skipStagesAfterUnstable()
        parallelsAlwaysFailFast()
        ansiColor('xterm')
    }
    
    stages {
        stage('🚀 Pipeline Start') {
            steps {
                script {
                    // 发送开始通知
                    sendNotification('start')
                    
                    // 显示构建信息
                    echo """
                    ===================================
                    🏗️  Bank Transfer CI Pipeline
                    ===================================
                    Branch: ${env.GIT_BRANCH}
                    Commit: ${env.GIT_COMMIT}
                    Version: ${RELEASE_VERSION}
                    Deploy Env: ${params.DEPLOY_ENV}
                    ===================================
                    """
                }
            }
        }
        
        stage('📥 Checkout & Validate') {
            parallel {
                stage('Source Code') {
                    steps {
                        container('maven') {
                            checkout scm
                            
                            script {
                                // 验证项目结构
                                sh """
                                    echo "验证项目结构..."
                                    [ -f pom.xml ] || { echo "pom.xml not found"; exit 1; }
                                    [ -d src/main/java ] || { echo "src/main/java not found"; exit 1; }
                                    [ -d src/test/java ] || { echo "src/test/java not found"; exit 1; }
                                """
                                
                                // 读取项目信息
                                def pom = readMavenPom file: 'pom.xml'
                                env.PROJECT_VERSION = pom.version
                                env.PROJECT_NAME = pom.artifactId
                                
                                echo "项目: ${env.PROJECT_NAME}, 版本: ${env.PROJECT_VERSION}"
                            }
                        }
                    }
                }
                
                stage('Environment Check') {
                    steps {
                        container('maven') {
                            sh """
                                echo "检查构建环境..."
                                java -version
                                mvn -version
                                echo "Docker状态检查..."
                                docker --version || echo "Docker not available in this container"
                            """
                        }
                    }
                }
            }
        }
        
        stage('🔍 Pre-build Analysis') {
            parallel {
                stage('Dependency Check') {
                    steps {
                        container('maven') {
                            sh 'mvn dependency:analyze dependency:resolve-sources'
                            
                            // 生成依赖报告
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target/site',
                                reportFiles: 'dependency-info.html',
                                reportName: 'Dependency Report'
                            ])
                        }
                    }
                }
                
                stage('License Check') {
                    steps {
                        container('maven') {
                            sh """
                                mvn license:check || true
                                mvn license:aggregate-add-third-party
                            """
                        }
                    }
                }
            }
        }
    }
}
```

### 1.2 编译和验证阶段

```groovy
        stage('🔨 Build & Compile') {
            steps {
                container('maven') {
                    script {
                        try {
                            sh """
                                echo "开始编译项目..."
                                mvn clean compile -B -V \
                                    -Dmaven.test.skip=true \
                                    -Dmaven.javadoc.skip=true
                            """
                            
                            // 编译成功标记
                            env.COMPILE_SUCCESS = 'true'
                            
                        } catch (Exception e) {
                            env.COMPILE_SUCCESS = 'false'
                            error "编译失败: ${e.getMessage()}"
                        }
                    }
                }
            }
            
            post {
                success {
                    echo "✅ 编译成功"
                }
                failure {
                    echo "❌ 编译失败"
                    sendNotification('compile_failed')
                }
            }
        }
```

---

## 阶段二：测试和质量检查

### 2.1 全面测试阶段

```groovy
        stage('🧪 Testing Phase') {
            when {
                not { params.SKIP_TESTS }
            }
            
            parallel {
                stage('Unit Tests') {
                    steps {
                        container('maven') {
                            script {
                                sh """
                                    echo "执行单元测试..."
                                    mvn test -B \
                                        -Dtest.groups=unit \
                                        -Djacoco.destFile=target/jacoco-unit.exec
                                """
                                
                                // 发布测试结果
                                publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                                
                                // 生成JaCoCo报告
                                sh 'mvn jacoco:report'
                                
                                publishHTML([
                                    allowMissing: false,
                                    alwaysLinkToLastBuild: true,
                                    keepAll: true,
                                    reportDir: 'target/site/jacoco',
                                    reportFiles: 'index.html',
                                    reportName: 'Unit Test Coverage Report'
                                ])
                            }
                        }
                    }
                    
                    post {
                        always {
                            // 始终发布测试结果
                            junit 'target/surefire-reports/*.xml'
                            
                            // JaCoCo覆盖率
                            jacoco(
                                execPattern: 'target/jacoco-unit.exec',
                                classPattern: 'target/classes',
                                sourcePattern: 'src/main/java',
                                inclusionPattern: '**/*.class',
                                changeBuildStatus: true,
                                minimumLineCoverage: '80',
                                maximumLineCoverage: '95'
                            )
                        }
                    }
                }
                
                stage('Integration Tests') {
                    steps {
                        container('maven') {
                            sh """
                                echo "执行集成测试..."
                                mvn verify -B \
                                    -Dtest.groups=integration \
                                    -Djacoco.destFile=target/jacoco-integration.exec \
                                    -DfailIfNoTests=false
                            """
                            
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target/site/jacoco-integration',
                                reportFiles: 'index.html',
                                reportName: 'Integration Test Coverage'
                            ])
                        }
                    }
                    
                    post {
                        always {
                            junit allowEmptyResults: true, testResultsPattern: 'target/failsafe-reports/*.xml'
                        }
                    }
                }
                
                stage('Performance Tests') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'develop'
                            expression { params.DEPLOY_ENV != 'none' }
                        }
                    }
                    
                    steps {
                        container('maven') {
                            sh """
                                echo "执行性能测试..."
                                mvn test -B \
                                    -Dtest.groups=performance \
                                    -Djmeter.testFiles=src/test/jmeter/*.jmx || true
                            """
                            
                            // 发布性能测试报告
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target/jmeter/reports',
                                reportFiles: 'index.html',
                                reportName: 'Performance Test Report'
                            ])
                        }
                    }
                }
            }
            
            post {
                always {
                    script {
                        // 合并测试报告
                        sh """
                            mvn jacoco:merge \
                                -Dfileset=target/jacoco-unit.exec,target/jacoco-integration.exec \
                                -DdestFile=target/jacoco-merged.exec
                            mvn jacoco:report -Djacoco.dataFile=target/jacoco-merged.exec
                        """
                    }
                }
                
                success {
                    echo "✅ 所有测试通过"
                }
                
                unstable {
                    echo "⚠️ 测试不稳定"
                    sendNotification('tests_unstable')
                }
                
                failure {
                    echo "❌ 测试失败"
                    sendNotification('tests_failed')
                }
            }
        }
```

### 2.2 代码质量检查

```groovy
        stage('📊 Quality Analysis') {
            parallel {
                stage('SonarQube Analysis') {
                    steps {
                        container('maven') {
                            withSonarQubeEnv('SonarQube-Server') {
                                sh """
                                    mvn sonar:sonar -B \
                                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                        -Dsonar.junit.reportPaths=target/surefire-reports \
                                        -Dsonar.java.binaries=target/classes \
                                        -Dsonar.java.libraries=target/dependency/*.jar
                                """
                            }
                        }
                    }
                }
                
                stage('Static Code Analysis') {
                    parallel {
                        stage('Checkstyle') {
                            steps {
                                container('maven') {
                                    sh 'mvn checkstyle:check -Dcheckstyle.failOnViolation=false'
                                    
                                    publishHTML([
                                        allowMissing: false,
                                        alwaysLinkToLastBuild: true,
                                        keepAll: true,
                                        reportDir: 'target/site',
                                        reportFiles: 'checkstyle.html',
                                        reportName: 'Checkstyle Report'
                                    ])
                                    
                                    recordIssues(
                                        enabledForFailure: true,
                                        tools: [checkStyle(pattern: 'target/checkstyle-result.xml')]
                                    )
                                }
                            }
                        }
                        
                        stage('SpotBugs') {
                            steps {
                                container('maven') {
                                    sh 'mvn spotbugs:check -Dspotbugs.failOnError=false'
                                    
                                    publishHTML([
                                        allowMissing: false,
                                        alwaysLinkToLastBuild: true,
                                        keepAll: true,
                                        reportDir: 'target/site',
                                        reportFiles: 'spotbugs.html',
                                        reportName: 'SpotBugs Report'
                                    ])
                                    
                                    recordIssues(
                                        enabledForFailure: true,
                                        tools: [spotBugs(pattern: 'target/spotbugsXml.xml')]
                                    )
                                }
                            }
                        }
                        
                        stage('PMD') {
                            steps {
                                container('maven') {
                                    sh 'mvn pmd:check -Dpmd.failOnViolation=false'
                                    
                                    publishHTML([
                                        allowMissing: false,
                                        alwaysLinkToLastBuild: true,
                                        keepAll: true,
                                        reportDir: 'target/site',
                                        reportFiles: 'pmd.html',
                                        reportName: 'PMD Report'
                                    ])
                                    
                                    recordIssues(
                                        enabledForFailure: true,
                                        tools: [pmdParser(pattern: 'target/pmd.xml')]
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            post {
                always {
                    script {
                        // 等待SonarQube质量门禁结果
                        timeout(time: 10, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                error "SonarQube质量门禁失败: ${qg.status}"
                            } else {
                                echo "✅ SonarQube质量门禁通过"
                            }
                        }
                    }
                }
            }
        }
```

---

## 阶段三：安全扫描

### 3.1 安全扫描阶段

```groovy
        stage('🔒 Security Scanning') {
            parallel {
                stage('Dependency Vulnerability Scan') {
                    steps {
                        container('maven') {
                            sh """
                                echo "执行依赖漏洞扫描..."
                                mvn org.owasp:dependency-check-maven:check \
                                    -DfailBuildOnCVSS=7 \
                                    -DsuppressionsFile=owasp-suppressions.xml
                            """
                            
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'OWASP Dependency Check'
                            ])
                        }
                    }
                }
                
                stage('License Compliance') {
                    steps {
                        container('maven') {
                            sh """
                                echo "执行许可证合规检查..."
                                mvn license:aggregate-add-third-party
                                mvn license:check-file-header -Dlicense.failIfMissing=false
                            """
                        }
                    }
                }
                
                stage('Secret Scanning') {
                    when {
                        anyOf {
                            branch 'main'
                            changeRequest()
                        }
                    }
                    
                    steps {
                        container('maven') {
                            sh """
                                echo "执行密钥扫描..."
                                # 使用git-secrets或类似工具
                                git secrets --scan --recursive . || true
                            """
                        }
                    }
                }
                
                stage('Container Security Scan') {
                    when {
                        expression { params.DEPLOY_ENV != 'none' }
                    }
                    
                    steps {
                        container('docker') {
                            sh """
                                echo "构建Docker镜像进行安全扫描..."
                                docker build -t ${PROJECT_NAME}:${BUILD_VERSION} .
                                
                                # 使用Trivy进行容器安全扫描
                                docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                                    aquasec/trivy image --format json --output trivy-report.json \
                                    ${PROJECT_NAME}:${BUILD_VERSION}
                            """
                            
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: '.',
                                reportFiles: 'trivy-report.json',
                                reportName: 'Container Security Scan'
                            ])
                        }
                    }
                }
            }
            
            post {
                success {
                    echo "✅ 安全扫描通过"
                }
                failure {
                    echo "❌ 安全扫描发现严重问题"
                    sendNotification('security_failed')
                }
            }
        }
```

---

## 阶段四：构建制品

### 4.1 构建和打包

```groovy
        stage('📦 Build Artifacts') {
            steps {
                container('maven') {
                    script {
                        sh """
                            echo "构建最终制品..."
                            mvn clean package -B \
                                -DskipTests=true \
                                -Dmaven.javadoc.skip=false \
                                -Dproject.build.finalName=${PROJECT_NAME}-${RELEASE_VERSION}
                        """
                        
                        // 生成构建信息文件
                        writeFile file: 'target/build-info.json', text: """
                        {
                            "project": "${PROJECT_NAME}",
                            "version": "${RELEASE_VERSION}",
                            "buildNumber": "${BUILD_NUMBER}",
                            "gitCommit": "${GIT_COMMIT}",
                            "gitBranch": "${GIT_BRANCH}",
                            "buildTime": "${new Date()}",
                            "jenkinsBuildUrl": "${BUILD_URL}"
                        }
                        """
                        
                        // 归档制品
                        archiveArtifacts artifacts: """
                            target/*.jar,
                            target/build-info.json,
                            target/site/jacoco/**,
                            target/*-reports/**
                        """, allowEmptyArchive: false
                    }
                }
            }
            
            post {
                success {
                    echo "✅ 制品构建成功"
                }
            }
        }
        
        stage('📤 Publish Artifacts') {
            parallel {
                stage('Maven Repository') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'release/*'
                        }
                    }
                    
                    steps {
                        container('maven') {
                            withCredentials([
                                usernamePassword(
                                    credentialsId: 'artifactory-credentials',
                                    usernameVariable: 'REPO_USER',
                                    passwordVariable: 'REPO_PASS'
                                )
                            ]) {
                                sh """
                                    mvn deploy -B \
                                        -DskipTests=true \
                                        -Dartifactory.username=${REPO_USER} \
                                        -Dartifactory.password=${REPO_PASS}
                                """
                            }
                        }
                    }
                }
                
                stage('Docker Registry') {
                    when {
                        expression { params.DEPLOY_ENV != 'none' }
                    }
                    
                    steps {
                        container('docker') {
                            withCredentials([
                                usernamePassword(
                                    credentialsId: 'docker-registry-credentials',
                                    usernameVariable: 'DOCKER_USER',
                                    passwordVariable: 'DOCKER_PASS'
                                )
                            ]) {
                                sh """
                                    echo "推送Docker镜像到仓库..."
                                    echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin
                                    
                                    docker tag ${PROJECT_NAME}:${BUILD_VERSION} \
                                        registry.company.com/${PROJECT_NAME}:${RELEASE_VERSION}
                                    
                                    docker push registry.company.com/${PROJECT_NAME}:${RELEASE_VERSION}
                                    
                                    # 推送latest标签 (仅main分支)
                                    if [ "${GIT_BRANCH}" = "main" ]; then
                                        docker tag ${PROJECT_NAME}:${BUILD_VERSION} \
                                            registry.company.com/${PROJECT_NAME}:latest
                                        docker push registry.company.com/${PROJECT_NAME}:latest
                                    fi
                                """
                            }
                        }
                    }
                }
            }
        }
```

---

## 阶段五：部署和门禁

### 5.1 部署阶段

```groovy
        stage('🚀 Deployment') {
            when {
                expression { params.DEPLOY_ENV != 'none' }
            }
            
            stages {
                stage('Deploy to Development') {
                    when {
                        expression { params.DEPLOY_ENV == 'dev' }
                    }
                    
                    steps {
                        container('maven') {
                            sh """
                                echo "部署到开发环境..."
                                ansible-playbook -i inventory/dev \
                                    playbooks/deploy.yml \
                                    -e app_version=${RELEASE_VERSION} \
                                    -e deploy_env=dev
                            """
                        }
                    }
                }
                
                stage('Deploy to Test') {
                    when {
                        allOf {
                            expression { params.DEPLOY_ENV == 'test' }
                            branch 'develop'
                        }
                    }
                    
                    steps {
                        script {
                            // 人工审批
                            def approved = input(
                                message: '是否部署到测试环境？',
                                parameters: [
                                    choice(choices: ['Deploy', 'Abort'], name: 'action')
                                ]
                            )
                            
                            if (approved == 'Deploy') {
                                container('maven') {
                                    sh """
                                        echo "部署到测试环境..."
                                        kubectl set image deployment/${PROJECT_NAME} \
                                            ${PROJECT_NAME}=registry.company.com/${PROJECT_NAME}:${RELEASE_VERSION} \
                                            -n test
                                        
                                        kubectl rollout status deployment/${PROJECT_NAME} -n test
                                    """
                                }
                            } else {
                                error "部署被用户取消"
                            }
                        }
                    }
                }
                
                stage('Deploy to Staging') {
                    when {
                        allOf {
                            expression { params.DEPLOY_ENV == 'staging' }
                            branch 'main'
                        }
                    }
                    
                    steps {
                        script {
                            // 自动化测试验证
                            container('maven') {
                                sh """
                                    echo "部署到预生产环境..."
                                    helm upgrade --install ${PROJECT_NAME} ./helm-chart \
                                        --namespace staging \
                                        --set image.tag=${RELEASE_VERSION} \
                                        --set environment=staging
                                    
                                    # 等待部署完成
                                    kubectl wait --for=condition=available \
                                        deployment/${PROJECT_NAME} -n staging --timeout=300s
                                """
                            }
                            
                            // 冒烟测试
                            sh """
                                echo "执行冒烟测试..."
                                mvn test -B -Dtest.groups=smoke \
                                    -Dapp.base.url=https://staging.company.com
                            """
                        }
                    }
                }
                
                stage('Deploy to Production') {
                    when {
                        allOf {
                            expression { params.DEPLOY_ENV == 'prod' }
                            branch 'main'
                        }
                    }
                    
                    steps {
                        script {
                            // 多级审批
                            def approvers = ['team-lead', 'ops-manager', 'security-officer']
                            def approvals = [:]
                            
                            parallel approvers.collectEntries { approver ->
                                [approver, {
                                    approvals[approver] = input(
                                        message: "${approver}审批生产环境部署",
                                        submitterParameter: 'approver',
                                        parameters: [
                                            choice(choices: ['Approve', 'Reject'], name: 'decision'),
                                            text(name: 'comments', description: '审批意见')
                                        ]
                                    )
                                }]
                            }
                            
                            // 检查所有审批
                            approvals.each { approver, approval ->
                                if (approval.decision != 'Approve') {
                                    error "生产部署被${approver}拒绝: ${approval.comments}"
                                }
                            }
                            
                            // 蓝绿部署
                            container('maven') {
                                sh """
                                    echo "执行生产环境蓝绿部署..."
                                    
                                    # 部署到绿色环境
                                    helm upgrade --install ${PROJECT_NAME}-green ./helm-chart \
                                        --namespace production \
                                        --set image.tag=${RELEASE_VERSION} \
                                        --set environment=production \
                                        --set service.name=${PROJECT_NAME}-green
                                    
                                    # 健康检查
                                    kubectl wait --for=condition=available \
                                        deployment/${PROJECT_NAME}-green -n production --timeout=600s
                                    
                                    # 切换流量
                                    kubectl patch service ${PROJECT_NAME} -n production \
                                        -p '{"spec":{"selector":{"version":"green"}}}'
                                    
                                    echo "生产环境部署完成"
                                """
                            }
                        }
                    }
                    
                    post {
                        success {
                            sendNotification('prod_deployed')
                        }
                        failure {
                            // 自动回滚
                            sh """
                                echo "部署失败，执行回滚..."
                                kubectl patch service ${PROJECT_NAME} -n production \
                                    -p '{"spec":{"selector":{"version":"blue"}}}'
                            """
                            sendNotification('prod_deploy_failed')
                        }
                    }
                }
            }
        }
```

### 5.2 最终门禁验证

```groovy
        stage('✅ Final Quality Gate') {
            steps {
                script {
                    def qualityGateResult = evaluateQualityGate()
                    
                    echo """
                    ===========================================
                    📊 最终质量门禁报告
                    ===========================================
                    🔨 编译状态: ${env.COMPILE_SUCCESS}
                    🧪 测试通过率: ${qualityGateResult.testPassRate}%
                    📈 代码覆盖率: ${qualityGateResult.coverage}%
                    🔍 SonarQube: ${qualityGateResult.sonarStatus}
                    🔒 安全扫描: ${qualityGateResult.securityStatus}
                    📦 制品状态: ${qualityGateResult.artifactStatus}
                    🚀 部署状态: ${qualityGateResult.deployStatus}
                    ===========================================
                    """
                    
                    if (qualityGateResult.overallStatus == 'PASSED') {
                        currentBuild.result = 'SUCCESS'
                        env.QUALITY_GATE_STATUS = 'PASSED'
                        echo "🎉 质量门禁全部通过！"
                    } else {
                        currentBuild.result = 'FAILURE'
                        env.QUALITY_GATE_STATUS = 'FAILED'
                        error "❌ 质量门禁失败！"
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                // 清理工作空间
                sh 'docker system prune -f || true'
                
                // 收集度量数据
                collectMetrics()
                
                // 生成最终报告
                generateFinalReport()
            }
        }
        
        success {
            sendNotification('success')
            
            // 更新质量看板
            updateQualityDashboard('success')
        }
        
        failure {
            sendNotification('failure')
            
            // 创建JIRA缺陷单
            createJiraIssue()
            
            updateQualityDashboard('failure')
        }
        
        unstable {
            sendNotification('unstable')
            updateQualityDashboard('unstable')
        }
        
        cleanup {
            // 清理临时资源
            sh 'kubectl delete pods -l job=ci-${BUILD_NUMBER} -n jenkins || true'
        }
    }
}

// ================================
// 共享函数库
// ================================

def sendNotification(String stage) {
    def color = getNotificationColor(stage)
    def message = getNotificationMessage(stage)
    
    // Slack通知
    slackSend(
        channel: '#ci-cd',
        color: color,
        message: message,
        teamDomain: 'company',
        tokenCredentialId: 'slack-token'
    )
    
    // 企业微信通知
    httpRequest(
        url: "${env.WECHAT_WEBHOOK_URL}",
        httpMode: 'POST',
        requestBody: """
        {
            "msgtype": "markdown",
            "markdown": {
                "content": "${message}"
            }
        }
        """
    )
}

def evaluateQualityGate() {
    def result = [:]
    
    // 收集各项指标
    result.testPassRate = getTestPassRate()
    result.coverage = getCoveragePercent()
    result.sonarStatus = getSonarQubeStatus()
    result.securityStatus = getSecurityScanStatus()
    result.artifactStatus = getArtifactStatus()
    result.deployStatus = getDeploymentStatus()
    
    // 计算整体状态
    def criticalFailed = (
        result.testPassRate < 95 ||
        result.coverage < 80 ||
        result.sonarStatus == 'FAILED' ||
        result.securityStatus == 'FAILED'
    )
    
    result.overallStatus = criticalFailed ? 'FAILED' : 'PASSED'
    
    return result
}

def collectMetrics() {
    // 发送度量数据到监控系统
    def metrics = [
        buildDuration: currentBuild.duration,
        testCount: getTestCount(),
        coverage: getCoveragePercent(),
        qualityGateStatus: env.QUALITY_GATE_STATUS,
        deploymentEnv: params.DEPLOY_ENV
    ]
    
    // 发送到Prometheus/InfluxDB
    httpRequest(
        url: "${env.METRICS_ENDPOINT}/api/v1/metrics",
        httpMode: 'POST',
        requestBody: JsonOutput.toJson(metrics)
    )
}
```

---

## 企业级配置和集成

### 6.1 共享库配置

创建 `vars/bankTransferPipeline.groovy`：

```groovy
def call(Map config) {
    pipeline {
        agent {
            kubernetes {
                yaml libraryResource('pod-templates/maven-docker.yaml')
            }
        }
        
        environment {
            PROJECT_NAME = config.projectName ?: 'bank-transfer'
            QUALITY_PROFILE = config.qualityProfile ?: 'enterprise'
            NOTIFICATION_CHANNELS = config.notificationChannels ?: '#ci-cd'
        }
        
        stages {
            stage('Enterprise Validation') {
                steps {
                    script {
                        // 企业合规检查
                        validateEnterpriseCompliance(config)
                        
                        // 安全基线检查
                        validateSecurityBaseline(config)
                        
                        // 架构合规检查
                        validateArchitectureCompliance(config)
                    }
                }
            }
            
            // 其他阶段...
        }
    }
}
```

### 6.2 多环境配置管理

创建 `config/environments/production.yaml`：

```yaml
# 生产环境配置
quality_gates:
  code_coverage:
    minimum: 85
    target: 90
  
  security_scan:
    vulnerability_threshold: "HIGH"
    license_compliance: true
    
  performance:
    response_time_p95: 500ms
    throughput_min: 1000rps
    
approval_workflow:
  required_approvers: 3
  approver_groups:
    - "team-leads"
    - "security-officers"
    - "ops-managers"
    
deployment:
  strategy: "blue-green"
  rollback_enabled: true
  health_check_timeout: "10m"
  
notifications:
  channels:
    - "#production-alerts"
    - "#security-team"
  escalation:
    - "on-call-engineer"
    - "incident-manager"
```

### 6.3 企业级监控集成

```groovy
stage('Enterprise Monitoring Setup') {
    steps {
        script {
            // 注册应用到APM
            registerToAPM([
                appName: PROJECT_NAME,
                version: RELEASE_VERSION,
                environment: params.DEPLOY_ENV
            ])
            
            // 配置日志聚合
            setupLogAggregation([
                logLevel: 'INFO',
                retentionDays: 30,
                alertRules: ['ERROR', 'FATAL']
            ])
            
            // 设置健康检查
            configureHealthChecks([
                endpoints: ['/health', '/metrics'],
                interval: '30s',
                timeout: '5s'
            ])
        }
    }
}
```

---

## 演练总结和最佳实践

### 实施检查清单

- [ ] Jenkins环境配置和插件安装
- [ ] 创建Jenkinsfile流水线脚本
- [ ] 配置Maven pom.xml集成插件
- [ ] 设置SonarQube服务器集成
- [ ] 配置Artifactory/Nexus制品仓库
- [ ] 创建Kubernetes部署配置
- [ ] 设置企业级通知机制
- [ ] 配置多环境部署策略
- [ ] 建立审批工作流
- [ ] 集成监控和告警系统

### 企业级特性

1. **多级审批工作流**：不同环境需要不同级别的审批
2. **蓝绿/金丝雀部署**：零停机部署策略
3. **自动回滚机制**：失败时自动恢复
4. **企业安全集成**：LDAP、SSO、RBAC
5. **合规性检查**：符合企业安全和质量标准
6. **全链路监控**：从构建到运行的完整可观测性

### 性能优化建议

1. **并行执行**：充分利用Jenkins的并行能力
2. **资源池管理**：合理配置构建节点和容器资源
3. **缓存策略**：Maven依赖、Docker层缓存
4. **增量构建**：只构建变更的模块
5. **制品复用**：避免重复构建相同版本

### 运维和维护

1. **定期备份**：Jenkins配置、作业历史
2. **性能监控**：构建时间、成功率统计
3. **容量规划**：根据团队规模调整资源
4. **安全更新**：定期更新Jenkins和插件
5. **故障恢复**：建立快速恢复机制

通过这个企业级Jenkins CI门禁演练，团队将掌握构建生产级CI/CD流水线的完整技能，满足企业级项目的质量和安全要求。
