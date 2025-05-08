# DSP Build 构建工具

## 项目概述

DSP Build 是一个为大型复杂项目设计的统一构建框架，旨在解决大型软件项目在依赖管理、版本控制和构建过程中遇到的一系列问题。

### 解决的核心问题

1. **依赖版本管理混乱**：在大型项目中，不同模块可能使用同一依赖的不同版本，导致版本冲突和不兼容问题。DSP Build 通过集中式的 BOM（物料清单）管理，确保所有模块使用一致的依赖版本。

2. **构建过程复杂**：大型项目的构建过程通常涉及多个步骤和工具，DSP Build 提供统一的构建流程和命令行工具，简化构建过程。

3. **版本发布困难**：项目发布时需要协调多个组件的版本，DSP Build 提供自动化的版本更新和发布机制，减少人为错误。

4. **跨团队协作障碍**：不同团队可能使用不同的构建规范和流程，DSP Build 提供统一的构建标准，促进团队协作。

## 项目结构

DSP Build 项目由以下模块组成：

- **dsp-parent**：管理项目的插件配置
- **dsp-dependencies**：管理项目的依赖关系
- **dsp-build-core**：实现发布的核心逻辑
- **dsp-build-plugin**：Maven 插件实现
- **dsp-build-rule**：自定义构建规则
- **dsp-build-extension**：构建扩展
- **dsp-build-tool**：命令行任务设置和流程执行

## dsp-build-core 核心模块

`dsp-build-core` 模块包含管理 DSP 项目发布的核心逻辑，提供以下基本功能：

- 从代码仓库发现和解析 BOM（物料清单）信息
- 根据 BOM 信息更新项目依赖
- 跨项目管理版本信息

### 核心组件

- **Releaser**：编排发布过程的主类
- **BomRepository**：表示包含版本信息的仓库
- **Bom**：表示版本兼容性信息
- **ProjectUpdater**：用于使用新版本信息更新项目文件的接口
- **MavenUpdater**：更新 Maven 项目的实现

### 配置

核心模块使用配置文件 (`build.yml`) 定义：

- Git 仓库信息
- BOM 分支和文件位置
- 版本注册设置

## dsp-build-tool 工具模块

`dsp-build-tool` 模块提供用于执行构建和发布任务的命令行界面。它使用 Spring Boot 创建一个 CLI 应用程序，可以：

- 从仓库发现 BOM 信息
- 使用版本信息更新项目的 POM 文件
- 将版本信息注册回仓库

### 核心组件

- **ReleaserCommandLineRunner**：CLI 应用程序的入口点
- **ReleaserGoal**：定义可以执行的目标的接口
- **TasksFactory**：根据选项创建任务链
- **TasksRunner**：使用 Spring Batch 执行任务链
- **发布任务**：
  - **DiscoverBomReleaseTask**：发现 BOM 信息
  - **UpdatingPomsReleaseTask**：使用版本信息更新 POM
  - **RegisterBomReleaseTask**：注册版本信息

### 使用方法

工具可以使用以下命令执行：

```
cli [<goal>] [options]
```

其中 `goal` 是要执行的操作（例如，release），`options` 是附加参数。

## 配置

DSP Build 系统可以使用属性文件进行配置：

### releaser.properties

```properties
releaser.repository.source=GIT
releaser.repository.url=git@gitlab.com:example/buildconfig.git
releaser.repository.coordinate=pom.xml
releaser.repository.baseline=1.0
```

## 开发

### 构建项目

使用以下命令构建项目：

```bash
mvn clean install
```

### 运行测试

集成测试位于各个模块的 `src/it` 目录中。

## 许可证

基于 Apache License 2.0 许可。
