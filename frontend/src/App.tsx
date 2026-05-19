import { Navigate, Route, Routes } from 'react-router-dom'
import { BrowserRouter } from 'react-router-dom'
import { AppShell } from './layout/AppShell'
import { ProtectedRoute } from './routes/ProtectedRoute'
import { LoginPage } from './pages/auth/LoginPage'
import { RegisterPage } from './pages/auth/RegisterPage'
import { WorkspaceDashboardPage } from './pages/workspace/WorkspaceDashboardPage'
import { WorkspaceSubscriptionsPage } from './pages/workspace/WorkspaceSubscriptionsPage'
import { WorkspaceContentPage } from './pages/workspace/WorkspaceContentPage'
import { WorkspacePushesPage } from './pages/workspace/WorkspacePushesPage'
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage'
import { AdminUsersPage } from './pages/admin/AdminUsersPage'
import { AdminSubscriptionsPage } from './pages/admin/AdminSubscriptionsPage'
import { OpsDashboardPage } from './pages/ops/OpsDashboardPage'
import { OpsDigestPage } from './pages/ops/OpsDigestPage'
import { OpsTasksPage } from './pages/ops/OpsTasksPage'
import { OpsPushFailuresPage } from './pages/ops/OpsPushFailuresPage'
import { OpsAlertsPage } from './pages/ops/OpsAlertsPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
            <Route path="/workspace/dashboard" element={<WorkspaceDashboardPage />} />
            <Route path="/workspace/subscriptions" element={<WorkspaceSubscriptionsPage />} />
            <Route path="/workspace/content" element={<WorkspaceContentPage />} />
            <Route path="/workspace/pushes" element={<WorkspacePushesPage />} />
          </Route>
        </Route>

        <Route element={<ProtectedRoute allowRoles={['admin']} />}>
          <Route element={<AppShell />}>
            <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
            <Route path="/admin/users" element={<AdminUsersPage />} />
            <Route path="/admin/subscriptions" element={<AdminSubscriptionsPage />} />
            <Route path="/ops/dashboard" element={<OpsDashboardPage />} />
            <Route path="/ops/digest" element={<OpsDigestPage />} />
            <Route path="/ops/tasks" element={<OpsTasksPage />} />
            <Route path="/ops/push-failures" element={<OpsPushFailuresPage />} />
            <Route path="/ops/alerts" element={<OpsAlertsPage />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
