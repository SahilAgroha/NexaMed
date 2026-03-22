import type { ActivitySummary } from '../../types'
import { timeAgo } from '../../utils/formatDate'
const typeIcon: Record<string,string> = { ENROLLMENT:'📚', INTERVIEW_COMPLETED:'🎤', QUIZ_SUBMITTED:'🧠', COURSE_COMPLETED:'🏆' }
export default function ActivityFeed({ items }: { items: ActivitySummary[] }) {
  if (!items.length) return <p className="text-sm text-gray-400 text-center py-4">No activity yet</p>
  return (
    <div className="space-y-0">
      {items.map((a, i) => (
        <div key={i} className="flex items-center justify-between py-2.5 border-b border-gray-50 last:border-0">
          <div className="flex items-center gap-3">
            <span className="text-lg">{typeIcon[a.type] ?? '🔔'}</span>
            <div>
              <p className="text-sm text-gray-900">{a.description}</p>
              <p className="text-xs text-gray-400">{timeAgo(a.occurredAt)}</p>
            </div>
          </div>
          {a.score != null && <span className="text-sm font-semibold text-green-600">{a.score}%</span>}
        </div>
      ))}
    </div>
  )
}