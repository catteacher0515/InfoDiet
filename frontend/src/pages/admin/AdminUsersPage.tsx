import { useEffect, useState } from 'react'
import { adminCreateUser } from '../../api/auth'
import { fetchAdminUserSubscription, fetchUserDetail, fetchUserList, updateUserStatus } from '../../api/users'
import { DataTable } from '../../components/DataTable'
import type { AdminUserSubscription } from '../../types/admin-user'
import type { UserDetail, UserListItem } from '../../types/user'

function formatDateTime(value?: string) {
  if (!value) {
    return '暂无'
  }
  return value.replace('T', ' ').slice(0, 19)
}

export function AdminUsersPage() {
  const [users, setUsers] = useState<UserListItem[]>([])
  const [selectedUser, setSelectedUser] = useState<UserDetail | null>(null)
  const [selectedSubscription, setSelectedSubscription] = useState<AdminUserSubscription | null>(null)
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

  async function handleSelectUser(userId: number) {
    const [detailResponse, subscriptionResponse] = await Promise.all([
      fetchUserDetail(userId),
      fetchAdminUserSubscription(userId),
    ])
    setSelectedUser(detailResponse.data as UserDetail)
    setSelectedSubscription(subscriptionResponse.data)
  }

  async function handleToggleStatus(user: UserListItem) {
    const nextStatus = user.status === 1 ? 0 : 1
    const response = await updateUserStatus(user.id, nextStatus)
    setMessage(response.code === 0 && response.data ? '用户状态已更新' : response.message || '状态更新失败')
    if (response.code === 0 && response.data) {
      await loadUsers()
      if (selectedUser?.id === user.id) {
        await handleSelectUser(user.id)
      }
    }
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

      <div className="content-split admin-users-split">
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
              {
                key: 'action',
                title: '操作',
                render: (item) => (
                  <div className="button-row">
                    <button className="ghost-button table-button" type="button" onClick={() => handleSelectUser(item.id)}>
                      查看
                    </button>
                    <button className="ghost-button table-button" type="button" onClick={() => handleToggleStatus(item)}>
                      {item.status === 1 ? '禁用' : '启用'}
                    </button>
                  </div>
                ),
              },
            ]}
            data={users}
          />
        </div>

        <div className="panel">
          <h3>用户详情</h3>
          {selectedUser ? (
            <div className="stack-list">
              <article className="stack-item">
                <strong>{selectedUser.nickname}</strong>
                <span>账号：{selectedUser.username}</span>
                <em>角色：{selectedUser.role}</em>
              </article>
              <article className="stack-item">
                <strong>推送配置</strong>
                <span>渠道：{selectedUser.pushChannel || '未设置'}</span>
                <span>飞书用户 ID：{selectedUser.feishuUserId || '未设置'}</span>
                <em>每日上限：{selectedUser.dailyPushLimit ?? '未设置'} / 冷却小时：{selectedUser.pushCooldownHours ?? '未设置'}</em>
              </article>
              <article className="stack-item">
                <strong>时间信息</strong>
                <span>创建时间：{formatDateTime(selectedUser.createTime)}</span>
                <em>更新时间：{formatDateTime(selectedUser.updateTime)}</em>
              </article>
            </div>
          ) : (
            <p className="empty-inline">先从左侧用户列表选择一个用户</p>
          )}
        </div>

        <div className="panel">
          <h3>用户订阅查看</h3>
          {selectedSubscription ? (
            <div className="stack-list">
              <article className="stack-item">
                <strong>关键词</strong>
                <span>{selectedSubscription.keywords.length ? selectedSubscription.keywords.join(' / ') : '暂无关键词'}</span>
              </article>
              <article className="stack-item">
                <strong>规则数量</strong>
                <span>{selectedSubscription.rules.length}</span>
                <em>{selectedSubscription.rules.map((rule) => `${rule.ruleType}:${rule.ruleValue}`).join('；') || '暂无规则'}</em>
              </article>
              <article className="stack-item">
                <strong>订阅源数量</strong>
                <span>{selectedSubscription.sources.length}</span>
                <em>{selectedSubscription.sources.map((source) => `${source.platform}/${source.sourceType}/${source.sourceValue}`).join('；') || '暂无订阅源'}</em>
              </article>
            </div>
          ) : (
            <p className="empty-inline">先查看一个用户，才能看到订阅详情</p>
          )}
        </div>
      </div>
    </section>
  )
}
