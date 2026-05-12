import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchWorkspaceDashboard } from '../../api/dashboard'
import { StatCard } from '../../components/StatCard'
import type { WorkspaceDashboard } from '../../types/dashboard'

export function WorkspaceDashboardPage() {
  const [data, setData] = useState<WorkspaceDashboard | null>(null)

  useEffect(() => {
    void fetchWorkspaceDashboard().then((response) => setData(response.data))
  }, [])

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="section-kicker">WORKSPACE</p>
        <h2>我的工作台</h2>
        <span>这里展示当前登录用户自己的订阅、来源和推送状态。</span>
      </div>
      <div className="stats-grid">
        <StatCard label="关键词订阅" value={data?.keywordCount ?? '--'} hint="当前用户启用的关键词数" />
        <StatCard label="订阅源" value={data?.sourceCount ?? '--'} hint="当前用户有效的来源配置" />
        <StatCard label="总推送" value={data?.totalPushCount ?? '--'} hint="当前用户累计推送记录" />
        <StatCard label="成功推送" value={data?.successPushCount ?? '--'} hint="当前用户成功送达记录" />
        <StatCard label="失败推送" value={data?.failedPushCount ?? '--'} hint="当前用户待处理异常记录" />
      </div>

      <div className="content-grid">
        <div className="panel">
          <h3>当前建议</h3>
          <div className="stack-list">
            <article className="stack-item">
              <strong>先维护订阅</strong>
              <span>如果关键词和订阅源太少，内容列表会比较空。</span>
            </article>
            <article className="stack-item">
              <strong>再查看内容</strong>
              <span>确认命中的内容是否真的符合你想看的信息范围。</span>
            </article>
            <article className="stack-item">
              <strong>关注失败推送</strong>
              <span>如果失败推送大于 0，需要去排查渠道或账号配置。</span>
            </article>
          </div>
        </div>

        <div className="panel">
          <h3>快捷入口</h3>
          <div className="quick-link-list">
            <Link className="quick-link-card" to="/workspace/subscriptions">
              <strong>管理订阅</strong>
              <span>新增关键词、规则和订阅源</span>
            </Link>
            <Link className="quick-link-card" to="/workspace/content">
              <strong>查看内容</strong>
              <span>筛选当前匹配到的统一内容</span>
            </Link>
            <Link className="quick-link-card" to="/workspace/pushes">
              <strong>查看推送记录</strong>
              <span>确认是否成功送达与历史状态</span>
            </Link>
          </div>
        </div>
      </div>
    </section>
  )
}
