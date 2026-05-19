export interface UserListItem {
  id: number
  nickname: string
  username: string
  role: string
  status: number
}

export interface UserDetail extends UserListItem {
  feishuUserId?: string
  pushChannel?: string
  dailyPushLimit?: number
  pushCooldownHours?: number
  createTime?: string
  updateTime?: string
}

export interface UserPushConfigPayload {
  feishuUserId: string
  pushChannel: string
  dailyPushLimit: number
  pushCooldownHours: number
}
