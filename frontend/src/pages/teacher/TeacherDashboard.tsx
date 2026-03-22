import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { courseService } from '../../services/courseService'
import type { Course } from '../../types'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import { Plus, BookOpen, Users, Eye, Globe } from 'lucide-react'
import { formatDate } from '../../utils/formatDate'
import { difficultyColor } from '../../utils/roleGuard'

export default function TeacherDashboard() {
  const [courses, setCourses] = useState<Course[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    courseService.getMyCourses().then(setCourses).finally(() => setLoading(false))
  }, [])

  const handlePublish = async (id: string) => {
    const updated = await courseService.publish(id)
    setCourses(c => c.map(x => x.id === id ? updated : x))
  }

  const published = courses.filter(c => c.published).length
  const total     = courses.length
  const enrolled  = courses.reduce((s, c) => s + c.enrollmentCount, 0)

  return (
    <div className="max-w-5xl mx-auto px-6 py-8">
      <PageHeader
        title="Teacher Dashboard"
        subtitle="Manage your courses and students"
        action={
          <Link to="/teacher/courses/new" className="btn-primary flex items-center gap-2">
            <Plus size={16} /> New Course
          </Link>
        }
      />

      {/* Summary cards */}
      <div className="grid grid-cols-3 gap-4 mb-8">
        {[
          { label: 'Total courses', value: total, icon: <BookOpen size={20} />, bg: 'text-blue-600 bg-blue-50' },
          { label: 'Published',     value: published, icon: <Globe size={20} />, bg: 'text-green-600 bg-green-50' },
          { label: 'Total students', value: enrolled, icon: <Users size={20} />, bg: 'text-purple-600 bg-purple-50' },
        ].map(s => (
          <div key={s.label} className="card text-center">
            <div className={`inline-flex p-2 rounded-lg mb-2 ${s.bg}`}>{s.icon}</div>
            <p className="text-3xl font-bold text-gray-900">{s.value}</p>
            <p className="text-xs text-gray-500 mt-1">{s.label}</p>
          </div>
        ))}
      </div>

      {loading ? <Spinner /> : courses.length === 0 ? (
        <div className="card text-center py-12">
          <BookOpen size={40} className="mx-auto text-gray-300 mb-3" />
          <p className="text-gray-500 mb-4">You haven't created any courses yet</p>
          <Link to="/teacher/courses/new" className="btn-primary">Create your first course</Link>
        </div>
      ) : (
        <div className="card">
          <h2 className="font-semibold text-gray-900 mb-4">Your courses</h2>
          <div className="space-y-3">
            {courses.map(c => (
              <div key={c.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className="p-2 bg-white rounded-lg border border-gray-200">
                    <BookOpen size={16} className="text-blue-600" />
                  </div>
                  <div className="min-w-0">
                    <p className="font-medium text-gray-900 truncate">{c.title}</p>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className={`text-xs px-2 py-0.5 rounded-full ${difficultyColor(c.difficulty)}`}>
                        {c.difficulty}
                      </span>
                      <span className="text-xs text-gray-400">{c.category}</span>
                      <span className="text-xs text-gray-400">{formatDate(c.createdAt)}</span>
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-4 ml-4 flex-shrink-0">
                  <span className="flex items-center gap-1 text-sm text-gray-500">
                    <Users size={14} /> {c.enrollmentCount}
                  </span>
                  {c.published ? (
                    <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full flex items-center gap-1">
                      <Globe size={11} /> Live
                    </span>
                  ) : (
                    <button onClick={() => handlePublish(c.id)}
                      className="text-xs btn-secondary px-3 py-1 flex items-center gap-1">
                      <Eye size={11} /> Publish
                    </button>
                  )}
                  <Link to={`/teacher/courses/${c.id}`} className="text-xs text-primary-600 hover:underline">
                    Manage
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}