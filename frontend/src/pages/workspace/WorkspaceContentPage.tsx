import { useEffect, useState } from 'react'
import { fetchMyContent } from '../../api/workspace'
import type { WorkspaceContentItem } from '../../types/subscription'

function formatDateTime(value?: string) {
  if (!value) {
    return '暂无'
  }
  return value.replace('T', ' ').slice(0, 19)
}

function renderMetric(label?: string, value?: number) {
  if (!label || value === undefined || value === null) {
    return '暂无'
  }
  return `${label} ${value}`
}

export function WorkspaceContentPage() {
  const [items, setItems] = useState<WorkspaceContentItem[]>([])
  const [loading, setLoading] = useState(false)
  const [platform, setPlatform] = useState('')
  const [contentType, setContentType] = useState('')
  const [limit, setLimit] = useState('20')

  async function loadContent() {
    setLoading(true)
    try {
      const response = await fetchMyContent({
        platform: platform || undefined,
        contentType: contentType || undefined,
        limit: Number(limit) || 20,
      })
      setItems(response.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadContent()
  }, [])

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="section-kicker">CONTENT</p>
        <h2>内容列表</h2>
        <span>这里展示当前登录用户按订阅匹配出来的内容，并支持平台、内容类型和条数筛选。</span>
      </div>

      <div className="panel">
        <div className="filter-grid">
          <label>
            平台
            <select value={platform} onChange={(event) => setPlatform(event.target.value)}>
              <option value="">全部</option>
              <option value="github">github</option>
              <option value="youtube">youtube</option>
            </select>
          </label>
          <label>
            内容类型
            <select value={contentType} onChange={(event) => setContentType(event.target.value)}>
              <option value="">全部</option>
              <option value="repository">repository</option>
              <option value="video">video</option>
            </select>
          </label>
          <label>
            条数
            <input value={limit} onChange={(event) => setLimit(event.target.value)} placeholder="20" />
          </label>
          <button className="primary-button" type="button" onClick={loadContent}>
            {loading ? '加载中...' : '刷新内容'}
          </button>
        </div>
      </div>

      <div className="content-feed">
        {items.length ? items.map((item) => (
          <article key={item.id} className="content-card">
            <div className="content-card-top">
              <div>
                <p className="section-kicker">{item.platform} / {item.contentType || 'unknown'}</p>
                <h3>{item.title}</h3>
              </div>
              {item.contentUrl ? (
                <a className="ghost-button content-link-button" href={item.contentUrl} target="_blank" rel="noreferrer">
                  查看原文
                </a>
              ) : null}
            </div>

            <p className="content-description">{item.description || '暂无描述'}</p>

            <div className="content-meta-grid">
              <span>作者：{item.authorName || '未知'}</span>
              <span>主指标：{renderMetric(item.primaryMetricLabel, item.primaryMetricValue)}</span>
              <span>次指标：{renderMetric(item.secondaryMetricLabel, item.secondaryMetricValue)}</span>
              <span>发布时间：{formatDateTime(item.publishTime)}</span>
              <span>抓取时间：{formatDateTime(item.crawlTime)}</span>
              <span>排序时间：{formatDateTime(item.sortTime)}</span>
            </div>
          </article>
        )) : (
          <div className="placeholder-panel">
            <strong>{loading ? '正在加载内容' : '暂无匹配内容'}</strong>
            <p>如果这里为空，通常说明当前用户的订阅条件还没有匹配到已入库内容。</p>
          </div>
        )}
      </div>
    </section>
  )
}
