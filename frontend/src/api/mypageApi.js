import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 마이페이지 API 호출 함수 모음
 */
const mypageApi = {
  /**
   * 사용자 프로필 조회
   */
  getUserProfile: async () => {
    const response = await axios.get(`${API_BASE_URL}/api/mypage/profile`, {
      withCredentials: true
    });
    return response.data;
  },

  /**
   * 예측 통계 조회
   */
  getPredictionStats: async () => {
    const response = await axios.get(`${API_BASE_URL}/api/mypage/stats/predictions`, {
      withCredentials: true
    });
    return response.data;
  },

  /**
   * 최근 10경기 예측 결과 조회
   */
  getRecentPredictionResults: async () => {
    const response = await axios.get(`${API_BASE_URL}/api/mypage/predictions/recent`, {
      withCredentials: true
    });
    return response.data;
  },

  /**
   * 사용자 예측 내역 조회 (페이징)
   */
  getUserPredictionHistory: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_BASE_URL}/api/mypage/predictions/history`, {
      params: { page, size },
      withCredentials: true
    });
    return response.data;
  },

  /**
   * 사용자 작성 게시글 조회 (페이징)
   */
  getUserPosts: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_BASE_URL}/api/mypage/posts`, {
      params: { page, size },
      withCredentials: true
    });
    return response.data;
  },

  /**
   * 사용자 작성 댓글 조회 (페이징)
   */
  getUserComments: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_BASE_URL}/api/mypage/comments`, {
      params: { page, size },
      withCredentials: true
    });
    return response.data;
  },

  /**
   * 닉네임 변경
   */
  updateNickname: async (nickname) => {
    const response = await axios.put(`${API_BASE_URL}/api/mypage/nickname`, 
      { nickname },
      { withCredentials: true }
    );
    return response.data;
  },

  /**
   * 비밀번호 변경
   */
  updatePassword: async (currentPassword, newPassword, confirmPassword) => {
    const response = await axios.put(`${API_BASE_URL}/api/mypage/password`, 
      { currentPassword, newPassword, confirmPassword },
      { withCredentials: true }
    );
    return response.data;
  },

  /**
   * 알림 설정 조회
   */
  getUserSettings: async () => {
    const response = await axios.get(`${API_BASE_URL}/api/mypage/settings`, {
      withCredentials: true
    });
    return response.data;
  },

  /**
   * 알림 설정 변경
   */
  updateUserSettings: async (settings) => {
    const response = await axios.put(`${API_BASE_URL}/api/mypage/settings`, 
      settings,
      { withCredentials: true }
    );
    return response.data;
  }
};

export default mypageApi;