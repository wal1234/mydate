# book-common

微服务**供应代码**模块，供其他微服务通过 Maven 依赖引入，统一响应格式、异常与常量。

## 依赖方式

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>book-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

同仓库多模块可使用 `${project.version}`；独立项目需先在本项目根目录执行 `mvn clean install` 或从私服拉取对应版本。

## 提供内容

| 包/类 | 说明 |
|-------|------|
| `com.example.common.result.Result` | 统一 API 响应体：`Result.ok(data)`、`Result.fail(message)` 等 |
| `com.example.common.result.PageResult` | 分页结果：`PageResult.of(list, total, page, size)`，供列表分页返回 |
| `com.example.common.result.ResultCode` | 状态码枚举：SUCCESS、FAIL、BAD_REQUEST、NOT_FOUND 等 |
| `com.example.common.exception.BusinessException` | 业务异常，可带 code/message，便于全局异常处理 |
| `com.example.common.constant.CommonConstant` | 公共常量（如请求头名） |

## 使用示例

- Controller 返回：`return Result.ok(book);`、`return Result.fail("图书不存在");`
- 业务层抛异常：`throw new BusinessException(ResultCode.NOT_FOUND);` 或 `new BusinessException("自定义消息");`
- 全局异常处理：捕获 `BusinessException` 等，构造 `Result.fail(code, message)` 返回

本仓库内 `book-service` 已依赖并使用了上述类，可参考其 Controller 与 `GlobalExceptionHandler`。
