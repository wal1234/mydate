-- 日程事件表（与 DESIGN.md 一致，H2 兼容）
CREATE TABLE IF NOT EXISTS schedule_event (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL,
    is_all_day  TINYINT DEFAULT 0,
    color       VARCHAR(20),
    repeat_rule VARCHAR(100) DEFAULT 'NONE',
    repeat_until DATE,
    reminder_minutes INT,
    notify_wechat TINYINT DEFAULT 0,
    notify_qq    TINYINT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_schedule_user_time ON schedule_event(user_id, start_time, end_time);

-- 每日文档表
CREATE TABLE IF NOT EXISTS daily_document (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,
    doc_date     DATE NOT NULL,
    title        VARCHAR(200),
    content      CLOB,
    content_type VARCHAR(20) DEFAULT 'plain',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_daily_doc_user_date ON daily_document(user_id, doc_date);
CREATE INDEX IF NOT EXISTS idx_daily_doc_user_date ON daily_document(user_id, doc_date);

-- 笔记分类表（知识库）
CREATE TABLE IF NOT EXISTS note_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT,
    name        VARCHAR(100) NOT NULL,
    parent_id   BIGINT,
    sort_order  INT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_note_category_user ON note_category(user_id);

-- 笔记表
CREATE TABLE IF NOT EXISTS note (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,
    category_id  BIGINT,
    title        VARCHAR(200),
    content      CLOB,
    content_type VARCHAR(20) DEFAULT 'quick', -- quick:快速笔记, rich:知识库笔记
    color        VARCHAR(20), -- 卡片颜色
    is_pinned    TINYINT DEFAULT 0, -- 是否置顶
    tags         VARCHAR(500), -- 标签，逗号分隔
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_note_user ON note(user_id);
CREATE INDEX IF NOT EXISTS idx_note_category ON note(category_id);
CREATE INDEX IF NOT EXISTS idx_note_pinned ON note(is_pinned);

-- 用户通知配置表
CREATE TABLE IF NOT EXISTS user_notification_config (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT,
    wechat_serverchan_key VARCHAR(200),
    qq_number            VARCHAR(20),
    qmsg_key             VARCHAR(200),
    wechat_enabled       TINYINT DEFAULT 0,
    qq_enabled           TINYINT DEFAULT 0,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted              TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_notification_user ON user_notification_config(user_id);

-- 大模型API配置表
CREATE TABLE IF NOT EXISTS llm_api_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,
    config_name  VARCHAR(100) NOT NULL,
    api_url      VARCHAR(500) NOT NULL,
    api_key      VARCHAR(500),
    model_name   VARCHAR(100) NOT NULL,
    provider_type VARCHAR(50) DEFAULT 'openai',
    stream_enabled TINYINT DEFAULT 1,
    temperature  DECIMAL(3,2) DEFAULT 0.7,
    max_tokens   INT DEFAULT 2048,
    headers      VARCHAR(2000),
    is_active    TINYINT DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_llm_config_user ON llm_api_config(user_id);

-- 对话会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,
    session_title VARCHAR(200),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_chat_session_user ON chat_session(user_id);

-- 对话消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id   BIGINT NOT NULL,
    role         VARCHAR(20) NOT NULL,
    content      TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_chat_message_session ON chat_message(session_id);

-- 提示词模板表
CREATE TABLE IF NOT EXISTS prompt_template (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    template_name   VARCHAR(200) NOT NULL,
    description     VARCHAR(500),
    template_content TEXT,
    variables       VARCHAR(2000),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_prompt_template_user ON prompt_template(user_id);

-- 提示词版本表
CREATE TABLE IF NOT EXISTS prompt_version (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id     BIGINT NOT NULL,
    version_number  INT NOT NULL,
    content         TEXT,
    release_notes   VARCHAR(500),
    is_active       TINYINT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_prompt_version_template ON prompt_version(template_id);
CREATE INDEX IF NOT EXISTS idx_prompt_version_active ON prompt_version(template_id, is_active);

-- 会话上下文配置表
CREATE TABLE IF NOT EXISTS conversation_context (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                  BIGINT,
    session_id               BIGINT NOT NULL,
    max_tokens               INT DEFAULT 6000,
    context_window           INT DEFAULT 128000,
    preserve_system_messages TINYINT DEFAULT 1,
    summarize_enabled         TINYINT DEFAULT 1,
    summarize_threshold      DECIMAL(3,2) DEFAULT 0.8,
    current_summary          TEXT,
    created_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_conversation_context_session ON conversation_context(session_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_conversation_context_unique ON conversation_context(user_id, session_id);

-- AI技能分类表
CREATE TABLE IF NOT EXISTS ai_skill_category (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    category_name   VARCHAR(100) NOT NULL,
    category_code   VARCHAR(100),
    description     VARCHAR(500),
    icon            VARCHAR(100),
    sort_order      INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_skill_category_user ON ai_skill_category(user_id);

-- AI技能表
CREATE TABLE IF NOT EXISTS ai_skill (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    category_id     BIGINT,
    skill_name      VARCHAR(200) NOT NULL,
    skill_code      VARCHAR(100),
    description     VARCHAR(500),
    skill_type      VARCHAR(50) DEFAULT 'assistant',
    icon            VARCHAR(100),
    color           VARCHAR(20),
    status          TINYINT DEFAULT 1,
    is_default      TINYINT DEFAULT 0,
    priority        INT DEFAULT 0,
    parameters      TEXT,
    prompt_template TEXT,
    max_tokens      INT DEFAULT 2048,
    temperature     DECIMAL(3,2) DEFAULT 0.7,
    model_name      VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_skill_user ON ai_skill(user_id);
CREATE INDEX IF NOT EXISTS idx_skill_category ON ai_skill(category_id);
CREATE INDEX IF NOT EXISTS idx_skill_code ON ai_skill(user_id, skill_code);

-- AI技能版本表
CREATE TABLE IF NOT EXISTS ai_skill_version (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_id        BIGINT NOT NULL,
    version_number  INT NOT NULL,
    version_name    VARCHAR(50),
    prompt_template TEXT,
    parameters      TEXT,
    release_notes   VARCHAR(500),
    is_active       TINYINT DEFAULT 0,
    usage_count     INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_skill_version_skill ON ai_skill_version(skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_version_active ON ai_skill_version(skill_id, is_active);

-- AI技能配置表
CREATE TABLE IF NOT EXISTS ai_skill_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_id        BIGINT NOT NULL,
    config_key      VARCHAR(100) NOT NULL,
    config_value    TEXT,
    config_type     VARCHAR(50),
    description     VARCHAR(200),
    required        TINYINT DEFAULT 0,
    is_active       TINYINT DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_skill_config_skill ON ai_skill_config(skill_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_skill_config_key ON ai_skill_config(skill_id, config_key);

-- 垃圾箱表
CREATE TABLE IF NOT EXISTS trash_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    item_type       VARCHAR(50) NOT NULL,
    original_id     BIGINT NOT NULL,
    item_title      VARCHAR(500),
    item_content    TEXT,
    original_data   TEXT,
    deleted_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expire_at       TIMESTAMP,
    status          TINYINT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_trash_user ON trash_item(user_id);
CREATE INDEX IF NOT EXISTS idx_trash_type ON trash_item(item_type);
CREATE INDEX IF NOT EXISTS idx_trash_status ON trash_item(status);
CREATE INDEX IF NOT EXISTS idx_trash_expire ON trash_item(expire_at);


