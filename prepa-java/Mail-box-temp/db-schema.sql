-- MySQL schema for Mail-Box
CREATE DATABASE IF NOT EXISTS mailbox CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mailbox;

-- Table to store emails (sent and received)
CREATE TABLE IF NOT EXISTS emails (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  external_id VARCHAR(255) NULL,
  direction ENUM('SENT','RECEIVED') NOT NULL,
  sender VARCHAR(255) NULL,
  recipient VARCHAR(255) NULL,
  subject VARCHAR(512) NULL,
  body MEDIUMTEXT NULL,
  received_at DATETIME NOT NULL,
  INDEX idx_external_id (external_id),
  INDEX idx_received_at (received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Optional: store accounts/config snapshots
CREATE TABLE IF NOT EXISTS accounts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  provider VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

