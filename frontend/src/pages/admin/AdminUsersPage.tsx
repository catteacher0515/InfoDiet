import { useEffect, useState } from 'react'
import { adminCreateUser } from '../../api/auth'
import { fetchUserList } from '../../api/users'
import { DataTable } from '../../components/DataTable'
import type { UserListItem } from '../../types/user'

export function AdminUsersPage() {
  const [users, setUsers] = useState<UserListItem[]>([])
  const [form, setForm] = useState({
    nickname: '',
    username: '',
    password: '',
    role: 'user' as 'admin' | 'user',
  })
  const [message, setMessage] = useState('')

  async function loadUsers() {
    const response = await fetchUserList()
    setUsers(response.data)
  }

  useEffect(() => {
    void loadUsers()
  }, [])

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage('')
    try {
      const response = await adminCreateUser(form)
      if (response.code === 0) {
        setMessage('创建成功')
        setForm({ nickname: '', username: '', password: '', role: 'user' })
        await loadUsers()
      } else {
        setMessage(response.message || '创建失败')
      }
    } catch {
      setMessage('创建失败')
    }
  }

  return (
    <section className="page-section split-page">
      <div className="page-heading">
        <p className="section-kicker">USERS</p>
        <h2>用户管理</h2>
        <span>管理员可以查看用户列表，并直接创建普通用户或管理员账号。</span>
      </div>

      <div className="content-split">
        <form className="panel form-panel" onSubmit={handleSubmit}>
          <h3>创建用户</h3>
          <label>
            昵称
            <input
              value={form.nickname}
              onChange={(event) => setForm((prev) => ({ ...prev, nickname: event.target.value }))}
            />
          </label>
          <label>
            账号
            <input
              value={form.username}
              onChange={(event) => setForm((prev) => ({ ...prev, username: event.target.value }))}
            />
          </label>
          <label>
            密码
            <input
              type="password"
              value={form.password}
              onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
            />
          </label>
          <label>
            角色
            <select value={form.role} onChange={(event) => setForm((prev) => ({ ...prev, role: event.target.value as 'admin' | 'user' }))}>
              <option value="user">user</option>
              <option value="admin">admin</option>
            </select>
          </label>
          {message ? <p className="form-message">{message}</p> : null}
          <button className="primary-button" type="submit">
            创建账号
          </button>
        </form>

        <div className="panel">
          <h3>用户列表</h3>
          <DataTable
            columns={[
              { key: 'nickname', title: '昵称', render: (item) => item.nickname },
              { key: 'username', title: '账号', render: (item) => item.username },
              { key: 'role', title: '角色', render: (item) => item.role },
              { key: 'status', title: '状态', render: (item) => (item.status === 1 ? '启用' : '禁用') },
            ]}
            data={users}
          />
        </div>
      </div>
    </section>
  )
}
