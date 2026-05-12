export interface WorkspaceDashboard {
  keywordCount: number
  sourceCount: number
  totalPushCount: number
  successPushCount: number
  failedPushCount: number
}

export interface AdminDashboard {
  userCount: number
  enabledUserCount: number
  keywordSubscriptionCount: number
  sourceSubscriptionCount: number
}

export interface OpsDashboard {
  recentTaskCount: number
  pendingAlertCount: number
  failedPushCount: number
}
