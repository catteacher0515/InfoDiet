export type UserRole = 'admin' | 'user'

export interface LoginUser {
  id: number
  nickname: string
  username: string
  role: UserRole
  token?: string | null
}

export interface BaseResponse<T> {
  code: number
  data: T
  message: string
}
