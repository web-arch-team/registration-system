-- ==========================================
-- Drop existing tables (avoid conflicts)
-- ==========================================
DROP TABLE IF EXISTS doctor_department_schedule CASCADE;
DROP TABLE IF EXISTS patient_doctor_registration CASCADE;
DROP TABLE IF EXISTS doctor_disease CASCADE;
DROP TABLE IF EXISTS disease CASCADE;
DROP TABLE IF EXISTS doctor_profile CASCADE;
DROP TABLE IF EXISTS patient_profile CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;
DROP TABLE IF EXISTS department CASCADE;

-- ==========================================
-- Drop ENUM types (统一改为 VARCHAR+CHECK)
-- ==========================================
-- DROP TYPE IF EXISTS gender_enum CASCADE;
DROP TYPE IF EXISTS time_slot CASCADE;

-- ==========================================
-- ENUM Definitions
-- ==========================================
-- 性别与时段均不再使用数据库 ENUM 类型，统一改为 VARCHAR + CHECK 约束
-- （便于跨数据库迁移与与 JPA @Enumerated(EnumType.STRING) 的一致性）

-- ==========================================
-- User Table (Unified Login Table)
-- ==========================================
CREATE TABLE app_user (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,     -- 'PATIENT' / 'DOCTOR' / 'ADMIN'
    created_at TIMESTAMP DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- ==========================================
-- Department Table
-- ==========================================
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL
);

-- Insert basic departments
INSERT INTO department (department_name) VALUES
    ('Internal Medicine'),
    ('Surgical');

-- ==========================================
-- Patient Profile
-- ==========================================
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

-- ==========================================
-- Doctor Profile
-- ==========================================
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

-- ==========================================
-- Disease Table
-- ==========================================
CREATE TABLE disease (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE,
    description TEXT,
    department_id INT NOT NULL REFERENCES department(id)
);

-- Insert sample diseases for departments
-- Internal Medicine (id=1)
INSERT INTO disease (name, code, description, department_id) VALUES
    ('Heart Disease', 'IM-HD', '心脏相关疾病', 1),
    ('Liver Disease', 'IM-LD', '肝脏相关疾病', 1),
    ('Spleen Disease', 'IM-SD', '脾脏相关疾病', 1),
    ('Stomach Disease', 'IM-ST', '胃部相关疾病', 1),
    ('Kidney Disease', 'IM-KD', '肾脏相关疾病', 1);

-- Surgical (id=2)
INSERT INTO disease (name, code, description, department_id) VALUES
    ('Hand and Foot', 'SU-HF', '手足相关疾病/外伤', 2),
    ('Thoracic Surgery', 'SU-TS', '胸外科相关疾病', 2),
    ('Joint Disease', 'SU-JD', '关节相关疾病', 2),
    ('Burns', 'SU-BN', '烧伤相关', 2),
    ('Plastic Surgery', 'SU-PS', '整形相关', 2);

-- ==========================================
-- Doctor-Disease Relation Table
-- ==========================================
CREATE TABLE doctor_disease (
    id SERIAL PRIMARY KEY,
    doctor_profile_id INT NOT NULL REFERENCES doctor_profile(id),
    disease_id INT NOT NULL REFERENCES disease(id),
    UNIQUE (doctor_profile_id, disease_id)
);

-- ==========================================
-- Registration Table (by Disease)
-- ==========================================
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

-- ==========================================
-- Doctor Department Schedule
-- ==========================================
CREATE TABLE doctor_department_schedule (
    id SERIAL PRIMARY KEY,
    doctor_profile_id INT REFERENCES doctor_profile(id),
    department_id INT REFERENCES department(id),
    weekday INT NOT NULL CHECK (weekday BETWEEN 1 AND 5),
    timeslot VARCHAR(4) NOT NULL CHECK (timeslot IN ('AM1','AM2','AM3','AM4','PM1','PM2','PM3','PM4')),
    max_patients_per_slot INT,

    UNIQUE (doctor_profile_id, weekday, timeslot)
);

-- 周末值班表（仅周六/周日，分早中晚三个时段）
-- 周末值班表（仅周六/周日，分早中晚三个时段）
CREATE TABLE doctor_duty_schedule (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL,
    doctor_profile_id BIGINT NOT NULL,
    weekend_type INT NOT NULL CHECK (weekend_type IN (6, 7)), -- 6=周六，7=周日
    duty_timeslot VARCHAR(20) NOT NULL CHECK (duty_timeslot IN ('MORNING', 'AFTERNOON', 'NIGHT')), -- 早班/中班/夜班
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 唯一约束：同一科室、同一周末类型、同一时段只能有一个值班医生
    UNIQUE (department_id, weekend_type, duty_timeslot),
    -- 外键关联
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_profile_id) REFERENCES doctor_profile(id) ON DELETE CASCADE
);

-- ==========================================
-- Insert Example Data
-- ==========================================

