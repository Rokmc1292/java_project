package com.example.backend.service;

import com.example.backend.dto.CommentDto;
import com.example.backend.dto.PostDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 커뮤니티 서비스
 * 게시글, 댓글, 추천, 스크랩, 신고 등 모든 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final UserRepository userRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final PostScrapRepository postScrapRepository;
    private final ReportRepository reportRepository;
    private final UserBlockRepository userBlockRepository;
    private final NotificationService notificationService;

    // 인기글 기준 추천수
    private static final int POPULAR_POST_THRESHOLD = 10;

    // ========== 게시글 조회 ==========

    /**
     * 전체 게시글 조회 (공지사항 상단 고정, 블라인드 제외)
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(Pageable pageable) {
        return postRepository.findByIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(pageable)
                .map(this::convertToDto);
    }

    /**
     * 카테고리별 게시글 조회
     */
    // ========== 게시글 조회 (카테고리별) ==========

    @Transactional(readOnly = true)
    public Page<PostDto> getPostsByCategory(String categoryName, Pageable pageable) {
        try {
            Page<Post> posts;

            if (categoryName == null || categoryName.isEmpty() || categoryName.equals("전체") || categoryName.equals("all")) {
                // 전체 게시글 조회 (블라인드 제외)
                posts = postRepository.findByIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(pageable);
            } else {
                // 특정 카테고리 게시글 조회 (블라인드 제외)
                BoardCategory category = boardCategoryRepository.findByCategoryName(categoryName).orElse(null);
                if (category == null) {
                    // 카테고리가 없으면 빈 페이지 반환
                    return Page.empty(pageable);
                }
                posts = postRepository.findByCategoryAndIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(category, pageable);
            }

            return posts.map(this::convertToDto);
        } catch (Exception e) {
            log.error("게시글 조회 중 에러 발생", e);
            return Page.empty(pageable);
        }
    }

