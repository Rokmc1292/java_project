CREATE DATABASE IF NOT EXISTS sports_community CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sports_community;

-- ====================================
-- 1. 사용자 관리 테이블
-- ====================================

-- 사용자 기본 정보 테이블
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 아이디',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    nickname VARCHAR(50) NOT NULL UNIQUE COMMENT '커뮤니티 활동 닉네임',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일 주소',
    profile_image VARCHAR(255) DEFAULT '/images/default-profile.png' COMMENT '프로필 이미지 URL',
    tier VARCHAR(20) DEFAULT 'BRONZE' COMMENT '티어 등급',
    tier_score INT DEFAULT 0 COMMENT '티어 점수',
    is_active BOOLEAN DEFAULT TRUE COMMENT '계정 활성화 상태',
    is_admin BOOLEAN DEFAULT FALSE COMMENT '관리자 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '가입일',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '정보 수정일',
    INDEX idx_username (username),
    INDEX idx_nickname (nickname),
    INDEX idx_tier (tier, tier_score)
) ENGINE=InnoDB COMMENT='사용자 기본 정보';

-- 사용자 설정 테이블
CREATE TABLE user_settings (
    setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    comment_notification BOOLEAN DEFAULT TRUE COMMENT '댓글 알림',
    reply_notification BOOLEAN DEFAULT TRUE COMMENT '대댓글 알림',
    popular_post_notification BOOLEAN DEFAULT TRUE COMMENT '인기글 진입 알림',
    prediction_result_notification BOOLEAN DEFAULT TRUE COMMENT '예측 결과 알림',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='사용자 설정';

-- 사용자 차단 테이블
CREATE TABLE user_blocks (
    block_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blocker_id BIGINT NOT NULL COMMENT '차단한 사용자 ID',
    blocked_id BIGINT NOT NULL COMMENT '차단된 사용자 ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (blocker_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_block (blocker_id, blocked_id)
) ENGINE=InnoDB COMMENT='사용자 차단';

-- ====================================
-- 2. 커뮤니티 게시판 테이블
-- ====================================

-- 게시판 카테고리 테이블
CREATE TABLE board_categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE COMMENT '카테고리 이름',
    display_order INT DEFAULT 0 COMMENT '표시 순서',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='게시판 카테고리';

-- 게시글 테이블
CREATE TABLE posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL COMMENT '카테고리 ID',
    user_id BIGINT NOT NULL COMMENT '작성자 ID',
    title VARCHAR(200) NOT NULL COMMENT '게시글 제목',
    content TEXT NOT NULL COMMENT '게시글 내용',
    view_count INT DEFAULT 0 COMMENT '조회수',
    like_count INT DEFAULT 0 COMMENT '추천수',
    dislike_count INT DEFAULT 0 COMMENT '비추천수',
    comment_count INT DEFAULT 0 COMMENT '댓글 수',
    is_notice BOOLEAN DEFAULT FALSE COMMENT '공지사항 여부',
    is_popular BOOLEAN DEFAULT FALSE COMMENT '인기글 여부',
    is_best BOOLEAN DEFAULT FALSE COMMENT '베스트 게시글 여부',
    is_blinded BOOLEAN DEFAULT FALSE COMMENT '관리자 블라인드 처리 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '작성일',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    FOREIGN KEY (category_id) REFERENCES board_categories(category_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_category (category_id),
    INDEX idx_user (user_id),
    INDEX idx_popular (is_popular, like_count),
    INDEX idx_created (created_at),
    FULLTEXT INDEX idx_search (title, content)
) ENGINE=InnoDB COMMENT='게시글';

-- 게시글 첨부파일 테이블
CREATE TABLE post_attachments (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    file_type VARCHAR(20) NOT NULL COMMENT '파일 타입 (IMAGE, VIDEO, LINK)',
    file_url VARCHAR(500) NOT NULL COMMENT '파일 URL',
    file_name VARCHAR(255) COMMENT '원본 파일명',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    INDEX idx_post (post_id)
) ENGINE=InnoDB COMMENT='게시글 첨부파일';

-- 댓글 테이블
CREATE TABLE comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    user_id BIGINT NOT NULL COMMENT '작성자 ID',
    parent_comment_id BIGINT DEFAULT NULL COMMENT '부모 댓글 ID',
    content TEXT NOT NULL COMMENT '댓글 내용',
    like_count INT DEFAULT 0 COMMENT '추천수',
    dislike_count INT DEFAULT 0 COMMENT '비추천수',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '삭제 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_comment_id)
) ENGINE=InnoDB COMMENT='댓글';

