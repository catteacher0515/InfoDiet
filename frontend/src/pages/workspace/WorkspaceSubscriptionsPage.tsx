import { useEffect, useState } from 'react'
import {
  addMyKeyword,
  addMyRule,
  addMySource,
  fetchMySubscriptions,
  removeMyKeyword,
  removeMyRule,
  removeMySource,
} from '../../api/workspace'
import type { WorkspaceSubscriptions } from '../../types/subscription'

export function WorkspaceSubscriptionsPage() {
  const [data, setData] = useState<WorkspaceSubscriptions | null>(null)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [keyword, setKeyword] = useState('')
  const [ruleType, setRuleType] = useState('keyword')
  const [ruleValue, setRuleValue] = useState('')
  const [ruleWeight, setRuleWeight] = useState('1')
  const [sourcePlatform, setSourcePlatform] = useState('youtube')
  const [sourceType, setSourceType] = useState('channel')
  const [sourceValue, setSourceValue] = useState('')

  async function loadSubscriptions() {
    setLoading(true)
    try {
      const response = await fetchMySubscriptions()
      setData(response.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadSubscriptions()
  }, [])

  async function handleAddKeyword() {
    if (!keyword.trim()) {
      setMessage('请输入关键词')
      return
    }
    const response = await addMyKeyword(keyword.trim())
    setMessage(response.code === 0 ? '关键词已添加' : response.message || '关键词添加失败')
    if (response.code === 0) {
      setKeyword('')
      await loadSubscriptions()
    }
  }

  async function handleRemoveKeyword(item: string) {
    const response = await removeMyKeyword(item)
    setMessage(response.code === 0 ? '关键词已删除' : response.message || '关键词删除失败')
    if (response.code === 0) {
      await loadSubscriptions()
    }
  }

  async function handleAddRule() {
    if (!ruleValue.trim()) {
      setMessage('请输入规则值')
      return
    }
    const response = await addMyRule(ruleType, ruleValue.trim(), Number(ruleWeight) || 1)
    setMessage(response.code === 0 ? '规则已添加' : response.message || '规则添加失败')
    if (response.code === 0) {
      setRuleValue('')
      setRuleWeight('1')
      await loadSubscriptions()
    }
  }

  async function handleRemoveRule(itemRuleType: string, itemRuleValue: string) {
    const response = await removeMyRule(itemRuleType, itemRuleValue)
    setMessage(response.code === 0 ? '规则已删除' : response.message || '规则删除失败')
    if (response.code === 0) {
      await loadSubscriptions()
    }
  }

  async function handleAddSource() {
    if (!sourceValue.trim()) {
      setMessage('请输入订阅源值')
      return
    }
    const response = await addMySource(sourcePlatform, sourceType, sourceValue.trim())
    setMessage(response.code === 0 ? '订阅源已添加' : response.message || '订阅源添加失败')
    if (response.code === 0) {
      setSourceValue('')
      await loadSubscriptions()
    }
  }

  async function handleRemoveSource(id: number) {
    const response = await removeMySource(id)
    setMessage(response.code === 0 ? '订阅源已删除' : response.message || '订阅源删除失败')
    if (response.code === 0) {
      await loadSubscriptions()
    }
  }

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="section-kicker">SUBSCRIPTIONS</p>
        <h2>我的订阅</h2>
        <span>这一页已经接上真实订阅聚合结果，并支持直接维护关键词、规则和订阅源。</span>
      </div>

      <div className="toolbar">
        <span>{loading ? '正在刷新订阅数据...' : '你可以在这里直接维护订阅配置。'}</span>
        {message ? <p className="form-message">{message}</p> : null}
      </div>

      <div className="content-grid">
        <div className="panel">
          <h3>关键词订阅</h3>
          <div className="form-panel compact-form">
            <label>
              新增关键词
              <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="例如：agent" />
            </label>
            <button className="primary-button" type="button" onClick={handleAddKeyword}>
              添加关键词
            </button>
          </div>
          <div className="tag-list">
            {data?.keywords.length ? data.keywords.map((item) => (
              <button key={item} className="tag-action" type="button" onClick={() => handleRemoveKeyword(item)}>
                <span className="tag">{item}</span>
                <strong>删除</strong>
              </button>
            )) : <p className="empty-inline">暂无关键词订阅</p>}
          </div>
        </div>

        <div className="panel">
          <h3>订阅规则</h3>
          <div className="form-panel compact-form">
            <label>
              规则类型
              <select value={ruleType} onChange={(event) => setRuleType(event.target.value)}>
                <option value="keyword">keyword</option>
                <option value="author">author</option>
                <option value="platform">platform</option>
                <option value="content_type">content_type</option>
              </select>
            </label>
            <label>
              规则值
              <input value={ruleValue} onChange={(event) => setRuleValue(event.target.value)} placeholder="例如：openai" />
            </label>
            <label>
              权重
              <input value={ruleWeight} onChange={(event) => setRuleWeight(event.target.value)} placeholder="1" />
            </label>
            <button className="primary-button" type="button" onClick={handleAddRule}>
              添加规则
            </button>
          </div>
          <div className="stack-list">
            {data?.rules.length ? data.rules.map((rule) => (
              <article key={rule.id} className="stack-item">
                <strong>{rule.ruleType}</strong>
                <span>{rule.ruleValue}</span>
                <em>权重 {rule.ruleWeight}</em>
                <button
                  className="ghost-button inline-button"
                  type="button"
                  onClick={() => handleRemoveRule(rule.ruleType, rule.ruleValue)}
                >
                  删除
                </button>
              </article>
            )) : <p className="empty-inline">暂无规则</p>}
          </div>
        </div>

        <div className="panel">
          <h3>订阅源</h3>
          <div className="form-panel compact-form">
            <label>
              平台
              <select value={sourcePlatform} onChange={(event) => setSourcePlatform(event.target.value)}>
                <option value="youtube">youtube</option>
                <option value="github">github</option>
              </select>
            </label>
            <label>
              订阅类型
              <select value={sourceType} onChange={(event) => setSourceType(event.target.value)}>
                <option value="channel">channel</option>
                <option value="repo">repo</option>
                <option value="author">author</option>
              </select>
            </label>
            <label>
              订阅源值
              <input
                value={sourceValue}
                onChange={(event) => setSourceValue(event.target.value)}
                placeholder="例如：UC_x5XG1OV2P6uZZ5FSM9Ttw"
              />
            </label>
            <button className="primary-button" type="button" onClick={handleAddSource}>
              添加订阅源
            </button>
          </div>
          <div className="stack-list">
            {data?.sources.length ? data.sources.map((source) => (
              <article key={source.id} className="stack-item">
                <strong>{source.platform}</strong>
                <span>{source.sourceType}</span>
                <em>{source.sourceValue}</em>
                <button className="ghost-button inline-button" type="button" onClick={() => handleRemoveSource(source.id)}>
                  删除
                </button>
              </article>
            )) : <p className="empty-inline">暂无订阅源</p>}
          </div>
        </div>
      </div>
    </section>
  )
}
