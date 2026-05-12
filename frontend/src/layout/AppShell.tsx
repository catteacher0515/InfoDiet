import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../store/auth'

const workspaceItems = [
  { to: '/workspace/dashboard', label: '工作台' },
  { to: '/workspace/subscriptions', label: '我的订阅' },
  { to: '/workspace/content', label: '内容列表' },
  { to: '/workspace/pushes', label: '推送记录' },
]

const adminItems = [
  { to: '/admin/dashboard', label: '管理概览' },
  { to: '/admin/users', label: '用户管理' },
  { to: '/admin/subscriptions', label: '订阅总览' },
]

const opsItems = [
  { to: '/ops/dashboard', label: '运维概览' },
  { to: '/ops/tasks', label: '任务日志' },
  { to: '/ops/push-failures', label: '失败推送' },
  { to: '/ops/alerts', label: '失败告警' },
]

export function AppShell() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="shell">
      <aside className="shell-sidebar">
        <div className="brand-block">
          <p className="brand-kicker">INFO DIET</p>
          <h1>信息节食</h1>
          <span>少看一些，更准一些。</span>
        </div>

        <nav className="nav-block">
          <p className="nav-title">用户区</p>
          {workspaceItems.map((item) => (
            <NavLink key={item.to} className="nav-link" to={item.to}>
              {item.label}
            </NavLink>
          ))}
        </nav>

        {user?.role === 'admin' ? (
          <>
            <nav className="nav-block">
              <p className="nav-title">管理区</p>
              {adminItems.map((item) => (
                <NavLink key={item.to} className="nav-link" to={item.to}>
                  {item.label}
                </NavLink>
              ))}
            </nav>

            <nav className="nav-block">
              <p className="nav-title">运维区</p>
              {opsItems.map((item) => (
                <NavLink key={item.to} className="nav-link" to={item.to}>
                  {item.label}
                </NavLink>
              ))}
            </nav>
          </>
        ) : null}
      </aside>

      <main className="shell-main">
        <header className="shell-header">
          <div>
            <p className="section-kicker">CURRENT USER</p>
            <strong>
              {user?.nickname} / {user?.role}
            </strong>
          </div>
          <button className="ghost-button" onClick={handleLogout} type="button">
            退出登录
          </button>
        </header>
        <section className="shell-content">
          <Outlet />
        </section>
      </main>
    </div>
  )
}
