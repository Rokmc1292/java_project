import { useEffect, useState } from 'react';
import '../styles/SplashScreen.css';

/**
 * Netflix 스타일 로딩 화면 컴포넌트
 */
function SplashScreen({ onFinish }) {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    // 2초 후 페이드 아웃 시작
    const timer = setTimeout(() => {
      setIsVisible(false);
      // 페이드 아웃 애니메이션 완료 후 onFinish 호출
      setTimeout(() => {
        onFinish();
      }, 500);
    }, 2000);

    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <div className={`splash-screen ${!isVisible ? 'fade-out' : ''}`}>
      <div className="splash-content">
        <h1 className="splash-logo">
          <span className="logo-sport">SPORT</span>
          <span className="logo-hub">HUB</span>
        </h1>
        <div className="splash-tagline">모든 스포츠, 하나의 플랫폼</div>
      </div>
    </div>
  );
}

export default SplashScreen;
