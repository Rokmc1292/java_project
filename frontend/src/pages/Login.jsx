import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/auth';

/**
 * 로그인 페이지 컴포넌트
 * 아이디/비밀번호 입력 및 로그인 처리
 */
function Login() {
  const navigate = useNavigate();

  // 로그인 폼 입력값 상태 관리
  const [formData, setFormData] = useState({
    username: '',  // 아이디
    password: ''   // 비밀번호
  });

  // 에러 메시지 상태
  const [error, setError] = useState('');

  // 로딩 상태
  const [isLoading, setIsLoading] = useState(false);

  /**
   * 입력값 변경 핸들러
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // 입력 시 에러 메시지 초기화
    setError('');
  };

  /**
   * 로그인 제출 핸들러
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // 입력값 검증
    if (!formData.username || !formData.password) {
      setError('아이디와 비밀번호를 입력해주세요.');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      // 로그인 API 호출
      const result = await login(formData);

      // 로그인 성공 시 사용자 정보를 localStorage에 저장
      localStorage.setItem('user', JSON.stringify(result));

      alert(`${result.nickname}님, 환영합니다!`);
      navigate('/'); // 홈(경기 일정)으로 이동
    } catch (error) {
      setError(error.message || '로그인에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '100px auto', padding: '20px' }}>
      {/* 페이지 제목 */}
      <h1 style={{ textAlign: 'center', marginBottom: '30px' }}>로그인</h1>

      {/* 로그인 폼 */}
      <form onSubmit={handleSubmit}>
        
        {/* 아이디 입력 필드 */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            아이디
          </label>
          <input
            type="text"
            name="username"
            value={formData.username}
            onChange={handleChange}
            placeholder="아이디를 입력하세요"
            style={{ 
              width: '100%', 
              padding: '12px', 
              border: '1px solid #ddd',
              borderRadius: '4px',
              boxSizing: 'border-box',
              fontSize: '14px'
            }}
          />
        </div>

        {/* 비밀번호 입력 필드 */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            비밀번호
          </label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="비밀번호를 입력하세요"
            style={{ 
              width: '100%', 
              padding: '12px', 
              border: '1px solid #ddd',
              borderRadius: '4px',
              boxSizing: 'border-box',
              fontSize: '14px'
            }}
          />
        </div>

        {/* 에러 메시지 표시 */}
        {error && (
          <div style={{ 
            marginBottom: '20px', 
            padding: '10px', 
            backgroundColor: '#ffebee',
            color: '#c62828',
            borderRadius: '4px',
            fontSize: '14px',
            textAlign: 'center'
          }}>
            {error}
          </div>
        )}

        {/* 로그인 버튼 */}
        <button
          type="submit"
          disabled={isLoading}
          style={{
            width: '100%',
            padding: '12px',
            backgroundColor: isLoading ? '#ccc' : '#646cff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            fontSize: '16px',
            cursor: isLoading ? 'not-allowed' : 'pointer',
            marginTop: '10px'
          }}
        >
          {isLoading ? '로그인 중...' : '로그인'}
        </button>

        {/* 회원가입 페이지로 이동 링크 */}
        <div style={{ textAlign: 'center', marginTop: '20px' }}>
          <span>계정이 없으신가요? </span>
          <button
            type="button"
            onClick={() => navigate('/signup')}
            style={{
              background: 'none',
              border: 'none',
              color: '#646cff',
              textDecoration: 'underline',
              cursor: 'pointer'
            }}
          >
            회원가입
          </button>
        </div>
      </form>
    </div>
  );
}

export default Login;