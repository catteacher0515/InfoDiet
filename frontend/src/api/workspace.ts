import http from './http'
import type { BaseResponse } from '../types/auth'
import type {
  WorkspaceContentItem,
  WorkspaceContentQuery,
  WorkspacePushItem,
  WorkspaceSubscriptions,
} from '../types/subscription'

export async function fetchMySubscriptions() {
  const { data } = await http.get<BaseResponse<WorkspaceSubscriptions>>('/workspace/subscriptions/me')
  return data
}

export async function addMyKeyword(keyword: string) {
  const { data } = await http.post<BaseResponse<boolean>>('/workspace/keywords', { keyword })
  return data
}

export async function removeMyKeyword(keyword: string) {
  const { data } = await http.delete<BaseResponse<boolean>>('/workspace/keywords', { data: { keyword } })
  return data
}

export async function addMyRule(ruleType: string, ruleValue: string, ruleWeight: number) {
  const { data } = await http.post<BaseResponse<boolean>>('/workspace/rules', {
    ruleType,
    ruleValue,
    ruleWeight,
  })
  return data
}

export async function removeMyRule(ruleType: string, ruleValue: string) {
  const { data } = await http.delete<BaseResponse<boolean>>('/workspace/rules', {
    data: { ruleType, ruleValue },
  })
  return data
}

export async function addMySource(platform: string, sourceType: string, sourceValue: string) {
  const { data } = await http.post<BaseResponse<boolean>>('/workspace/sources', {
    platform,
    sourceType,
    sourceValue,
  })
  return data
}

export async function removeMySource(id: number) {
  const { data } = await http.delete<BaseResponse<boolean>>('/workspace/sources', { data: { id } })
  return data
}

export async function fetchMyContent(params: WorkspaceContentQuery) {
  const { data } = await http.get<BaseResponse<WorkspaceContentItem[]>>('/workspace/content/me', { params })
  return data
}

export async function fetchMyPushes() {
  const { data } = await http.get<BaseResponse<WorkspacePushItem[]>>('/workspace/pushes/me')
  return data
}
