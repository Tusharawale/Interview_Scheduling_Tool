CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    verification_token VARCHAR(255),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin (
    admin_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

-- Interview scheduling tables

CREATE TABLE IF NOT EXISTS interview_slots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    scheduled_at DATETIME NOT NULL,
    duration_minutes INT NOT NULL,
    capacity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS interview_bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    slot_id INT NOT NULL,
    user_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_slot FOREIGN KEY (slot_id) REFERENCES interview_slots(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_booking UNIQUE (slot_id, user_id)
);

-- User profile tables (joined by user_id)
CREATE TABLE IF NOT EXISTS user_profile (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    first_name VARCHAR(100),
    middle_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(50),
    gender VARCHAR(50),
    date_of_birth DATE,
    country VARCHAR(100),
    state VARCHAR(100),
    city VARCHAR(100),
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255),
    profile_image_path VARCHAR(500),
    current_course VARCHAR(50),
    current_college_code VARCHAR(50),
    current_branch VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_certificates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    certificate_name VARCHAR(200),
    issuer VARCHAR(200),
    issue_date DATE,
    certificate_file VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cert_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    document_name VARCHAR(200),
    file_path VARCHAR(255),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_education (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    college_name VARCHAR(200),
    branch VARCHAR(100),
    education_level VARCHAR(100),
    semester INT,
    start_year YEAR,
    end_year YEAR,
    total_marks INT,
    marks_obtained INT,
    cgpa DECIMAL(4,2),
    document_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_edu_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_experience (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    company_name VARCHAR(200),
    job_role VARCHAR(150),
    start_date DATE,
    end_date DATE,
    description TEXT,
    document_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_programming_languages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    language_name VARCHAR(100),
    proficiency_level VARCHAR(50),
    certificate_company VARCHAR(200),
    certificate_file VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Global meeting live chat (persisted; last messages loaded on join)
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

CREATE TABLE IF NOT EXISTS user_skills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    skill_name VARCHAR(100),
    skill_level VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_skill_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

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
