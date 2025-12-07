-- ==========================================
-- 周末医生值班排班表 (周六、周日)
-- 依赖 init.sql 已经初始化完基础表和医生数据
-- 值班时段：MORNING(早班 08:00-12:00), AFTERNOON(中班 12:00-18:00), NIGHT(夜班 18:00-次日08:00)
-- ==========================================

-- 内科（Department 1）周六值班
-- 采用早中晚三班制，不同医生轮值
INSERT INTO doctor_duty_schedule (department_id, doctor_profile_id, weekend_type, duty_timeslot)
VALUES
    -- 周六早班：Dr. John Chen
    (1, (SELECT id FROM doctor_profile WHERE doctor_id = '00000001'), 6, 'MORNING'),
    -- 周六中班：Dr. Linda Zhao
    (1, (SELECT id FROM doctor_profile WHERE doctor_id = '00000004'), 6, 'AFTERNOON'),
    -- 周六晚班：Dr. Kevin Zhou
    (1, (SELECT id FROM doctor_profile WHERE doctor_id = '00000003'), 6, 'NIGHT');

-- 内科周日值班
INSERT INTO doctor_duty_schedule (department_id, doctor_profile_id, weekend_type, duty_timeslot)
VALUES
    -- 周日上午：Dr. Michael Liu
    (1, (SELECT id FROM doctor_profile WHERE doctor_id = '00000005'), 7, 'MORNING'),
    -- 周日下午：Dr. Linda Zhao (周六中班后休息半天，周日继续)
    (1, (SELECT id FROM doctor_profile WHERE doctor_id = '00000004'), 7, 'AFTERNOON'),
    -- 周日晚上：Dr. John Chen (周六早班后休息一天，周日值晚班)
    (1, (SELECT id FROM doctor_profile WHERE doctor_id = '00000001'), 7, 'NIGHT');

-- 外科（Department 2）周六值班
INSERT INTO doctor_duty_schedule (department_id, doctor_profile_id, weekend_type, duty_timeslot)
VALUES
    -- 周六早班：Dr. Emily Sun
    (2, (SELECT id FROM doctor_profile WHERE doctor_id = '00000002'), 6, 'MORNING'),
    -- 周六中班：Dr. Sophia Wu
    (2, (SELECT id FROM doctor_profile WHERE doctor_id = '00000006'), 6, 'AFTERNOON'),
    -- 周六晚班：Dr. David Zheng
    (2, (SELECT id FROM doctor_profile WHERE doctor_id = '00000007'), 6, 'NIGHT');

-- 外科周日值班
INSERT INTO doctor_duty_schedule (department_id, doctor_profile_id, weekend_type, duty_timeslot)
VALUES
    -- 周日上午：Dr. David Zheng (周六晚班后休息半天)
    (2, (SELECT id FROM doctor_profile WHERE doctor_id = '00000007'), 7, 'MORNING'),
    -- 周日下午：Dr. Emily Sun (周六早班后休息一天)
    (2, (SELECT id FROM doctor_profile WHERE doctor_id = '00000002'), 7, 'AFTERNOON'),
    -- 周日晚上：Dr. Sophia Wu (周六中班后休息一天)
    (2, (SELECT id FROM doctor_profile WHERE doctor_id = '00000006'), 7, 'NIGHT');
