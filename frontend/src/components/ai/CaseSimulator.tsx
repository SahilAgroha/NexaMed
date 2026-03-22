import { useState } from 'react'
import { aiService } from '../../services/aiService'
import CaseViewer from './CaseViewer'
import Spinner from '../common/Spinner'
import toast from 'react-hot-toast'
import { Stethoscope, RefreshCw } from 'lucide-react'

const SPECIALTIES = ['Cardiology','Emergency Medicine','Internal Medicine','Neurology','Pediatrics','Psychiatry','Surgery','Obstetrics']
const DIFFICULTIES = ['BEGINNER','INTERMEDIATE','ADVANCED']

interface Props {
  defaultSpecialty?: string
  onCaseGenerated?: (data: unknown) => void
}

/**
 * Self-contained case simulator widget.
 * Can be dropped into any page — handles its own state.
 * Used inside CaseSimPage, but also embeddable in course pages.
 */
export default function CaseSimulator({ defaultSpecialty = 'Cardiology', onCaseGenerated }: Props) {
  const [specialty,  setSpecialty]  = useState(defaultSpecialty)
  const [difficulty, setDifficulty] = useState('INTERMEDIATE')
  const [age,        setAge]        = useState('')
  const [complaint,  setComplaint]  = useState('')
  const [caseData,   setCaseData]   = useState<any>(null)
  const [loading,    setLoading]    = useState(false)

  const generate = async () => {
    setLoading(true)
    setCaseData(null)
    try {
      const data = await aiService.generateCase(specialty, difficulty)
      setCaseData(data)
      onCaseGenerated?.(data)
    } catch {
      toast.error('Failed to generate case. Check your OpenAI key.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-4">
      {/* Config row */}
      <div className="card">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Specialty</label>
            <select value={specialty} onChange={e => setSpecialty(e.target.value)} className="input">
              {SPECIALTIES.map(s => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Difficulty</label>
            <select value={difficulty} onChange={e => setDifficulty(e.target.value)} className="input">
              {DIFFICULTIES.map(d => <option key={d}>{d}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Patient age</label>
            <input value={age} onChange={e => setAge(e.target.value)}
              className="input" placeholder="e.g. 55-year-old male" />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Chief complaint</label>
            <input value={complaint} onChange={e => setComplaint(e.target.value)}
              className="input" placeholder="e.g. chest pain" />
          </div>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={generate} disabled={loading} className="btn-primary flex items-center gap-2">
            <Stethoscope size={15} />
            {loading ? 'Generating...' : 'Generate case'}
          </button>
          {caseData && (
            <button onClick={generate} disabled={loading}
              className="btn-secondary flex items-center gap-2 text-sm">
              <RefreshCw size={14} /> New case
            </button>
          )}
          {loading && <p className="text-xs text-gray-400">May take 5–15 seconds...</p>}
        </div>
      </div>

      {loading && <Spinner />}
      {!loading && caseData && <CaseViewer data={caseData} />}
    </div>
  )
}