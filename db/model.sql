/*
 Navicat Premium Dump SQL

 Source Server         : 本地虚拟机192.168.113.130
 Source Server Type    : MySQL
 Source Server Version : 90500 (9.5.0)
 Source Host           : 192.168.113.130:3306
 Source Schema         : model

 Target Server Type    : MySQL
 Target Server Version : 90500 (9.5.0)
 File Encoding         : 65001

 Date: 27/03/2026 17:08:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_model
-- ----------------------------
DROP TABLE IF EXISTS `ai_model`;
CREATE TABLE `ai_model`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider_id` bigint NOT NULL,
  `model_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `display_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `max_context_tokens` int NOT NULL DEFAULT 8000,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `sort_no` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_model_provider_code`(`provider_id` ASC, `model_code` ASC) USING BTREE,
  INDEX `idx_model_sort`(`sort_no` ASC, `id` ASC) USING BTREE,
  CONSTRAINT `fk_model_provider` FOREIGN KEY (`provider_id`) REFERENCES `ai_provider` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_model
-- ----------------------------
INSERT INTO `ai_model` VALUES (1, 1, 'deepseek-chat', 'DeepSeek Chat', 16000, 1, 1);
INSERT INTO `ai_model` VALUES (2, 1, 'deepseek-reasoner', 'DeepSeek Reasoner', 16000, 1, 2);

-- ----------------------------
-- Table structure for ai_provider
-- ----------------------------
DROP TABLE IF EXISTS `ai_provider`;
CREATE TABLE `ai_provider`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `api_key_cipher` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_provider
-- ----------------------------
INSERT INTO `ai_provider` VALUES (1, 'DeepSeek', 'https://api.deepseek.com/v1', 'u3OLPetg0brnIzR+cjPgu2iEiMPiO350xgUc6fgAVxlMG4Tty8RV36OcXyd3cgwIe3D87C5uwsvbmtBqsc0B', 1, '2026-03-27 15:46:26', '2026-03-27 17:05:19');

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `seq_no` int NOT NULL,
  `model_id` bigint NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_seq`(`session_id` ASC, `seq_no` ASC) USING BTREE,
  INDEX `idx_message_session_seq`(`session_id` ASC, `seq_no` ASC) USING BTREE,
  CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (1, 1, 'USER', '你好', 1, 1, 'DONE', '2026-03-27 16:31:32');
INSERT INTO `chat_message` VALUES (2, 1, 'ASSISTANT', '', 2, 1, 'FAILED', '2026-03-27 16:31:32');
INSERT INTO `chat_message` VALUES (3, 1, 'USER', '你好', 3, 1, 'DONE', '2026-03-27 16:49:49');
INSERT INTO `chat_message` VALUES (4, 1, 'ASSISTANT', '', 4, 1, 'FAILED', '2026-03-27 16:49:49');
INSERT INTO `chat_message` VALUES (5, 1, 'USER', '你好', 5, 1, 'DONE', '2026-03-27 16:49:58');
INSERT INTO `chat_message` VALUES (6, 1, 'ASSISTANT', '', 6, 1, 'FAILED', '2026-03-27 16:49:58');
INSERT INTO `chat_message` VALUES (7, 1, 'USER', '你好', 7, 1, 'DONE', '2026-03-27 16:50:36');
INSERT INTO `chat_message` VALUES (8, 1, 'ASSISTANT', '', 8, 1, 'FAILED', '2026-03-27 16:50:36');
INSERT INTO `chat_message` VALUES (9, 2, 'USER', '????:????', 1, 1, 'DONE', '2026-03-27 16:53:53');
INSERT INTO `chat_message` VALUES (10, 2, 'ASSISTANT', '', 2, 1, 'FAILED', '2026-03-27 16:53:53');
INSERT INTO `chat_message` VALUES (11, 2, 'USER', '123', 3, 1, 'DONE', '2026-03-27 17:00:10');
INSERT INTO `chat_message` VALUES (12, 2, 'ASSISTANT', '', 4, 1, 'FAILED', '2026-03-27 17:00:10');
INSERT INTO `chat_message` VALUES (13, 2, 'USER', '123', 5, 1, 'DONE', '2026-03-27 17:00:33');
INSERT INTO `chat_message` VALUES (14, 2, 'ASSISTANT', '', 6, 1, 'FAILED', '2026-03-27 17:00:33');
INSERT INTO `chat_message` VALUES (15, 2, 'USER', '1', 7, 1, 'DONE', '2026-03-27 17:01:34');
INSERT INTO `chat_message` VALUES (16, 2, 'ASSISTANT', '', 8, 1, 'FAILED', '2026-03-27 17:01:34');
INSERT INTO `chat_message` VALUES (17, 2, 'USER', '123', 9, 1, 'DONE', '2026-03-27 17:04:49');
INSERT INTO `chat_message` VALUES (18, 2, 'ASSISTANT', '', 10, 1, 'FAILED', '2026-03-27 17:04:49');
INSERT INTO `chat_message` VALUES (19, 2, 'USER', '123', 11, 1, 'DONE', '2026-03-27 17:05:26');
INSERT INTO `chat_message` VALUES (20, 2, 'ASSISTANT', '看起来你连续发送了“123”和“1”这样的数字序列。  \n\n如果这是一个测试或者误触，我可以忽略这些消息。  \n如果你有其他问题或需要帮助，请告诉我具体内容，我会认真为你解答！ 😊', 12, 1, 'DONE', '2026-03-27 17:05:26');
INSERT INTO `chat_message` VALUES (21, 2, 'USER', '你好', 13, 1, 'DONE', '2026-03-27 17:05:44');
INSERT INTO `chat_message` VALUES (22, 2, 'ASSISTANT', '你好！😊 很高兴见到你！\n\n有什么我可以帮助你的吗？无论是回答问题、提供建议、协助学习，还是聊天解闷，我都很乐意为你效劳！请随时告诉我你需要什么～', 14, 1, 'DONE', '2026-03-27 17:05:44');
INSERT INTO `chat_message` VALUES (23, 2, 'USER', '你是什么模型', 15, 1, 'DONE', '2026-03-27 17:05:53');
INSERT INTO `chat_message` VALUES (24, 2, 'ASSISTANT', '我是DeepSeek，由深度求索公司创造的AI大语言模型！😊\n\n关于我的基本信息：\n- **身份**：DeepSeek最新版本模型\n- **知识截止**：2024年7月\n- **上下文长度**：128K\n- **功能特点**：\n  - 纯文本模型，支持文件上传（可处理图像、txt、pdf、ppt、word、excel等文件并读取其中的文字信息）\n  - 支持联网搜索（需要用户在Web/App中手动点开联网搜索按键）\n  - 完全免费使用\n- **服务方式**：通过官方应用商店下载App使用\n\n我没有语音功能，但可以帮你处理各种文本任务，包括回答问题、写作、分析、编程、学习辅导等等。有什么特别想了解的，或者需要我帮助的吗？我会热情地为你服务！✨', 16, 1, 'DONE', '2026-03-27 17:05:53');

-- ----------------------------
-- Table structure for chat_session
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `last_model_id` bigint NULL DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_updated_at`(`updated_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_session
-- ----------------------------
INSERT INTO `chat_session` VALUES (1, '新会话', 1, 0, '2026-03-27 16:28:31', '2026-03-27 16:50:36');
INSERT INTO `chat_session` VALUES (2, '??????', 1, 0, '2026-03-27 16:53:53', '2026-03-27 17:06:02');

SET FOREIGN_KEY_CHECKS = 1;
