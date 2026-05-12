import http from './http'
import type { BaseResponse } from '../types/auth'
import type {
  AdminDashboard,
  OpsDashboard,
  WorkspaceDashboard,
} from '../types/dashboard'

export async function fetchWorkspaceDashboard() {
  const { data } = await http.get<BaseResponse<WorkspaceDashboard>>('/workspace/dashboard/me')
  return data
}

export async function fetchAdminDashboard() {
  const { data } = await http.get<BaseResponse<AdminDashboard>>('/admin/dashboard')
  return data
}

export async function fetchOpsDashboard() {
  const { data } = await http.get<BaseResponse<OpsDashboard>>('/ops/dashboard')
  return data
}
