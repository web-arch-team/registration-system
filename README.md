# 系统框架性描述 —— 可直接给 Copilot 的完整说明

技术栈（必须一致）

   后端：
- Spring Boot 4.0.0
- Spring MVC
- Spring Data JPA
- Spring Security（按三类用户分端登录）
- PostgreSQL

   前端：
- Vue3（Composition API）
- Vite
- ElementPlus
- Vue Router
- Pinia
- Axios

   架构模式：
- 前后端分离
- RESTful API

## 项目环境要求

- JDK 21
- PostgreSQL 18
- Spring Boot 4.0.0
- Maven 4.0.0

## 数据库配置，快速开始项目

PostgreSQL 18版本

下载好数据库之后，会有一个默认用户postgres，为这个用户设置密码123456。

首先如果是之前安装的时候可以给用户输入密码，就输入。如果已经过了，那么就打开POWERSHELL，输入
`psql -U postgres`回车登陆postgres之后直接

```sql
ALTER USER postgres WITH PASSWORD '123456';
```

然后创建一个名为`registration_system`的数据库

```sql
REATE DATABASE registration_system;
```

然后回到powershell下执行

```powershell
psql -U postgres -d registration_system -f "src/main/resources/db/init.sql"
```

linux环境中执行：

```bash
psql
```

就完成了数据库初始化。最后在你的idea中链接数据库就可以了。

![idea数据库链接](./resources/img/idea数据库链接.png)

# 期望实现效果

1. 有三个登陆账户：病人（patient）、医生（doctor）、管理员（admin）。但是这只是登陆账户并不是ER图的全部。还有科室等关系实体是我们还没有分离出来的
  注：我们这里删除的时候是需要将我们设定的`is_active`设为`false`，而不是真的删除。
2. 我们会架空一个时间体，也就是我们虚拟出了一个周的循环。如果一个病人开始尝试登陆，然后他先选择相应的科室，我们可以先简单的只设置外科 + 内科，选择科室之后他可以看到这个科室下有的疾病，然后他选择他需要去看的疾病，为了明确，我们设置疾病和科室关系如下：
  1. 内科：
    1. 心脏病
    2. 肝脏病
    3. 脾病
    4. 胃病
    5. 肾病

  2. 外科：
    1. 手足病
    2. 胸外科
    3. 关节病
    4. 烧伤病
    5. 整形


3. 关于排班，我们将一天拆分为8个阶段，分别为上午1到下午4。他对应不同的时段，但是时段这些可以后续继续更进，暂时这样设想。然后每个医生每天有16个号，均匀分。然后每个人病人申请，缴费，后面通过。

4. 病人的行为是简单的，两个地方，1. 注册，病人通过提供身份证信息和姓名性别，年龄等相关信息（本质上这些如果通过提供身份证的情况下是可以自然读取的，总的来说就是提供身份证信息和手机号就可以注册账号），然后病人进入挂号页面开始挂号挂号功能需要先选择科室，然后选择相应的疾病，然后病人选择查看这周的排班情况，点击申请，挂号。

1. 对于医生来说，医生会看到自己的值班情况，以及每天的工作，一个医生的页面应该就两个东西
  1. 今天自己的工作安排todo，排班排到的病人，他会看到挂号病人相关的全部信息；
  2. 自己的排班情况，也就是这一周的个工作情况，有可能自己周一上班，周二就不上班这样。（后续可以增加，比如说请假申请、换班申请等等，但是这是后话） 
2. 系统中有一个类似root的管理员，他有三个工作板块-三个功能界面
  1. 病人管理界面：可以对病人信息进行查询，对病人增删改查
  2. 医生管理界面：对医生增删改查，增加医生，“删除”医生，更改医生科室，更改医生对应疾病
  3. 排班管理：点击相应科室，然后可以对这个科室进行排班管理，也就是对排班表进行修改

那么我们就需要有如下的一些数据库设计：

---

统一的用户表

```sql
CREATE TABLE app_user (
                          id SERIAL PRIMARY KEY,
                          username VARCHAR(100) UNIQUE NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          role VARCHAR(20) NOT NULL,     -- 'PATIENT' / 'DOCTOR' / 'ADMIN'
                          created_at TIMESTAMP DEFAULT NOW(),
                          is_active BOOLEAN NOT NULL DEFAULT TRUE
);
```

患者档案

