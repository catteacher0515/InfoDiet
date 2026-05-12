import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchAdminDashboard } from '../../api/dashboard'
import { StatCard } from '../../components/StatCard'
import type { AdminDashboard } from '../../types/dashboard'

export function AdminDashboardPage() {
  const [data, setData] = useState<AdminDashboard | null>(null)

  useEffect(() => {
    void fetchAdminDashboard().then((response) => setData(response.data))
  }, [])

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="section-kicker">ADMIN</p>
        <h2>管理概览</h2>
        <span>给管理员看平台级别的用户、订阅和来源规模。</span>
      </div>
      <div className="stats-grid">
        <StatCard label="用户总数" value={data?.userCount ?? '--'} hint="平台注册用户数量" />
        <StatCard label="启用用户" value={data?.enabledUserCount ?? '--'} hint="当前可参与匹配与推送的用户" />
        <StatCard label="关键词订阅" value={data?.keywordSubscriptionCount ?? '--'} hint="平台所有关键词订阅总量" />
        <StatCard label="订阅源" value={data?.sourceSubscriptionCount ?? '--'} hint="平台所有来源订阅总量" />
      </div>

      <div className="content-grid">
        <div className="panel">
          <h3>管理重点</h3>
          <div className="stack-list">
            <article className="stack-item">
              <strong>用户状态治理</strong>
              <span>当测试用户过多时，及时禁用无效账号，避免影响匹配与推送。</span>
            </article>
            <article className="stack-item">
              <strong>订阅密度观察</strong>
              <span>如果订阅源明显高于关键词，说明用户在偏向“固定来源订阅”。</span>
            </article>
          </div>
        </div>

        <div className="panel">
          <h3>快捷入口</h3>
          <div className="quick-link-list">
            <Link className="quick-link-card" to="/admin/users">
              <strong>用户管理</strong>
              <span>查看详情、订阅和启用状态</span>
            </Link>
            <Link className="quick-link-card" to="/admin/subscriptions">
              <strong>订阅总览</strong>
              <span>查看平台级订阅结构和来源分布</span>
            </Link>
          </div>
        </div>
      </div>
    </section>
  )
}
