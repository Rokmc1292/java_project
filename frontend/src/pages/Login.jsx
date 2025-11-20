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

      // 응답이 { user: {...} } 형태면 user만 저장
      const user = result?.user ?? result;
      localStorage.setItem('user', JSON.stringify(user));

      // 팝업 없이 조용하게 페이지 이동
      if (user.isAdmin) {
          navigate('/admin');
      } else {
          navigate('/');
      }
    } catch (error) {
      setError(error.message || '로그인에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

    return (
        <div style={{
            minHeight: '100vh',
            backgroundColor: '#1a1a2e',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '20px'
        }}>
            <div style={{
                maxWidth: '400px',
                width: '100%',
                padding: '40px',
                backgroundColor: '#16213e',
                borderRadius: '10px',
                boxShadow: '0 4px 6px rgba(0, 0, 0, 0.3)'
            }}>
                {/* 페이지 제목 */}
                <h1 style={{
                    textAlign: 'center',
                    marginBottom: '30px',
                    color: '#ffffff',
                    fontSize: '28px',
                    fontWeight: 'bold'
                }}>
                    로그인
                </h1>

                {/* 로그인 폼 */}
                <form onSubmit={handleSubmit}>

                    {/* 아이디 입력 필드 */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{
                            display: 'block',
                            marginBottom: '8px',
                            color: '#e0e0e0',
                            fontSize: '14px',
                            fontWeight: '500'
                        }}>
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
                                border: '1px solid #444',
                                borderRadius: '6px',
                                boxSizing: 'border-box',
                                fontSize: '14px',
                                backgroundColor: '#0f3460',
                                color: '#ffffff'
                            }}
                        />
                    </div>

                    {/* 비밀번호 입력 필드 */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{
                            display: 'block',
                            marginBottom: '8px',
                            color: '#e0e0e0',
                            fontSize: '14px',
                            fontWeight: '500'
                        }}>
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
                                border: '1px solid #444',
                                borderRadius: '6px',
                                boxSizing: 'border-box',
                                fontSize: '14px',
                                backgroundColor: '#0f3460',
                                color: '#ffffff'
                            }}
                        />
                    </div>

                    {/* 에러 메시지 표시 */}
                    {error && (
                        <div style={{
                            marginBottom: '20px',
                            padding: '12px',
                            backgroundColor: '#ff4444',
                            color: '#ffffff',
                            borderRadius: '6px',
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
                            padding: '14px',
                            backgroundColor: isLoading ? '#555' : '#3b82f6',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            fontSize: '16px',
                            fontWeight: 'bold',
                            cursor: isLoading ? 'not-allowed' : 'pointer',
                            marginTop: '10px',
                            transition: 'background-color 0.3s'
                        }}
                        onMouseEnter={(e) => {
                            if (!isLoading) e.currentTarget.style.backgroundColor = '#2563eb';
                        }}
                        onMouseLeave={(e) => {
                            if (!isLoading) e.currentTarget.style.backgroundColor = '#3b82f6';
                        }}
                    >
                        {isLoading ? '로그인 중...' : '로그인'}
                    </button>

                    {/* 회원가입 페이지로 이동 링크 */}
                    <div style={{ textAlign: 'center', marginTop: '20px' }}>
                        <span style={{ color: '#b0b0b0' }}>계정이 없으신가요? </span>
                        <button
                            type="button"
                            onClick={() => navigate('/signup')}
                            style={{
                                background: 'none',
                                border: 'none',
                                color: '#3b82f6',
                                textDecoration: 'underline',
                                cursor: 'pointer',
                                fontSize: '14px'
                            }}
                        >
                            회원가입
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Login;