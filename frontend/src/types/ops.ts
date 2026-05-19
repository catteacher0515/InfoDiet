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

export interface DailyDigestPushRecordItem {
  id: number
  digestDate: string
  digestTitle?: string
  userId: number
  pushChannel: string
  receiveId?: string
  pushStatus: number
  pushTime?: string
  failReason?: string
}

export interface DailyDigestSectionClusterItem {
  clusterKey?: string
  clusterTitle?: string
  clusterScore?: number
  clusterSize?: number
}

export interface DailyDigestSectionItem {
  sectionTitle: string
  itemCount: number
  clusters: DailyDigestSectionClusterItem[]
}

export interface DailyDigestHistoryItem {
  digestDate: string
  digestTitle?: string
  totalClusterCount?: number
  totalItemCount?: number
  summary?: string
  sections?: DailyDigestSectionItem[]
}
