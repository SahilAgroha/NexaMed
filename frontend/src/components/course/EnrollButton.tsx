import { useState } from 'react'
import { courseService } from '../../services/courseService'
import toast from 'react-hot-toast'
import { CheckCircle, Loader } from 'lucide-react'

interface Props {
  courseId: string
  enrolled: boolean
  onEnrolled?: () => void
  size?: 'sm' | 'md'
}

/**
 * Smart enroll button — handles its own loading and enrolled state.
 * Shows "Enrolled" badge after success.
 */
export default function EnrollButton({ courseId, enrolled: initialEnrolled, onEnrolled, size = 'md' }: Props) {
  const [enrolled, setEnrolled] = useState(initialEnrolled)
  const [loading,  setLoading]  = useState(false)

  const handleEnroll = async () => {
    if (enrolled) return
    setLoading(true)
    try {
      await courseService.enroll(courseId)
      setEnrolled(true)
      toast.success('Successfully enrolled!')
      onEnrolled?.()
    } catch (err: any) {
      const msg = err.response?.data?.error || 'Enrollment failed'
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  const textSize = size === 'sm' ? 'text-xs px-3 py-1.5' : 'text-sm px-4 py-2'

  if (enrolled) {
    return (
      <div className={`flex items-center justify-center gap-1.5 ${textSize} bg-green-50 text-green-700 font-medium rounded-lg border border-green-200`}>
        <CheckCircle size={size === 'sm' ? 13 : 15} />
        Enrolled
      </div>
    )
  }

  return (
    <button onClick={handleEnroll} disabled={loading}
      className={`btn-primary flex items-center justify-center gap-1.5 w-full ${textSize}`}>
      {loading ? <Loader size={14} className="animate-spin" /> : null}
      {loading ? 'Enrolling...' : 'Enroll'}
    </button>
  )
}