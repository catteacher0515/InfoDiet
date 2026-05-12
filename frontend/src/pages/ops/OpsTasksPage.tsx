import { useEffect, useState } from 'react'
import { fetchRecentTasks, rerunTask, runAllSourceCrawl, runGithubDailyFlow, runYoutubeSourceDailyFlow } from '../../api/ops'
import { DataTable } from '../../components/DataTable'
import type { CrawlTaskLogItem } from '../../types/ops'

function formatDateTime(value?: string) {
  if (!value) {
    return '暂无'
  }
  return value.replace('T', ' ').slice(0, 19)
}

export function OpsTasksPage() {
  const [tasks, setTasks] = useState<CrawlTaskLogItem[]>([])
  const [message, setMessage] = useState('')

  async function loadTasks() {
    const response = await fetchRecentTasks()
    setTasks(response.data)
  }

  useEffect(() => {
    void loadTasks()
  }, [])

  async function handleRunGithubFlow() {
    const response = await runGithubDailyFlow()
    setMessage(response.code === 0 ? 'GitHub 每日流程已触发' : response.message || '触发失败')
    await loadTasks()
  }

  async function handleRunYoutubeFlow() {
    const response = await runYoutubeSourceDailyFlow()
    setMessage(response.code === 0 ? 'YouTube 订阅源流程已触发' : response.message || '触发失败')
    await loadTasks()
  }

  async function handleRunAllSourceCrawl() {
    const response = await runAllSourceCrawl()
    setMessage(response.code === 0 ? '全部订阅源采集已触发' : response.message || '触发失败')
    await loadTasks()
  }

  async function handleRerun(taskType: string) {
    const response = await rerunTask(taskType)
    setMessage(response.code === 0 ? `任务 ${taskType} 已重跑` : response.message || '任务重跑失败')
    await loadTasks()
  }

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="section-kicker">TASK LOGS</p>
        <h2>任务日志</h2>
        <span>查看最近一批采集与推送编排任务的执行情况。</span>
      </div>
      <div className="toolbar">
        <div className="button-row">
          <button className="primary-button" type="button" onClick={handleRunGithubFlow}>
            触发 GitHub 每日流程
          </button>
          <button className="ghost-button" type="button" onClick={handleRunYoutubeFlow}>
            触发 YouTube 订阅源流程
          </button>
          <button className="ghost-button" type="button" onClick={handleRunAllSourceCrawl}>
            触发全部订阅源采集
          </button>
        </div>
        {message ? <p className="form-message">{message}</p> : null}
      </div>
      <div className="panel">
        <DataTable
          columns={[
            { key: 'taskType', title: '任务类型', render: (item) => item.taskType },
            { key: 'triggerSource', title: '触发来源', render: (item) => item.triggerSource },
            { key: 'taskStatus', title: '状态', render: (item) => (item.taskStatus === 1 ? '成功' : item.taskStatus === 2 ? '失败' : '运行中') },
            { key: 'totalSourceCount', title: '订阅源数', render: (item) => item.totalSourceCount ?? '--' },
            { key: 'crawlCount', title: '抓取数', render: (item) => item.crawlCount ?? '--' },
            { key: 'savedCount', title: '新增数', render: (item) => item.savedCount ?? '--' },
            { key: 'matchedCount', title: '匹配数', render: (item) => item.matchedCount ?? '--' },
            { key: 'enqueuedCount', title: '入队数', render: (item) => item.enqueuedCount ?? '--' },
            { key: 'durationMs', title: '耗时(ms)', render: (item) => item.durationMs ?? '--' },
            { key: 'startTime', title: '开始时间', render: (item) => formatDateTime(item.startTime) },
            { key: 'errorMessage', title: '错误信息', render: (item) => item.errorMessage || '无' },
            {
              key: 'action',
              title: '操作',
              render: (item) => (
                <button className="ghost-button table-button" type="button" onClick={() => handleRerun(item.taskType)}>
                  重跑
                </button>
              ),
            },
          ]}
          data={tasks}
        />
      </div>
    </section>
  )
}
