# DSP Build 构建工具

DSP Build 是一个为数字服务平台（Digital Service Platform，DSP）项目设计的综合构建框架，提供依赖管理、版本控制和构建流程的工具与实用程序。

## 解决的核心问题

DSP Build 主要解决大型复杂项目中的依赖版本管理问题：

- **集中式版本控制**：通过 BOM（物料清单）管理确保所有模块使用一致的依赖版本
- **自动化版本更新**：基于集中定义的版本自动更新项目依赖
- **跨项目协调**：管理多个相关项目之间的版本兼容性
- **简化发布流程**：通过自动化任务简化发布工作流

### 相比传统方法的优势

- **单一事实来源**：与手动版本管理不同，DSP Build 在中央仓库维护版本信息
- **减少冲突**：防止大型项目中常见的依赖版本冲突
- **自动化工作流**：消除容易出错的手动版本更新
- **跨团队标准化**：在开发团队间强制执行一致的构建标准

## 项目模块

### dsp-build-core

包含管理发布基础逻辑的核心模块：

- 从仓库发现和解析 BOM 信息
- 基于 BOM 信息更新项目依赖
- 跨项目管理版本信息

### dsp-build-extension

将 DSP Build 功能直接集成到 Maven 构建过程的 Maven 扩展：

- 在构建过程中自动更新项目依赖
- 无需显式插件配置即可应用版本信息
- 支持 Git 和 Maven 仓库作为 BOM 信息源

### dsp-build-plugin

Maven 插件实现，提供：

- 用于版本管理的自定义 Maven 目标
- 与现有构建流程集成
- 可配置的版本更新行为

### dsp-build-tool

用于执行构建和发布任务的命令行界面：

- 从仓库发现 BOM 信息
- 使用版本信息更新项目 POM
- 将版本信息注册回仓库

## 使用指南

DSP Build 提供三种不同的使用方式：Maven 扩展、Maven 插件和命令行工具。您可以根据项目需求选择最适合的方式。

### 配置

在项目根目录创建 `releaser.properties` 文件：

```properties
# Git 仓库配置
releaser.repository.source=GIT
releaser.repository.url=git@gitlab.com:example/buildconfig.git
releaser.repository.coordinate=pom.xml
releaser.repository.baseline=1.0

# 或者 Maven 仓库配置
# releaser.repository.source=MAVEN
# releaser.repository.url=https://mirrors.huaweicloud.com/repository/maven/
# releaser.repository.coordinate=com.example:bom-project
# releaser.repository.baseline=1.0
```

BOM 配置通过 Git 仓库中的 POM 文件或 Maven 仓库中的 BOM 项目定义，例如：

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>bom-project</artifactId>
    <version>1.0</version>
    <packaging>pom</packaging>
    
    <properties>
        <!-- 框架版本 -->
        <spring.version>5.3.20</spring.version>
        <springboot.version>2.7.2</springboot.version>
        
        <!-- 内部组件版本 -->
        <component-a.version>1.2.3</component-a.version>
        <component-b.version>2.3.4</component-b.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <!-- 依赖管理配置 -->
        </dependencies>
    </dependencyManagement>
</project>
```

### 方式一：使用 Maven 扩展

将扩展添加到项目的 `.mvn/extensions.xml` 文件中：

```xml
<extensions>
  <extension>
    <groupId>com.sample</groupId>
    <artifactId>dsp-build-extension</artifactId>
    <version>1.0.0</version>
  </extension>
</extensions>
```

使用此方式，Maven 构建过程会自动应用版本管理，无需额外配置。

### 方式二：使用 Maven 插件

将插件添加到项目的 `pom.xml` 文件中：

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.sample</groupId>
      <artifactId>dsp-build-plugin</artifactId>
      <version>1.0.0</version>
    </plugin>
  </plugins>
</build>
```

然后可以执行插件目标：

```bash
mvn dsp-build:update-versions
```

### 方式三：使用命令行工具

执行 CLI 工具：

```bash
java -jar dsp-build-tool.jar release
```

或使用自定义选项：

```bash
java -jar dsp-build-tool.jar release --repository.url=git@github.com:example/config.git
```

## 构建项目

```bash
mvn clean install
```

## 许可证

基于 Apache License 2.0 许可。
