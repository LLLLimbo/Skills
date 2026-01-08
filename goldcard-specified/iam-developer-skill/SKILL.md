---
name: goldcard-iam-develop
description: 金卡身份中心开发者指引
---

# GoldCard IAM Developer Skill

## 概览

本文档提供金卡身份中心开发者指引，帮助开发者了解如何开发、构建和运维金卡身份中心。
文档涉及的 Git 仓库、服务器地址、凭据等可能会过期, 如遇到问题, 请向用户请求相关信息。

## 何时使用

- 当需要了解、开发、构建和运维金卡身份中心时
- 当前工作和[整体架构](#整体架构)中提到的服务、组件存在关联时
- 当前项目涉及身份认证、鉴权需求时

## 整体架构

身份中心目前由两个独立服务组成，另外还包括一些相关的 SDK

### iam-management-service

该服务主要提供各类数据的CRUD接口, 接口分为以下几类:

- 前台门户接口: 提供到前台门户的接口, 接口要求认证鉴权
- 运营管理后台接口: 用于身分中心后台的接口, 接口要求认证和鉴权
- 内部接口: 用于服务间调用的接口, 没有认证要求
- 数据迁移/同步接口: 仅在数据迁移和同步过程中使用的接口, 用于和遗留系统 ETBC 之间的数据迁移和同步

详情参考参考:

- Git 仓库: `http://10.200.6.70/common_components/iam-management-service.git`
- 开发分支: `CI_dev`

### iam-auth-center-service

该服务提供认证、鉴权端点, 以及会话管理的能力，单向依赖于 `iam-maagement-service`

详情参考参考:

- Git 仓库: `http://10.200.6.70/common_components/iam-auth-center-service.git`
- 开发分支: `CI_dev`

### iam-clients

这里面包括了 `iam-management-service` 、`iam-auth-center-service` 两个服务的内部接口的 Java SDK,
以及提供给业务应用的用于获取当前用户信息的拦截器 Java SDK

详情参考:

- Git 仓库: `http://10.200.6.70/common_components/iam-clients.git`
- 开发分支: `CI_dev`

### APISIX Plugins

用户的身份认证和鉴权还依赖于 APISIX 的自定义插件

简单来说, 对于部分受保护接口, 用户请求在到达它们的上游服务之前, 会通过网关先转发到 `iam-auth-center-service` 的认证和鉴权接口

这些插件源码和相关配置存放在 Git 仓库里, 详情参考:

- Git 仓库: `http://10.200.6.70/common_components/iam-deploy.git`
- 开发分支: `CI_dev`

### iam-migration

一个用于从 ETBC 迁移系统菜单/权限/租户/组织和员工等身份信息到 身份中心的小型应用程序, 涉及复杂的数据映射和转换逻辑,
具体逻辑可以参考项目根目录下的 `FIELD_MAPPING_CN.md`

详情参考:

- Git 仓库: `http://10.200.6.70/common_components/iam-migration.git`
- 分支: `master`

### 其他相关组件

**auth-common**

- 主要作用：该项目创建于身份中心之前, 提供通用认证/鉴权基础能力（操作鉴权 +
  数据鉴权），统一注解与拦截器流程，封装会话/用户信息与数据过滤条件，支持多提供方（Eslink/IoT/Mix）的鉴权与用户信息获取。
- 技术栈：Java 8，Spring Boot 2.1.7，Spring MVC/拦截器，SpEL，Spring Data Redis/多 Redis
  配置，Maven，多模块工程，Lombok，SLF4J，Hutool，Commons Pool2
- 相关的服务/组件：
    - 上游：接入该库的业务服务/控制器（通过 `@OpAuth/@DataAuth/@RequestSessionInfo` 等注解触发鉴权与会话注入）。
    - 下游：Eslink/IoT 相关的鉴权与用户信息服务（对应 `auth-op-auth-provider-*`、`auth-user-info-provider-*` 中的对外调用实现）。
    - 基础设施：Redis（多 Redis 配置与集成点）、HTTP 对外调用（用户/权限数据获取）

该项目之所以会和身份中心相关, 是因为身份中心计划替代旧的身份系统(ETBC 和 UTOS), 而 ETBC 以及 UTOS 原先通过 `auth-common`
SDK 与其他业务应用产生耦合,
因此 `auth-common` 需要提供额外的兼容层.

- Git 仓库地址: `http://10.200.1.145/framework/server/auth-common.git`
- 分支说明:
    - `pro`: 生产分支, 暂时和身份中心无关
    - `feature/iam-eslink-compatible`: 额外提供了身份中心和 ETBC 的兼容层, 暂未合并到生产
    - `feature/datapermission-field`: 和身份中心无关

## 环境指引

> 仅限公司内网访问

### 开发环境

- Kubernetes 1.20
- Nodes: `10.200.6.200`,`10.200.6.207`
- Kubeconfig: 参考 [kubeconfig-dev.yaml](references/kubeconfig-dev.yaml)
- Namespaces: `iam`
- Loadbalancer(Caddy): `10.200.6.152`

### 测试环境

- Kubernetes 1.20
- Nodes: `10.200.6.200`,`10.200.6.207`
- Kubeconfig: 参考 [kubeconfig-dev.yaml](references/kubeconfig-dev.yaml)
- Namespaces: `eslink-test`
- Loadbalancer(Caddy): `10.200.6.152`