import { BookOpen, Users, ChevronRight } from 'lucide-react'
import type { Course } from '../../types'
import { difficultyColor } from '../../utils/roleGuard'

interface Props {
  course: Course
  enrolled?: boolean
  onEnroll?: (id: string) => void
  onView?: (id: string) => void
}

export default function CourseCard({ course, enrolled, onEnroll, onView }: Props) {
  return (
    <div className="card flex flex-col h-full">
      <div className="flex items-start justify-between mb-3">
        <div className="p-2 bg-blue-50 rounded-lg">
          <BookOpen size={20} className="text-blue-600" />
        </div>
        <span className={`text-xs font-medium px-2 py-1 rounded-full ${difficultyColor(course.difficulty)}`}>
          {course.difficulty}
        </span>
      </div>
      <h3 className="font-semibold text-gray-900 mb-1 line-clamp-2">{course.title}</h3>
      <p className="text-sm text-gray-500 flex-1 line-clamp-2 mb-3">{course.description}</p>
      <div className="flex items-center gap-3 text-xs text-gray-400 mb-4">
        <span className="flex items-center gap-1"><Users size={12} />{course.enrollmentCount}</span>
        <span className="bg-gray-100 px-2 py-0.5 rounded">{course.category}</span>
        <span>{course.moduleCount} modules</span>
      </div>
      <div className="flex gap-2">
        {enrolled ? (
          <button onClick={() => onView?.(course.id)}
            className="btn-primary flex-1 text-sm flex items-center justify-center gap-1">
            Continue <ChevronRight size={14} />
          </button>
        ) : (
          <button onClick={() => onEnroll?.(course.id)} className="btn-primary flex-1 text-sm">
            Enroll
          </button>
        )}
      </div>
    </div>
  )
}