import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';

/**
 * 게시글 수정 페이지
 */
function PostEdit() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    title: '',
    content: ''
  });
  const [attachments, setAttachments] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  // 로그인 확인
  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (!userData) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      setUser(JSON.parse(userData));
    } catch (e) {
      console.error('사용자 정보 파싱 오류:', e);
      navigate('/login');
    }
  }, [navigate]);

  // 게시글 조회
  useEffect(() => {
    const fetchPost = async () => {
      if (!user) return;

      setLoading(true);
      try {
        const response = await fetch(
          `http://localhost:8080/api/community/posts/${postId}?username=${user.username}`
        );

        if (!response.ok) throw new Error('게시글을 찾을 수 없습니다.');

        const post = await response.json();

        // 작성자 확인
        if (post.username !== user.username) {
          alert('본인이 작성한 게시글만 수정할 수 있습니다.');
          navigate(`/community/post/${postId}`);
          return;
        }

        setFormData({
          title: post.title,
          content: post.content
        });
        setAttachments(post.attachments || []);
      } catch (error) {
        console.error('게시글 조회 실패:', error);
        alert('게시글을 불러올 수 없습니다.');
        navigate('/community');
      } finally {
        setLoading(false);
      }
    };

    fetchPost();
  }, [postId, user, navigate]);

  // 폼 입력 핸들러
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // 첨부파일 추가
  const handleAddAttachment = (type) => {
    let url = '';
    if (type === 'IMAGE') {
      url = prompt('이미지 URL을 입력하세요:');
    } else if (type === 'LINK') {
      url = prompt('링크 URL을 입력하세요:');
    }

    if (url) {
      setAttachments(prev => [
        ...prev,
        {
          fileType: type,
          fileUrl: url,
          fileName: url.split('/').pop() || url
        }
      ]);
    }
  };

  // 첨부파일 삭제
  const handleRemoveAttachment = (index) => {
    setAttachments(prev => prev.filter((_, i) => i !== index));
  };

  // 게시글 수정
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.title.trim()) {
      alert('제목을 입력하세요.');
      return;
    }

    if (!formData.content.trim()) {
      alert('내용을 입력하세요.');
      return;
    }

    setSubmitting(true);

    try {
      const response = await fetch(
        `http://localhost:8080/api/community/posts/${postId}?username=${user.username}`,
        {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            title: formData.title,
            content: formData.content,
            attachments: attachments.length > 0 ? attachments : null
          })
        }
      );

      if (response.ok) {
        alert('게시글이 수정되었습니다!');
        navigate(`/community/post/${postId}`);
      } else {
        alert('게시글 수정에 실패했습니다.');
      }
    } catch (error) {
      console.error('게시글 수정 실패:', error);
      alert('게시글 수정 중 오류가 발생했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  if (!user || loading) {
    return (
      <div>
        <Navbar />
        <div style={{ maxWidth: '900px', margin: '0 auto', padding: '20px', textAlign: 'center' }}>
          로딩 중...
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: '900px', margin: '0 auto', padding: '20px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '30px' }}>
          ✏️ 게시글 수정
        </h1>

        <form onSubmit={handleSubmit}>
          {/* 제목 */}
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              제목
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="제목을 입력하세요"
              required
              maxLength={200}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            />
          </div>

          {/* 내용 */}
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              내용
            </label>
            <textarea
              name="content"
              value={formData.content}
              onChange={handleChange}
              placeholder="내용을 입력하세요"
              required
              style={{
                width: '100%',
                minHeight: '400px',
                padding: '15px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px',
                resize: 'vertical',
                fontFamily: 'inherit'
              }}
            />
          </div>

          {/* 첨부파일 */}
          <div style={{ marginBottom: '30px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
              첨부파일
            </label>
            <div style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
              <button
                type="button"
                onClick={() => handleAddAttachment('IMAGE')}
                style={{
                  padding: '10px 20px',
                  backgroundColor: '#f5f5f5',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                📷 이미지 URL 추가
              </button>
              <button
                type="button"
                onClick={() => handleAddAttachment('LINK')}
                style={{
                  padding: '10px 20px',
                  backgroundColor: '#f5f5f5',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                🔗 링크 추가
              </button>
            </div>

            {/* 첨부파일 목록 */}
            {attachments.length > 0 && (
              <div style={{
                padding: '15px',
                backgroundColor: '#f9f9f9',
                borderRadius: '5px',
                border: '1px solid #e0e0e0'
              }}>
                {attachments.map((att, index) => (
                  <div
                    key={index}
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: '10px',
                      marginBottom: '10px',
                      backgroundColor: 'white',
                      borderRadius: '5px',
                      border: '1px solid #e0e0e0'
                    }}
                  >
                    <div style={{ fontSize: '14px' }}>
                      <span style={{ fontWeight: 'bold', marginRight: '10px' }}>
                        {att.fileType === 'IMAGE' ? '📷' : '🔗'}
                      </span>
                      <span>{att.fileUrl}</span>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleRemoveAttachment(index)}
                      style={{
                        padding: '5px 12px',
                        backgroundColor: '#ff4444',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        fontSize: '12px'
                      }}
                    >
                      삭제
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* 버튼 */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <button
              type="button"
              onClick={() => navigate(`/community/post/${postId}`)}
              style={{
                padding: '12px 30px',
                backgroundColor: '#f5f5f5',
                border: '1px solid #ddd',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '16px'
              }}
            >
              취소
            </button>
            <button
              type="submit"
              disabled={submitting}
              style={{
                padding: '12px 30px',
                backgroundColor: submitting ? '#ccc' : '#646cff',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: submitting ? 'not-allowed' : 'pointer',
                fontSize: '16px',
                fontWeight: 'bold'
              }}
            >
              {submitting ? '수정 중...' : '수정 완료'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default PostEdit;
