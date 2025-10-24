# 커뮤니티 기능 구현 완료

## 구현된 기능 목록

### 1. 게시판 구조
- ✅ 전체글/인기글 탭 전환 UI
- ✅ 시간순 정렬 (최신순)
- ✅ 추천수 기준 인기글 자동 선별 (10개 이상 → 인기글, 50개 이상 → 베스트글)
- ✅ 관리자 공지사항 상단 고정

### 2. 게시판 기본 기능
- ✅ 게시글 CRUD (생성, 읽기, 수정, 삭제)
- ✅ 댓글/대댓글 시스템
- ✅ 이미지/동영상/링크 첨부 (PostAttachment 엔티티)
- ✅ 검색 및 필터링 (제목, 내용, 작성자)
- ✅ 리스트형 레이아웃
- ✅ 페이지네이션 (20개씩)

### 3. 카테고리별 관리
- ✅ 축구, 야구, 농구, 기타 종목별 게시판
- ✅ 종목별 전체글/인기글 독립 관리
- ✅ 카테고리별 통계

### 4. 추천/비추천 시스템
- ✅ 1인 1회 제한 (PostLike 엔티티로 관리)
- ✅ 추천수 실시간 반영
- ✅ 일정 추천수 이상 → 인기글 자동 이동
- ✅ 베스트 배지 표시 (⭐)
- ✅ 인기글 배지 표시 (🔥)

### 5. 사용자 참여 기능
- ✅ 조회수 자동 증가
- ✅ 댓글 수 표시
- ✅ 스크랩 기능
- ✅ 작성자 프로필 정보 표시

### 6. 안전 및 관리 기능
- ✅ 신고 시스템 (PostReport 엔티티)
- ✅ 사용자 차단 (UserBlock 엔티티)
- ✅ 본인 작성 글만 수정/삭제 가능
- ✅ 관리자 블라인드 처리

### 7. 알림 기능
- ✅ 댓글 알림
- ✅ 대댓글 알림
- ✅ 인기글 진입 알림
- ✅ 읽음/안읽음 상태 관리
- ✅ 알림 개수 조회

### 8. 통계 및 랭킹
- ✅ 주간 베스트 게시글 (최근 7일)
- ✅ 월간 베스트 게시글 (최근 30일)
- ✅ 활동 왕성 유저 랭킹
- ✅ 카테고리별 베스트 게시글

## 백엔드 API 엔드포인트

### 게시글 관련
- `GET /api/community/posts` - 전체 게시글 조회
- `GET /api/community/posts/category/{categoryName}` - 카테고리별 게시글
- `GET /api/community/posts/popular` - 인기 게시글
- `GET /api/community/posts/popular/{categoryName}` - 카테고리별 인기글
- `GET /api/community/posts/{postId}` - 게시글 상세
- `POST /api/community/posts` - 게시글 작성
- `PUT /api/community/posts/{postId}` - 게시글 수정
- `DELETE /api/community/posts/{postId}` - 게시글 삭제

### 추천/비추천
- `POST /api/community/posts/{postId}/like` - 게시글 추천
- `POST /api/community/posts/{postId}/dislike` - 게시글 비추천

### 스크랩
- `POST /api/community/posts/{postId}/scrap` - 스크랩/스크랩 취소
- `GET /api/community/scraps` - 스크랩 목록 조회

### 신고/차단
- `POST /api/community/posts/{postId}/report` - 게시글 신고
- `POST /api/community/block` - 사용자 차단
- `POST /api/community/posts/{postId}/blind` - 블라인드 처리 (관리자)

### 댓글
- `GET /api/community/posts/{postId}/comments` - 댓글 목록
- `POST /api/community/posts/{postId}/comments` - 댓글 작성
- `DELETE /api/community/comments/{commentId}` - 댓글 삭제

### 통계
- `GET /api/community/stats/weekly-best` - 주간 베스트
- `GET /api/community/stats/monthly-best` - 월간 베스트
- `GET /api/community/stats/active-users` - 활동 유저 랭킹
- `GET /api/community/stats/weekly-best/{categoryName}` - 카테고리별 주간 베스트

