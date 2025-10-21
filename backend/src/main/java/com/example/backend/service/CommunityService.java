package com.example.backend.service;

import com.example.backend.dto.PostDto;
import com.example.backend.dto.CommentDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    /**
     * 전체 게시글 조회
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(Pageable pageable) {
        return postRepository.findByIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(pageable)
                .map(this::convertToDto);
    }

    /**
     * 카테고리별 게시글 조회
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getPostsByCategory(String categoryName, Pageable pageable) {
        BoardCategory category = boardCategoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        return postRepository.findByCategoryAndIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(category, pageable)
                .map(this::convertToDto);
    }

    /**
     * 인기 게시글 조회
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getPopularPosts(Pageable pageable) {
        return postRepository.findByIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(pageable)
                .map(this::convertToDto);
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional
    public PostDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        return convertToDto(post);
    }

    /**
     * 게시글 생성
     */
    @Transactional
    public PostDto createPost(String categoryName, String username, String title, String content) {
        BoardCategory category = boardCategoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = new Post();
        post.setCategory(category);
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);

        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
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
        }

        Comment savedComment = commentRepository.save(comment);

        // 게시글의 댓글 수 증가
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return convertToCommentDto(savedComment);
    }

    /**
     * Entity를 DTO로 변환
     */
    private PostDto convertToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setPostId(post.getPostId());
        dto.setCategoryName(post.getCategory().getCategoryName());
        dto.setUsername(post.getUser().getUsername());
        dto.setNickname(post.getUser().getNickname());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setDislikeCount(post.getDislikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setIsNotice(post.getIsNotice());
        dto.setIsPopular(post.getIsPopular());
        dto.setIsBest(post.getIsBest());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
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
