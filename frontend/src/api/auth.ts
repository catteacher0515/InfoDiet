import http from './http'
import type { BaseResponse, LoginUser } from '../types/auth'

interface RegisterPayload {
  nickname: string
  username: string
  password: string
}

interface LoginPayload {
  username: string
  password: string
}

interface AdminCreatePayload {
  nickname: string
  username: string
  password: string
  role: 'admin' | 'user'
}

export async function register(payload: RegisterPayload) {
  const { data } = await http.post<BaseResponse<number>>('/auth/register', payload)
  return data
}

export async function login(payload: LoginPayload) {
  const { data } = await http.post<BaseResponse<LoginUser>>('/auth/login', payload)
  return data
}

export async function fetchCurrentUser() {
  const { data } = await http.get<BaseResponse<LoginUser>>('/auth/me')
  return data
}

export async function logout() {
  const { data } = await http.post<BaseResponse<boolean>>('/auth/logout')
  return data
}

export async function adminCreateUser(payload: AdminCreatePayload) {
  const { data } = await http.post<BaseResponse<number>>('/auth/admin/create', payload)
  return data
}
