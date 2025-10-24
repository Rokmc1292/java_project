-- 커뮤니티 기능 추가 마이그레이션 스크립트

-- 게시글 추천/비추천 테이블
CREATE TABLE IF NOT EXISTS post_likes (
    like_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    like_type VARCHAR(10) NOT NULL COMMENT 'LIKE or DISLIKE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_post_user_like (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 게시글 스크랩 테이블
CREATE TABLE IF NOT EXISTS post_scraps (
    scrap_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_post_user_scrap (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 게시글 신고 테이블
CREATE TABLE IF NOT EXISTS post_reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL COMMENT '신고 사유',
    description TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, PROCESSED, REJECTED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME NULL,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 차단 테이블
CREATE TABLE IF NOT EXISTS user_blocks (
    block_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blocker_id BIGINT NOT NULL COMMENT '차단한 사용자',
    blocked_id BIGINT NOT NULL COMMENT '차단된 사용자',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (blocker_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_blocker_blocked (blocker_id, blocked_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 알림 테이블
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '알림 받을 사용자',
    type VARCHAR(50) NOT NULL COMMENT 'COMMENT, REPLY, POPULAR_POST, LIKE, etc.',
    content TEXT NOT NULL COMMENT '알림 내용',
    related_post_id BIGINT NULL,
    related_comment_id BIGINT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 게시글 첨부파일 테이블
CREATE TABLE IF NOT EXISTS post_attachments (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    file_type VARCHAR(20) NOT NULL COMMENT 'IMAGE, VIDEO, LINK',
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(200),
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    display_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    INDEX idx_post_order (post_id, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 게시글 테이블 인덱스 추가 (성능 최적화)
CREATE INDEX idx_posts_category_created ON posts(category_id, created_at DESC);
CREATE INDEX idx_posts_popular_likes ON posts(is_popular, like_count DESC);
CREATE INDEX idx_posts_blinded ON posts(is_blinded);

-- 댓글 테이블 인덱스 추가
CREATE INDEX idx_comments_post_created ON comments(post_id, created_at ASC);
CREATE INDEX idx_comments_parent ON comments(parent_comment_id);

-- 초기 카테고리 데이터 삽입 (종목별)
INSERT INTO board_categories (category_name, display_order, is_active, created_at)
VALUES
    ('축구', 1, TRUE, NOW()),
    ('야구', 2, TRUE, NOW()),
    ('농구', 3, TRUE, NOW()),
    ('기타', 4, TRUE, NOW())
ON DUPLICATE KEY UPDATE display_order = VALUES(display_order);
