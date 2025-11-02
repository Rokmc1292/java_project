import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import mypageApi from '../api/mypageApi';
import '../styles/ActivitySection.css';

// 환경변수에서 API Base URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const ActivitySection = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('predictions');
  const [predictions, setPredictions] = useState([]);
  const [predictionsPage, setPredictionsPage] = useState(0);
  const [predictionsTotalPages, setPredictionsTotalPages] = useState(0);
  const [posts, setPosts] = useState([]);
  const [postsPage, setPostsPage] = useState(0);
  const [postsTotalPages, setPostsTotalPages] = useState(0);
  const [comments, setComments] = useState([]);
  const [commentsPage, setCommentsPage] = useState(0);
  const [commentsTotalPages, setCommentsTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadTabData();
  }, [activeTab]);

  const loadTabData = async () => {
    try {
      setLoading(true);
      if (activeTab === 'predictions') {
        const data = await mypageApi.getUserPredictionHistory(0, 10);
        setPredictions(data.content);
        setPredictionsTotalPages(data.totalPages);
        setPredictionsPage(0);
      } else if (activeTab === 'posts') {
        const data = await mypageApi.getUserPosts(0, 10);
        setPosts(data.content);
        setPostsTotalPages(data.totalPages);
        setPostsPage(0);
      } else if (activeTab === 'comments') {
        const data = await mypageApi.getUserComments(0, 10);
        setComments(data.content);
        setCommentsTotalPages(data.totalPages);
        setCommentsPage(0);
      }
    } catch (error) {
      console.error('활동 내역 로드 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = async (newPage) => {
    try {
      setLoading(true);
      if (activeTab === 'predictions') {
        const data = await mypageApi.getUserPredictionHistory(newPage, 10);
        setPredictions(data.content);
        setPredictionsPage(newPage);
      } else if (activeTab === 'posts') {
        const data = await mypageApi.getUserPosts(newPage, 10);
        setPosts(data.content);
        setPostsPage(newPage);
      } else if (activeTab === 'comments') {
        const data = await mypageApi.getUserComments(newPage, 10);
        setComments(data.content);
        setCommentsPage(newPage);
      }
    } catch (error) {
      console.error('페이지 로드 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateString) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  };

  const getPredictionResultText = (result) => {
    const texts = { HOME: '홈승', DRAW: '무승부', AWAY: '원정승' };
    return texts[result] || result;
  };

  const getMatchStatusText = (status) => {
    const texts = { SCHEDULED: '예정', LIVE: '진행중', FINISHED: '종료' };
    return texts[status] || status;
  };

  const getMatchStatusColor = (status) => {
    const colors = { SCHEDULED: '#2196F3', LIVE: '#4CAF50', FINISHED: '#757575' };
    return colors[status] || '#757575';
  };

  return (
    <div className="activity-section">
      <div className="activity-tabs">
        <button className={`activity-tab-button ${activeTab === 'predictions' ? 'active' : ''}`} onClick={() => setActiveTab('predictions')}>나의 예측</button>
        <button className={`activity-tab-button ${activeTab === 'posts' ? 'active' : ''}`} onClick={() => setActiveTab('posts')}>작성한 글</button>
        <button className={`activity-tab-button ${activeTab === 'comments' ? 'active' : ''}`} onClick={() => setActiveTab('comments')}>작성한 댓글</button>
      </div>

      {loading && <div className="activity-loading"><div className="spinner"></div></div>}

      {!loading && activeTab === 'predictions' && (
        <div className="predictions-list">
          {predictions.length === 0 ? (
            <div className="empty-state">
              <p>아직 예측한 경기가 없습니다.</p>
              <button className="go-prediction-button" onClick={() => navigate('/predictions')}>승부예측 하러 가기</button>
            </div>
          ) : (
            <>
              {predictions.map((prediction) => (
                <div key={prediction.predictionId} className="prediction-card">
                  <div className="prediction-header">
                    <span className="sport-badge">{prediction.sportName}</span>
                    <span className="match-status" style={{ color: getMatchStatusColor(prediction.matchStatus) }}>{getMatchStatusText(prediction.matchStatus)}</span>
                    <span className="prediction-date">{formatDateTime(prediction.matchDate)}</span>
                  </div>
                  <div className="prediction-teams">
                    <div className="team-info">
                      {prediction.homeTeamLogo && <img src={`${API_BASE_URL}/${prediction.homeTeamLogo}`} alt={prediction.homeTeam} />}
                      <span>{prediction.homeTeam}</span>
                    </div>
                    <div className="vs-divider">VS</div>
                    <div className="team-info">
                      {prediction.awayTeamLogo && <img src={`${API_BASE_URL}/${prediction.awayTeamLogo}`} alt={prediction.awayTeam} />}
                      <span>{prediction.awayTeam}</span>
                    </div>
                  </div>
                  <div className="prediction-info">
                    <div className="predicted-result"><strong>예측:</strong> {getPredictionResultText(prediction.predictedResult)}</div>
                    {prediction.matchStatus === 'FINISHED' && (
                      <div className="match-result">
                        <strong>결과:</strong> {prediction.homeScore} - {prediction.awayScore}
                        <span className={`result-badge ${prediction.isCorrect ? 'correct' : 'incorrect'}`}>{prediction.isCorrect ? '적중 ✅' : '실패 ❌'}</span>
                      </div>
                    )}
                  </div>
                  <div className="prediction-comment"><strong>코멘트:</strong><p>{prediction.comment}</p></div>
                  <div className="prediction-footer"><span className="created-at">예측일: {formatDateTime(prediction.createdAt)}</span></div>
                </div>
              ))}
              {predictionsTotalPages > 1 && (
                <div className="pagination">
                  <button disabled={predictionsPage === 0} onClick={() => handlePageChange(predictionsPage - 1)} className="page-button">이전</button>
                  <span className="page-info">{predictionsPage + 1} / {predictionsTotalPages}</span>
                  <button disabled={predictionsPage >= predictionsTotalPages - 1} onClick={() => handlePageChange(predictionsPage + 1)} className="page-button">다음</button>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {!loading && activeTab === 'posts' && (
        <div className="posts-list">
          {posts.length === 0 ? (
            <div className="empty-state">
              <p>작성한 게시글이 없습니다.</p>
              <button className="go-community-button" onClick={() => navigate('/community')}>커뮤니티 가기</button>
            </div>
          ) : (
            <>
              {posts.map((post) => (
                <div key={post.postId} className="post-card" onClick={() => navigate(`/community/post/${post.postId}`)}>
                  <div className="post-header">
                    <span className="category-badge">{post.categoryName}</span>
                    {post.isBest && <span className="best-badge">베스트</span>}
                    {post.isPopular && <span className="popular-badge">인기</span>}
                  </div>
                  <h4 className="post-title">{post.title}</h4>
                  <div className="post-stats">
                    <span className="stat">👁️ {post.viewCount}</span>
                    <span className="stat">👍 {post.likeCount}</span>
                    <span className="stat">💬 {post.commentCount}</span>
                  </div>
                  <div className="post-footer"><span className="created-at">{formatDateTime(post.createdAt)}</span></div>
                </div>
              ))}
              {postsTotalPages > 1 && (
                <div className="pagination">
                  <button disabled={postsPage === 0} onClick={() => handlePageChange(postsPage - 1)} className="page-button">이전</button>
                  <span className="page-info">{postsPage + 1} / {postsTotalPages}</span>
                  <button disabled={postsPage >= postsTotalPages - 1} onClick={() => handlePageChange(postsPage + 1)} className="page-button">다음</button>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {!loading && activeTab === 'comments' && (
        <div className="comments-list">
          {comments.length === 0 ? (
            <div className="empty-state"><p>작성한 댓글이 없습니다.</p></div>
          ) : (
            <>
              {comments.map((comment) => (
                <div key={comment.commentId} className="comment-card" onClick={() => navigate(`/community/post/${comment.postId}`)}>
                  <div className="comment-post-title"><strong>게시글:</strong> {comment.postTitle}</div>
                  <div className={`comment-content ${comment.isDeleted ? 'deleted' : ''}`}>
                    {comment.isDeleted ? <p className="deleted-message">삭제된 댓글입니다.</p> : <p>{comment.content}</p>}
                  </div>
                  <div className="comment-footer">
                    <span className="like-count">👍 {comment.likeCount}</span>
                    <span className="created-at">{formatDateTime(comment.createdAt)}</span>
                  </div>
                </div>
              ))}
              {commentsTotalPages > 1 && (
                <div className="pagination">
                  <button disabled={commentsPage === 0} onClick={() => handlePageChange(commentsPage - 1)} className="page-button">이전</button>
                  <span className="page-info">{commentsPage + 1} / {commentsTotalPages}</span>
                  <button disabled={commentsPage >= commentsTotalPages - 1} onClick={() => handlePageChange(commentsPage + 1)} className="page-button">다음</button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default ActivitySection;