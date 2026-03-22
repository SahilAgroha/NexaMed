interface Props { label: string; value: string | number; icon: React.ReactNode; iconBg: string }
export default function StatCard({ label, value, icon, iconBg }: Props) {
  return (
    <div className="card text-center">
      <div className={`inline-flex p-2 rounded-lg mb-2 ${iconBg}`}>{icon}</div>
      <p className="text-2xl font-bold text-gray-900">{value}</p>
      <p className="text-xs text-gray-500 mt-1">{label}</p>
    </div>
  )
}