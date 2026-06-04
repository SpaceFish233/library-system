-- 创建数据库
CREATE DATABASE IF NOT EXISTS librarysystem DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE librarysystem;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `phone` VARCHAR(20) COMMENT '手机号',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: user/admin',
    `credit_score` INT NOT NULL DEFAULT 100 COMMENT '信用分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 图书分类表
CREATE TABLE IF NOT EXISTS `book_category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID, 0表示一级分类',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 图书表
CREATE TABLE IF NOT EXISTS `book` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `isbn` VARCHAR(20) COMMENT 'ISBN',
    `title` VARCHAR(100) NOT NULL COMMENT '书名',
    `author` VARCHAR(50) COMMENT '作者',
    `publisher` VARCHAR(50) COMMENT '出版社',
    `category_id` BIGINT COMMENT '分类ID',
    `cover_image` VARCHAR(200) COMMENT '封面图片',
    `description` TEXT COMMENT '简介',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`category_id`) REFERENCES `book_category`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 借阅记录表
CREATE TABLE IF NOT EXISTS `borrow_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `book_id` BIGINT NOT NULL COMMENT '图书ID',
    `borrow_date` DATE NOT NULL COMMENT '借书日期',
    `due_date` DATE NOT NULL COMMENT '应还日期',
    `return_date` DATE COMMENT '实际归还日期',
    `status` VARCHAR(20) NOT NULL COMMENT '状态: BORROWED/RETURNED/OVERDUE',
    `fine_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '罚金',
    `overdue_days` INT DEFAULT 0 COMMENT '逾期天数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 预约记录表
CREATE TABLE IF NOT EXISTS `reserve_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `book_id` BIGINT NOT NULL COMMENT '图书ID',
    `reserve_time` DATETIME NOT NULL COMMENT '预约时间',
    `status` VARCHAR(20) NOT NULL COMMENT '状态: WAITING/READY/CANCELLED/FULFILLED',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约记录表';

-- 初始化管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `credit_score`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin', 100);

-- 初始化分类数据
INSERT INTO `book_category` (`name`, `parent_id`, `sort_order`) VALUES
('文学', 0, 1),
('科技', 0, 2),
('教育', 0, 3),
('历史', 0, 4),
('小说', 1, 1),
('散文', 1, 2),
('诗歌', 1, 3),
('计算机', 2, 1),
('电子', 2, 2),
('机械', 2, 3);

-- 初始化图书数据
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `category_id`, `description`, `stock`) VALUES
('978-7-02-000001-1', '红楼梦', '曹雪芹', '人民文学出版社', 5, '中国古典四大名著之一', 5),
('978-7-02-000002-2', '西游记', '吴承恩', '人民文学出版社', 5, '中国古典四大名著之一', 3),
('978-7-02-000003-3', '水浒传', '施耐庵', '人民文学出版社', 5, '中国古典四大名著之一', 4),
('978-7-02-000004-4', '三国演义', '罗贯中', '人民文学出版社', 5, '中国古典四大名著之一', 2),
('978-7-111-00001-1', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', 8, 'Java经典教程', 3),
('978-7-111-00002-2', 'Spring实战', 'Craig Walls', '人民邮电出版社', 8, 'Spring框架权威指南', 2);
