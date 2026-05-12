import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { login } from '../../api/auth'
import { useAuth } from '../../store/auth'

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { setLoginState } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const response = await login({ username, password })
      if (response.code !== 0 || !response.data.token) {
        setError(response.message || '登录失败')
        return
      }
      setLoginState(response.data, response.data.token)
      const fallback = response.data.role === 'admin' ? '/admin/dashboard' : '/workspace/dashboard'
      const nextPath = (location.state as { from?: string } | null)?.from ?? fallback
      navigate(nextPath, { replace: true })
    } catch {
      setError('登录失败，请检查账号和密码')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-panel">
        <div className="auth-copy">
          <p className="section-kicker">SIGN IN</p>
          <h2>把信息流，收拢成工作流。</h2>
          <p>
            这里不是刷榜工具，而是一套从订阅、采集、匹配到推送与重试的节制型信息系统。
          </p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            账号
            <input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="请输入账号" />
          </label>
          <label>
            密码
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="请输入密码"
            />
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" type="submit" disabled={submitting}>
            {submitting ? '登录中...' : '登录'}
          </button>
          <p className="form-footnote">
            还没有账号？<Link to="/register">去注册</Link>
          </p>
        </form>
      </div>
    </section>
  )
}
