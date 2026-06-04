# 图书借阅管理系统

一个基于 Spring Boot + Vue 3 的全栈图书借阅管理系统，支持图书管理、借阅归还、预约排队、信用分体系等功能。

## 技术栈

### 后端
- Java 21 + Spring Boot 4.0
- MyBatis-Plus（数据库操作）
- MySQL 8.0
- JWT（身份认证）
- BCrypt（密码加密）

### 前端
- Vue 3（Composition API）
- Vue Router + Pinia
- Element Plus UI
- Axios
- Vite

## 环境要求

| 环境 | 版本要求 |
|------|----------|
| JDK | 21+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ |

## 项目结构

```
library system/
├── library-backend/          # 后端项目
│   ├── src/main/java/
│   │   └── com/example/librarybackend/
│   │       ├── annotation/   # 自定义注解
│   │       ├── common/       # 通用类（Result、UserContext）
│   │       ├── config/       # 配置类
│   │       ├── controller/   # 控制器
│   │       ├── entity/       # 实体类
│   │       ├── exception/    # 异常处理
│   │       ├── interceptor/  # 拦截器
│   │       ├── mapper/       # Mapper接口
│   │       ├── service/      # 服务层
│   │       ├── task/         # 定时任务
│   │       └── util/         # 工具类
│   └── src/main/resources/
│       └── application.yml   # 配置文件
│
└── frontend/                 # 前端项目
    └── src/
        ├── api/              # API接口
        ├── components/       # 公共组件
        ├── router/           # 路由配置
        ├── store/            # 状态管理
        ├── utils/            # 工具函数
        └── views/            # 页面组件
            ├── admin/        # 管理员页面
            ├── book/         # 图书页面
            ├── borrow/       # 借阅页面
            ├── notification/ # 通知页面
            ├── reserve/      # 预约页面
            └── user/         # 用户页面
```

## 快速开始

### 1. 数据库配置

1. 创建 MySQL 数据库：
```sql
CREATE DATABASE librarysystem DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 创建数据表（项目启动后 MyBatis-Plus 自动映射，或手动执行建表 SQL）

3. 修改数据库配置（`library-backend/src/main/resources/application.yml`）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/librarysystem?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root        # 改成你的用户名
    password: root        # 改成你的密码
```

### 2. 启动后端

```bash
cd library-backend
./mvnw spring-boot:run
```

后端启动在 `http://localhost:8080`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:8081`

### 4. 访问系统

打开浏览器访问 `http://localhost:8081`

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | 注册即可 | - |

## 功能说明

### 用户功能
- 注册/登录
- 浏览图书列表、搜索图书
- 查看图书详情
- 借阅图书（支持自选借阅时长：秒/天/周/月）
- 申请归还图书（提交后等待管理员确认）
- 预约图书（库存不足时）
- 查看借阅记录
- 查看预约记录（到货后可直接借阅）
- 消息通知中心（预约到货通知、未读红点提醒）
- 个人中心、修改密码

### 管理员功能
- 图书管理（增删改查，补货自动触发预约通知）
- 分类管理（一级/二级分类）
- 借阅管理（确认归还、逾期/罚金处理）
- 查看所有预约记录

### 核心业务逻辑
- **信用分体系**：初始100分，低于60分无法借阅
- **借阅时长选择**：支持秒/天/周/月单位，最大30天，秒单位方便测试逾期
- **还书流程**：用户申请归还 → 管理员核验实体书 → 系统执行还书（库存回流、逾期计算、罚金记录、预约触发）
- **逾期处理**：定时任务每秒自动检测，逾期自动扣信用分并记录罚金
- **预约排队**：库存不足时可预约，还书或补货时自动通知最早预约者
- **消息通知**：独立通知表，预约到货时推送通知，支持未读红点和一键已读
- **事务管理**：借书、还书使用 @Transactional 保证数据一致性

## API 接口

### 用户相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/user/register` | POST | 注册 |
| `/api/user/login` | POST | 登录 |
| `/api/user/info` | GET | 获取个人信息 |
| `/api/user/info` | PUT | 修改个人信息 |
| `/api/user/password` | PUT | 修改密码 |

### 通知相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/notifications` | GET | 获取我的通知列表 |
| `/api/notifications/unread-count` | GET | 获取未读通知数量 |
| `/api/notifications/{id}/read` | PUT | 标记单条已读 |
| `/api/notifications/read-all` | PUT | 全部标记已读 |

### 图书相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/books` | GET | 图书列表（支持分页、搜索） |
| `/api/books/{id}` | GET | 图书详情 |
| `/api/books` | POST | 添加图书（管理员） |
| `/api/books/{id}` | PUT | 修改图书（管理员，补货自动触发预约通知） |
| `/api/books/{id}` | DELETE | 删除图书（管理员） |

### 分类相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/categories` | GET | 一级分类列表 |
| `/api/categories/{parentId}/children` | GET | 子分类列表 |
| `/api/categories` | POST | 新增分类（管理员） |
| `/api/categories/{id}` | PUT | 修改分类（管理员） |
| `/api/categories/{id}` | DELETE | 删除分类（管理员） |

### 借阅相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/borrow` | POST | 借书（支持 duration/unit 参数自选时长） |
| `/api/borrow/{recordId}/apply-return` | PUT | 申请归还（用户） |
| `/api/borrow/{recordId}/confirm-return` | PUT | 确认归还（管理员） |
| `/api/borrow/my` | GET | 我的借阅记录 |
| `/api/borrow/all` | GET | 所有记录（管理员） |

### 预约相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/reserve/{bookId}` | POST | 预约图书 |
| `/api/reserve/my` | GET | 我的预约 |
| `/api/reserve/ready` | GET | 待领取预约 |
| `/api/reserve/{reserveId}/claim` | PUT | 领取预约（支持 duration/unit 参数） |
| `/api/reserve/{reserveId}/cancel` | PUT | 取消预约 |
| `/api/reserve/all` | GET | 所有预约（管理员） |

## 答辩要点

1. **JWT 认证**：Token 生成、拦截器校验、ThreadLocal 存储用户信息
2. **事务管理**：借书、还书使用 @Transactional 保证一致性
3. **还书流程**：用户申请归还 → 管理员确认，符合实体书业务场景
4. **预约排队**：还书或补货时自动通知最早预约者，支持直接借阅
5. **消息通知**：独立通知表，预约到货推送，未读红点提醒
6. **信用分机制**：借书校验、逾期自动扣分（定时任务每秒检测）
7. **组合条件搜索**：MyBatis-Plus 动态 SQL
8. **文件上传**：封面图片上传到本地，静态资源映射

## 常见问题

**Q: 启动报数据库连接错误？**
A: 检查 MySQL 是否启动，用户名密码是否正确（默认 root / 123456）。

**Q: 前端请求 404？**
A: 确保后端已启动，Vite 代理配置正确。

**Q: 上传封面失败？**
A: 检查 `E:/uploads/library-covers/` 目录是否存在且有写入权限。

**Q: 需要管理员权限运行 Maven？**
A: Windows 下启动 MySQL 服务需要管理员权限，可右键终端以管理员身份运行。
