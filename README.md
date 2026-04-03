# Book 微服务项目

基于 **Java 17**、**MyBatis-Plus**、**Nacos** 的微服务示例，所有 SQL 均在对应 Mapper XML 中实现。

## 设计文档

本项目已具备**日程管理**方向的设计说明，包含每日日程登记与日常文档编辑管理的功能需求、架构、数据模型与接口设计，详见 **[日程管理项目设计文档](docs/DESIGN.md)**。

## 技术栈

- Java 17
- Spring Boot 3.2.x
- Spring Cloud Alibaba（Nacos 注册中心）
- MyBatis-Plus（mapper-locations 指向 XML，SQL 写在 XML）
- 默认 H2 内存库（可切换 MySQL）

## 项目结构

```
book/
├── pom.xml                 # 父 POM
├── book-common/             # 供应代码：公共库，供其他微服务 Maven 依赖
│   ├── pom.xml
│   └── src/main/java/.../common/
│       ├── result/Result.java、ResultCode.java   # 统一响应
│       ├── exception/BusinessException.java      # 业务异常
│       └── constant/CommonConstant.java          # 公共常量
├── book-service/            # 图书示例微服务（已依赖 book-common）
│   └── ...
├── schedule-service/        # 日程管理微服务（按 DESIGN.md 实现）
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../scheduleservice/
│       │   ├── ScheduleServiceApplication.java
│       │   ├── config/GlobalExceptionHandler.java
│       │   ├── entity/ScheduleEvent.java、DailyDocument.java
│       │   ├── mapper/ + service/ + controller/（日程与每日文档）
│       └── resources/
│           ├── application.yml（端口 8081）
│           ├── schema-h2.sql（schedule_event、daily_document）
│           └── mapper/*.xml # 所有 SQL 在 XML 中实现
└── README.md
```

### 其他微服务通过 Maven 引入 book-common

**方式一：同仓库多模块**

在父 POM 的 `<modules>` 中加入该微服务模块，在该模块的 `pom.xml` 中：

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>book-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**方式二：独立项目引用**

先在本项目根目录执行 `mvn clean install`，将 `book-common` 安装到本地仓库；在**其他微服务项目**的 `pom.xml` 中增加上述依赖即可。若发布到私服，将 `version` 改为已发布的版本。

## 运行前准备

1. **JDK 17**
2. **Nacos**（可选）：若需注册到 Nacos，请先启动 Nacos（默认 `127.0.0.1:8848`）。

## 启动方式

```bash
# 在项目根目录
mvn clean install
cd book-service
mvn spring-boot:run
```

**日程管理微服务（schedule-service，端口 8081）**：

```bash
cd schedule-service
mvn spring-boot:run
```

无 Nacos 时加 profile：`-Dspring-boot.run.profiles=local`。使用 MySQL 时先建库 `schedule`，再使用 profile `dev`。

### 日程管理接口摘要（schedule-service）

- **日程**：`GET/POST/PUT/DELETE /api/schedules`；`GET /api/schedules?start=&end=&view=&page=&size=` 按时间范围查询（传 page/size 时分页返回 PageResult）。
- **每日文档**：`GET /api/daily-docs/{date}` 按日期查询，`GET /api/daily-docs?from=&to=&page=&size=` 按区间（支持分页），`GET /api/daily-docs/id/{id}` 按 ID；`POST/PUT/DELETE` 创建/更新/删除。
- **每日聚合**：`GET /api/daily-view?date=yyyy-MM-dd` 返回该日日程列表 + 当日文档（一日一屏）。
- 日程支持 `reminderMinutes`（提前提醒分钟数）；详见 [设计文档](docs/DESIGN.md)。

## 配置说明

- **application.yml**：服务端口、应用名、Nacos 地址、MyBatis-Plus 的 `mapper-locations`（`classpath*:/mapper/**/*.xml`）、H2 数据源。
- **application-dev.yml**：MySQL 数据源，按需修改 `url/username/password`。
- **application-local.yml**：关闭 Nacos 注册，便于本地不启 Nacos 时运行。

## SQL 规范

- 所有自定义 SQL 均在 `src/main/resources/mapper/*.xml` 中编写。
- Mapper 接口继承 `BaseMapper<Entity>` 后，简单 CRUD 仍可用；自定义方法在对应 XML 中写实现（如 `selectByAuthor`、`updateTitleById`）。

## 示例接口

- `GET /api/books/{id}` 根据 ID 查询
- `GET /api/books?author=xxx` 根据作者查询（XML 中实现）
- `POST /api/books` 新增图书
- `PATCH /api/books/{id}/title?title=xxx` 更新标题（XML 中实现）
