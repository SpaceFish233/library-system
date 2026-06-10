-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: librarysystem
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `librarysystem`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `librarysystem` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `librarysystem`;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `isbn` varchar(20) DEFAULT NULL COMMENT 'ISBN',
  `title` varchar(100) NOT NULL COMMENT '书名',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `publisher` varchar(50) DEFAULT NULL COMMENT '出版社',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `cover_image` varchar(200) DEFAULT NULL COMMENT '封面图片',
  `description` text COMMENT '简介',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `book_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `book_category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图书表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book`
--

LOCK TABLES `book` WRITE;
/*!40000 ALTER TABLE `book` DISABLE KEYS */;
INSERT INTO `book` VALUES (1,'9787020002207','红楼梦','曹雪芹','人民文学出版社',5,'9b3583cc-adf8-48fd-9c6e-1ed8d774dee1.jpg','中国古典四大名著之一',5,'2026-06-04 14:59:16','2026-06-09 16:39:20'),(2,'9787020008735','西游记','吴承恩','人民文学出版社',5,'17a2f633-d6de-45d3-bdc0-17be79a26906.jpg','中国古典四大名著之一',3,'2026-06-04 14:59:16','2026-06-09 16:40:26'),(3,'9787020008742','水浒传','施耐庵','人民文学出版社',5,'ecc604e4-9b81-4757-adbd-824ca33a2625.jpg','中国古典四大名著之一',4,'2026-06-04 14:59:16','2026-06-09 16:42:12'),(4,'9787020008728','三国演义','罗贯中','人民文学出版社',5,'3b457964-7c0c-4df5-9231-994e4172d2b4.jpg','中国古典四大名著之一',2,'2026-06-04 14:59:16','2026-06-09 16:42:57'),(5,'9787115279460','C++ Primer Plus','[美]StephenPrata 著,张海龙,袁国忠 译','人民邮电出版社',8,'38b5d1b6-ded8-44e7-bb38-c196f22e9853.webp','Java经典教程',3,'2026-06-04 14:59:16','2026-06-09 16:58:29'),(6,'9787302683667','Spring Boot源码精讲','王涛','清华大学出版社',8,'a3579c31-0c39-4fa4-be83-3b0b91df7a1b.jpg','对Spring Boot框架进行完善的源码分析',1,'2026-06-04 14:59:16','2026-06-09 16:53:48'),(7,'9787020008728','史记','司马迁','上海古籍出版社',4,'76faad3d-17d4-4193-8a11-3145825c0c6d.jpg','“前四史”之一；史学经典著作',1,'2026-06-09 16:44:50','2026-06-09 16:44:50'),(8,'9787550225909','诗经','李青,译','北京联合出版公司',7,'cc03c8bf-8361-4209-bd5b-573fe41c3200.jpg','中华国学经典精粹·诗词文论本',1,'2026-06-09 16:51:29','2026-06-09 16:51:29'),(9,'9787115373892','教育心理学：理论与实践','［美］罗伯特·斯莱文 著，吕红梅，姚梅林 等译','人民邮电出版社',3,'272146c3-93aa-4025-ba23-ec552e62ac41.jpg','一部堪称典范的教育心理学教材，教学成果奖获得者姚梅林教授领衔翻译，中国心理学界泰斗张厚粲教授、中国心理学会教育心理学专业委员会主任陈英和教授倾力推荐',1,'2026-06-09 17:04:28','2026-06-09 17:04:28'),(10,'9787502595968','信号与系统','于慧敏 等编著','化学工业出版社',9,'8086c3a7-0bf8-44e3-ad0b-461e04419f0a.jpg','电子信息类专业常用教学用书',1,'2026-06-09 17:09:09','2026-06-09 17:10:34'),(11,'9787577203027','机械原理课程设计','刘晓阳','华中科技大学出版社',10,'522dfbf4-7608-4ed7-887e-c2a51311465c.jpg','机械类专业常用教学用书',1,'2026-06-09 17:10:29','2026-06-09 17:10:43'),(12,'9787540364519','朝花夕拾','鲁迅','崇文书局（原湖北辞书出版社）',6,'ddf1ad0b-6f40-444b-b6ea-1cd27238e99f.jpg','鲁迅经典回忆性散文集，承载着一代文豪 温馨的回忆 和 理性的批判 。以关注人生、关注社会的态度完整记录了自己的生活道路和成长经历，字里行间传递着正义、进步的力量。',1,'2026-06-09 17:35:39','2026-06-09 17:35:39');
/*!40000 ALTER TABLE `book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book_category`
--

DROP TABLE IF EXISTS `book_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID, 0表示一级分类',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图书分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_category`
--

