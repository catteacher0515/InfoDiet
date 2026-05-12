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
