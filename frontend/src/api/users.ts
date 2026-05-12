import http from './http'
import type { BaseResponse } from '../types/auth'
import type { UserListItem } from '../types/user'
import type { AdminUserSubscription } from '../types/admin-user'

export async function fetchUserList() {
  const { data } = await http.get<BaseResponse<UserListItem[]>>('/user/list')
  return data
}

export async function fetchUserDetail(userId: number) {
  const { data } = await http.get<BaseResponse<UserListItem & Record<string, unknown>>>(`/user/get/${userId}`)
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
