import http from './http'
import type { BaseResponse } from '../types/auth'
import type { AlertRecordItem, CrawlTaskLogItem, PageResult, PushFailureItem } from '../types/ops'
import type { FailedPushOverview, TaskLogDetail } from '../types/ops-detail'

export async function fetchRecentTasks() {
  const { data } = await http.get<BaseResponse<CrawlTaskLogItem[]>>('/crawl-task-log/recent?limit=10')
  return data
}

export async function fetchTaskLogPage(params: {
  taskTypeKeyword?: string
  taskStatus?: number
  pageNum?: number
  pageSize?: number
}) {
  const search = new URLSearchParams()
  if (params.taskTypeKeyword) {
    search.set('taskTypeKeyword', params.taskTypeKeyword)
  }
  if (params.taskStatus !== undefined) {
    search.set('taskStatus', String(params.taskStatus))
  }
  search.set('pageNum', String(params.pageNum ?? 1))
  search.set('pageSize', String(params.pageSize ?? 10))
  const { data } = await http.get<BaseResponse<PageResult<CrawlTaskLogItem>>>(`/crawl-task-log/page?${search.toString()}`)
  return data
}

export async function fetchTaskLogDetail(taskLogId: number) {
  const { data } = await http.get<BaseResponse<TaskLogDetail>>(`/crawl-task-log/detail/${taskLogId}`)
  return data
}

export async function fetchPendingAlerts() {
  const { data } = await http.get<BaseResponse<AlertRecordItem[]>>('/alert-record/pending')
  return data
}

export async function markAlertSent(alertId: number) {
  const { data } = await http.post<BaseResponse<boolean>>(`/alert-record/mark-sent?alertId=${alertId}`)
  return data
}

export async function sendAlertToFeishu(alertId: number) {
  const { data } = await http.post<BaseResponse<boolean>>(`/alert-record/send?alertId=${alertId}`)
  return data
}

export async function sendPendingAlertsToFeishu() {
  const { data } = await http.post<BaseResponse<number>>('/alert-record/send/pending')
  return data
}

export async function fetchFailedPushes() {
  const { data } = await http.get<BaseResponse<PushFailureItem[]>>('/user-content-push/failed/list')
  return data
}

export async function fetchFailedPushPage(params: {
  keyword?: string
  retryCount?: number
  pageNum?: number
  pageSize?: number
}) {
  const search = new URLSearchParams()
  if (params.keyword) {
    search.set('keyword', params.keyword)
  }
  if (params.retryCount !== undefined) {
    search.set('retryCount', String(params.retryCount))
  }
  search.set('pageNum', String(params.pageNum ?? 1))
  search.set('pageSize', String(params.pageSize ?? 10))
  const { data } = await http.get<BaseResponse<PageResult<PushFailureItem>>>(`/user-content-push/failed/page?${search.toString()}`)
  return data
}

export async function runGithubDailyFlow() {
  const { data } = await http.post<BaseResponse<Record<string, unknown>>>('/schedule/github/daily/run')
  return data
}

export async function runYoutubeSourceDailyFlow() {
  const { data } = await http.post<BaseResponse<Record<string, unknown>>>('/schedule/github/youtube/source/daily/run')
  return data
}

export async function runAllSourceCrawl() {
  const { data } = await http.post<BaseResponse<Record<string, unknown>>>('/source/crawl/all/run')
  return data
}

export async function rerunTask(taskType: string) {
  const { data } = await http.post<BaseResponse<Record<string, unknown>>>(`/schedule/github/rerun?taskType=${taskType}`)
  return data
}

export async function retryFailedPush(pushId: number) {
  const { data } = await http.post<BaseResponse<boolean>>(`/user-content-push/retry?pushId=${pushId}`)
  return data
}

export async function fetchFailedPushOverview(pushId: number) {
  const { data } = await http.get<BaseResponse<FailedPushOverview>>(`/user-content-push/failed/overview?pushId=${pushId}`)
  return data
}

export async function batchRetryFailedPushes(pushIdList: number[]) {
  const { data } = await http.post<BaseResponse<{ totalCount: number; successCount: number; failedCount: number }>>(
    '/user-content-push/failed/retry',
    pushIdList,
  )
  return data
}
