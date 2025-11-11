// frontend/src/components/ProtectedRoute.jsx
import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children, requireAdmin = false }) => {
    const [loading, setLoading] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);

    useEffect(() => {
        checkAuth();
    }, []);

    const checkAuth = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/auth/check', {
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                setIsAuthenticated(true);
                setIsAdmin(data.isAdmin);
            } else {
                setIsAuthenticated(false);
            }
        } catch (error) {
            console.error('인증 확인 실패:', error);
            setIsAuthenticated(false);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-xl">로딩 중...</div>
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (requireAdmin && !isAdmin) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-center">
                    <h1 className="text-2xl font-bold text-red-600 mb-4">접근 권한 없음</h1>
                    <p className="text-gray-600">관리자 권한이 필요합니다.</p>
                </div>
            </div>
        );
    }

    return children;
};

export default ProtectedRoute;