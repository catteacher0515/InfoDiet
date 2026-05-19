import http from './http'
import type { BaseResponse } from '../types/auth'
import type { UserDetail, UserListItem, UserPushConfigPayload } from '../types/user'
import type { AdminUserSubscription } from '../types/admin-user'

export async function fetchUserList() {
  const { data } = await http.get<BaseResponse<UserListItem[]>>('/user/list')
  return data
}

export async function fetchUserDetail(userId: number) {
  const { data } = await http.get<BaseResponse<UserDetail>>(`/user/get/${userId}`)
  return data
}

export async function fetchAdminUserSubscription(userId: number) {
  const { data } = await http.get<BaseResponse<AdminUserSubscription>>(`/user/admin/subscription/${userId}`)
  return data
}

export async function updateUserStatus(userId: number, status: number) {
  const { data } = await http.put<BaseResponse<boolean>>('/user/update', { id: userId, status })
  return data
}

export async function updateUserPushConfig(userId: number, payload: UserPushConfigPayload) {
  const { data } = await http.put<BaseResponse<boolean>>(`/user/${userId}/push-config`, payload)
  return data
}