```sql
CREATE TABLE patient_profile (
                                 id SERIAL PRIMARY KEY,
                                 user_id INT UNIQUE NOT NULL REFERENCES app_user(id),

                                 id_card VARCHAR(18) UNIQUE NOT NULL,
                                 name VARCHAR(100) NOT NULL,
                                 phone_number VARCHAR(15) NOT NULL UNIQUE,
                                 age INT,
                                 gender VARCHAR(10) NOT NULL CHECK (gender IN ('male','female')),
                                 is_active BOOLEAN NOT NULL DEFAULT TRUE
);
```

医生档案

```sql
CREATE TABLE doctor_profile (
                                id SERIAL PRIMARY KEY,
                                user_id INT UNIQUE NOT NULL REFERENCES app_user(id),

                                doctor_id VARCHAR(10) UNIQUE NOT NULL,
                                name VARCHAR(100) NOT NULL,
                                age INT,
                                gender VARCHAR(10) NOT NULL CHECK (gender IN ('male','female')),
                                title VARCHAR(100),

                                department_id INT REFERENCES department(id),
                                is_active BOOLEAN NOT NULL DEFAULT TRUE
);
```

科室表

```sql
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL
);
```

疾病表

```sql
CREATE TABLE disease (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE,
    description TEXT,
    department_id INT NOT NULL REFERENCES department(id)
);
```

医生疾病表

```sql
CREATE TABLE doctor_disease (
    id SERIAL PRIMARY KEY,
    doctor_profile_id INT NOT NULL REFERENCES doctor_profile(id),
    disease_id INT NOT NULL REFERENCES disease(id),
    UNIQUE (doctor_profile_id, disease_id)
);
```

时间槽
有一个而时间槽的概念，我们将他使用varchar定义，使用check规范。

挂号表

```sql
CREATE TABLE patient_doctor_registration (
                                             id SERIAL PRIMARY KEY,
                                             patient_profile_id INT REFERENCES patient_profile(id),
                                             doctor_profile_id INT REFERENCES doctor_profile(id),
                                             disease_id INT REFERENCES disease(id),

                                             weekday INT NOT NULL CHECK (weekday BETWEEN 1 AND 5),
                                             timeslot VARCHAR(4) NOT NULL CHECK (timeslot IN ('AM1','AM2','AM3','AM4','PM1','PM2','PM3','PM4')),
                                             registration_time TIMESTAMP NOT NULL DEFAULT NOW(),
                                             status VARCHAR(20) NOT NULL
);
```

doctor_department_schedule // 医生-科室-排班表（科室值班表）

```sql
CREATE TABLE doctor_department_schedule (
                                            id SERIAL PRIMARY KEY,
                                            doctor_profile_id INT REFERENCES doctor_profile(id),
                                            department_id INT REFERENCES department(id),
                                            weekday INT NOT NULL CHECK (weekday BETWEEN 1 AND 5),
                                            timeslot VARCHAR(4) NOT NULL CHECK (timeslot IN ('AM1','AM2','AM3','AM4','PM1','PM2','PM3','PM4')),

                                            UNIQUE (doctor_profile_id, weekday, timeslot)
);
```

---

![ER图](./resources/img/ER图-test.png)

# 项目结构

为了方便程序的开发管理，我们项目目录结构如下：

```cmd
├── src
│         ├── main
│         │         ├── java
│         │         │         └── com
│         │         │             └── hospital
│         │         │                 └── ouc
│         │         │                     └── registrationsystem
│         │         │                         ├── config
│         │         │                         ├── domain
│         │         │                         │         ├── entity // 实体类，和数据库表一一对应
│         │         │                         │         ├── enums
│         │         │                         │         ├── repository
│         │         │                         │         └── service
│         │         │                         ├── RegistrationSystemApplication.java // 启动类
│         │         │                         ├── security
│         │         │                         └── web
│         │         └── resources
│                   └── application.properties
```

# 最新进展

我们实现了实体类，并且使用枚举类型规范类型。这里需要注意，因为我们这里使用的是枚举类型。所以数据库中的字符串值就应该是和枚举名一致才可以。
（枚举没办法存储换成string）

- Role：PATIENT/DOCTOR/ADMIN（大写）
- Gender：male/female（我按你的库定义，特意使用了小写枚举名，确保与 PostgreSQL gender_enum 一致）
- TimeSlot：AM1…PM4（大写）

按照最小分支来说entity分支到这里就应该结束了，但是我为了知道这个分支是可用的，所以这里会写一个登陆界面尝试验证。

我们密码的处理了逻辑是 SHA-256（明文 + SALT） 

2025-12-3

实现了登陆逻辑的跳转，将登陆验证相关的东西实现，密码是哈希加盐存储。盐为：OucWebDev123  