// ========== 인기글 조회 ==========

    @Transactional(readOnly = true)
    public Page<PostDto> getPopularPosts(Pageable pageable) {
        try {
            // 블라인드 제외한 인기글 조회
            Page<Post> posts = postRepository.findByIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(pageable);
            return posts.map(this::convertToDto);
        } catch (Exception e) {
            log.error("인기글 조회 중 에러 발생", e);
            return Page.empty(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getPopularPostsByCategory(String categoryName, Pageable pageable) {
        try {
            if (categoryName == null || categoryName.isEmpty() || categoryName.equals("전체") || categoryName.equals("all")) {
                // 전체 인기글 (블라인드 제외)
                Page<Post> posts = postRepository.findByIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(pageable);
                return posts.map(this::convertToDto);
            }

            BoardCategory category = boardCategoryRepository.findByCategoryName(categoryName).orElse(null);
            if (category == null) {
                return Page.empty(pageable);
            }

            // 카테고리별 인기글 (블라인드 제외)
            Page<Post> posts = postRepository.findByCategoryAndIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(category, pageable);
            return posts.map(this::convertToDto);
        } catch (Exception e) {
            log.error("카테고리별 인기글 조회 중 에러 발생", e);
            return Page.empty(pageable);
        }
    }

    /**
     * 게시글 검색 (제목, 내용, 작성자) - 블라인드 제외
     */
    @Transactional(readOnly = true)
    public Page<PostDto> searchPosts(String keyword, Pageable pageable) {
        return postRepository.searchByKeywordExcludingBlinded(keyword, pageable)
                .map(this::convertToDto);
    }

    /**
     * 게시글 상세 조회 (조회수 증가)
     */
    @Transactional
    public PostDto getPost(Long postId, HttpSession session) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 블라인드된 게시글은 조회 불가
        if (post.getIsBlinded()) {
            throw new RuntimeException("블라인드 처리된 게시글입니다.");
        }

        // 조회수 증가 (중복 방지 로직)
        String viewKey = "viewed_post_" + postId;
        Long lastViewTime = (Long) session.getAttribute(viewKey);
        long currentTime = System.currentTimeMillis();

        // 5분(300,000ms) 이내에 같은 게시글을 조회한 적이 없으면 조회수 증가
        if (lastViewTime == null || (currentTime - lastViewTime) > 300000) {
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
            session.setAttribute(viewKey, currentTime);
        }

        return convertToDto(post);
    }

    // ========== 게시글 작성/수정/삭제 ==========

    /**
     * 게시글 작성
     */
    @Transactional
    public PostDto createPost(String username, String categoryName, String title, String content) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // categoryName이 null이거나 비어있으면 기본값 설정
        final String finalCategoryName;  // ⭐ final 변수로 선언
        if (categoryName == null || categoryName.isEmpty()) {
            finalCategoryName = "자유게시판";
        } else {
            finalCategoryName = categoryName;
        }

        BoardCategory category = boardCategoryRepository.findByCategoryName(finalCategoryName)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + finalCategoryName));

        Post post = new Post();
        post.setCategory(category);
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);

        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
    }

    /**
     * 게시글 수정 (본인만 가능)
     */
    @Transactional
    public PostDto updatePost(Long postId, String username, String title, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 본인 확인
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        post.setTitle(title);
        post.setContent(content);
        Post updatedPost = postRepository.save(post);

        return convertToDto(updatedPost);
    }

    /**
     * 게시글 삭제 (본인만 가능)
     */
    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 본인 확인
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // ⭐ 게시글에 달린 모든 댓글 먼저 삭제
        List<Comment> comments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(post);

        // 대댓글 먼저 삭제
        for (Comment comment : comments) {
            List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);

            // ⭐ 대댓글의 투표 먼저 삭제
            for (Comment reply : replies) {
                commentVoteRepository.deleteByComment(reply);
            }
            commentRepository.deleteAll(replies);

            // ⭐ 부모 댓글의 투표 삭제
            commentVoteRepository.deleteByComment(comment);
        }

        // 그 다음 부모 댓글 삭제
        commentRepository.deleteAll(comments);

        // ⭐ 게시글 관련 추천/비추천 데이터 삭제
        postVoteRepository.deleteByPost(post);

        // ⭐ 게시글 스크랩 데이터 삭제
        postScrapRepository.deleteByPost(post);

        // 마지막으로 게시글 삭제
        postRepository.delete(post);
    }

    // ========== 게시글 추천/비추천 ==========

    /**
     * 게시글 추천 (1인 1회 제한)
     */
    @Transactional
    public void likePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 투표했는지 확인
        Optional<PostVote> existingVote = postVoteRepository.findByPostAndUser(post, user);
        if (existingVote.isPresent()) {
            throw new IllegalArgumentException("이미 추천/비추천한 게시글입니다.");
        }

        // 추천 저장
        PostVote vote = new PostVote();
        vote.setPost(post);
        vote.setUser(user);
        vote.setVoteType("LIKE");
        postVoteRepository.save(vote);

        // 추천수 증가
        post.setLikeCount(post.getLikeCount() + 1);

        // 인기글 기준 체크
        checkAndUpdatePopularStatus(post);

        postRepository.save(post);

        // 인기글 진입 알림 (기준 달성 시)
        if (post.getIsPopular() && post.getLikeCount() == POPULAR_POST_THRESHOLD) {
            notificationService.createPopularPostNotification(post);
        }
    }

    /**
     * 게시글 비추천 (1인 1회 제한)
     */
    @Transactional
    public void dislikePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 투표했는지 확인
        Optional<PostVote> existingVote = postVoteRepository.findByPostAndUser(post, user);
        if (existingVote.isPresent()) {
            throw new IllegalArgumentException("이미 추천/비추천한 게시글입니다.");
        }

        // 비추천 저장
        PostVote vote = new PostVote();
        vote.setPost(post);
        vote.setUser(user);
        vote.setVoteType("DISLIKE");
        postVoteRepository.save(vote);

        // 비추천수 증가
        post.setDislikeCount(post.getDislikeCount() + 1);

        checkAndUpdatePopularStatus(post);

        postRepository.save(post);
    }

    /**
     * 추천/비추천 취소
     */
    @Transactional
    public void cancelPostVote(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        PostVote vote = postVoteRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new RuntimeException("투표 내역을 찾을 수 없습니다."));

        // 추천/비추천 수 감소
        if ("LIKE".equals(vote.getVoteType())) {
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            post.setDislikeCount(post.getDislikeCount() - 1);
        }

        // 인기글 상태 재확인
        checkAndUpdatePopularStatus(post);

        postVoteRepository.delete(vote);
        postRepository.save(post);
    }

    /**
     * 인기글 상태 체크 및 업데이트
     * 추천수가 기준 이상이면 인기글로 설정
     */
    private void checkAndUpdatePopularStatus(Post post) {
        int netLikes = post.getLikeCount() - post.getDislikeCount();  // 순수 추천 수
        if (netLikes >= POPULAR_POST_THRESHOLD) {
            post.setIsPopular(true);
        } else {
            post.setIsPopular(false);
        }
    }

    // ========== 댓글 기능 ==========

    /**
     * 게시글의 댓글 목록 조회 (계층 구조)
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 삭제된 댓글도 포함하여 조회
        List<Comment> comments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(post);

        List<CommentDto> commentDtos = comments.stream()
                .map(this::convertCommentToDto)
                .collect(Collectors.toList());

        // 모든 댓글에 대해 isBest 설정 (댓글과 대댓글 모두)
        for (CommentDto comment : commentDtos) {
            // 부모 댓글의 isBest 설정
            int commentNetLikes = comment.getLikeCount() - comment.getDislikeCount();
            if (!comment.getIsDeleted() && commentNetLikes >= 10) {
                comment.setIsBest(true);
            } else {

                comment.setIsBest(false);
            }

            // 대댓글들의 isBest 설정
            if (comment.getReplies() != null) {
                for (CommentDto reply : comment.getReplies()) {
                    int replyNetLikes = reply.getLikeCount() - reply.getDislikeCount();
                    if (!reply.getIsDeleted() && replyNetLikes >= 10) {
                        reply.setIsBest(true);
                    } else {
                        reply.setIsBest(false);
                    }
                }
            }
        }

        return commentDtos;
    }

    /**
     * 게시글의 댓글 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCommentsPaginated(Long postId, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 전체 댓글 조회
        List<Comment> allComments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(post);

        List<CommentDto> allCommentDtos = allComments.stream()
                .map(this::convertCommentToDto)
                .collect(Collectors.toList());

        // ⭐ 베스트 댓글 수집 (페이지와 상관없이)
        List<CommentDto> bestComments = new ArrayList<>();

        // 모든 댓글에 대해 isBest 설정 및 베스트 댓글 수집
        for (CommentDto comment : allCommentDtos) {
            // 부모 댓글의 isBest 설정
            int commentNetLikes = comment.getLikeCount() - comment.getDislikeCount();
            if (!comment.getIsDeleted() && commentNetLikes >= 10) {  // 1 → 10, 순수 추천 수 계산
                comment.setIsBest(true);
                bestComments.add(comment);
            } else {
                comment.setIsBest(false);
            }

            // 대댓글의 isBest 설정
            if (comment.getReplies() != null) {
                for (CommentDto reply : comment.getReplies()) {
                    int replyNetLikes = reply.getLikeCount() - reply.getDislikeCount();
                    if (!reply.getIsDeleted() && replyNetLikes >= 10) {  // 1 → 10, 순수 추천 수 계산
                        reply.setIsBest(true);
                        // ⭐ 대댓글도 베스트 댓글에 추가
                        CommentDto bestReply = new CommentDto();
                        bestReply.setCommentId(reply.getCommentId());
                        bestReply.setPostId(reply.getPostId());
                        bestReply.setUsername(reply.getUsername());
                        bestReply.setNickname(reply.getNickname());
                        bestReply.setContent(reply.getContent());
                        bestReply.setLikeCount(reply.getLikeCount());
                        bestReply.setDislikeCount(reply.getDislikeCount());
                        bestReply.setIsDeleted(reply.getIsDeleted());
                        bestReply.setCreatedAt(reply.getCreatedAt());
                        bestReply.setUpdatedAt(reply.getUpdatedAt());
                        bestReply.setIsBest(true);
                        bestComments.add(bestReply);
                    } else {
                        reply.setIsBest(false);
                    }
                }
            }
        }

        // ⭐ 베스트 댓글 추천순 정렬 후 상위 5개
        bestComments.sort((c1, c2) -> {
            int net1 = c1.getLikeCount() - c1.getDislikeCount();
            int net2 = c2.getLikeCount() - c2.getDislikeCount();
            return Integer.compare(net2, net1);
        });
        List<CommentDto> topBestComments = bestComments.stream()
                .limit(5)
                .collect(Collectors.toList());

        // ⭐ 총 댓글 개수 계산 (대댓글 포함, 삭제된 댓글 제외)
        int totalCommentsCount = 0;
        for (CommentDto comment : allCommentDtos) {
            if (!comment.getIsDeleted()) {
                totalCommentsCount++;
            }
            if (comment.getReplies() != null) {
                for (CommentDto reply : comment.getReplies()) {
                    if (!reply.getIsDeleted()) {
                        totalCommentsCount++;
                    }
                }
            }
        }

        // 페이지네이션 적용
        int totalComments = allCommentDtos.size();
        int totalPages = (int) Math.ceil((double) totalComments / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalComments);

        List<CommentDto> pagedComments;
        if (fromIndex < totalComments) {
            pagedComments = allCommentDtos.subList(fromIndex, toIndex);
        } else {
            pagedComments = new ArrayList<>();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("comments", pagedComments);
        result.put("bestComments", topBestComments);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalComments", totalCommentsCount);
        result.put("hasNext", page < totalPages - 1);
        result.put("hasPrevious", page > 0);

        return result;
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public CommentDto createComment(Long postId, String username, String content, Long parentCommentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);

        // 대댓글인 경우
        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));
            comment.setParentComment(parentComment);

            // 대댓글 알림 발송
            notificationService.createReplyNotification(parentComment, comment);
        } else {
            // 댓글 알림 발송 (게시글 작성자에게)
            notificationService.createCommentNotification(post, comment);
        }

        // 댓글 수 증가
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        Comment savedComment = commentRepository.save(comment);
        return convertCommentToDto(savedComment);
    }

    /**
     * 댓글 수정 (본인만 가능)
     */
    @Transactional
    public CommentDto updateComment(Long commentId, String username, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 본인 확인
        if (!comment.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.setContent(content);
        Comment updatedComment = commentRepository.save(comment);

        return convertCommentToDto(updatedComment);
    }

    /**
     * 댓글 삭제 (본인만 가능)
     * 실제 삭제가 아닌 isDeleted = true 처리
     */
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 본인 확인
        if (!comment.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        // 소프트 삭제
        comment.setIsDeleted(true);
        comment.setContent("삭제된 댓글입니다.");
        commentRepository.save(comment);
    }

    /**
     * 댓글 추천
     */
    @Transactional
    public void likeComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 투표했는지 확인
        Optional<CommentVote> existingVote = commentVoteRepository.findByCommentAndUser(comment, user);
        if (existingVote.isPresent()) {
            throw new IllegalArgumentException("이미 추천/비추천한 댓글입니다.");
        }

        // 추천 저장
        CommentVote vote = new CommentVote();
        vote.setComment(comment);
        vote.setUser(user);
        vote.setVoteType("LIKE");
        commentVoteRepository.save(vote);

        // 추천수 증가
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    /**
     * 댓글 비추천
     */
    @Transactional
    public void dislikeComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 투표했는지 확인
        Optional<CommentVote> existingVote = commentVoteRepository.findByCommentAndUser(comment, user);
        if (existingVote.isPresent()) {
            throw new IllegalArgumentException("이미 추천/비추천한 댓글입니다.");
        }

        // 비추천 저장
        CommentVote vote = new CommentVote();
        vote.setComment(comment);
        vote.setUser(user);
        vote.setVoteType("DISLIKE");
        commentVoteRepository.save(vote);

        // 비추천수 증가
        comment.setDislikeCount(comment.getDislikeCount() + 1);
        commentRepository.save(comment);
    }

    // ========== 스크랩 기능 ==========

    /**
     * 게시글 스크랩
     */
    @Transactional
    public void scrapPost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 스크랩했는지 확인
        if (postScrapRepository.findByPostAndUser(post, user).isPresent()) {
            throw new IllegalArgumentException("이미 스크랩한 게시글입니다.");
        }

        PostScrap scrap = new PostScrap();
        scrap.setPost(post);
        scrap.setUser(user);
        postScrapRepository.save(scrap);
    }

    /**
     * 게시글 스크랩 취소
     */
    @Transactional
    public void unscrapPost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        PostScrap scrap = postScrapRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new RuntimeException("스크랩 내역을 찾을 수 없습니다."));

        postScrapRepository.delete(scrap);
    }

    /**
     * 내 스크랩 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getMyScraps(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return postScrapRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(scrap -> convertToDto(scrap.getPost()));
    }

    // ========== 신고 기능 ==========

    /**
     * 게시글 신고
     */
    @Transactional
    public void reportPost(Long postId, String username, String reason, String description) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType("POST");
        report.setTargetId(postId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus("PENDING");

        reportRepository.save(report);
    }

    /**
     * 댓글 신고
     */
    @Transactional
    public void reportComment(Long commentId, String username, String reason, String description) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType("COMMENT");
        report.setTargetId(commentId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus("PENDING");

        reportRepository.save(report);
    }

    // ========== 사용자 차단 ==========

    /**
     * 사용자 차단
     */
    @Transactional
    public void blockUser(String username, String blockedUsername) {
        User blocker = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        User blocked = userRepository.findByUsername(blockedUsername)
                .orElseThrow(() -> new RuntimeException("차단할 사용자를 찾을 수 없습니다."));

        // 자기 자신 차단 방지
        if (username.equals(blockedUsername)) {
            throw new IllegalArgumentException("자기 자신을 차단할 수 없습니다.");
        }

        // 이미 차단했는지 확인
        if (userBlockRepository.findByBlockerAndBlocked(blocker, blocked).isPresent()) {
            throw new IllegalArgumentException("이미 차단한 사용자입니다.");
        }

        UserBlock block = new UserBlock();
        block.setBlocker(blocker);
        block.setBlocked(blocked);
        userBlockRepository.save(block);
    }

    /**
     * 사용자 차단 해제
     */
    @Transactional
    public void unblockUser(String username, String blockedUsername) {
        User blocker = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        User blocked = userRepository.findByUsername(blockedUsername)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserBlock block = userBlockRepository.findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new RuntimeException("차단 내역을 찾을 수 없습니다."));

        userBlockRepository.delete(block);
    }

    // ========== 통계 및 랭킹 ==========

    /**
     * 주간 베스트 게시글 (최근 7일)
     */
    @Transactional(readOnly = true)
    public List<PostDto> getWeeklyBestPosts() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        Pageable topTen = PageRequest.of(0, 10);

        return postRepository.findWeeklyBest(weekAgo, topTen)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 월간 베스트 게시글 (최근 30일)
     */
    @Transactional(readOnly = true)
    public List<PostDto> getMonthlyBestPosts() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        Pageable topTen = PageRequest.of(0, 10);

        return postRepository.findMonthlyBest(monthAgo, topTen)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 활동 왕성 유저 조회 (게시글 + 댓글 수 기준)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopActiveUsers() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        Page<User> topUsersPage = userRepository.findTopActiveUsers(monthAgo, PageRequest.of(0, 10));
        List<User> topUsers = topUsersPage.getContent();
        return topUsers.stream().map(user -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("tier", user.getTier());
            userInfo.put("postCount", postRepository.countByUserAndCreatedAtAfter(user, monthAgo));
            userInfo.put("commentCount", commentRepository.countByUserAndCreatedAtAfter(user, monthAgo));
            return userInfo;
        }).collect(Collectors.toList());
    }

    /**
     * 내가 작성한 게시글 목록
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getMyPosts(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return postRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToDto);
    }

    /**
     * 내가 작성한 댓글 목록
     */
    @Transactional(readOnly = true)
    public Page<CommentDto> getMyComments(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return commentRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertCommentToDto);
    }

    // ========== 관리자 기능 ==========

    /**
     * 게시글 블라인드 처리 (관리자만)
     */
    @Transactional
    public void blindPost(Long postId, String username) {
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!admin.getIsAdmin()) {
            throw new IllegalArgumentException("관리자만 블라인드 처리할 수 있습니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.setIsBlinded(true);
        postRepository.save(post);
    }

    /**
     * 게시글 블라인드 해제 (관리자만)
     */
    @Transactional
    public void unblindPost(Long postId, String username) {
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!admin.getIsAdmin()) {
            throw new IllegalArgumentException("관리자만 블라인드 해제할 수 있습니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.setIsBlinded(false);
        postRepository.save(post);
    }

    // ========== DTO 변환 헬퍼 메서드 ==========

    /**
     * Post 엔티티를 PostDto로 변환
     */
    private PostDto convertToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setPostId(post.getPostId());

        // 카테고리 null 체크
        if (post.getCategory() != null) {
            dto.setCategoryName(post.getCategory().getCategoryName());
        } else {
            dto.setCategoryName("미분류");
        }

        // 사용자 null 체크
        if (post.getUser() != null) {
            dto.setUsername(post.getUser().getUsername());
            dto.setNickname(post.getUser().getNickname());
        } else {
            dto.setUsername("알 수 없음");
            dto.setNickname("알 수 없음");
        }

        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setDislikeCount(post.getDislikeCount());

        // ⭐ 실제 댓글 개수 계산 (대댓글 포함, 삭제된 댓글 제외)
        int actualCommentCount = calculateActualCommentCount(post);
        dto.setCommentCount(actualCommentCount);
        dto.setIsNotice(post.getIsNotice());
        dto.setIsPopular(post.getIsPopular());
        dto.setIsBest(post.getIsBest());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());

        return dto;
    }
    /**
     * 실제 댓글 개수 계산 (대댓글 포함, 삭제된 댓글 제외)
     */
    private int calculateActualCommentCount(Post post) {
        List<Comment> allComments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(post);

        int count = 0;
        for (Comment comment : allComments) {
            // 부모 댓글이 삭제되지 않았으면 카운트
            if (!comment.getIsDeleted()) {
                count++;
            }

            // 대댓글 카운트
            List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
            for (Comment reply : replies) {
                if (!reply.getIsDeleted()) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Comment 엔티티를 CommentDto로 변환 (대댓글 포함)
     */
    private CommentDto convertCommentToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setCommentId(comment.getCommentId());
        dto.setPostId(comment.getPost().getPostId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setNickname(comment.getUser().getNickname());
        dto.setContent(comment.getContent());
        dto.setLikeCount(comment.getLikeCount());
        dto.setDislikeCount(comment.getDislikeCount());
        dto.setIsDeleted(comment.getIsDeleted());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        // ⭐ 대댓글 조회 및 변환 (삭제된 대댓글도 포함)
        if (comment.getParentComment() == null) {
            List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
            dto.setReplies(replies.stream()
                    .map(this::convertCommentToDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}