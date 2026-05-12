import { useEffect, useState } from 'react'
import { batchRetryFailedPushes, fetchFailedPushes, retryFailedPush } from '../../api/ops'
import { DataTable } from '../../components/DataTable'
import type { PushFailureItem } from '../../types/ops'

export function OpsPushFailuresPage() {
  const [items, setItems] = useState<PushFailureItem[]>([])
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

  return (
    <section className="page-section">
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
                <button className="ghost-button table-button" type="button" onClick={() => handleRetry(item.id)}>
                  单条重试
                </button>
              ),
            },
          ]}
          data={items}
        />
      </div>
    </section>
  )
}
