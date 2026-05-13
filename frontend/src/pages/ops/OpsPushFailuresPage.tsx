import { useEffect, useState } from 'react'
import { batchRetryFailedPushes, fetchFailedPushOverview, fetchFailedPushPage, retryFailedPush } from '../../api/ops'
import { DataTable } from '../../components/DataTable'
import type { PushFailureItem } from '../../types/ops'
import type { FailedPushOverview } from '../../types/ops-detail'

export function OpsPushFailuresPage() {
  const [items, setItems] = useState<PushFailureItem[]>([])
  const [overview, setOverview] = useState<FailedPushOverview | null>(null)
  const [keyword, setKeyword] = useState('')
  const [retryCount, setRetryCount] = useState('')
  const [pageNum, setPageNum] = useState(1)
  const [pageSize] = useState(10)
  const [totalCount, setTotalCount] = useState(0)
  const [message, setMessage] = useState('')

  async function loadFailedPushes(nextPageNum = pageNum) {
    const response = await fetchFailedPushPage({
      keyword: keyword || undefined,
      retryCount: retryCount === '' ? undefined : Number(retryCount),
      pageNum: nextPageNum,
      pageSize,
    })
    setItems(response.data.records)
    setTotalCount(response.data.totalCount)
  }

  useEffect(() => {
    void loadFailedPushes(pageNum)
  }, [pageNum])

  async function handleRetry(pushId: number) {
    setMessage('')
    const response = await retryFailedPush(pushId)
    setMessage(response.code === 0 && response.data ? `已重试 ${pushId}` : response.message || '重试失败')
    await loadFailedPushes()
  }

  async function handleBatchRetry() {
    setMessage('')
    const pushIdList = items.map((item) => item.id)
    if (pushIdList.length === 0) {
      setMessage('当前没有可重试的失败记录')
      return
    }
    const response = await batchRetryFailedPushes(pushIdList)
    if (response.code === 0) {
      setMessage(`批量重试完成：成功 ${response.data.successCount}，失败 ${response.data.failedCount}`)
      await loadFailedPushes()
      return
    }
    setMessage(response.message || '批量重试失败')
  }

  async function handleViewOverview(pushId: number) {
    const response = await fetchFailedPushOverview(pushId)
    setOverview(response.data)
  }

  async function handleSearch() {
    setPageNum(1)
    const response = await fetchFailedPushPage({
      keyword: keyword || undefined,
      retryCount: retryCount === '' ? undefined : Number(retryCount),
      pageNum: 1,
      pageSize,
    })
    setItems(response.data.records)
    setTotalCount(response.data.totalCount)
    setOverview(null)
  }

  return (
    <section className="page-section split-page">
      <div className="page-heading">
        <p className="section-kicker">FAILED PUSHES</p>
        <h2>失败推送</h2>
        <span>这里已经接入后端真实失败推送列表，并提供单条重试与批量重试。</span>
      </div>
      <div className="toolbar">
        <button className="primary-button" type="button" onClick={handleBatchRetry}>
          批量重试全部失败记录
        </button>
        {message ? <p className="form-message">{message}</p> : null}
      </div>
      <div className="content-split admin-users-split">
        <div className="panel">
          <div className="filter-bar">
            <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="搜索记录 ID / 用户 ID / 内容 ID / 失败原因" />
            <select value={retryCount} onChange={(event) => setRetryCount(event.target.value)}>
              <option value="">全部重试次数</option>
              <option value="0">0 次</option>
              <option value="1">1 次</option>
              <option value="2">2 次</option>
              <option value="3">3 次</option>
            </select>
            <button className="ghost-button" type="button" onClick={handleSearch}>
              查询
            </button>
          </div>
          <DataTable
            columns={[
              { key: 'id', title: '记录 ID', render: (item) => item.id },
              { key: 'userId', title: '用户 ID', render: (item) => item.userId },
              { key: 'contentItemId', title: '内容 ID', render: (item) => item.contentItemId },
              { key: 'retryCount', title: '重试次数', render: (item) => item.retryCount ?? 0 },
              { key: 'failReason', title: '失败原因', render: (item) => item.failReason || '未知' },
              {
                key: 'action',
                title: '操作',
                render: (item) => (
                  <div className="button-row">
                    <button className="ghost-button table-button" type="button" onClick={() => handleViewOverview(item.id)}>
                      查看关联
                    </button>
                    <button className="ghost-button table-button" type="button" onClick={() => handleRetry(item.id)}>
                      单条重试
                    </button>
                  </div>
                ),
              },
            ]}
            data={items}
          />
          <div className="pager-bar">
            <span>共 {totalCount} 条</span>
            <div className="button-row">
              <button className="ghost-button table-button" type="button" disabled={pageNum <= 1} onClick={() => setPageNum((prev) => prev - 1)}>
                上一页
              </button>
              <span>第 {pageNum} 页</span>
              <button
                className="ghost-button table-button"
                type="button"
                disabled={pageNum * pageSize >= totalCount}
                onClick={() => setPageNum((prev) => prev + 1)}
              >
                下一页
              </button>
            </div>
          </div>
        </div>

        <div className="panel">
          <h3>失败推送联动信息</h3>
          {overview ? (
            <div className="stack-list">
              <article className="stack-item">
                <strong>推送记录</strong>
                <span>pushId：{overview.push.id}</span>
                <span>userId：{overview.push.userId}</span>
                <em>失败原因：{overview.push.failReason || '无'}</em>
              </article>
              <article className="stack-item">
                <strong>关联内容</strong>
                <span>{overview.contentItem?.title || '暂无内容标题'}</span>
                <span>{overview.contentItem?.platform || '未知平台'} / {overview.contentItem?.contentType || '未知类型'}</span>
                <em>{overview.contentItem?.authorName || '未知作者'}</em>
              </article>
              <article className="stack-item">
                <strong>关联告警</strong>
                <span>{overview.alertRecord?.alertTitle || '暂无告警记录'}</span>
                <span>{overview.alertRecord?.alertType || '无类型'}</span>
                <em>{overview.alertRecord?.alertContent || '暂无告警内容'}</em>
              </article>
            </div>
          ) : (
            <p className="empty-inline">先在左侧选择一条失败推送查看关联信息</p>
          )}
        </div>
      </div>
    </section>
  )
}
