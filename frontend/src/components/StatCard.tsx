interface StatCardProps {
  label: string
  value: number | string
  hint: string
}

export function StatCard({ label, value, hint }: StatCardProps) {
  return (
    <article className="stat-card">
      <span className="stat-label">{label}</span>
      <strong className="stat-value">{value}</strong>
      <p className="stat-hint">{hint}</p>
    </article>
  )
}
