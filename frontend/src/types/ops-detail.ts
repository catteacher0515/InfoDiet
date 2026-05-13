import type { AlertRecordItem, PushFailureItem } from './ops'

export interface FailedPushOverview {
  push: PushFailureItem
  contentItem?: {
    id: number
    title?: string
    platform?: string
    contentType?: string
    authorName?: string
    contentUrl?: string
  }
  alertRecord?: AlertRecordItem
}

export interface TaskLogDetail {
  id: number
  taskType: string
  triggerSource: string
  taskStatus: number
  totalSourceCount?: number
  crawlCount?: number
  savedCount?: number
  skippedCount?: number
  matchedCount?: number
  unmatchedCount?: number
  enqueuedCount?: number
  enqueueSkippedCount?: number
  errorMessage?: string
  startTime?: string
  endTime?: string
  durationMs?: number
}