-- 게시글 추천/비추천 테이블
CREATE TABLE post_votes (
    vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    vote_type VARCHAR(10) NOT NULL COMMENT '투표 타입 (LIKE, DISLIKE)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_vote (post_id, user_id)
) ENGINE=InnoDB COMMENT='게시글 추천/비추천';

-- 댓글 추천/비추천 테이블
CREATE TABLE comment_votes (
    vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL COMMENT '댓글 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    vote_type VARCHAR(10) NOT NULL COMMENT '투표 타입 (LIKE, DISLIKE)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_vote (comment_id, user_id)
) ENGINE=InnoDB COMMENT='댓글 추천/비추천';

-- 게시글 스크랩 테이블
CREATE TABLE post_scraps (
    scrap_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_scrap (post_id, user_id)
) ENGINE=InnoDB COMMENT='게시글 스크랩';

-- ====================================
-- 3. 경기 정보 테이블 (수동 입력 방식)
-- ====================================

-- 종목 테이블
CREATE TABLE sports (
    sport_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sport_name VARCHAR(50) NOT NULL UNIQUE COMMENT '종목 이름 (FOOTBALL, BASKETBALL, BASEBALL, LOL, MMA)',
    display_name VARCHAR(50) NOT NULL COMMENT '화면 표시 이름 (축구, 농구, 야구, 롤, UFC)',
    display_order INT DEFAULT 0 COMMENT '표시 순서',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='스포츠 종목';

-- 리그 테이블 (수동 입력)
CREATE TABLE leagues (
    league_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sport_id BIGINT NOT NULL COMMENT '종목 ID',
    league_name VARCHAR(100) NOT NULL COMMENT '리그 이름 (EPL, NBA, LCK 등)',
    country VARCHAR(50) COMMENT '국가',
    league_logo VARCHAR(500) COMMENT '리그 로고 URL',
    display_order INT DEFAULT 0 COMMENT '표시 순서',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sport_id) REFERENCES sports(sport_id) ON DELETE CASCADE,
    INDEX idx_sport (sport_id)
) ENGINE=InnoDB COMMENT='리그 정보';

-- 팀 테이블 (수동 입력)
CREATE TABLE teams (
    team_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    league_id BIGINT NOT NULL COMMENT '리그 ID',
    team_name VARCHAR(100) NOT NULL COMMENT '팀 이름',
    team_logo VARCHAR(500) COMMENT '팀 로고 URL',
    country VARCHAR(50) COMMENT '국가',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (league_id) REFERENCES leagues(league_id) ON DELETE CASCADE,
    INDEX idx_league (league_id)
) ENGINE=InnoDB COMMENT='팀 정보';

