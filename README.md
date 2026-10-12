# Java / Spring Boot 个人记账本

独立开发的收支记账系统。同一套后端有两种入口：Thymeleaf 网页使用 Session 登录，Vue 页面和接口使用 JWT。支持记账、按月筛选、分类统计，以及按当前筛选条件导出 Excel。

## 技术

Java 21、Spring Boot 3.4、Spring Security、JWT、Redis、Spring Data JPA、MySQL、Vue 3、Maven

## 运行

1. 安装 JDK 21、Maven、MySQL 8、Redis。Redis 使用本机 `6379`。
2. 打开 `project-ledger/src/main/resources/application.properties`，把数据库密码改成你本机 MySQL 的密码。文件里现在写的是本地默认账号 `root` / `123456`。
3. 启动后端：

```bash
cd project-ledger
mvn spring-boot:run
```

4. 启动 Vue：

```bash
cd ledger-ui
npm install
npm run dev
```

5. 网页打开 http://localhost:8091 ，Vue 打开 http://localhost:5173 。

首次启动会写入三个账号，密码都是 `123456`：

- `xiaoming`、`xiaohong`：普通用户
- `admin`：管理员

## 登录

网页表单走 Session。`/api` 走 JWT，并把登录编号写进 Redis，退出时作废该编号，所以可以多台设备同时在线。普通用户访问管理员接口返回 403。

Excel 导出使用当前的月份筛选结果，不会把整张表全部倒出。