**已实现功能**
管理员功能
1、目前已实现病人管理功能，实现了病人的增删查改：可以按照姓名、身份证号、电话号、性别来进行查询，姓名可以模糊查询（创新点）
（1）DTO           
               PatientDTO（患者信息传输）
               PatientQueryDTO（患者查询条件）
（2）业务逻辑层
               BusinessException 业务异常类（自定义）
               PatientManagementService 接口
               PatientManagementServiceImpl 实现类
（3）接口层
               AdminPatientController

2、已实现医生管理功能，实现了医生的增删查改：可以按照ID、工号、姓名、性别、职称、所属科室、是否被软删除来进行查询

3、已实现科室和科室疾病管理功能，实现了科室和科室疾病的增删查改：科室和疾病的关联关系

4、已实现排班值班管理功能，实现了排班和值班的增删查改：可以按照科室进行增删查改

病人功能
1、已实现挂号功能：患者需要选择相应的department，然后就可以选择相应的disease。从而他可以在后端的doctor_disease表中
查询到对应的医生列表。然后他与 doctor_department_schedule表进行关联查询，查询能够治疗这个疾病的
医生的排班情况。从而最后他在前端就能拼接出一张8x5的值班表 行代表timeslot，列代表weekday。然后患者
选择相应的时间段和医生，提交挂号请求。而为了方便查看，患者可以选择星期几，然后查看这一天的排班情况。

2、可以查看自己的挂号记录，同时可以取消挂号，可以按照星期来筛查自己的挂号记录

2、已实现病人可以查看医院医生情况（可以按照科室和可治愈疾病来查询），查看个人信息，对个人信息进行修改，查看周末值班情况（可以按照科室、值班星期、值班时段进行筛查）

医生功能
1、已实现查看需要治疗的病人（可以按照星期来进行筛查），查看自己本周排班情况，查看个人信息，对个人信息进行修改（只可以修改用户名和密码），查看自己周末值班情况

# 前端开发规划

> 前端采用独立的 `frontend` 子项目，基于 **Vue3 + Vite + ElementPlus + Vue Router + Pinia + Axios**，与后端通过 RESTful API 通信。入口：`frontend/src/main.ts`。

## 工程结构（摘要）
- `frontend/vite.config.ts`：`/api` 代理到 `http://localhost:8080`，别名 `@ -> src`
- `frontend/src/router/index.ts`：登录页 + 管理员/医生/患者三端路由
- `frontend/src/stores/auth.ts`：Pinia，持久化登录用户（含 patientId/doctorId）
- 主要页面：
  - `views/LoginView.vue`：统一登录（必须走后端 `/api/auth/login`，已去掉示例直跳）
  - 管理员：`AdminLayout.vue`，`PatientManagementView.vue`（患者 CRUD，对接 `/api/admin/patients`），`DoctorManagementView.vue`（医生 CRUD，对接 `/api/admin/doctors` 等）
  - 医生：`DoctorLayout.vue`，`DoctorTodayTodoView.vue`（今日待诊），`DoctorScheduleView.vue`（周排班）
  - 患者：`PatientLayout.vue`，`PatientRegisterView.vue`（注册 `/api/auth/register`），`PatientBookingView.vue`（挂号排班查询+提交）

## 运行
```bash
cd frontend
npm install
npm run dev   # 默认 http://localhost:5173
```
后端默认 `http://localhost:8080`，前端所有 `/api` 请求自动代理。

## 登录账号（后端初始化）
- 患者：`patient001 / 123456`
- 医生：`doc001 / 123456`
- 管理员：`admin / 123456`

## 挂号相关说明
- 排班查询：`/api/schedule/disease/{diseaseId}/timetable?weekday=` 返回能诊疗该病的医生在对应科室的排班（含 `currentPatients/maxPatients/available`），患者端以 8×5 表格展示。
- 挂号提交：`POST /api/registration`，参数 `patientProfileId/doctorProfileId/diseaseId/weekday/timeslot`，后端校验号源并写入 `patient_doctor_registration`（status=PAID）。时段剩余为 0 时前端禁止挂号。
- 号源上限：默认每时段 2；若 `doctor_department_schedule.max_patients_per_slot` 有值，则用排班的值。

## 数据库（挂号相关）
- `doctor_department_schedule` 表含 `max_patients_per_slot`（可空，空用默认 2）。
- 排班剩余号 = 上限 - `patient_doctor_registration` 中 doctorProfileId+weekday+timeslot 且状态 PAID/PENDING/COMPLETED 的记录数。

---
