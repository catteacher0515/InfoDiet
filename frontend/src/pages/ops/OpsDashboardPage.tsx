import { useEffect, useState } from 'react'
import { fetchOpsDashboard } from '../../api/dashboard'
import { StatCard } from '../../components/StatCard'
import type { OpsDashboard } from '../../types/dashboard'

export function OpsDashboardPage() {
  const [data, setData] = useState<OpsDashboard | null>(null)

  useEffect(() => {
    void fetchOpsDashboard().then((response) => setData(response.data))
  }, [])

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="section-kicker">OPS</p>
        <h2>运维概览</h2>
        <span>给管理员看最近任务、待处理告警和失败推送的压缩视图。</span>
      </div>
      <div className="stats-grid">
        <StatCard label="最近任务" value={data?.recentTaskCount ?? '--'} hint="最近一批任务执行记录数量" />
        <StatCard label="待处理告警" value={data?.pendingAlertCount ?? '--'} hint="还未完成发送或处理的告警" />
        <StatCard label="失败推送" value={data?.failedPushCount ?? '--'} hint="需要人工介入或重试的推送异常" />
      </div>
    </section>
  )
}
