import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { courseService } from '../../services/courseService'
import type { Course } from '../../types'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import toast from 'react-hot-toast'
import { Plus, Globe, BookOpen } from 'lucide-react'

export default function ManageCoursePage() {
  const { id } = useParams<{ id: string }>()
  const [course,  setCourse]  = useState<Course | null>(null)
  const [loading, setLoading] = useState(true)
  const [adding,  setAdding]  = useState(false)
  const [module,  setModule]  = useState({ title: '', content: '', videoUrl: '', orderIndex: 0 })

  useEffect(() => {
    courseService.getById(id!).then(setCourse).finally(() => setLoading(false))
  }, [id])

  const handlePublish = async () => {
    const updated = await courseService.publish(id!)
    setCourse(updated)
    toast.success('Course is now live!')
  }

  const handleAddModule = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!module.title.trim()) return
    setAdding(true)
    try {
      const updated = await courseService.create({ ...course, ...module } as any)
      // Re-fetch course to get updated module list
      const refreshed = await courseService.getById(id!)
      setCourse(refreshed)
      setModule({ title: '', content: '', videoUrl: '', orderIndex: course!.moduleCount + 1 })
      toast.success('Module added!')
    } catch {
      toast.error('Failed to add module')
    } finally {
      setAdding(false)
    }
  }

  if (loading) return <Spinner />
  if (!course)  return <div className="p-8 text-gray-500">Course not found</div>

  return (
    <div className="max-w-3xl mx-auto px-6 py-8">
      <PageHeader
        title={course.title}
        subtitle={`${course.category} · ${course.difficulty} · ${course.enrollmentCount} students`}
        action={
          !course.published && (
            <button onClick={handlePublish} className="btn-primary flex items-center gap-2">
              <Globe size={16} /> Publish
            </button>
          )
        }
      />

      {course.published && (
        <div className="mb-4 px-3 py-2 bg-green-50 border border-green-200 rounded-lg flex items-center gap-2 text-sm text-green-700">
          <Globe size={14} /> This course is live and visible to students
        </div>
      )}

      {/* Add module form */}
      <div className="card mb-6">
        <h2 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Plus size={16} className="text-primary-600" /> Add module
        </h2>
        <form onSubmit={handleAddModule} className="space-y-3">
          <input value={module.title} onChange={e => setModule(m => ({ ...m, title: e.target.value }))}
            className="input" placeholder="Module title *" required />
          <textarea value={module.content} onChange={e => setModule(m => ({ ...m, content: e.target.value }))}
            rows={2} className="input" placeholder="Module content / notes" />
          <div className="flex gap-3">
            <input value={module.videoUrl} onChange={e => setModule(m => ({ ...m, videoUrl: e.target.value }))}
              className="input flex-1" placeholder="Video URL (optional)" />
            <button type="submit" disabled={adding} className="btn-primary px-5">
              {adding ? 'Adding...' : 'Add'}
            </button>
          </div>
        </form>
      </div>

      {/* Module list */}
      <div className="card">
        <h2 className="font-semibold text-gray-900 mb-3">Modules ({course.moduleCount})</h2>
        {course.moduleCount === 0 ? (
          <div className="text-center py-6">
            <BookOpen size={32} className="mx-auto text-gray-300 mb-2" />
            <p className="text-sm text-gray-400">No modules yet — add your first module above</p>
          </div>
        ) : (
          <p className="text-sm text-gray-500">{course.moduleCount} module(s) added.</p>
        )}
      </div>
    </div>
  )
}