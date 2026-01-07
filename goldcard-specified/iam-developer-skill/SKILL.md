---
name: goldcard-iam-develop
description: 金卡身份中心开发者指引
---

# GoldCard IAM Developer Skill

## 概览

本文档提供金卡身份中心开发者指引，帮助开发者了解如何开发、构建和运维金卡身份中心。

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

## 环境指引

### 开发环境

- Kubernetes 1.20
- Nodes: 10.200.6.200,10.200.6.207
- Kubeconfig: 参考 [kubeconfig-dev.yaml](references/kubeconfig-dev.yaml) 

