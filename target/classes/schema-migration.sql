-- Migration: add new columns to existing tables (may fail if columns exist - continue-on-error handles it)
ALTER TABLE user_education ADD COLUMN total_marks INT;
ALTER TABLE user_education ADD COLUMN marks_obtained INT;
ALTER TABLE user_education ADD COLUMN cgpa DECIMAL(4,2);
ALTER TABLE user_education ADD COLUMN document_path VARCHAR(500);

ALTER TABLE user_experience ADD COLUMN document_path VARCHAR(500);

ALTER TABLE user_programming_languages ADD COLUMN certificate_company VARCHAR(200);
ALTER TABLE user_programming_languages ADD COLUMN certificate_file VARCHAR(500);

-- Current programme (B.Tech / Diploma / M.Tech from edit profile) for reporting / charts
ALTER TABLE user_profile ADD COLUMN current_course VARCHAR(50);

CREATE TABLE IF NOT EXISTS meeting_chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_role VARCHAR(20),
    sender_name VARCHAR(200),
    sender_user_id INT,
    from_client_id VARCHAR(64),
    message_text TEXT,
    file_name VARCHAR(500),
    file_url VARCHAR(500),
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
ALTER TABLE user_profile ADD COLUMN current_college_code VARCHAR(50);
ALTER TABLE user_profile ADD COLUMN current_branch VARCHAR(100);

-- Last update time for profile row (MySQL auto-updates on any column change to user_profile)
-- Fails silently if column already exists (spring.sql.init.continue-on-error=true).
ALTER TABLE user_profile ADD COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Optional manual reset of live meeting chat (normally cleared automatically when admin ends meeting via API).
-- DELETE FROM meeting_chat_messages;

CREATE TABLE IF NOT EXISTS geocode_cache (
    id INT AUTO_INCREMENT PRIMARY KEY,
    query_key VARCHAR(300) NOT NULL UNIQUE,
    country VARCHAR(100),
    state VARCHAR(100),
    city VARCHAR(100),
    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL,
    resolved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_email_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message_body TEXT,
    attachment_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS interview_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    communication_score DECIMAL(5,2),
    technical_score DECIMAL(5,2),
    behavioral_score DECIMAL(5,2),
    final_score DECIMAL(5,2),
    transcript_text MEDIUMTEXT,
    speaking_duration_seconds INT,
    words_count INT,
    filler_words_count INT,
    speaking_speed_wpm DECIMAL(7,2),
    clarity_score DECIMAL(5,2),
    confidence_score DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_booking FOREIGN KEY (booking_id) REFERENCES interview_bookings(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ranking_weights (
    id INT PRIMARY KEY,
    communication_weight DECIMAL(5,2) NOT NULL,
    technical_weight DECIMAL(5,2) NOT NULL,
    behavioral_weight DECIMAL(5,2) NOT NULL,
    profile_weight DECIMAL(5,2) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO ranking_weights (id, communication_weight, technical_weight, behavioral_weight, profile_weight)
VALUES (1, 35.0, 40.0, 15.0, 10.0);

CREATE TABLE IF NOT EXISTS coding_challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    language VARCHAR(30) NOT NULL,
    starter_code MEDIUMTEXT,
    expected_output TEXT,
    time_limit_seconds INT DEFAULT 2,
    memory_limit_mb INT DEFAULT 256,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coding_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    challenge_id BIGINT NOT NULL,
    user_id INT NOT NULL,
    language VARCHAR(30) NOT NULL,
    source_code MEDIUMTEXT,
    stdout_text TEXT,
    status VARCHAR(30) NOT NULL,
    execution_time_ms INT,
    score DECIMAL(5,2),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_submission_booking FOREIGN KEY (booking_id) REFERENCES interview_bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_challenge FOREIGN KEY (challenge_id) REFERENCES coding_challenges(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS final_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_users INT,
    report_path VARCHAR(500),
    report_json MEDIUMTEXT
);
