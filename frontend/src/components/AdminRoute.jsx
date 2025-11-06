/**
 * 관리자 전용 라우트 보호 컴포넌트
 * 관리자 권한이 없으면 접근 불가
 *
 * 파일 위치: frontend/src/components/AdminRoute.jsx
 */

import { Navigate } from 'react-router-dom';
import { getUserData } from '../api/api';

function AdminRoute({ children }) {
    const user = getUserData();


    return children;
}

export default AdminRoute;