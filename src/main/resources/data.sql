-- Demo profile data for user id 1 (tusharawale) - idempotent inserts
-- Matches user_profile with columns: phone, date_of_birth (not contact_number, dob)
INSERT IGNORE INTO user_profile (user_id, first_name, last_name, phone, gender, date_of_birth, country, state, city, linkedin_url, github_url) VALUES
(1, 'Tushar', 'Awale', '+91 9876543210', 'Male', '2002-05-15', 'India', 'Maharashtra', 'Pune', 'https://linkedin.com/in/tusharawale', 'https://github.com/tusharawale');

INSERT INTO user_education (user_id, college_name, branch, education_level, semester, start_year, end_year)
SELECT 1, 'Pune University', 'Computer Science', 'B.Tech', 6, 2020, 2024 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_education WHERE user_id=1 AND college_name='Pune University');
INSERT INTO user_education (user_id, college_name, branch, education_level, semester, start_year, end_year)
SELECT 1, 'XYZ High School', 'Science', 'Higher Secondary', NULL, 2018, 2020 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_education WHERE user_id=1 AND college_name='xyz college');

INSERT INTO user_experience (user_id, company_name, job_role, start_date, end_date, description)
SELECT 1, 'Tech Solutions Pvt Ltd', 'Intern Developer', '2023-06-01', '2023-12-31', 'Worked on web applications using Spring Boot and React. Developed REST APIs and improved application performance.' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_experience WHERE user_id=1 AND company_name='Tech Solutions Pvt Ltd');
INSERT INTO user_experience (user_id, company_name, job_role, start_date, end_date, description)
SELECT 1, 'StartupXYZ', 'Freelance Developer', '2024-01-01', NULL, 'Building scalable web applications and mobile apps. Technologies: Java, Spring, Node.js' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_experience WHERE user_id=1 AND company_name='StartupXYZ');

INSERT INTO user_skills (user_id, skill_name, skill_level)
SELECT 1, 'Java', 'Intermediate' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user_skills WHERE user_id=1 AND skill_name='Java');
INSERT INTO user_skills (user_id, skill_name, skill_level)
SELECT 1, 'Spring Boot', 'Intermediate' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user_skills WHERE user_id=1 AND skill_name='Spring Boot');
INSERT INTO user_skills (user_id, skill_name, skill_level)
SELECT 1, 'React', 'Beginner' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user_skills WHERE user_id=1 AND skill_name='React');
INSERT INTO user_skills (user_id, skill_name, skill_level)
SELECT 1, 'MySQL', 'Intermediate' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user_skills WHERE user_id=1 AND skill_name='MySQL');
INSERT INTO user_skills (user_id, skill_name, skill_level)
SELECT 1, 'Problem Solving', 'Advanced' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user_skills WHERE user_id=1 AND skill_name='Problem Solving');

INSERT INTO user_programming_languages (user_id, language_name, proficiency_level)
SELECT 1, 'Java', 'Intermediate' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user_programming_languages WHERE user_id=1 AND language_name='Java');
INSERT INTO user_programming_languages (user_id, language_name, proficiency_level)
SELECT 1, 'JavaScript', 'Beginner' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user_programming_languages WHERE user_id=1 AND language_name='JavaScript');
INSERT INTO user_programming_languages (user_id, language_name, proficiency_level)
SELECT 1, 'Python', 'Beginner' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user_programming_languages WHERE user_id=1 AND language_name='Python');
INSERT INTO user_programming_languages (user_id, language_name, proficiency_level)
SELECT 1, 'SQL', 'Intermediate' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user_programming_languages WHERE user_id=1 AND language_name='SQL');

INSERT INTO user_certificates (user_id, certificate_name, issuer, issue_date)
SELECT 1, 'AWS Cloud Practitioner', 'Amazon Web Services', '2023-08-15' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_certificates WHERE user_id=1 AND certificate_name='AWS Cloud Practitioner');
INSERT INTO user_certificates (user_id, certificate_name, issuer, issue_date)
SELECT 1, 'Java Programming Certificate', 'Coursera', '2023-03-20' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_certificates WHERE user_id=1 AND certificate_name='Java Programming Certificate');
INSERT INTO user_certificates (user_id, certificate_name, issuer, issue_date)
SELECT 1, 'Web Development Fundamentals', 'Udemy', '2022-11-10' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_certificates WHERE user_id=1 AND certificate_name='Web Development Fundamentals');

INSERT INTO user_documents (user_id, document_name, file_path)
SELECT 1, 'Resume.pdf', '/uploads/resume_1.pdf' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_documents WHERE user_id=1 AND document_name='Resume.pdf');
INSERT INTO user_documents (user_id, document_name, file_path)
SELECT 1, 'Degree Certificate', '/uploads/degree_1.pdf' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_documents WHERE user_id=1 AND document_name='Degree Certificate');
