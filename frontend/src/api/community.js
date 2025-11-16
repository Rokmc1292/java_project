/**
 * 커뮤니티 관련 API 함수들
 */

import { apiGet, apiPost, apiPut, apiDelete } from './api';

// ========== 게시글 조회 ==========

/**
 * 전체 게시글 조회 (페이징, 검색)
 */
export const getPosts = async (page = 0, size = 20, search = '', searchType = 'all') => {
  const searchParam = search ? `&keyword=${encodeURIComponent(search)}&searchType=${searchType}` : '';
  return await apiGet(`/api/community/posts?page=${page}&size=${size}${searchParam}`);
};

/**
 * 카테고리별 게시글 조회
 */
export const getPostsByCategory = async (categoryName, page = 0, size = 20) => {
  return await apiGet(`/api/community/posts?categoryName=${encodeURIComponent(categoryName)}&type=all&page=${page}&size=${size}`);
};

/**
 * 인기 게시글 조회 (전체)
 */
export const getPopularPosts = async (page = 0, size = 20) => {
  return await apiGet(`/api/community/posts?type=popular&page=${page}&size=${size}`);
};

/**
 * 카테고리별 인기 게시글 조회
 */
export const getPopularPostsByCategory = async (categoryName, page = 0, size = 20) => {
  return await apiGet(`/api/community/posts?categoryName=${encodeURIComponent(categoryName)}&type=popular&page=${page}&size=${size}`);
};

/**
 * 게시글 상세 조회
 */
export const getPost = async (postId) => {
  return await apiGet(`/api/community/posts/${postId}`);
};

// ========== 게시글 작성/수정/삭제 ==========

/**
 * 게시글 작성 (인증 필요)
 */
export const createPost = async (categoryName, title, content) => {
  return await apiPost('/api/community/posts', {
    categoryName,
    title,
    content
  });
};

/**
 * 게시글 수정 (본인만 가능)
 */
export const updatePost = async (postId, title, content) => {
  return await apiPut(`/api/community/posts/${postId}`, {
    title,
    content
  });
};

/**
 * 게시글 삭제 (본인만 가능)
 */
export const deletePost = async (postId) => {
  return await apiDelete(`/api/community/posts/${postId}`);
};

// ========== 게시글 추천/비추천 ==========

/**
 * 게시글 추천
 */
export const likePost = async (postId) => {
  return await apiPost(`/api/community/posts/${postId}/like`);
};

/**
 * 게시글 비추천
 */
export const dislikePost = async (postId) => {
  return await apiPost(`/api/community/posts/${postId}/dislike`);
};

// ========== 댓글 기능 ==========

/**
 * 게시글의 댓글 목록 조회
 */
export const getComments = async (postId) => {
  return await apiGet(`/api/community/posts/${postId}/comments`);
};

/**
 * 댓글 작성
 */
export const createComment = async (postId, content, parentCommentId = null) => {
  return await apiPost(`/api/community/posts/${postId}/comments`, {
    content,
    parentCommentId
  });
};

/**
 * 댓글 수정
 */
export const updateComment = async (commentId, content) => {
  return await apiPut(`/api/community/comments/${commentId}`, {
    content
  });
};

/**
 * 댓글 삭제
 */
export const deleteComment = async (commentId) => {
  return await apiDelete(`/api/community/comments/${commentId}`);
};

/**
 * 댓글 추천
 */
export const likeComment = async (commentId) => {
  return await apiPost(`/api/community/comments/${commentId}/like`);
};

/**
 * 댓글 비추천
 */
export const dislikeComment = async (commentId) => {
  return await apiPost(`/api/community/comments/${commentId}/dislike`);
};

// ========== 스크랩 기능 ==========

/**
 * 게시글 스크랩
 */
export const scrapPost = async (postId) => {
  return await apiPost(`/api/community/posts/${postId}/scrap`);
};

/**
 * 게시글 스크랩 취소
 */
export const unscrapPost = async (postId) => {
  return await apiDelete(`/api/community/posts/${postId}/scrap`);
};

// ========== 신고 기능 ==========

/**
 * 게시글 신고
 */
export const reportPost = async (postId, reason, description) => {
  return await apiPost(`/api/community/posts/${postId}/report`, {
    reason,
    description
  });
};

/**
 * 댓글 신고
 */
export const reportComment = async (commentId, reason, description) => {
  return await apiPost(`/api/community/comments/${commentId}/report`, {
    reason,
    description
  });
};

// ========== 사용자 차단 ==========

/**
 * 사용자 차단
 */
export const blockUser = async (blockedUsername) => {
  return await apiPost(`/api/community/users/${blockedUsername}/block`);
};

/**
 * 사용자 차단 해제
 */
export const unblockUser = async (blockedUsername) => {
  return await apiDelete(`/api/community/users/${blockedUsername}/block`);
};