### 알림
- `GET /api/notifications` - 알림 목록
- `GET /api/notifications/unread` - 읽지 않은 알림
- `GET /api/notifications/unread/count` - 읽지 않은 알림 개수
- `POST /api/notifications/{notificationId}/read` - 알림 읽음 처리
- `POST /api/notifications/read-all` - 모든 알림 읽음 처리
- `DELETE /api/notifications/{notificationId}` - 알림 삭제

## 데이터베이스 스키마

### 새로 추가된 테이블
1. **post_likes** - 게시글 추천/비추천 기록
2. **post_scraps** - 게시글 스크랩
3. **post_reports** - 게시글 신고
4. **user_blocks** - 사용자 차단
5. **notifications** - 알림
6. **post_attachments** - 게시글 첨부파일

## 사용 방법

### 1. 데이터베이스 마이그레이션 실행
```bash
# SQL 스크립트 실행
mysql -u root -p sports_community < backend/src/main/resources/db/migration/V2__Add_Community_Features.sql
```

### 2. 백엔드 실행
```bash
cd backend
./gradlew bootRun
```

### 3. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```

### 4. 접속
- 프론트엔드: http://localhost:5173
- 백엔드 API: http://localhost:8080

## 주요 특징

### 성능 최적화
- 데이터베이스 인덱스 추가 (조회 성능 향상)
- 페이지네이션 적용
- Lazy Loading (지연 로딩)

### 보안
- 본인 작성 글만 수정/삭제 가능
- 관리자 권한 체크
- XSS/SQL Injection 방지

### 사용자 경험
- 실시간 추천수 반영
- 자동 인기글 선정
- 알림 시스템
- 배지 표시 (공지, 인기, 베스트)

## ✅ 프론트엔드 추가 구현 완료 (2025-10-24)

### 새로 추가된 페이지 및 컴포넌트
1. ✅ **PostDetail.jsx** - 게시글 상세 페이지
   - 게시글 내용 표시
   - 추천/비추천 버튼 (1인 1회 제한)
   - 스크랩 기능
   - 신고 기능
   - 본인 작성 글 수정/삭제
   - 첨부파일 표시 (이미지, 링크)

2. ✅ **PostWrite.jsx** - 게시글 작성 페이지
   - 카테고리 선택
   - 제목/내용 입력
   - 이미지/링크 첨부 (URL 방식)
   - 첨부파일 관리 (추가/삭제)

3. ✅ **PostEdit.jsx** - 게시글 수정 페이지
   - 본인 작성 글만 수정 가능
   - 기존 내용 불러오기
   - 제목/내용/첨부파일 수정

4. ✅ **CommentSection.jsx** - 댓글/대댓글 컴포넌트
   - 댓글 목록 표시 (계층 구조)
   - 댓글 작성/삭제
   - 대댓글 작성/삭제
   - 티어 배지 표시
   - 작성 시간 표시

5. ✅ **Navbar 확장** - 알림 드롭다운 UI
   - 🔔 알림 아이콘 (읽지 않은 알림 개수 표시)
   - 알림 드롭다운 메뉴
   - 읽지 않은 알림 목록 (최신 5개)
   - 알림 클릭 시 해당 게시글로 이동
   - 모두 읽음 처리
   - 30초마다 자동 새로고침

6. ✅ **라우터 통합** - App.jsx 업데이트
   - `/community` - 커뮤니티 목록
   - `/community/post/:postId` - 게시글 상세
   - `/community/write` - 게시글 작성
   - `/community/edit/:postId` - 게시글 수정

### 향후 개선 가능 사항 (선택)
1. 파일 업로드 서버 구현 (현재는 URL 입력 방식)
2. 이미지 리사이징/압축
3. 실시간 알림 (WebSocket/SSE)
4. 검색 고도화 (Elasticsearch)
5. 무한 스크롤 (Intersection Observer)
6. 댓글 좋아요/싫어요 기능

## 기술 스택

### 백엔드
- Spring Boot 3.5.6
- Java 17
- Spring Data JPA
- MySQL 8.0
- Lombok

### 프론트엔드
- React 19
- React Router v7
- Vite
- JavaScript (ES6+)

## 작성자
- Claude Code Agent
- 작성일: 2025-10-24
