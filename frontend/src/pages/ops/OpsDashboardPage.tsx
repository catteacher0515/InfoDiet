import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchOpsDashboard } from '../../api/dashboard'
import { fetchRecentFailedDigestPushRecords } from '../../api/ops'
import { StatCard } from '../../components/StatCard'
import type { OpsDashboard } from '../../types/dashboard'
import type { DailyDigestPushRecordItem } from '../../types/ops'

export function OpsDashboardPage() {
  const [data, setData] = useState<OpsDashboard | null>(null)
  const [failedDigestPushes, setFailedDigestPushes] = useState<DailyDigestPushRecordItem[]>([])

  useEffect(() => {
    void fetchOpsDashboard().then((response) => setData(response.data))
    void fetchRecentFailedDigestPushRecords(5).then((response) => setFailedDigestPushes(response.data))
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
        <StatCard
          label="今日日报"
          value={data?.todayDigestGenerated ? '已生成' : '未生成'}
          hint="日报是否已经完成生成并可供推送"
        />
        <StatCard
          label="日报推送成功"
          value={data?.todayDigestPushSuccessCount ?? '--'}
          hint="今天日报推送成功的用户数"
        />
        <StatCard
          label="日报推送失败"
          value={data?.todayDigestPushFailedCount ?? '--'}
          hint="今天日报推送失败的用户数"
        />
      </div>

      <div className="content-grid">
        <div className="panel">
          <h3>日报运营状态</h3>
          <div className="stack-list">
            <article className="stack-item">
              <strong>生成状态</strong>
              <span>{data?.todayDigestGenerated ? '今日日报已生成，调度链路前半段正常。' : '今日日报尚未生成，先检查日报生成或调度任务。'}</span>
            </article>
            <article className="stack-item">
              <strong>推送结果</strong>
              <span>
                今日成功 {data?.todayDigestPushSuccessCount ?? 0} 条，失败 {data?.todayDigestPushFailedCount ?? 0} 条，
                最近失败记录 {data?.recentDigestFailedRecordCount ?? 0} 条。
              </span>
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

        <div className="panel">
          <h3>最近日报推送失败</h3>
          {failedDigestPushes.length > 0 ? (
            <div className="stack-list">
              {failedDigestPushes.map((item) => (
                <article className="stack-item" key={item.id}>
                  <strong>{item.digestTitle || '未命名日报'}</strong>
                  <span>{item.digestDate} / userId {item.userId} / {item.receiveId || '无接收人 ID'}</span>
                  <em>{item.failReason || '未知失败原因'}</em>
                </article>
              ))}
            </div>
          ) : (
            <p className="empty-inline">最近没有日报推送失败记录</p>
          )}
        </div>
      </div>
    </section>
  )
}