LOCK TABLES `book_category` WRITE;
/*!40000 ALTER TABLE `book_category` DISABLE KEYS */;
INSERT INTO `book_category` VALUES (1,'文学',0,1,'2026-06-04 14:59:16','2026-06-04 14:59:16'),(2,'科技',0,2,'2026-06-04 14:59:16','2026-06-04 14:59:16'),(3,'教育',0,3,'2026-06-04 14:59:16','2026-06-04 14:59:16'),(4,'历史',0,4,'2026-06-04 14:59:16','2026-06-04 14:59:16'),(5,'小说',1,1,'2026-06-04 14:59:16','2026-06-04 14:59:16'),(6,'散文',1,2,'2026-06-04 14:59:16','2026-06-04 14:59:16'),(7,'诗歌',1,3,'2026-06-04 14:59:16','2026-06-04 14:59:16'),(8,'计算机',2,1,'2026-06-04 14:59:16','2026-06-04 14:59:16'),(9,'电子',2,2,'2026-06-04 14:59:16','2026-06-04 14:59:16'),(10,'机械',2,3,'2026-06-04 14:59:16','2026-06-04 14:59:16');
/*!40000 ALTER TABLE `book_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `borrow_record`
--

DROP TABLE IF EXISTS `borrow_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `borrow_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_id` bigint NOT NULL COMMENT '图书ID',
  `borrow_date` date NOT NULL COMMENT '借书日期',
  `due_date` datetime NOT NULL,
  `return_date` date DEFAULT NULL COMMENT '实际归还日期',
  `status` varchar(20) NOT NULL COMMENT '状态: BORROWED/RETURNED/OVERDUE',
  `fine_amount` decimal(10,2) DEFAULT '0.00' COMMENT '罚金',
  `overdue_days` int DEFAULT '0' COMMENT '逾期天数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `borrow_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `borrow_record_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='借阅记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `borrow_record`
--

LOCK TABLES `borrow_record` WRITE;
/*!40000 ALTER TABLE `borrow_record` DISABLE KEYS */;
INSERT INTO `borrow_record` VALUES (1,2,1,'2026-06-04','2026-07-04 00:00:00','2026-06-04','RETURNED',0.00,0,'2026-06-04 16:01:04','2026-06-04 16:02:19'),(2,2,2,'2026-06-04','2026-07-04 00:00:00','2026-06-04','RETURNED',0.00,0,'2026-06-04 16:02:05','2026-06-04 16:02:17'),(3,2,1,'2026-06-04','2026-07-04 00:00:00','2026-06-04','RETURNED',0.00,0,'2026-06-04 16:08:01','2026-06-04 16:31:33'),(4,1,1,'2026-06-04','2026-06-04 00:00:00','2026-06-04','RETURNED',0.00,0,'2026-06-04 16:30:46','2026-06-04 16:30:55'),(5,2,1,'2026-06-04','2026-06-04 00:00:00','2026-06-04','RETURNED',0.50,1,'2026-06-04 16:31:45','2026-06-04 16:54:52');
/*!40000 ALTER TABLE `borrow_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `content` varchar(500) NOT NULL,
  `type` varchar(50) NOT NULL,
  `related_id` bigint DEFAULT NULL,
  `is_read` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,2,'您预约的《Spring实战》目前库存为1，请尽快领取','RESERVE_READY',2,1,'2026-06-04 17:31:54','2026-06-04 17:36:34');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reserve_record`
--

DROP TABLE IF EXISTS `reserve_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reserve_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_id` bigint NOT NULL COMMENT '图书ID',
  `reserve_time` datetime NOT NULL COMMENT '预约时间',
  `status` varchar(20) NOT NULL COMMENT '状态: WAITING/READY/CANCELLED/FULFILLED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `reserve_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reserve_record_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reserve_record`
--

LOCK TABLES `reserve_record` WRITE;
/*!40000 ALTER TABLE `reserve_record` DISABLE KEYS */;
INSERT INTO `reserve_record` VALUES (1,1,6,'2026-06-04 16:07:18','CANCELLED','2026-06-04 16:07:18','2026-06-04 17:31:24'),(2,2,6,'2026-06-04 16:08:22','READY','2026-06-04 16:08:22','2026-06-04 17:31:54'),(3,1,6,'2026-06-04 17:31:37','WAITING','2026-06-04 17:31:37','2026-06-04 17:31:37');
/*!40000 ALTER TABLE `reserve_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role` varchar(20) NOT NULL DEFAULT 'user' COMMENT '角色: user/admin',
  `credit_score` int NOT NULL DEFAULT '100' COMMENT '信用分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$Z93yrI8uS5zNHcQ3s/.8B.vB6rRLFTLqTR8WJfBHq1eGI/n03Wodq','系统管理员',NULL,'admin',100,'2026-06-04 14:59:16','2026-06-04 16:06:17'),(2,'hechunqiu','$2a$10$slOgcIfPQNkgsXwNgRLnxOWqi4DKc4iBhlXWjSg6BvBCHZA90rYdm','何春秋',NULL,'user',98,'2026-06-04 16:00:28','2026-06-04 16:54:52');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-10 17:20:24
