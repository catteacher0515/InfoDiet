import { useEffect, useState } from 'react'
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
    </section>
  )
}
