package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;
    private final PostReportRepository postReportRepository;
    private final UserBlockRepository userBlockRepository;
    private final PostAttachmentRepository postAttachmentRepository;
    private final NotificationService notificationService;

    // 인기글 기준 추천수
    private static final int POPULAR_POST_THRESHOLD = 10;
    // 베스트글 기준 추천수
    private static final int BEST_POST_THRESHOLD = 50;

    /**
     * 전체 게시글 조회 (블라인드 및 차단 사용자 제외)
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(String currentUsername, Pageable pageable) {
        Page<Post> posts = postRepository.findByIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(pageable);
        return posts.map(post -> convertToDto(post, currentUsername));
    }

    /**
     * 카테고리별 게시글 조회
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getPostsByCategory(String categoryName, String currentUsername, Pageable pageable) {
        BoardCategory category = boardCategoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        Page<Post> posts = postRepository.findByCategoryAndIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(category, pageable);
        return posts.map(post -> convertToDto(post, currentUsername));
    }

    /**
     * 인기 게시글 조회 (전체)
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getPopularPosts(String currentUsername, Pageable pageable) {
        Page<Post> posts = postRepository.findByIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(pageable);
        return posts.map(post -> convertToDto(post, currentUsername));
    }

    /**
     * 카테고리별 인기 게시글 조회
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getPopularPostsByCategory(String categoryName, String currentUsername, Pageable pageable) {
        BoardCategory category = boardCategoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        Page<Post> posts = postRepository.findByCategoryAndIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(category, pageable);
        return posts.map(post -> convertToDto(post, currentUsername));
    }

    /**
     * 게시글 검색 (제목, 내용, 작성자)
     */
    @Transactional(readOnly = true)
    public Page<PostDto> searchPosts(String keyword, String searchType, String currentUsername, Pageable pageable) {
        // 검색 로직 구현 (제목, 내용, 작성자)
        // searchType: "title", "content", "author", "all"
        Page<Post> posts = postRepository.findByIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(pageable);
        return posts.map(post -> convertToDto(post, currentUsername));
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional
    public PostDto getPost(Long postId, String currentUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if (post.getIsBlinded()) {
            throw new RuntimeException("블라인드 처리된 게시글입니다.");
        }

        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        return convertToDto(post, currentUsername);
    }

    /**
     * 게시글 생성
     */
    @Transactional
    public PostDto createPost(CreatePostRequest request, String username) {
        BoardCategory category = boardCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = new Post();
        post.setCategory(category);
        post.setUser(user);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        Post savedPost = postRepository.save(post);

        // 첨부파일 저장
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (PostAttachmentDto attachmentDto : request.getAttachments()) {
                PostAttachment attachment = new PostAttachment();
                attachment.setPost(savedPost);
                attachment.setFileType(attachmentDto.getFileType());
                attachment.setFileUrl(attachmentDto.getFileUrl());
                attachment.setFileName(attachmentDto.getFileName());
                postAttachmentRepository.save(attachment);
            }
        }

        return convertToDto(savedPost, username);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostDto updatePost(Long postId, UpdatePostRequest request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getUsername().equals(username)) {
            throw new RuntimeException("게시글 작성자만 수정할 수 있습니다.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // 기존 첨부파일 삭제
        postAttachmentRepository.deleteByPost(post);

        // 새 첨부파일 저장
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (PostAttachmentDto attachmentDto : request.getAttachments()) {
                PostAttachment attachment = new PostAttachment();
                attachment.setPost(post);
                attachment.setFileType(attachmentDto.getFileType());
                attachment.setFileUrl(attachmentDto.getFileUrl());
                attachment.setFileName(attachmentDto.getFileName());
                postAttachmentRepository.save(attachment);
            }
        }

        Post updatedPost = postRepository.save(post);
        return convertToDto(updatedPost, username);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!post.getUser().getUsername().equals(username) && !user.getIsAdmin()) {
            throw new RuntimeException("게시글 작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    /**
     * 게시글 추천
     */
    @Transactional
    public void likePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 추천/비추천 했는지 확인
        if (postLikeRepository.existsByPostAndUser(post, user)) {
            PostLike existingLike = postLikeRepository.findByPostAndUser(post, user).get();
            if (existingLike.getLikeType().equals("LIKE")) {
                // 이미 추천한 경우 추천 취소
                postLikeRepository.delete(existingLike);
                post.setLikeCount(post.getLikeCount() - 1);
            } else {
                // 비추천을 추천으로 변경
                post.setDislikeCount(post.getDislikeCount() - 1);
                existingLike.setLikeType("LIKE");
                postLikeRepository.save(existingLike);
                post.setLikeCount(post.getLikeCount() + 1);
            }
        } else {
            // 새로운 추천
            PostLike postLike = new PostLike();
            postLike.setPost(post);
            postLike.setUser(user);
            postLike.setLikeType("LIKE");
            postLikeRepository.save(postLike);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        // 인기글/베스트글 상태 업데이트
        updatePostStatus(post);
        postRepository.save(post);
    }

    /**
     * 게시글 비추천
     */
    @Transactional
    public void dislikePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (postLikeRepository.existsByPostAndUser(post, user)) {
            PostLike existingLike = postLikeRepository.findByPostAndUser(post, user).get();
            if (existingLike.getLikeType().equals("DISLIKE")) {
                // 이미 비추천한 경우 비추천 취소
                postLikeRepository.delete(existingLike);
                post.setDislikeCount(post.getDislikeCount() - 1);
            } else {
                // 추천을 비추천으로 변경
                post.setLikeCount(post.getLikeCount() - 1);
                existingLike.setLikeType("DISLIKE");
                postLikeRepository.save(existingLike);
                post.setDislikeCount(post.getDislikeCount() + 1);
            }
        } else {
            // 새로운 비추천
            PostLike postLike = new PostLike();
            postLike.setPost(post);
            postLike.setUser(user);
            postLike.setLikeType("DISLIKE");
            postLikeRepository.save(postLike);
            post.setDislikeCount(post.getDislikeCount() + 1);
        }

        updatePostStatus(post);
        postRepository.save(post);
    }

    /**
     * 게시글 스크랩
     */
    @Transactional
    public void scrapPost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (postScrapRepository.existsByPostAndUser(post, user)) {
            // 이미 스크랩한 경우 스크랩 취소
            postScrapRepository.deleteByPostAndUser(post, user);
        } else {
            // 새로운 스크랩
            PostScrap scrap = new PostScrap();
            scrap.setPost(post);
            scrap.setUser(user);
            postScrapRepository.save(scrap);
        }
    }

    /**
     * 사용자 스크랩 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getUserScraps(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Page<PostScrap> scraps = postScrapRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return scraps.map(scrap -> convertToDto(scrap.getPost(), username));
    }

    /**
     * 게시글 신고
     */
    @Transactional
    public void reportPost(Long postId, String username, String reason, String description) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (postReportRepository.existsByPostAndReporter(post, reporter)) {
            throw new RuntimeException("이미 신고한 게시글입니다.");
        }

        PostReport report = new PostReport();
        report.setPost(post);
        report.setReporter(reporter);
        report.setReason(reason);
        report.setDescription(description);
        postReportRepository.save(report);
    }

    /**
     * 사용자 차단
     */
    @Transactional
    public void blockUser(String blockerUsername, String blockedUsername) {
        User blocker = userRepository.findByUsername(blockerUsername)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        User blocked = userRepository.findByUsername(blockedUsername)
                .orElseThrow(() -> new RuntimeException("차단할 사용자를 찾을 수 없습니다."));

        if (blockerUsername.equals(blockedUsername)) {
            throw new RuntimeException("자기 자신을 차단할 수 없습니다.");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            // 이미 차단한 경우 차단 해제
            userBlockRepository.deleteByBlockerAndBlocked(blocker, blocked);
        } else {
            // 새로운 차단
            UserBlock block = new UserBlock();
            block.setBlocker(blocker);
            block.setBlocked(blocked);
            userBlockRepository.save(block);
        }
    }

    /**
     * 게시글 블라인드 처리 (관리자 전용)
     */
    @Transactional
    public void blindPost(Long postId, String adminUsername) {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!admin.getIsAdmin()) {
            throw new RuntimeException("관리자만 블라인드 처리할 수 있습니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.setIsBlinded(!post.getIsBlinded());
        postRepository.save(post);
    }

    /**
     * 게시글 댓글 조회
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        return commentRepository.findByPostAndIsDeletedFalseOrderByCreatedAtAsc(post)
                .stream()
                .map(this::convertToCommentDto)
                .collect(Collectors.toList());
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

        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));
            comment.setParentComment(parentComment);

            // 대댓글 알림 전송
            notificationService.createNotification(
                parentComment.getUser().getUsername(),
                "REPLY",
                user.getNickname() + "님이 회원님의 댓글에 답글을 남겼습니다.",
                postId,
                comment.getCommentId()
            );
        } else {
            // 댓글 알림 전송 (게시글 작성자에게)
            if (!post.getUser().getUsername().equals(username)) {
                notificationService.createNotification(
                    post.getUser().getUsername(),
                    "COMMENT",
                    user.getNickname() + "님이 회원님의 게시글에 댓글을 남겼습니다.",
                    postId,
                    null
                );
            }
        }

        Comment savedComment = commentRepository.save(comment);

        // 게시글의 댓글 수 증가
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return convertToCommentDto(savedComment);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!comment.getUser().getUsername().equals(username) && !user.getIsAdmin()) {
            throw new RuntimeException("댓글 작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);

        // 게시글의 댓글 수 감소
        Post post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);
    }

    /**
     * 인기글/베스트글 상태 업데이트
     */
    private void updatePostStatus(Post post) {
        boolean wasPopular = post.getIsPopular();

        if (post.getLikeCount() >= BEST_POST_THRESHOLD) {
            post.setIsBest(true);
            post.setIsPopular(true);
        } else if (post.getLikeCount() >= POPULAR_POST_THRESHOLD) {
            post.setIsBest(false);
            post.setIsPopular(true);
        } else {
            post.setIsBest(false);
            post.setIsPopular(false);
        }

        // 인기글로 진입한 경우 알림 전송
        if (!wasPopular && post.getIsPopular()) {
            notificationService.createNotification(
                post.getUser().getUsername(),
                "POPULAR_POST",
                "회원님의 게시글이 인기글이 되었습니다!",
                post.getPostId(),
                null
            );
        }
    }

    /**
     * Entity를 DTO로 변환
     */
    private PostDto convertToDto(Post post, String currentUsername) {
        PostDto dto = new PostDto();
        dto.setPostId(post.getPostId());
        dto.setCategoryName(post.getCategory().getCategoryName());
        dto.setUsername(post.getUser().getUsername());
        dto.setNickname(post.getUser().getNickname());
        dto.setUserTier(post.getUser().getTier());
        dto.setProfileImage(post.getUser().getProfileImage());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setDislikeCount(post.getDislikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setIsNotice(post.getIsNotice());
        dto.setIsPopular(post.getIsPopular());
        dto.setIsBest(post.getIsBest());
        dto.setIsBlinded(post.getIsBlinded());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());

        // 현재 사용자의 추천/비추천 상태
        if (currentUsername != null) {
            User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
            if (currentUser != null) {
                PostLike userLike = postLikeRepository.findByPostAndUser(post, currentUser).orElse(null);
                if (userLike != null) {
                    dto.setUserLikeStatus(userLike.getLikeType());
                } else {
                    dto.setUserLikeStatus("NONE");
                }

                // 스크랩 여부
                dto.setIsScraped(postScrapRepository.existsByPostAndUser(post, currentUser));
            }
        }

        // 첨부파일 목록
        List<PostAttachment> attachments = postAttachmentRepository.findByPostOrderByDisplayOrderAsc(post);
        dto.setAttachments(attachments.stream()
                .map(this::convertToAttachmentDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private PostAttachmentDto convertToAttachmentDto(PostAttachment attachment) {
        PostAttachmentDto dto = new PostAttachmentDto();
        dto.setAttachmentId(attachment.getAttachmentId());
        dto.setFileType(attachment.getFileType());
        dto.setFileUrl(attachment.getFileUrl());
        dto.setFileName(attachment.getFileName());
        dto.setCreatedAt(attachment.getCreatedAt());
        return dto;
    }

    /**
     * 카테고리 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BoardCategoryDto> getAllCategories() {
        List<BoardCategory> categories = boardCategoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
        return categories.stream()
                .map(this::convertToCategoryDto)
                .collect(Collectors.toList());
    }

    private BoardCategoryDto convertToCategoryDto(BoardCategory category) {
        BoardCategoryDto dto = new BoardCategoryDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setDisplayOrder(category.getDisplayOrder());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }

    private CommentDto convertToCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setCommentId(comment.getCommentId());
        dto.setPostId(comment.getPost().getPostId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setNickname(comment.getUser().getNickname());
        dto.setUserTier(comment.getUser().getTier());
        dto.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null);
        dto.setContent(comment.getContent());
        dto.setLikeCount(comment.getLikeCount());
        dto.setDislikeCount(comment.getDislikeCount());
        dto.setIsDeleted(comment.getIsDeleted());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}
