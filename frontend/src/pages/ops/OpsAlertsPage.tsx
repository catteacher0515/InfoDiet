import { useEffect, useState } from 'react'
import { fetchPendingAlerts, markAlertSent, sendAlertToFeishu, sendPendingAlertsToFeishu } from '../../api/ops'
import { DataTable } from '../../components/DataTable'
import type { AlertRecordItem } from '../../types/ops'

function formatDateTime(value?: string) {
  if (!value) {
    return '暂无'
  }
  return value.replace('T', ' ').slice(0, 19)
}

export function OpsAlertsPage() {
  const [items, setItems] = useState<AlertRecordItem[]>([])
  const [message, setMessage] = useState('')

  async function loadAlerts() {
    const response = await fetchPendingAlerts()
    setItems(response.data)
  }

  useEffect(() => {
    void loadAlerts()
  }, [])

  async function handleSend(alertId: number) {
    const response = await sendAlertToFeishu(alertId)
    setMessage(response.code === 0 && response.data ? `告警 ${alertId} 已发送` : response.message || '发送失败')
    await loadAlerts()
  }

  async function handleMarkSent(alertId: number) {
    const response = await markAlertSent(alertId)
    setMessage(response.code === 0 && response.data ? `告警 ${alertId} 已标记为已发送` : response.message || '标记失败')
    await loadAlerts()
  }

  async function handleSendPending() {
    const response = await sendPendingAlertsToFeishu()
    setMessage(response.code === 0 ? `批量发送完成，本次处理 ${response.data} 条` : response.message || '批量发送失败')
    await loadAlerts()
  }

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="section-kicker">ALERTS</p>
        <h2>失败告警</h2>
        <span>这里可以查看待处理告警，并直接发送到飞书或手动标记状态。</span>
      </div>
      <div className="toolbar">
        <button className="primary-button" type="button" onClick={handleSendPending}>
          批量发送待处理告警
        </button>
        {message ? <p className="form-message">{message}</p> : null}
      </div>
      <div className="panel">
        <DataTable
          columns={[
            { key: 'alertType', title: '告警类型', render: (item) => item.alertType },
            { key: 'alertLevel', title: '级别', render: (item) => item.alertLevel },
            { key: 'alertTitle', title: '标题', render: (item) => item.alertTitle },
            { key: 'alertStatus', title: '状态', render: (item) => (item.alertStatus === 0 ? '待处理' : item.alertStatus === 1 ? '已发送' : '发送失败') },
            { key: 'sourceType', title: '来源类型', render: (item) => item.sourceType || '未知' },
            { key: 'lastOccurTime', title: '最近发生', render: (item) => formatDateTime(item.lastOccurTime) },
            { key: 'failReason', title: '失败原因', render: (item) => item.failReason || '无' },
            {
              key: 'action',
              title: '操作',
              render: (item) => (
                <div className="button-row">
                  <button className="ghost-button table-button" type="button" onClick={() => handleSend(item.id)}>
                    发送
                  </button>
                  <button className="ghost-button table-button" type="button" onClick={() => handleMarkSent(item.id)}>
                    标记已发
                  </button>
                </div>
              ),
            },
          ]}
          data={items}
        />
      </div>
    </section>
  )
}
