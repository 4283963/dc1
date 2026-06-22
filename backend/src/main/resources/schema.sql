-- 创建数据库（需要在 postgres 中执行）
-- CREATE DATABASE jubensha;

-- 剧本表
CREATE TABLE IF NOT EXISTS script (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_players INTEGER NOT NULL,
    min_male INTEGER,
    min_female INTEGER,
    price INTEGER,
    duration INTEGER,
    description TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 房间表
CREATE TABLE IF NOT EXISTS room (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 拼单表
CREATE TABLE IF NOT EXISTS group_order (
    id BIGSERIAL PRIMARY KEY,
    script_id BIGINT NOT NULL,
    room_id BIGINT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    current_male INTEGER NOT NULL DEFAULT 0,
    current_female INTEGER NOT NULL DEFAULT 0,
    total_players INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    remark TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_order_status ON group_order(status);
CREATE INDEX IF NOT EXISTS idx_order_start_time ON group_order(start_time);
CREATE INDEX IF NOT EXISTS idx_order_room ON group_order(room_id);

-- 插入示例剧本
INSERT INTO script (name, total_players, min_male, min_female, price, duration, description, created_at, updated_at) VALUES
('古木吟', 6, 3, 3, 128, 240, '情感沉浸本，校园背景，哭哭本首选', NOW(), NOW()),
('病娇男孩的精分日记', 7, 4, 2, 138, 300, '硬核推理本，多重人格，烧脑神作', NOW(), NOW()),
('漓川怪谈簿', 7, 3, 4, 148, 360, '日式妖怪本，变格推理，机制新颖', NOW(), NOW()),
('年轮', 5, 3, 2, 118, 240, '经典硬核本，逻辑闭环，口碑神作', NOW(), NOW()),
('来电', 6, 3, 3, 98, 180, '欢乐机制本，新手友好，团建推荐', NOW(), NOW()),
('窗边的女人', 6, 4, 2, 108, 240, '本格推理本，真实案件改编，细思极恐', NOW(), NOW());

-- 插入示例房间
INSERT INTO room (name, capacity, description, created_at, updated_at) VALUES
('一号厅·日式', 8, '日式榻榻米风格，适合沉浸本', NOW(), NOW()),
('二号厅·古风', 10, '中式古风装修，适合古装本', NOW(), NOW()),
('三号厅·现代', 7, '现代简约风格，适合硬核本', NOW(), NOW()),
('四号厅·恐怖', 6, '暗黑恐怖风格，适合恐怖本', NOW(), NOW()),
('五号厅·欢乐', 9, '明亮活泼风格，适合欢乐机制本', NOW(), NOW());
