import type { SourceSubscriptionItem, SubscriptionRuleItem } from './subscription'
import type { UserListItem } from './user'

export interface AdminUserSubscription {
  user: UserListItem
  keywords: string[]
  rules: SubscriptionRuleItem[]
  sources: SourceSubscriptionItem[]
}
