export interface SubscriptionRuleItem {
  id: number
  ruleType: string
  ruleValue: string
  ruleWeight: number
  status?: number
}

export interface SourceSubscriptionItem {
  id: number
  platform: string
  sourceType: string
  sourceValue: string
  status?: number
}

export interface WorkspaceSubscriptions {
  keywords: string[]
  rules: SubscriptionRuleItem[]
  sources: SourceSubscriptionItem[]
}

export interface UserPushConfig {
  feishuUserId: string
  pushChannel: string
  dailyPushLimit: number
  pushCooldownHours: number
}

export interface WorkspaceContentQuery {
  platform?: string
  contentType?: string
  limit?: number
}

export interface WorkspaceContentItem {
  id: number
  platform: string
  sourceId?: string
  title: string
  contentType?: string
  description?: string
  contentUrl?: string
  authorName?: string
  authorUrl?: string
  primaryMetricValue?: number
  primaryMetricLabel?: string
  secondaryMetricValue?: number
  secondaryMetricLabel?: string
  publishTime?: string
  crawlTime?: string
  sortTime?: string
  dedupKey?: string
}

export interface WorkspacePushItem {
  id: number
  userId: number
  contentItemId: number
  pushChannel: string
  pushStatus: number
  queueStatus: number
  retryCount?: number
  maxRetryCount?: number
  nextRetryTime?: string
  lastQueueTime?: string
  pushTime?: string
  failReason?: string
  createTime?: string
  updateTime?: string
}