-- 경기 일정 테이블 (수동 입력)
CREATE TABLE matches (
    match_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    league_id BIGINT NOT NULL COMMENT '리그 ID',
    home_team_id BIGINT NOT NULL COMMENT '홈팀 ID',
    away_team_id BIGINT NOT NULL COMMENT '원정팀 ID',
    match_date DATETIME NOT NULL COMMENT '경기 날짜 및 시간',
    venue VARCHAR(200) COMMENT '경기장',
    status VARCHAR(20) DEFAULT 'SCHEDULED' COMMENT '경기 상태 (SCHEDULED, LIVE, FINISHED, POSTPONED)',
    home_score INT DEFAULT 0 COMMENT '홈팀 점수',
    away_score INT DEFAULT 0 COMMENT '원정팀 점수',
    weather VARCHAR(50) COMMENT '날씨 정보',
    precipitation_probability INT COMMENT '강수 확률',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (league_id) REFERENCES leagues(league_id) ON DELETE CASCADE,
    FOREIGN KEY (home_team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (away_team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    INDEX idx_match_date (match_date),
    INDEX idx_status (status),
    INDEX idx_league (league_id)
) ENGINE=InnoDB COMMENT='경기 일정';

-- 즐겨찾기 팀 테이블
CREATE TABLE favorite_teams (
    favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    team_id BIGINT NOT NULL COMMENT '팀 ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    UNIQUE KEY unique_favorite (user_id, team_id)
) ENGINE=InnoDB COMMENT='즐겨찾기 팀';

-- ====================================
-- 4. 승부예측 테이블
-- ====================================

-- 예측 테이블
CREATE TABLE predictions (
    prediction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL COMMENT '경기 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    predicted_result VARCHAR(10) NOT NULL COMMENT '예측 결과 (HOME, DRAW, AWAY)',
    comment TEXT NOT NULL COMMENT '예측 코멘트',
    is_correct BOOLEAN DEFAULT NULL COMMENT '예측 정답 여부',
    like_count INT DEFAULT 0 COMMENT '코멘트 추천수',
    dislike_count INT DEFAULT 0 COMMENT '코멘트 비추천수',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_prediction (match_id, user_id),
    INDEX idx_match (match_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB COMMENT='사용자 승부예측';

-- 예측 통계 테이블
CREATE TABLE prediction_statistics (
    stat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL COMMENT '경기 ID',
    home_votes INT DEFAULT 0 COMMENT '홈팀 승 예측 수',
    draw_votes INT DEFAULT 0 COMMENT '무승부 예측 수',
    away_votes INT DEFAULT 0 COMMENT '원정팀 승 예측 수',
    total_votes INT DEFAULT 0 COMMENT '전체 예측 수',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    UNIQUE KEY unique_match_stat (match_id)
) ENGINE=InnoDB COMMENT='예측 통계';

-- 예측 코멘트 추천/비추천 테이블
CREATE TABLE prediction_votes (
    vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prediction_id BIGINT NOT NULL COMMENT '예측 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    vote_type VARCHAR(10) NOT NULL COMMENT '투표 타입 (LIKE, DISLIKE)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prediction_id) REFERENCES predictions(prediction_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_vote (prediction_id, user_id)
) ENGINE=InnoDB COMMENT='예측 코멘트 투표';

-- ====================================
-- 5. 실시간 채팅방 테이블
-- ====================================

-- 채팅방 테이블
CREATE TABLE chatrooms (
    chatroom_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL COMMENT '경기 ID',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    viewer_count INT DEFAULT 0 COMMENT '현재 시청자 수',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    UNIQUE KEY unique_match_chatroom (match_id)
) ENGINE=InnoDB COMMENT='채팅방';

-- 채팅 메시지 테이블
CREATE TABLE chat_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chatroom_id BIGINT NOT NULL COMMENT '채팅방 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    message TEXT NOT NULL COMMENT '메시지 내용',
    message_type VARCHAR(20) DEFAULT 'USER' COMMENT '메시지 타입 (USER, SYSTEM)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chatroom_id) REFERENCES chatrooms(chatroom_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_chatroom_created (chatroom_id, created_at)
) ENGINE=InnoDB COMMENT='채팅 메시지';

-- ====================================
-- 6. 스포츠 뉴스 테이블
-- ====================================

-- 뉴스 테이블
CREATE TABLE news (
    news_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sport_id BIGINT COMMENT '종목 ID',
    title VARCHAR(300) NOT NULL COMMENT '뉴스 제목',
    content TEXT COMMENT '뉴스 내용 요약',
    thumbnail_url VARCHAR(500) COMMENT '썸네일 이미지 URL',
    source_url VARCHAR(500) NOT NULL COMMENT '원문 기사 URL',
    source_name VARCHAR(100) NOT NULL COMMENT '언론사 이름',
    published_at DATETIME NOT NULL COMMENT '발행 시간',
    view_count INT DEFAULT 0 COMMENT '조회수',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sport_id) REFERENCES sports(sport_id) ON DELETE SET NULL,
    INDEX idx_sport (sport_id),
    INDEX idx_published (published_at),
    INDEX idx_view_count (view_count)
) ENGINE=InnoDB COMMENT='스포츠 뉴스';

-- ====================================
-- 7. 신고 및 관리 테이블
-- ====================================

-- 신고 테이블
CREATE TABLE reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL COMMENT '신고자 ID',
    target_type VARCHAR(20) NOT NULL COMMENT '신고 대상 타입',
    target_id BIGINT NOT NULL COMMENT '신고 대상 ID',
    reason VARCHAR(50) NOT NULL COMMENT '신고 사유',
    description TEXT COMMENT '상세 설명',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '처리 상태',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_target (target_type, target_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='신고';

-- ====================================
-- 8. 알림 테이블
-- ====================================

-- 알림 테이블
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '수신자 ID',
    notification_type VARCHAR(50) NOT NULL COMMENT '알림 타입',
    content VARCHAR(500) NOT NULL COMMENT '알림 내용',
    related_type VARCHAR(20) COMMENT '관련 항목 타입',
    related_id BIGINT COMMENT '관련 항목 ID',
    is_read BOOLEAN DEFAULT FALSE COMMENT '읽음 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB COMMENT='알림';

-- ====================================
-- 9. 기본 데이터 삽입
-- ====================================

-- 스포츠 종목 기본 데이터
INSERT INTO sports (sport_name, display_name, display_order) VALUES
('FOOTBALL', '축구', 1),
('BASKETBALL', '농구', 2),
('BASEBALL', '야구', 3),
('LOL', '롤', 4),
('MMA', 'UFC', 5);

-- 게시판 카테고리 기본 데이터
INSERT INTO board_categories (category_name, display_order) VALUES
('축구', 1),
('야구', 2),
('농구', 3),
('롤', 4),
('UFC', 5),
('자유게시판', 99);

-- ====================================
-- UFC용 파이터 테이블 추가
-- ====================================

-- 파이터 테이블 (UFC 선수)
CREATE TABLE fighters (
    fighter_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fighter_name VARCHAR(100) NOT NULL COMMENT '파이터 이름',
    fighter_image VARCHAR(500) COMMENT '파이터 이미지 URL',
    country VARCHAR(50) COMMENT '국가',
    weight_class VARCHAR(50) COMMENT '체급 (Heavyweight, Lightweight 등)',
    record VARCHAR(50) COMMENT '전적 (예: 25-3-0)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_fighter_name (fighter_name)
) ENGINE=InnoDB COMMENT='UFC 파이터 정보';

-- UFC 경기 테이블 (기존 matches와 별도)
CREATE TABLE mma_fights (
    fight_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    league_id BIGINT NOT NULL COMMENT '리그 ID (UFC)',
    fighter1_id BIGINT NOT NULL COMMENT '파이터 1 ID',
    fighter2_id BIGINT NOT NULL COMMENT '파이터 2 ID',
    fight_date DATETIME NOT NULL COMMENT '경기 날짜 및 시간',
    venue VARCHAR(200) COMMENT '경기장',
    event_name VARCHAR(200) COMMENT '이벤트 이름 (예: UFC 300)',
    status VARCHAR(20) DEFAULT 'SCHEDULED' COMMENT '경기 상태 (SCHEDULED, LIVE, FINISHED)',
    winner_id BIGINT COMMENT '승자 ID',
    method VARCHAR(50) COMMENT '승리 방법 (KO, Submission, Decision 등)',
    round INT COMMENT '몇 라운드에서 끝났는지',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (league_id) REFERENCES leagues(league_id) ON DELETE CASCADE,
    FOREIGN KEY (fighter1_id) REFERENCES fighters(fighter_id) ON DELETE CASCADE,
    FOREIGN KEY (fighter2_id) REFERENCES fighters(fighter_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES fighters(fighter_id) ON DELETE SET NULL,
    INDEX idx_fight_date (fight_date),
    INDEX idx_status (status),
    INDEX idx_league (league_id)
) ENGINE=InnoDB COMMENT='UFC 경기 일정';

-- UFC 승부예측 테이블 (별도)
CREATE TABLE mma_predictions (
    prediction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fight_id BIGINT NOT NULL COMMENT 'UFC 경기 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    predicted_fighter_id BIGINT NOT NULL COMMENT '예측한 파이터 ID',
    comment TEXT NOT NULL COMMENT '예측 코멘트',
    is_correct BOOLEAN DEFAULT NULL COMMENT '예측 정답 여부',
    like_count INT DEFAULT 0 COMMENT '코멘트 추천수',
    dislike_count INT DEFAULT 0 COMMENT '코멘트 비추천수',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fight_id) REFERENCES mma_fights(fight_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (predicted_fighter_id) REFERENCES fighters(fighter_id) ON DELETE CASCADE,
    UNIQUE KEY unique_prediction (fight_id, user_id),
    INDEX idx_fight (fight_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB COMMENT='UFC 승부예측';

-- UFC 예측 통계 테이블
CREATE TABLE mma_prediction_statistics (
    stat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fight_id BIGINT NOT NULL COMMENT 'UFC 경기 ID',
    fighter1_votes INT DEFAULT 0 COMMENT '파이터 1 예측 수',
    fighter2_votes INT DEFAULT 0 COMMENT '파이터 2 예측 수',
    total_votes INT DEFAULT 0 COMMENT '전체 예측 수',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (fight_id) REFERENCES mma_fights(fight_id) ON DELETE CASCADE,
    UNIQUE KEY unique_fight_stat (fight_id)
) ENGINE=InnoDB COMMENT='UFC 예측 통계';

-- UFC 채팅방 테이블
CREATE TABLE mma_chatrooms (
    chatroom_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fight_id BIGINT NOT NULL COMMENT 'UFC 경기 ID',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    viewer_count INT DEFAULT 0 COMMENT '현재 시청자 수',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fight_id) REFERENCES mma_fights(fight_id) ON DELETE CASCADE,
    UNIQUE KEY unique_fight_chatroom (fight_id)
) ENGINE=InnoDB COMMENT='UFC 채팅방';

-- 관리자 계정
INSERT INTO users (username, password, nickname, email, is_admin) VALUES
('admin', '$2a$10$ExampleHashedPassword', '관리자', 'admin@sports.com', TRUE);
