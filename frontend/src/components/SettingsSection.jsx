import React, { useState, useEffect } from 'react';
import mypageApi from '../api/mypageApi';
import '../styles/SettingsSection.css';

const SettingsSection = ({ onLogout }) => {
  const [newNickname, setNewNickname] = useState('');
  const [nicknameError, setNicknameError] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [settings, setSettings] = useState({
    commentNotification: true,
    replyNotification: true,
    popularPostNotification: true,
    predictionResultNotification: true
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      const data = await mypageApi.getUserSettings();
      setSettings(data);
    } catch (error) {
      console.error('설정 로드 실패:', error);
    }
  };

  const handleNicknameChange = async () => {
    if (!newNickname.trim()) {
      setNicknameError('닉네임을 입력해주세요.');
      return;
    }
    if (newNickname.length < 2 || newNickname.length > 20) {
      setNicknameError('닉네임은 2-20자 사이여야 합니다.');
      return;
    }
    const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
    if (!nicknameRegex.test(newNickname)) {
      setNicknameError('닉네임은 한글, 영문, 숫자만 사용 가능합니다.');
      return;
    }
    try {
      setLoading(true);
      setNicknameError('');
      await mypageApi.updateNickname(newNickname);
      alert('닉네임이 변경되었습니다.');
      setNewNickname('');
      window.location.reload();
    } catch (error) {
      setNicknameError(error.response?.data || '닉네임 변경에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async () => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordError('모든 필드를 입력해주세요.');
      return;
    }
    if (newPassword.length < 8 || newPassword.length > 20) {
      setPasswordError('비밀번호는 8-20자 사이여야 합니다.');
      return;
    }
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]+$/;
    if (!passwordRegex.test(newPassword)) {
      setPasswordError('비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordError('새 비밀번호가 일치하지 않습니다.');
      return;
    }
    try {
      setLoading(true);
      setPasswordError('');
      await mypageApi.updatePassword(currentPassword, newPassword, confirmPassword);
      alert('비밀번호가 변경되었습니다.');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (error) {
      setPasswordError(error.response?.data || '비밀번호 변경에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSettingsChange = async (key, value) => {
    const updatedSettings = { ...settings, [key]: value };
    setSettings(updatedSettings);
    try {
      await mypageApi.updateUserSettings(updatedSettings);
    } catch (error) {
      console.error('설정 변경 실패:', error);
      setSettings(settings);
      alert('설정 변경에 실패했습니다.');
    }
  };

  return (
    <div className="settings-section">
      <div className="settings-card">
        <h3>닉네임 변경</h3>
        <div className="input-group">
          <input type="text" placeholder="새 닉네임 입력" value={newNickname} onChange={(e) => setNewNickname(e.target.value)} className="settings-input" maxLength={20} />
          <button onClick={handleNicknameChange} disabled={loading} className="settings-button">변경</button>
        </div>
        {nicknameError && <p className="error-message">{nicknameError}</p>}
        <p className="input-guide">💡 한글, 영문, 숫자만 사용 가능 (2-20자)</p>
      </div>

      <div className="settings-card">
        <h3>비밀번호 변경</h3>
        <div className="password-inputs">
          <input type="password" placeholder="현재 비밀번호" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} className="settings-input" />
          <input type="password" placeholder="새 비밀번호" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} className="settings-input" />
          <input type="password" placeholder="새 비밀번호 확인" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} className="settings-input" />
          <button onClick={handlePasswordChange} disabled={loading} className="settings-button full-width">비밀번호 변경</button>
        </div>
        {passwordError && <p className="error-message">{passwordError}</p>}
        <p className="input-guide">💡 영문, 숫자, 특수문자 포함 8-20자</p>
      </div>

      <div className="settings-card">
        <h3>알림 설정</h3>
        <div className="notification-settings">
          <div className="setting-item">
            <div className="setting-info"><strong>댓글 알림</strong><p>내가 작성한 글에 댓글이 달리면 알림을 받습니다.</p></div>
            <label className="toggle-switch">
              <input type="checkbox" checked={settings.commentNotification} onChange={(e) => handleSettingsChange('commentNotification', e.target.checked)} />
              <span className="slider"></span>
            </label>
          </div>
          <div className="setting-item">
            <div className="setting-info"><strong>대댓글 알림</strong><p>내 댓글에 대댓글이 달리면 알림을 받습니다.</p></div>
            <label className="toggle-switch">
              <input type="checkbox" checked={settings.replyNotification} onChange={(e) => handleSettingsChange('replyNotification', e.target.checked)} />
              <span className="slider"></span>
            </label>
          </div>
          <div className="setting-item">
            <div className="setting-info"><strong>인기글 진입 알림</strong><p>내 글이 인기글에 진입하면 알림을 받습니다.</p></div>
            <label className="toggle-switch">
              <input type="checkbox" checked={settings.popularPostNotification} onChange={(e) => handleSettingsChange('popularPostNotification', e.target.checked)} />
              <span className="slider"></span>
            </label>
          </div>
          <div className="setting-item">
            <div className="setting-info"><strong>예측 결과 알림</strong><p>예측한 경기가 종료되면 결과 알림을 받습니다.</p></div>
            <label className="toggle-switch">
              <input type="checkbox" checked={settings.predictionResultNotification} onChange={(e) => handleSettingsChange('predictionResultNotification', e.target.checked)} />
              <span className="slider"></span>
            </label>
          </div>
        </div>
      </div>

      <div className="settings-card logout-card">
        <h3>계정</h3>
        <button onClick={onLogout} className="logout-button">로그아웃</button>
      </div>
    </div>
  );
};

export default SettingsSection;