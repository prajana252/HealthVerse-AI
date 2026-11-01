-- Optional: manual MySQL setup. By default Hibernate (ddl-auto=update) creates these tables itself.
CREATE DATABASE IF NOT EXISTS healthverse CHARACTER SET utf8mb4;
USE healthverse;

CREATE TABLE IF NOT EXISTS assessments (
  id                VARCHAR(36)  PRIMARY KEY,
  created_at        DATETIME(6)  NOT NULL,
  label             VARCHAR(60),
  access_code_hash  VARCHAR(64)  NOT NULL,
  overall_indicator VARCHAR(20),
  bmi               DOUBLE,
  risk_factor_count INT          NOT NULL DEFAULT 0,
  demo              BOOLEAN      NOT NULL DEFAULT FALSE,
  request_json      TEXT,
  result_json       TEXT,
  plan_json         TEXT
);

CREATE TABLE IF NOT EXISTS risk_results (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  assessment_id VARCHAR(36) NOT NULL,
  category      VARCHAR(80),
  risk_level    VARCHAR(20),
  risk_score    INT,
  FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recommendations (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  assessment_id VARCHAR(36) NOT NULL,
  category      VARCHAR(80),
  risk_level    VARCHAR(20),
  meaning       VARCHAR(600),
  FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE
);
