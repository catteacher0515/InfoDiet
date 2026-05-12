import { useEffect, useState } from 'react'
import { batchRetryFailedPushes, fetchFailedPushOverview, fetchFailedPushes, retryFailedPush } from '../../api/ops'
import { DataTable } from '../../components/DataTable'
import type { FailedPushOverview } from '../../types/ops-detail'
import type { PushFailureItem } from '../../types/ops'

export function OpsPushFailuresPage() {
  const [items, setItems] = useState<PushFailureItem[]>([])
  const [overview, setOverview] = useState<FailedPushOverview | null>(null)
  const [message, setMessage] = useState('')

  async function loadFailedPushes() {
    const response = await fetchFailedPushes()
    setItems(response.data)
  }

  useEffect(() => {
    void loadFailedPushes()
  }, [])

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
