import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchDigestDetail, fetchDigestPushRecordPage, fetchRecentDigests, runTodayDigestPush } from '../../api/ops'
import { DataTable } from '../../components/DataTable'
import type { DailyDigestHistoryItem, DailyDigestPushRecordItem } from '../../types/ops'

function formatDateTime(value?: string) {
  if (!value) {
    return '暂无'
  }
  return value.replace('T', ' ').slice(0, 19)
}

export function OpsDigestPage() {
  const [digests, setDigests] = useState<DailyDigestHistoryItem[]>([])
  const [detail, setDetail] = useState<DailyDigestHistoryItem | null>(null)
  const [pushRecords, setPushRecords] = useState<DailyDigestPushRecordItem[]>([])
  const [pushStatus, setPushStatus] = useState('')
  const [keyword, setKeyword] = useState('')
  const [selectedDigestDate, setSelectedDigestDate] = useState('')
  const [pageNum, setPageNum] = useState(1)
  const [pageSize] = useState(10)
  const [totalCount, setTotalCount] = useState(0)
  const [message, setMessage] = useState('')
  const [showOnlyFailed, setShowOnlyFailed] = useState(false)
  const [expandedSections, setExpandedSections] = useState<Record<string, boolean>>({})
  const [lastTriggeredDigestDate, setLastTriggeredDigestDate] = useState('')

  async function loadDigestList() {
    const response = await fetchRecentDigests(10)
    setDigests(response.data)
    if (!detail && response.data.length > 0) {
      const firstDate = response.data[0].digestDate
      setSelectedDigestDate(firstDate)
      const detailResponse = await fetchDigestDetail(firstDate)
      setDetail(detailResponse.data)
    }
  }

  async function loadPushRecords(nextPageNum = pageNum) {
    const response = await fetchDigestPushRecordPage({
      pushStatus: showOnlyFailed ? 2 : pushStatus === '' ? undefined : Number(pushStatus),
      keyword: keyword || undefined,
      digestDate: selectedDigestDate || undefined,
      pageNum: nextPageNum,
      pageSize,
    })
    setPushRecords(response.data.records)
    setTotalCount(response.data.totalCount)
  }

  useEffect(() => {
    void loadDigestList()
    void loadPushRecords(pageNum)
  }, [pageNum])

  async function handleSelectDigest(digestDate: string) {
    const response = await fetchDigestDetail(digestDate)
    setSelectedDigestDate(digestDate)
    setDetail(response.data)
    setPageNum(1)
    const pushResponse = await fetchDigestPushRecordPage({
      pushStatus: showOnlyFailed ? 2 : pushStatus === '' ? undefined : Number(pushStatus),
      keyword: keyword || undefined,
      digestDate,
      pageNum: 1,
      pageSize,
    })
    setPushRecords(pushResponse.data.records)
    setTotalCount(pushResponse.data.totalCount)
  }

  async function handleSearchPushRecords() {
    setPageNum(1)
    const response = await fetchDigestPushRecordPage({
      pushStatus: showOnlyFailed ? 2 : pushStatus === '' ? undefined : Number(pushStatus),
      keyword: keyword || undefined,
      digestDate: selectedDigestDate || undefined,
      pageNum: 1,
      pageSize,
    })
    setPushRecords(response.data.records)
    setTotalCount(response.data.totalCount)
  }

  async function handleRunTodayDigestPush() {
    const response = await runTodayDigestPush()
    if (response.code === 0) {
      setMessage(`今日日报推送完成：成功 ${response.data.successCount}，失败 ${response.data.failedCount}`)
      setLastTriggeredDigestDate(selectedDigestDate)
      await loadDigestList()
      await loadPushRecords(1)
      setPageNum(1)
      return
    }
    setMessage(response.message || '今日日报推送失败')
  }

  const selectedDigestPushes = pushRecords.filter((item) => !selectedDigestDate || item.digestDate === selectedDigestDate)
  const selectedDigestTotal = selectedDigestPushes.length
  const selectedDigestSuccessCount = selectedDigestPushes.filter((item) => item.pushStatus === 1).length
  const selectedDigestFailedCount = selectedDigestPushes.filter((item) => item.pushStatus === 2).length
  const selectedDigestSuccessRate = selectedDigestTotal === 0 ? '--' : `${Math.round((selectedDigestSuccessCount / selectedDigestTotal) * 100)}%`

  function toggleSection(sectionTitle: string) {
    setExpandedSections((prev) => ({
      ...prev,
      [sectionTitle]: !prev[sectionTitle],
    }))
  }

  async function handleShowOnlyFailed() {
    setShowOnlyFailed(true)
    setPushStatus('')
    setPageNum(1)
    const response = await fetchDigestPushRecordPage({
      pushStatus: 2,
      keyword: keyword || undefined,
      digestDate: selectedDigestDate || undefined,
      pageNum: 1,
      pageSize,
    })
    setPushRecords(response.data.records)
    setTotalCount(response.data.totalCount)
  }

  async function handleClearFailedOnly() {
    setShowOnlyFailed(false)
    setPageNum(1)
    const response = await fetchDigestPushRecordPage({
      pushStatus: pushStatus === '' ? undefined : Number(pushStatus),
      keyword: keyword || undefined,
      digestDate: selectedDigestDate || undefined,
      pageNum: 1,
      pageSize,
    })
    setPushRecords(response.data.records)
    setTotalCount(response.data.totalCount)
  }

  return (
    <section className="page-section split-page">
      <div className="page-heading">
        <p className="section-kicker">DIGEST OPS</p>
        <h2>日报运营</h2>
        <span>把日报生成、日报详情和日报推送记录放到一页里统一看。</span>
      </div>

      <div className="toolbar">
        <div className="button-row">
          <button className="primary-button" type="button" onClick={handleRunTodayDigestPush}>
            手动推送今日日报
          </button>
          <Link className="ghost-button" to="/ops/push-failures">
            查看失败推送页
          </Link>
        </div>
        {message ? <p className="form-message">{message}</p> : null}
      </div>

      <div className="content-split admin-users-split">
        <div className="panel">
          <h3>最近日报</h3>
          {digests.length > 0 ? (
            <div className="stack-list">
              {digests.map((item) => (
                <article
                  className={`stack-item${selectedDigestDate === item.digestDate ? ' stack-item-active' : ''}${lastTriggeredDigestDate === item.digestDate ? ' stack-item-highlight' : ''}`}
                  key={item.digestDate}
                >
                  <strong>{item.digestTitle || item.digestDate}</strong>
                  <span>{item.digestDate} / 精选事件 {item.totalClusterCount ?? 0} / 精选内容 {item.totalItemCount ?? 0}</span>
                  <em>{item.summary || '暂无摘要'}</em>
                  <div className="button-row">
                    <button className="ghost-button table-button" type="button" onClick={() => handleSelectDigest(item.digestDate)}>
                      查看详情
                    </button>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <p className="empty-inline">还没有日报历史记录</p>
          )}
        </div>

        <div className="panel">
          <h3>日报详情</h3>
          {detail ? (
            <div className="stack-list">
              <article className="stack-item">
                <strong>{detail.digestTitle || detail.digestDate}</strong>
                <span>{detail.digestDate}</span>
                <em>{detail.summary || '暂无摘要'}</em>
              </article>
              {(detail.sections || []).map((section) => (
                <article className="stack-item" key={section.sectionTitle}>
                  <strong>{section.sectionTitle}</strong>
                  <span>事件数：{section.itemCount ?? 0}</span>
                  <em>
                    {(expandedSections[section.sectionTitle] ? section.clusters || [] : (section.clusters || []).slice(0, 3))
                      .map((cluster) => cluster.clusterTitle || '未命名事件')
                      .join(' / ') || '暂无事件'}
                  </em>
                  {(section.clusters || []).length > 3 ? (
                    <div className="button-row">
                      <button className="ghost-button table-button" type="button" onClick={() => toggleSection(section.sectionTitle)}>
                        {expandedSections[section.sectionTitle] ? '收起事件簇' : '展开更多'}
                      </button>
                    </div>
                  ) : null}
                </article>
              ))}
              <article className="stack-item">
                <strong>推送表现</strong>
                <span>成功 {selectedDigestSuccessCount} / 失败 {selectedDigestFailedCount} / 总数 {selectedDigestTotal}</span>
                <em>当前成功率：{selectedDigestSuccessRate}</em>
              </article>
            </div>
          ) : (
            <p className="empty-inline">先从左侧选择一份日报</p>
          )}
        </div>
      </div>

      <div className="panel">
        <h3>日报推送记录</h3>
        <div className="filter-bar">
          <select value={pushStatus} onChange={(event) => setPushStatus(event.target.value)}>
            <option value="">全部状态</option>
            <option value="1">推送成功</option>
            <option value="2">推送失败</option>
          </select>
          <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="搜索日报标题 / 用户 ID / 接收人 / 失败原因" />
          <button className="ghost-button" type="button" onClick={handleSearchPushRecords}>
            查询
          </button>
          {!showOnlyFailed ? (
            <button className="ghost-button" type="button" onClick={handleShowOnlyFailed}>
              只看失败记录
            </button>
          ) : (
            <button className="ghost-button" type="button" onClick={handleClearFailedOnly}>
              取消失败筛选
            </button>
          )}
        </div>
        {selectedDigestDate ? <p className="form-message">当前按日报日期筛选：{selectedDigestDate}</p> : null}
        {showOnlyFailed ? <p className="form-message">当前仅展示失败推送记录</p> : null}
        <DataTable
          columns={[
            { key: 'digestDate', title: '日报日期', render: (item) => item.digestDate },
            { key: 'digestTitle', title: '日报标题', render: (item) => item.digestTitle || '未命名日报' },
            { key: 'userId', title: '用户 ID', render: (item) => item.userId },
            { key: 'receiveId', title: '接收人', render: (item) => item.receiveId || '暂无' },
            { key: 'pushStatus', title: '状态', render: (item) => (item.pushStatus === 1 ? '成功' : '失败') },
            { key: 'pushTime', title: '推送时间', render: (item) => formatDateTime(item.pushTime) },
            { key: 'failReason', title: '失败原因', render: (item) => item.failReason || '--' },
          ]}
          data={pushRecords}
        />
        <div className="pager-bar">
          <span>共 {totalCount} 条</span>
          <div className="button-row">
            <button className="ghost-button table-button" type="button" disabled={pageNum <= 1} onClick={() => setPageNum((prev) => prev - 1)}>
              上一页
            </button>
            <span>第 {pageNum} 页</span>
            <button
              className="ghost-button table-button"
              type="button"
              disabled={pageNum * pageSize >= totalCount}
              onClick={() => setPageNum((prev) => prev + 1)}
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </section>
  )
}
