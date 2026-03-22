import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import { analyticsService } from '../../services/analyticsService'
import type { StudentDashboard } from '../../types'
import StatCard from '../../components/analytics/StatCard'
import ActivityFeed from '../../components/analytics/ActivityFeed'
import Spinner from '../../components/common/Spinner'
import { BookOpen, Mic, Brain, TrendingUp, Award, Flame, Stethoscope } from 'lucide-react'

export default function Dashboard() {
  const { user } = useAuthStore()
  const [data,    setData]    = useState<StudentDashboard | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    analyticsService.getDashboard()
      .then(setData).catch(() => {}).finally(() => setLoading(false))
  }, [])

  const stats = [
    { label: 'Enrolled',      value: data?.totalEnrollments      ?? 0,   icon: <BookOpen size={18} />,    iconBg: 'text-blue-600 bg-blue-50'   },
    { label: 'Interviews',    value: data?.totalInterviews        ?? 0,   icon: <Mic size={18} />,         iconBg: 'text-purple-600 bg-purple-50'},
    { label: 'Avg score',     value: `${data?.averageInterviewScore ?? 0}%`, icon: <TrendingUp size={18} />, iconBg: 'text-green-600 bg-green-50' },
    { label: 'Best score',    value: `${data?.bestInterviewScore  ?? 0}%`, icon: <Award size={18} />,      iconBg: 'text-amber-600 bg-amber-50' },
    { label: 'Quizzes',       value: data?.totalQuizzesTaken      ?? 0,   icon: <Brain size={18} />,       iconBg: 'text-pink-600 bg-pink-50'   },
    { label: 'Day streak',    value: data?.streakDays             ?? 0,   icon: <Flame size={18} />,       iconBg: 'text-orange-600 bg-orange-50'},
  ]

  const quickActions = [
    { to: '/courses',    icon: <BookOpen size={22} className="text-blue-600" />,    bg: 'bg-blue-50',    title: 'Browse courses',   sub: 'Explore medical topics'     },
    { to: '/interviews', icon: <Mic size={22} className="text-purple-600" />,       bg: 'bg-purple-50',  title: 'Mock interview',    sub: 'Practice with AI'           },
    { to: '/quiz',       icon: <Brain size={22} className="text-pink-600" />,        bg: 'bg-pink-50',    title: 'Generate quiz',     sub: 'AI-powered MCQs'            },
    { to: '/cases',      icon: <Stethoscope size={22} className="text-teal-600" />, bg: 'bg-teal-50',    title: 'Case simulator',    sub: 'Clinical case practice'     },
  ]

  return (
    <div className="max-w-6xl mx-auto px-6 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Welcome back, {user?.fullName} 👋</h1>
        <p className="text-gray-500 mt-1 text-sm">Here's your learning progress</p>
      </div>

      {loading ? <Spinner /> : (
        <>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
            {stats.map(s => <StatCard key={s.label} {...s} />)}
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
            {quickActions.map(a => (
              <Link key={a.to} to={a.to}
                className="card hover:shadow-md transition-shadow flex items-center gap-3 cursor-pointer">
                <div className={`p-2.5 ${a.bg} rounded-xl flex-shrink-0`}>{a.icon}</div>
                <div>
                  <p className="font-medium text-gray-900 text-sm">{a.title}</p>
                  <p className="text-xs text-gray-500">{a.sub}</p>
                </div>
              </Link>
            ))}
          </div>

          {!!data?.recentActivity?.length && (
            <div className="card">
              <h2 className="font-semibold text-gray-900 mb-4">Recent activity</h2>
              <ActivityFeed items={data.recentActivity.slice(0, 8)} />
            </div>
          )}
        </>
      )}
    </div>
  )
}