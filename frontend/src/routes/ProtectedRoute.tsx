import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../store/auth'
import type { UserRole } from '../types/auth'

interface ProtectedRouteProps {
  allowRoles?: UserRole[]
}

export function ProtectedRoute({ allowRoles }: ProtectedRouteProps) {
  const { ready, isAuthenticated, user } = useAuth()
  const location = useLocation()

  if (!ready) {
    return <div className="page-state">正在校验登录态...</div>
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (allowRoles && !allowRoles.includes(user.role)) {
    return <Navigate to={user.role === 'admin' ? '/admin/dashboard' : '/workspace/dashboard'} replace />
  }

  return <Outlet />
}
