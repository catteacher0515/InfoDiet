import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
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

      <div className="content-grid">
        <div className="panel">
          <h3>运维建议</h3>
          <div className="stack-list">
            <article className="stack-item">
              <strong>先看失败推送</strong>
              <span>如果失败推送不为 0，优先定位是内容、告警还是渠道问题。</span>
            </article>
            <article className="stack-item">
              <strong>再看任务日志</strong>
              <span>任务日志适合确认采集、入库、匹配和入队链路是否完整。</span>
            </article>
          </div>
        </div>

        <div className="panel">
          <h3>快捷入口</h3>
          <div className="quick-link-list">
            <Link className="quick-link-card" to="/ops/tasks">
              <strong>任务日志</strong>
              <span>查看最近执行并支持直接重跑</span>
            </Link>
            <Link className="quick-link-card" to="/ops/push-failures">
              <strong>失败推送</strong>
              <span>查看失败详情、关联内容和告警</span>
            </Link>
            <Link className="quick-link-card" to="/ops/alerts">
              <strong>失败告警</strong>
              <span>查看待处理告警并发送到飞书</span>
            </Link>
          </div>
        </div>
      </div>
    </section>
  )
}
