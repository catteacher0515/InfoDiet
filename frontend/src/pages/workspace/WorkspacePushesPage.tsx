import { useEffect, useState } from 'react'
import { fetchMyPushes } from '../../api/workspace'
import { DataTable } from '../../components/DataTable'
import type { WorkspacePushItem } from '../../types/subscription'

function formatDateTime(value?: string) {
  if (!value) {
    return '暂无'
  }
  return value.replace('T', ' ').slice(0, 19)
}

function renderPushStatus(status: number) {
  if (status === 1) {
    return '成功'
  }
  if (status === 2) {
    return '失败'
  }
  return '待推送'
}

function renderQueueStatus(status: number) {
  if (status === 1) {
    return '已入队'
  }
  if (status === 2) {
    return '消费中'
  }
  if (status === 3) {
    return '已完成'
  }
  return '待入队'
}

export function WorkspacePushesPage() {
  const [items, setItems] = useState<WorkspacePushItem[]>([])

  async function loadPushes() {
    const response = await fetchMyPushes()
    setItems(response.data)
  }

  useEffect(() => {
    void loadPushes()
  }, [])

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="section-kicker">PUSHES</p>
        <h2>推送记录</h2>
        <span>这里展示当前登录用户自己的推送记录、状态、重试次数与失败原因。</span>
      </div>

      <div className="panel">
        <DataTable
          data={items}
          columns={[
            { key: 'id', title: '记录 ID', render: (item) => item.id },
            { key: 'contentItemId', title: '内容 ID', render: (item) => item.contentItemId },
            { key: 'pushChannel', title: '渠道', render: (item) => item.pushChannel || '未设置' },
            { key: 'pushStatus', title: '推送状态', render: (item) => renderPushStatus(item.pushStatus) },
            { key: 'queueStatus', title: '队列状态', render: (item) => renderQueueStatus(item.queueStatus) },
            { key: 'retryCount', title: '重试次数', render: (item) => item.retryCount ?? 0 },
            { key: 'failReason', title: '失败原因', render: (item) => item.failReason || '无' },
            { key: 'pushTime', title: '推送时间', render: (item) => formatDateTime(item.pushTime) },
            { key: 'createTime', title: '创建时间', render: (item) => formatDateTime(item.createTime) },
          ]}
          emptyText="当前用户还没有推送记录"
        />
      </div>
    </section>
  )
}
