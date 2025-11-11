# 스포츠 허브 플랫폼

스포츠 팬들을 위한 종합 커뮤니티 플랫폼

## 프로젝트 개요

스포츠 허브는 다양한 스포츠 팬들이 모여 경기 정보를 확인하고, 승부를 예측하며, 실시간으로 소통할 수 있는 종합 플랫폼입니다.

### 주요 기능

1. **홈 (Dashboard)**
   - 오늘의 경기 일정 요약
   - 인기 커뮤니티 게시글
   - 인기 뉴스

2. **경기 일정 (Fixtures)**
   - 종목별 경기 확인 (축구, 농구, 야구, e스포츠, UFC)
   - 날짜별 경기 필터링
   - 경기 상세 정보 및 날씨 정보

3. **커뮤니티 (Community)**
   - 종목별 게시판
   - 인기글/전체글 탭
   - 댓글 및 대댓글
   - 추천/비추천 시스템

4. **승부예측 (Predictions)**
   - 경기 이틀 전부터 예측 참여
   - 예측 성공/실패에 따른 티어 점수 시스템
   - 예측 통계 및 랭킹

5. **실시간 (Live)**
   - 진행 중인 경기 실시간 점수 (30초마다 자동 업데이트)
   - 경기별 실시간 채팅
   - **점수는 DB에서 수동으로 업데이트**

6. **뉴스 (News)**
   - 종목별 스포츠 뉴스
   - 인기 뉴스 랭킹
   - 외부 기사 링크

7. **마이페이지 (MyPage)**
   - 프로필 관리
   - 티어 및 예측 통계
   - 활동 내역 확인

## 기술 스택

### 백엔드
- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **Spring Security**
- **MySQL**
- **Gradle**

### 프론트엔드
- **React 19**
- **Vite**
- **React Router DOM**

## 프로젝트 구조

```
java_project/
├── backend/
│   └── src/main/java/com/example/backend/
│       ├── controller/     # REST API 컨트롤러
│       ├── service/        # 비즈니스 로직
│       ├── repository/     # JPA 리포지토리
│       ├── entity/         # JPA 엔티티
│       ├── dto/            # 데이터 전송 객체
│       └── config/         # 설정 파일
├── frontend/
│   └── src/
│       ├── pages/          # 페이지 컴포넌트
│       ├── components/     # 재사용 컴포넌트
│       └── api/            # API 호출 함수
└── database/
    └── schema.sql          # DB 스키마
```

## 설치 및 실행

### 1. 데이터베이스 설정

MySQL에서 데이터베이스를 생성하고 스키마를 실행합니다:

```sql
CREATE DATABASE sports_community CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

제공된 `database/schema.sql` 파일을 실행하여 테이블을 생성합니다.

### 2. 백엔드 설정

`backend/src/main/resources/application.properties` 파일에서 데이터베이스 연결 정보를 수정합니다:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sports_community
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

백엔드 서버는 `http://localhost:8080`에서 실행됩니다.

### 4. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

프론트엔드는 `http://localhost:5173`에서 실행됩니다.

## 실시간 점수 업데이트 방법

실시간 경기 점수는 **DB에서 수동으로 업데이트**합니다.

### 예시: 경기 점수 업데이트

```sql
-- matches 테이블에서 특정 경기의 점수 업데이트
UPDATE matches
SET home_score = 2, away_score = 1, status = 'LIVE'
WHERE match_id = 1;
```

프론트엔드는 30초마다 자동으로 점수를 새로고침하여 업데이트된 점수를 표시합니다.

### 경기 상태

- `SCHEDULED`: 예정된 경기
- `LIVE`: 진행 중인 경기 (실시간 페이지에 표시됨)
- `FINISHED`: 종료된 경기
- `POSTPONED`: 연기된 경기

## API 엔드포인트

### 경기 일정
- `GET /api/matches?date={date}&sport={sport}` - 특정 날짜의 경기 조회
- `GET /api/matches/range?startDate={start}&endDate={end}` - 날짜 범위로 경기 조회

### 커뮤니티
- `GET /api/community/posts` - 전체 게시글 조회
- `GET /api/community/posts/category/{category}` - 카테고리별 게시글 조회
- `GET /api/community/posts/popular` - 인기 게시글 조회
- `POST /api/community/posts` - 게시글 작성
- `GET /api/community/posts/{postId}/comments` - 댓글 조회
- `POST /api/community/posts/{postId}/comments` - 댓글 작성

### 승부예측
- `GET /api/predictions/match/{matchId}` - 경기의 모든 예측 조회
- `GET /api/predictions/match/{matchId}/statistics` - 예측 통계
- `POST /api/predictions` - 예측 참여
- `GET /api/predictions/user/{username}` - 사용자 예측 내역

### 실시간
- `GET /api/live/matches` - 진행 중인 경기 조회
- `GET /api/live/chatroom/match/{matchId}` - 채팅방 조회/생성
- `GET /api/live/chatroom/{chatroomId}/messages` - 채팅 메시지 조회
- `POST /api/live/chatroom/{chatroomId}/messages` - 채팅 메시지 전송

### 뉴스
- `GET /api/news` - 전체 뉴스 조회
- `GET /api/news/sport/{sportName}` - 종목별 뉴스 조회
- `GET /api/news/popular` - 인기 뉴스 (TOP 10)

## 주요 특징

### 1. 실시간 점수 업데이트
- DB 수동 업데이트 방식
- 프론트엔드 30초마다 자동 새로고침
- 안정적이고 예측 가능한 동작

### 2. 티어 시스템
- 예측 성공: +10점
- 예측 실패: -10점
- 등급: BRONZE, SILVER, GOLD, PLATINUM, DIAMOND

### 3. 다양한 스포츠 지원
- 팀 스포츠: 축구, 농구, 야구, 롤
- 개인 스포츠: UFC (파이터 대전)

### 4. 커뮤니티 기능
- 종목별 게시판
- 실시간 인기글 반영
- 댓글 및 대댓글
- 추천/비추천 시스템

## 라이센스

MIT License

## 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.
