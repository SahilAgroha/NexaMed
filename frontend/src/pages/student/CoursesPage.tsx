import { useEffect, useState } from 'react'
import { courseService } from '../../services/courseService'
import type { Course, Enrollment } from '../../types'
import CourseCard from '../../components/course/CourseCard'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import toast from 'react-hot-toast'
import { Search } from 'lucide-react'
// import PageHeader from '@/components/common/PageHeader'

const CATEGORIES = ['All','ANATOMY','PHYSIOLOGY','PHARMACOLOGY','PATHOLOGY','CLINICAL','SURGERY','PSYCHIATRY','PEDIATRICS','GENERAL']

export default function CoursesPage() {
  const [courses,     setCourses]     = useState<Course[]>([])
  const [enrollments, setEnrollments] = useState<Enrollment[]>([])
  const [search,      setSearch]      = useState('')
  const [category,    setCategory]    = useState('All')
  const [loading,     setLoading]     = useState(true)

  useEffect(() => {
    Promise.all([courseService.getAll(), courseService.getMyEnrollments()])
      .then(([c, e]) => { setCourses(c); setEnrollments(e) })
      .finally(() => setLoading(false))
  }, [])

  const enrolledIds = new Set(enrollments.map(e => e.courseId))

  const filtered = courses.filter(c =>
    (category === 'All' || c.category === category) &&
    (c.title.toLowerCase().includes(search.toLowerCase()) ||
     c.description?.toLowerCase().includes(search.toLowerCase()))
  )

  const handleEnroll = async (courseId: string) => {
    try {
      const e = await courseService.enroll(courseId)
      setEnrollments(prev => [...prev, e])
      toast.success('Enrolled successfully!')
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Enrollment failed')
    }
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-8">
      <PageHeader title="Courses" subtitle={`${filtered.length} courses available`} />

      {/* Filters */}
      <div className="flex flex-col md:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input value={search} onChange={e => setSearch(e.target.value)}
            placeholder="Search courses..." className="input pl-9" />
        </div>
        <div className="flex gap-2 flex-wrap">
          {CATEGORIES.map(c => (
            <button key={c} onClick={() => setCategory(c)}
              className={`text-xs px-3 py-1.5 rounded-full border transition-colors ${
                category === c
                  ? 'bg-primary-600 text-white border-primary-600'
                  : 'border-gray-300 text-gray-600 hover:border-primary-400'}`}>
              {c}
            </button>
          ))}
        </div>
      </div>

      {loading ? <Spinner /> : filtered.length === 0 ? (
        <div className="text-center py-12 text-gray-400">No courses found</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filtered.map(course => (
            <CourseCard
              key={course.id}
              course={course}
              enrolled={enrolledIds.has(course.id)}
              onEnroll={handleEnroll}
            />
          ))}
        </div>
      )}
    </div>
  )
}