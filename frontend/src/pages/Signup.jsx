import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { signup, checkUsernameDuplicate, checkEmailDuplicate, checkNicknameDuplicate } from '../api/auth';

/**
 * 회원가입 페이지 컴포넌트
 * 폼 입력, 유효성 검사, 중복 체크, 회원가입 처리
 */
function Signup() {
  const navigate = useNavigate();

  // 폼 입력값 상태 관리
  const [formData, setFormData] = useState({
    username: '',      // 아이디
    password: '',      // 비밀번호
    passwordConfirm: '',  // 비밀번호 확인
    nickname: '',      // 닉네임
    email: ''          // 이메일
  });

  // 중복 체크 결과 상태 관리
  const [duplicateCheck, setDuplicateCheck] = useState({
    username: null,    // null: 미체크, true: 중복, false: 사용가능
    email: null,
    nickname: null
  });

  // 에러 메시지 상태 관리
  const [errors, setErrors] = useState({});

  // 로딩 상태
  const [isLoading, setIsLoading] = useState(false);

  /**
   * 입력값 변경 핸들러
   * 사용자가 입력할 때마다 formData 업데이트
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // 입력값 변경 시 해당 필드의 중복 체크 결과 초기화
    if (name in duplicateCheck) {
      setDuplicateCheck(prev => ({
        ...prev,
        [name]: null
      }));
    }
  };

  /**
   * 아이디 중복 체크 핸들러
   */
  const handleCheckUsername = async () => {
    if (!formData.username) {
      alert('아이디를 입력해주세요.');
      return;
    }

    // 아이디 유효성 검사 (4-20자, 영문+숫자)
    const usernameRegex = /^[a-zA-Z0-9]{4,20}$/;
    if (!usernameRegex.test(formData.username)) {
      alert('아이디는 4-20자의 영문과 숫자만 사용 가능합니다.');
      return;
    }

    const isDuplicate = await checkUsernameDuplicate(formData.username);
    setDuplicateCheck(prev => ({
      ...prev,
      username: isDuplicate
    }));

    if (isDuplicate) {
      alert('이미 사용중인 아이디입니다.');
    } else {
      alert('사용 가능한 아이디입니다.');
    }
  };

 /**
 * 이메일 중복 체크 핸들러
 */
const handleCheckEmail = async () => {
  if (!formData.email) {
    alert('이메일을 입력해주세요.');
    return;
  }

  // 이메일 형식 검사
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(formData.email)) {
    alert('올바른 이메일 형식이 아닙니다.');
    return;
  }

  const isDuplicate = await checkEmailDuplicate(formData.email);
  setDuplicateCheck(prev => ({
    ...prev,
    email: isDuplicate
  }));

  if (isDuplicate) {
    alert('이미 사용중인 이메일입니다.');
  } else {
    alert('사용 가능한 이메일입니다.');
  }
};

  /**
   * 닉네임 중복 체크 핸들러
   */
  const handleCheckNickname = async () => {
    if (!formData.nickname) {
      alert('닉네임을 입력해주세요.');
      return;
    }

    // 닉네임 유효성 검사 (2-10자, 한글/영문/숫자)
    const nicknameRegex = /^[가-힣a-zA-Z0-9]{2,10}$/;
    if (!nicknameRegex.test(formData.nickname)) {
      alert('닉네임은 2-10자의 한글, 영문, 숫자만 사용 가능합니다.');
      return;
    }

    const isDuplicate = await checkNicknameDuplicate(formData.nickname);
    setDuplicateCheck(prev => ({
      ...prev,
      nickname: isDuplicate
    }));

    if (isDuplicate) {
      alert('이미 사용중인 닉네임입니다.');
    } else {
      alert('사용 가능한 닉네임입니다.');
    }
  };

  /**
   * 폼 유효성 검사
   * 회원가입 전에 모든 입력값이 올바른지 확인
   */
  const validateForm = () => {
    const newErrors = {};

    // 아이디 검사
    if (!formData.username) {
      newErrors.username = '아이디를 입력해주세요.';
    } else if (duplicateCheck.username === null) {
      newErrors.username = '아이디 중복 체크를 해주세요.';
    } else if (duplicateCheck.username === true) {
      newErrors.username = '이미 사용중인 아이디입니다.';
    }

    // 비밀번호 검사
    if (!formData.password) {
      newErrors.password = '비밀번호를 입력해주세요.';
    } else if (formData.password.length < 8) {
      newErrors.password = '비밀번호는 최소 8자 이상이어야 합니다.';
    }

    // 비밀번호 확인 검사
    if (!formData.passwordConfirm) {
      newErrors.passwordConfirm = '비밀번호 확인을 입력해주세요.';
    } else if (formData.password !== formData.passwordConfirm) {
      newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
    }

    // 닉네임 검사
    if (!formData.nickname) {
      newErrors.nickname = '닉네임을 입력해주세요.';
    } else if (duplicateCheck.nickname === null) {
      newErrors.nickname = '닉네임 중복 체크를 해주세요.';
    } else if (duplicateCheck.nickname === true) {
      newErrors.nickname = '이미 사용중인 닉네임입니다.';
    }

    // 이메일 검사
    if (!formData.email) {
      newErrors.email = '이메일을 입력해주세요.';
    } else if (duplicateCheck.email === null) {
      newErrors.email = '이메일 중복 체크를 해주세요.';
    } else if (duplicateCheck.email === true) {
      newErrors.email = '이미 사용중인 이메일입니다.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * 회원가입 제출 핸들러
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // 유효성 검사
    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      // 회원가입 API 호출
      const result = await signup(formData);
      alert('회원가입이 완료되었습니다!');
      navigate('/login'); // 로그인 페이지로 이동
    } catch (error) {
      alert(error.message || '회원가입에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '500px', margin: '50px auto', padding: '20px' }}>
      {/* 페이지 제목 */}
      <h1 style={{ textAlign: 'center', marginBottom: '30px' }}>회원가입</h1>

      {/* 회원가입 폼 */}
      <form onSubmit={handleSubmit}>
        
        {/* 아이디 입력 필드 */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            아이디 *
          </label>
          <div style={{ display: 'flex', gap: '10px' }}>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="4-20자의 영문, 숫자"
              style={{ 
                flex: 1, 
                padding: '10px', 
                border: '1px solid #ddd',
                borderRadius: '4px'
              }}
            />
            <button
              type="button"
              onClick={handleCheckUsername}
              style={{
                padding: '10px 20px',
                backgroundColor: '#646cff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              중복체크
            </button>
          </div>
          {/* 중복 체크 결과 표시 */}
          {duplicateCheck.username !== null && (
            <p style={{ 
              margin: '5px 0', 
              fontSize: '14px', 
              color: duplicateCheck.username ? 'red' : 'green' 
            }}>
              {duplicateCheck.username ? '이미 사용중인 아이디입니다.' : '사용 가능한 아이디입니다.'}
            </p>
          )}
          {/* 에러 메시지 표시 */}
          {errors.username && (
            <p style={{ margin: '5px 0', fontSize: '14px', color: 'red' }}>
              {errors.username}
            </p>
          )}
        </div>

        {/* 비밀번호 입력 필드 */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            비밀번호 *
          </label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="최소 8자 이상"
            style={{ 
              width: '100%', 
              padding: '10px', 
              border: '1px solid #ddd',
              borderRadius: '4px',
              boxSizing: 'border-box'
            }}
          />
          {errors.password && (
            <p style={{ margin: '5px 0', fontSize: '14px', color: 'red' }}>
              {errors.password}
            </p>
          )}
        </div>

        {/* 비밀번호 확인 입력 필드 */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            비밀번호 확인 *
          </label>
          <input
            type="password"
            name="passwordConfirm"
            value={formData.passwordConfirm}
            onChange={handleChange}
            placeholder="비밀번호 재입력"
            style={{ 
              width: '100%', 
              padding: '10px', 
              border: '1px solid #ddd',
              borderRadius: '4px',
              boxSizing: 'border-box'
            }}
          />
          {errors.passwordConfirm && (
            <p style={{ margin: '5px 0', fontSize: '14px', color: 'red' }}>
              {errors.passwordConfirm}
            </p>
          )}
        </div>

        {/* 닉네임 입력 필드 */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            닉네임 *
          </label>
          <div style={{ display: 'flex', gap: '10px' }}>
            <input
              type="text"
              name="nickname"
              value={formData.nickname}
              onChange={handleChange}
              placeholder="2-10자의 한글, 영문, 숫자"
              style={{ 
                flex: 1, 
                padding: '10px', 
                border: '1px solid #ddd',
                borderRadius: '4px'
              }}
            />
            <button
              type="button"
              onClick={handleCheckNickname}
              style={{
                padding: '10px 20px',
                backgroundColor: '#646cff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              중복체크
            </button>
          </div>
          {duplicateCheck.nickname !== null && (
            <p style={{ 
              margin: '5px 0', 
              fontSize: '14px', 
              color: duplicateCheck.nickname ? 'red' : 'green' 
            }}>
              {duplicateCheck.nickname ? '이미 사용중인 닉네임입니다.' : '사용 가능한 닉네임입니다.'}
            </p>
          )}
          {errors.nickname && (
            <p style={{ margin: '5px 0', fontSize: '14px', color: 'red' }}>
              {errors.nickname}
            </p>
          )}
        </div>

        {/* 이메일 입력 필드 */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>
            이메일 *
          </label>
          <div style={{ display: 'flex', gap: '10px' }}>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="example@email.com"
              style={{ 
                flex: 1, 
                padding: '10px', 
                border: '1px solid #ddd',
                borderRadius: '4px'
              }}
            />
            <button
              type="button"
              onClick={handleCheckEmail}
              style={{
                padding: '10px 20px',
                backgroundColor: '#646cff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              중복체크
            </button>
          </div>
          {duplicateCheck.email !== null && (
            <p style={{ 
              margin: '5px 0', 
              fontSize: '14px', 
              color: duplicateCheck.email ? 'red' : 'green' 
            }}>
              {duplicateCheck.email ? '이미 사용중인 이메일입니다.' : '사용 가능한 이메일입니다.'}
            </p>
          )}
          {errors.email && (
            <p style={{ margin: '5px 0', fontSize: '14px', color: 'red' }}>
              {errors.email}
            </p>
          )}
        </div>

        {/* 회원가입 버튼 */}
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
          {isLoading ? '처리중...' : '회원가입'}
        </button>

        {/* 로그인 페이지로 이동 링크 */}
        <div style={{ textAlign: 'center', marginTop: '20px' }}>
          <span>이미 계정이 있으신가요? </span>
          <button
            type="button"
            onClick={() => navigate('/login')}
            style={{
              background: 'none',
              border: 'none',
              color: '#646cff',
              textDecoration: 'underline',
              cursor: 'pointer'
            }}
          >
            로그인
          </button>
        </div>
      </form>
    </div>
  );
}

export default Signup;