-- 示例医生排班数据插入脚本
-- 依赖 init.sql 已经初始化完基础表和医生数据

-- 内科医生 doc001 (00000001) 一周排班：周一到周五 上午 AM1, AM2
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 1, 'AM1' FROM doctor_profile WHERE doctor_id = '00000001';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 1, 'AM2' FROM doctor_profile WHERE doctor_id = '00000001';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'AM1' FROM doctor_profile WHERE doctor_id = '00000001';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'AM2' FROM doctor_profile WHERE doctor_id = '00000001';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'AM1' FROM doctor_profile WHERE doctor_id = '00000001';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'AM2' FROM doctor_profile WHERE doctor_id = '00000001';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'AM1' FROM doctor_profile WHERE doctor_id = '00000001';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'AM2' FROM doctor_profile WHERE doctor_id = '00000001';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 5, 'AM1' FROM doctor_profile WHERE doctor_id = '00000001';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 5, 'AM2' FROM doctor_profile WHERE doctor_id = '00000001';

-- 外科医生 doc002 (00000002) 一周排班：周一、周三、周五 下午 PM1, PM2
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 1, 'PM1' FROM doctor_profile WHERE doctor_id = '00000002';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 1, 'PM2' FROM doctor_profile WHERE doctor_id = '00000002';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'PM1' FROM doctor_profile WHERE doctor_id = '00000002';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'PM2' FROM doctor_profile WHERE doctor_id = '00000002';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 5, 'PM1' FROM doctor_profile WHERE doctor_id = '00000002';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 5, 'PM2' FROM doctor_profile WHERE doctor_id = '00000002';

-- 内科医生 doc003 (00000003) 一周排班：周二、周四 全天 AM1, AM2, PM1, PM2
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'AM1' FROM doctor_profile WHERE doctor_id = '00000003';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'AM2' FROM doctor_profile WHERE doctor_id = '00000003';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'PM1' FROM doctor_profile WHERE doctor_id = '00000003';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'PM2' FROM doctor_profile WHERE doctor_id = '00000003';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'AM1' FROM doctor_profile WHERE doctor_id = '00000003';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'AM2' FROM doctor_profile WHERE doctor_id = '00000003';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'PM1' FROM doctor_profile WHERE doctor_id = '00000003';
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'PM2' FROM doctor_profile WHERE doctor_id = '00000003';

-- 内科医生 doc004 (00000004) - Dr. Linda Zhao
-- 排班：周一上午，周三全天，周四下午
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 1, 'AM1' FROM doctor_profile WHERE doctor_id = '00000004';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 1, 'AM2' FROM doctor_profile WHERE doctor_id = '00000004';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'AM1' FROM doctor_profile WHERE doctor_id = '00000004';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'AM2' FROM doctor_profile WHERE doctor_id = '00000004';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'PM1' FROM doctor_profile WHERE doctor_id = '00000004';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'PM2' FROM doctor_profile WHERE doctor_id = '00000004';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'PM1' FROM doctor_profile WHERE doctor_id = '00000004';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'PM2' FROM doctor_profile WHERE doctor_id = '00000004';

-- 内科医生 doc005 (00000005) - Dr. Michael Liu
-- 排班：周二、周四、周五的下午时段
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'PM1' FROM doctor_profile WHERE doctor_id = '00000005';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'PM2' FROM doctor_profile WHERE doctor_id = '00000005';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'PM1' FROM doctor_profile WHERE doctor_id = '00000005';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'PM2' FROM doctor_profile WHERE doctor_id = '00000005';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 5, 'PM1' FROM doctor_profile WHERE doctor_id = '00000005';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 5, 'PM2' FROM doctor_profile WHERE doctor_id = '00000005';

-- 外科医生 doc006 (00000006) - Dr. Sophia Wu
-- 排班：周一、周三、周五的上午时段
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 1, 'AM1' FROM doctor_profile WHERE doctor_id = '00000006';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 1, 'AM2' FROM doctor_profile WHERE doctor_id = '00000006';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'AM1' FROM doctor_profile WHERE doctor_id = '00000006';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'AM2' FROM doctor_profile WHERE doctor_id = '00000006';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 5, 'AM1' FROM doctor_profile WHERE doctor_id = '00000006';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 5, 'AM2' FROM doctor_profile WHERE doctor_id = '00000006';

-- 外科医生 doc007 (00000007) - Dr. David Zheng
-- 排班：周二、周四的下午时段，周三的上午时段
INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'PM1' FROM doctor_profile WHERE doctor_id = '00000007';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 2, 'PM2' FROM doctor_profile WHERE doctor_id = '00000007';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'AM1' FROM doctor_profile WHERE doctor_id = '00000007';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 3, 'AM2' FROM doctor_profile WHERE doctor_id = '00000007';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'PM1' FROM doctor_profile WHERE doctor_id = '00000007';

INSERT INTO doctor_department_schedule (doctor_profile_id, department_id, weekday, timeslot)
SELECT id, department_id, 4, 'PM2' FROM doctor_profile WHERE doctor_id = '00000007';

