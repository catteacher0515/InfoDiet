export interface CrawlTaskLogItem {
  id: number
  taskType: string
  triggerSource: string
  taskStatus: number
  totalSourceCount?: number
  startTime?: string
  endTime?: string
  crawlCount?: number
  savedCount?: number
  skippedCount?: number
  matchedCount?: number
  unmatchedCount?: number
  enqueuedCount?: number
  enqueueSkippedCount?: number
  durationMs?: number
  errorMessage?: string
}

export interface PageResult<T> {
  totalCount: number
  pageNum: number
  pageSize: number
  records: T[]
}

export interface AlertRecordItem {
  id: number
  alertType: string
  alertLevel: string
  sourceType?: string
  sourceId?: number
  alertTitle: string
  alertStatus: number
  alertContent?: string
  failReason?: string
  lastOccurTime?: string
  sendTime?: string
}

export interface PushFailureItem {
  id: number
  userId: number
  contentItemId: number
  pushChannel: string
  pushStatus: number
  failReason?: string
  retryCount?: number
  updateTime?: string
}
