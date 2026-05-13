import { useEffect, useState } from 'react'
import {
  fetchTaskLogDetail,
  fetchTaskLogPage,
  rerunTask,
  runAllSourceCrawl,
  runGithubDailyFlow,
  runYoutubeSourceDailyFlow,
} from '../../api/ops'
import { DataTable } from '../../components/DataTable'
import type { CrawlTaskLogItem } from '../../types/ops'
import type { TaskLogDetail } from '../../types/ops-detail'

function formatDateTime(value?: string) {
  if (!value) {
    return '暂无'
  }
  return value.replace('T', ' ').slice(0, 19)
}

export function OpsTasksPage() {
  const [tasks, setTasks] = useState<CrawlTaskLogItem[]>([])
  const [detail, setDetail] = useState<TaskLogDetail | null>(null)
  const [taskTypeKeyword, setTaskTypeKeyword] = useState('')
  const [taskStatus, setTaskStatus] = useState('')
  const [pageNum, setPageNum] = useState(1)
  const [pageSize] = useState(10)
  const [totalCount, setTotalCount] = useState(0)
  const [message, setMessage] = useState('')

  async function loadTasks(nextPageNum = pageNum) {
    const response = await fetchTaskLogPage({
      taskTypeKeyword: taskTypeKeyword || undefined,
      taskStatus: taskStatus === '' ? undefined : Number(taskStatus),
      pageNum: nextPageNum,
      pageSize,
    })
    setTasks(response.data.records)
    setTotalCount(response.data.totalCount)
  }

  useEffect(() => {
    void loadTasks(pageNum)
  }, [pageNum])

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

  async function handleViewDetail(taskLogId: number) {
    const response = await fetchTaskLogDetail(taskLogId)
    setDetail(response.data)
  }

  async function handleSearch() {
    setPageNum(1)
    const response = await fetchTaskLogPage({
      taskTypeKeyword: taskTypeKeyword || undefined,
      taskStatus: taskStatus === '' ? undefined : Number(taskStatus),
      pageNum: 1,
      pageSize,
    })
    setTasks(response.data.records)
    setTotalCount(response.data.totalCount)
    setDetail(null)
  }

  return (
    <section className="page-section split-page">
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
      <div className="content-split admin-users-split">
        <div className="panel">
          <div className="filter-bar">
            <input value={taskTypeKeyword} onChange={(event) => setTaskTypeKeyword(event.target.value)} placeholder="按任务类型搜索" />
            <select value={taskStatus} onChange={(event) => setTaskStatus(event.target.value)}>
              <option value="">全部状态</option>
              <option value="1">成功</option>
              <option value="2">失败</option>
            </select>
            <button className="ghost-button" type="button" onClick={handleSearch}>
              查询
            </button>
          </div>
          <DataTable
            columns={[
              { key: 'taskType', title: '任务类型', render: (item) => item.taskType },
              { key: 'triggerSource', title: '触发来源', render: (item) => item.triggerSource },
              { key: 'taskStatus', title: '状态', render: (item) => (item.taskStatus === 1 ? '成功' : item.taskStatus === 2 ? '失败' : '运行中') },
              { key: 'crawlCount', title: '抓取数', render: (item) => item.crawlCount ?? '--' },
              { key: 'savedCount', title: '新增数', render: (item) => item.savedCount ?? '--' },
              { key: 'startTime', title: '开始时间', render: (item) => formatDateTime(item.startTime) },
              {
                key: 'action',
                title: '操作',
                render: (item) => (
                  <div className="button-row">
                    <button className="ghost-button table-button" type="button" onClick={() => handleViewDetail(item.id)}>
                      详情
                    </button>
                    <button className="ghost-button table-button" type="button" onClick={() => handleRerun(item.taskType)}>
                      重跑
                    </button>
                  </div>
                ),
              },
            ]}
            data={tasks}
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
          <h3>任务日志详情</h3>
          {detail ? (
            <div className="stack-list">
              <article className="stack-item">
                <strong>{detail.taskType}</strong>
                <span>触发来源：{detail.triggerSource}</span>
                <em>状态：{detail.taskStatus === 1 ? '成功' : detail.taskStatus === 2 ? '失败' : '运行中'}</em>
              </article>
              <article className="stack-item">
                <strong>执行指标</strong>
                <span>订阅源：{detail.totalSourceCount ?? '--'} / 抓取：{detail.crawlCount ?? '--'} / 入库：{detail.savedCount ?? '--'}</span>
                <span>匹配：{detail.matchedCount ?? '--'} / 入队：{detail.enqueuedCount ?? '--'}</span>
                <em>耗时：{detail.durationMs ?? '--'} ms</em>
              </article>
              <article className="stack-item">
                <strong>时间与异常</strong>
                <span>开始：{formatDateTime(detail.startTime)}</span>
                <span>结束：{formatDateTime(detail.endTime)}</span>
                <em>{detail.errorMessage || '无异常信息'}</em>
              </article>
            </div>
          ) : (
            <p className="empty-inline">先选择一条任务日志查看详情</p>
          )}
        </div>
      </div>
    </section>
  )
}