-- Enable pgcrypto for hashing (sha256 with salt)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ADMIN account（密码+盐 与后端一致）
INSERT INTO app_user (username, password, role)
VALUES ('admin', encode(digest('123456' || 'OucWebDev123', 'sha256'), 'hex'), 'ADMIN');

-- ==========================================
-- PATIENTS (3 examples)
-- ==========================================

-- Patient 1
INSERT INTO app_user (username, password, role)
VALUES ('patient001', encode(digest('123456' || 'OucWebDev123', 'sha256'), 'hex'), 'PATIENT');
INSERT INTO patient_profile (user_id, id_card, name, phone_number, age, gender)
VALUES (
    (SELECT id FROM app_user WHERE username='patient001'),
    '110101200001015555',
    'Alice Zhang',
    '13800000001',
    23,
    'female'
);

-- Patient 2
INSERT INTO app_user (username, password, role)
VALUES ('patient002', encode(digest('123456' || 'OucWebDev123', 'sha256'), 'hex'), 'PATIENT');
INSERT INTO patient_profile (user_id, id_card, name, phone_number, age, gender)
VALUES (
    (SELECT id FROM app_user WHERE username='patient002'),
    '110101199905203333',
    'Bob Li',
    '13800000002',
    25,
    'male'
);

-- Patient 3
INSERT INTO app_user (username, password, role)
VALUES ('patient003', encode(digest('123456' || 'OucWebDev123', 'sha256'), 'hex'), 'PATIENT');
INSERT INTO patient_profile (user_id, id_card, name, phone_number, age, gender)
VALUES (
    (SELECT id FROM app_user WHERE username='patient003'),
    '110101199802105666',
    'Charlie Wang',
    '13800000003',
    26,
    'male'
);

-- ==========================================
-- DOCTORS (3 examples)
-- ==========================================

-- Doctor 1 (Internal Medicine)
INSERT INTO app_user (username, password, role)
VALUES ('doc001', encode(digest('123456' || 'OucWebDev123', 'sha256'), 'hex'), 'DOCTOR');
INSERT INTO doctor_profile (user_id, doctor_id, name, age, gender, title, department_id)
VALUES (
    (SELECT id FROM app_user WHERE username='doc001'),
    '00000001',
    'Dr. John Chen',
    40,
    'male',
    'Attending Physician',
    1  -- Internal Medicine
);

-- Doctor 2 (Surgical)
INSERT INTO app_user (username, password, role)
VALUES ('doc002', encode(digest('123456' || 'OucWebDev123', 'sha256'), 'hex'), 'DOCTOR');
INSERT INTO doctor_profile (user_id, doctor_id, name, age, gender, title, department_id)
VALUES (
    (SELECT id FROM app_user WHERE username='doc002'),
    '00000002',
    'Dr. Emily Sun',
    35,
    'female',
    'Surgeon',
    2  -- Surgical
);

-- Doctor 3 (Internal Medicine)
INSERT INTO app_user (username, password, role)
VALUES ('doc003', encode(digest('123456' || 'OucWebDev123', 'sha256'), 'hex'), 'DOCTOR');
INSERT INTO doctor_profile (user_id, doctor_id, name, age, gender, title, department_id)
VALUES (
    (SELECT id FROM app_user WHERE username='doc003'),
    '00000003',
    'Dr. Kevin Zhou',
    45,
    'male',
    'Chief Physician',
    1  -- Internal Medicine
);

-- ==========================================
-- Map Doctors to Diseases (limit 1~3 per doctor)
-- ==========================================
-- 选择固定的 1~3 个疾病映射到医生，满足约束

-- doc001 -> 3 Internal Medicine diseases: Heart, Liver, Kidney
INSERT INTO doctor_disease (doctor_profile_id, disease_id)
VALUES
    ((SELECT id FROM doctor_profile WHERE doctor_id = '00000001'), (SELECT id FROM disease WHERE code = 'IM-HD')),
    ((SELECT id FROM doctor_profile WHERE doctor_id = '00000001'), (SELECT id FROM disease WHERE code = 'IM-LD')),
    ((SELECT id FROM doctor_profile WHERE doctor_id = '00000001'), (SELECT id FROM disease WHERE code = 'IM-KD'));

-- doc002 -> 2 Surgical diseases: Thoracic Surgery, Burns
INSERT INTO doctor_disease (doctor_profile_id, disease_id)
VALUES
    ((SELECT id FROM doctor_profile WHERE doctor_id = '00000002'), (SELECT id FROM disease WHERE code = 'SU-TS')),
    ((SELECT id FROM doctor_profile WHERE doctor_id = '00000002'), (SELECT id FROM disease WHERE code = 'SU-BN'));

-- doc003 -> 1 Internal Medicine disease: Stomach Disease
INSERT INTO doctor_disease (doctor_profile_id, disease_id)
VALUES
    ((SELECT id FROM doctor_profile WHERE doctor_id = '00000003'), (SELECT id FROM disease WHERE code = 'IM-ST'));
