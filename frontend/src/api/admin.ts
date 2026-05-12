import http from './http'
import type { BaseResponse } from '../types/auth'
import type { AdminSubscriptionOverview } from '../types/admin'

export async function fetchAdminSubscriptionOverview() {
  const { data } = await http.get<BaseResponse<AdminSubscriptionOverview>>('/admin/overview/subscriptions')
  return data
}
