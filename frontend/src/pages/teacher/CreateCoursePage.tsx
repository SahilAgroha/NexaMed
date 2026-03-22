import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { courseService } from '../../services/courseService'
import PageHeader from '../../components/common/PageHeader'
import toast from 'react-hot-toast'

// ✅ Make constants readonly (IMPORTANT)
const CATEGORIES = [
  'ANATOMY','PHYSIOLOGY','PHARMACOLOGY','PATHOLOGY',
  'CLINICAL','SURGERY','PSYCHIATRY','PEDIATRICS','GENERAL'
] as const

const DIFFICULTIES = ['BEGINNER','INTERMEDIATE','ADVANCED'] as const

// ✅ Derive types
type Category = typeof CATEGORIES[number]
type Difficulty = typeof DIFFICULTIES[number]

// ✅ Form type
type FormState = {
  title: string
  description: string
  difficulty: Difficulty
  category: Category
  thumbnailUrl: string
}

export default function CreateCoursePage() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)

  // ✅ Strongly typed state
  const [form, setForm] = useState<FormState>({
    title: '',
    description: '',
    difficulty: 'BEGINNER',
    category: 'GENERAL',
    thumbnailUrl: '',
  })

  // ✅ Type-safe setter
  const set = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setForm(prev => ({ ...prev, [key]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!form.title.trim()) {
      toast.error('Title is required')
      return
    }

    setLoading(true)
    try {
      const course = await courseService.create(form)
      toast.success('Course created!')
      navigate(`/teacher/courses/${course.id}`)
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Failed to create course')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto px-6 py-8">
      <PageHeader title="Create new course" subtitle="Fill in the details below" />

      <div className="card">
        <form onSubmit={handleSubmit} className="space-y-5">

          {/* Title */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Course title *
            </label>
            <input
              value={form.title}
              onChange={e => set('title', e.target.value)}
              className="input"
              placeholder="e.g. Pharmacology Fundamentals"
              required
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              value={form.description}
              onChange={e => set('description', e.target.value)}
              rows={3}
              className="input"
              placeholder="What will students learn?"
            />
          </div>

          {/* Difficulty + Category */}
          <div className="grid grid-cols-2 gap-4">

            {/* Difficulty */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Difficulty
              </label>
              <select
                value={form.difficulty}
                onChange={e => set('difficulty', e.target.value as Difficulty)}
                className="input"
              >
                {DIFFICULTIES.map(d => (
                  <option key={d} value={d}>{d}</option>
                ))}
              </select>
            </div>

            {/* Category */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Category
              </label>
              <select
                value={form.category}
                onChange={e => set('category', e.target.value as Category)}
                className="input"
              >
                {CATEGORIES.map(c => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>

          </div>

          {/* Thumbnail */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Thumbnail URL (optional)
            </label>
            <input
              value={form.thumbnailUrl}
              onChange={e => set('thumbnailUrl', e.target.value)}
              className="input"
              placeholder="https://..."
            />
          </div>

          {/* Buttons */}
          <div className="flex gap-3 pt-2">
            <button
              type="submit"
              disabled={loading}
              className="btn-primary flex-1"
            >
              {loading ? 'Creating...' : 'Create Course'}
            </button>

            <button
              type="button"
              onClick={() => navigate(-1)}
              className="btn-secondary px-6"
            >
              Cancel
            </button>
          </div>

        </form>
      </div>
    </div>
  )
}