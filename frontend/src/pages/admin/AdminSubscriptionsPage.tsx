import { useEffect, useState } from 'react'
import { fetchAdminSubscriptionOverview } from '../../api/admin'
import type { AdminSubscriptionOverview } from '../../types/admin'

function StatCard(props: { label: string; value: string | number; hint: string }) {
  return (
    <article className="stat-card">
      <p className="stat-label">{props.label}</p>
      <strong className="stat-value">{props.value}</strong>
      <p className="stat-hint">{props.hint}</p>
    </article>
  )
}

export function AdminSubscriptionsPage() {
  const [data, setData] = useState<AdminSubscriptionOverview | null>(null)

  useEffect(() => {
    void fetchAdminSubscriptionOverview().then((response) => setData(response.data))
  }, [])

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="section-kicker">SUBSCRIPTION OVERVIEW</p>
        <h2>订阅总览</h2>
        <span>这里展示全局订阅配置密度、来源结构和每用户平均订阅情况。</span>
      </div>

      <div className="stats-grid">
        <StatCard label="关键词订阅" value={data?.keywordCount ?? '--'} hint="当前系统累计关键词订阅数" />
        <StatCard label="规则总数" value={data?.ruleCount ?? '--'} hint="包含关键词、作者、平台等规则" />
        <StatCard label="订阅源总数" value={data?.sourceCount ?? '--'} hint="当前启用的订阅源配置数量" />
        <StatCard label="启用用户" value={data?.enabledUserCount ?? '--'} hint="当前参与订阅系统的用户数" />
        <StatCard label="人均订阅源" value={data?.avgSourcePerUser ?? '--'} hint="每位启用用户平均配置的订阅源数量" />
      </div>

      <div className="content-grid">
        <div className="panel">
          <h3>人均密度</h3>
          <div className="stack-list">
            <article className="stack-item">
              <strong>关键词</strong>
              <span>人均 {data?.avgKeywordPerUser ?? '--'}</span>
            </article>
            <article className="stack-item">
              <strong>规则</strong>
              <span>人均 {data?.avgRulePerUser ?? '--'}</span>
            </article>
            <article className="stack-item">
              <strong>订阅源</strong>
              <span>人均 {data?.avgSourcePerUser ?? '--'}</span>
            </article>
          </div>
        </div>

        <div className="panel">
          <h3>平台分布</h3>
          <div className="stack-list">
            <article className="stack-item">
              <strong>YouTube</strong>
              <span>{data?.youtubeSourceCount ?? '--'}</span>
            </article>
            <article className="stack-item">
              <strong>GitHub</strong>
              <span>{data?.githubSourceCount ?? '--'}</span>
            </article>
          </div>
        </div>

        <div className="panel">
          <h3>来源类型分布</h3>
          <div className="stack-list">
            <article className="stack-item">
              <strong>频道订阅</strong>
              <span>{data?.channelSourceCount ?? '--'}</span>
            </article>
            <article className="stack-item">
              <strong>仓库订阅</strong>
              <span>{data?.repoSourceCount ?? '--'}</span>
            </article>
            <article className="stack-item">
              <strong>作者订阅</strong>
              <span>{data?.authorSourceCount ?? '--'}</span>
            </article>
          </div>
        </div>
      </div>
    </section>
  )
}
