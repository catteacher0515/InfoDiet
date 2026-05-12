import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../../api/auth'

export function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    nickname: '',
    username: '',
    password: '',
  })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const response = await register(form)
      if (response.code !== 0) {
        setError(response.message || '注册失败')
        return
      }
      navigate('/login', { replace: true })
    } catch {
      setError('注册失败，请稍后重试')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="auth-page register-page">
      <div className="auth-panel">
        <div className="auth-copy">
          <p className="section-kicker">REGISTER</p>
          <h2>先定义你要看什么，再决定系统替你抓什么。</h2>
          <p>第一版采用最小注册闭环，注册后即可进入工作台配置订阅与查看推送结果。</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            昵称
            <input
              value={form.nickname}
              onChange={(event) => setForm((prev) => ({ ...prev, nickname: event.target.value }))}
              placeholder="请输入昵称"
            />
          </label>
          <label>
            账号
            <input
              value={form.username}
              onChange={(event) => setForm((prev) => ({ ...prev, username: event.target.value }))}
              placeholder="请输入登录账号"
            />
          </label>
          <label>
            密码
            <input
              type="password"
              value={form.password}
              onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
              placeholder="请输入密码"
            />
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" type="submit" disabled={submitting}>
            {submitting ? '注册中...' : '注册账号'}
          </button>
          <p className="form-footnote">
            已有账号？<Link to="/login">返回登录</Link>
          </p>
        </form>
      </div>
    </section>
  )
}
