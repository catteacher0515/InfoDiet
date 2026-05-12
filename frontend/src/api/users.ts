import http from './http'
import type { BaseResponse } from '../types/auth'
import type { UserListItem } from '../types/user'

export async function fetchUserList() {
  const { data } = await http.get<BaseResponse<UserListItem[]>>('/user/list')
  return data
}
