import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { interviewService } from '../../services/interviewService'
import type { InterviewSession } from '../../types'
import ScoreCard from '../../components/interview/ScoreCard'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import toast from 'react-hot-toast'
import { Mic, ChevronRight, Trophy, Clock } from 'lucide-react'
import { scoreColor } from '../../utils/roleGuard'
import { formatDate } from '../../utils/formatDate'

const SPECIALTIES = ['Cardiology','General Medicine','Pharmacology','Surgery','Psychiatry','Pediatrics','Neurology','Emergency Medicine']

export default function InterviewsPage() {
  const navigate = useNavigate()
  const [sessions,       setSessions]       = useState<InterviewSession[]>([])
  const [activeSession,  setActiveSession]  = useState<InterviewSession | null>(null)
  const [questions,      setQuestions]      = useState<any[]>([])
  const [currentQ,       setCurrentQ]       = useState<any>(null)
  const [answer,         setAnswer]         = useState('')
  const [evalResult,     setEvalResult]     = useState<any>(null)
  const [specialty,      setSpecialty]      = useState('Cardiology')
  const [loading,        setLoading]        = useState(false)
  const [historyLoading, setHistoryLoading] = useState(true)

  useEffect(() => {
    interviewService.getHistory()
      .then(setSessions).catch(() => {}).finally(() => setHistoryLoading(false))
  }, [])

  const startInterview = async () => {
    setLoading(true)
    try {
      const session = await interviewService.startMock(specialty)

      // Retry up to 3 times — questions are saved in a separate transaction
      // and may not be immediately visible right after startMock returns
      let qs: any[] = []
      for (let attempt = 0; attempt < 3; attempt++) {
        if (attempt > 0) await new Promise(r => setTimeout(r, 800))
        const raw = await interviewService.getQuestions(session.id)
        qs = Array.isArray(raw) ? raw : []
        if (qs.length > 0) break
      }

      // Fallback: if backend returned no questions, use client-side defaults
      if (qs.length === 0) {
        const fallbacks: Record<string, string[]> = {
          'Cardiology': [
            'Explain the mechanism of action of beta blockers and their clinical uses.',
            'How would you manage a patient presenting with acute STEMI?',
            'Describe the pathophysiology of atrial fibrillation.',
          ],
          'General Medicine': [
            'How do you approach a patient presenting with unexplained weight loss?',
            'Describe the management of a patient with type 2 diabetes.',
            'What are the red flag symptoms in a patient with headache?',
          ],
          'Pharmacology': [
            'Explain the difference between pharmacokinetics and pharmacodynamics.',
            'How do ACE inhibitors work and when are they contraindicated?',
            'Describe the adverse effects of long-term corticosteroid use.',
          ],
        }
        const bank = fallbacks[specialty] ?? fallbacks['General Medicine']
        qs = bank.map((q, i) => ({
          id: `fallback-${i}`,
          question: q,
          questionOrder: i,
          studentAnswer: null,
          questionScore: null,
          aiFeedback: null,
        }))
      }

      setActiveSession(session)
      setQuestions(qs)
      setCurrentQ(qs[0] ?? null)
      setEvalResult(null)
      setAnswer('')
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Failed to start interview')
    } finally {
      setLoading(false)
    }
  }

  const submitAnswer = async () => {
    if (!activeSession || !answer.trim()) return
    setLoading(true)
    try {
      const result = await interviewService.submitAnswer(activeSession.id, answer)
      setEvalResult(result)
      setAnswer('')
    } catch {
      toast.error('Failed to submit answer')
    } finally {
      setLoading(false)
    }
  }

  const nextQuestion = () => {
    const next = questions.find(q => q.questionOrder > currentQ.questionOrder)
    if (next) { setCurrentQ(next); setEvalResult(null) }
  }

  const completeSession = async () => {
    if (!activeSession) return
    const completed = await interviewService.complete(activeSession.id)
    setSessions(prev => [completed, ...prev])
    setActiveSession(null)
    setQuestions([])
    setCurrentQ(null)
    setEvalResult(null)
    toast.success(`Interview complete! Score: ${completed.overallScore}%`)
  }

  const safeQuestions = Array.isArray(questions) ? questions : []
  const hasNextQ = currentQ && safeQuestions.some(q => q.questionOrder > currentQ.questionOrder)

  return (
    <div className="max-w-4xl mx-auto px-6 py-8">
      {!activeSession ? (
        <>
          <PageHeader title="AI Mock Interviews" subtitle="Practice with AI — get instant feedback" />

          {/* Start panel */}
          <div className="card mb-6">
            <h2 className="font-semibold text-gray-900 mb-3">Start a new session</h2>
            <div className="flex gap-3">
              <select value={specialty} onChange={e => setSpecialty(e.target.value)} className="input flex-1">
                {SPECIALTIES.map(s => <option key={s}>{s}</option>)}
              </select>
              <button onClick={startInterview} disabled={loading} className="btn-primary flex items-center gap-2 px-5">
                <Mic size={16} />{loading ? 'Starting...' : 'Start'}
              </button>
            </div>
          </div>

          {/* History */}
          <div className="card">
            <h2 className="font-semibold text-gray-900 mb-4">Session history</h2>
            {historyLoading ? <Spinner size="sm" /> : sessions.length === 0 ? (
              <p className="text-sm text-gray-400 text-center py-6">No sessions yet — start your first interview above</p>
            ) : (
              <div className="space-y-2">
                {sessions.map(s => (
                  <div key={s.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className="p-2 bg-purple-50 rounded-lg"><Mic size={15} className="text-purple-600" /></div>
                      <div>
                        <p className="font-medium text-gray-900 text-sm">{s.specialty}</p>
                        <p className="text-xs text-gray-400 flex items-center gap-1">
                          <Clock size={11} /> {formatDate(s.createdAt)}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      {s.overallScore != null && (
                        <span className={`font-bold text-sm ${scoreColor(s.overallScore)}`}>
                          {s.overallScore}%
                        </span>
                      )}
                      <span className={`text-xs px-2 py-0.5 rounded-full ${
                        s.status === 'COMPLETED' ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'}`}>
                        {s.status}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      ) : (
        /* ── Active session ──────────────────────────────────── */
        <div>
          <div className="flex items-center justify-between mb-5">
            <div>
              <h1 className="text-xl font-bold text-gray-900">{activeSession.specialty} Interview</h1>
              <p className="text-sm text-gray-500 mt-0.5 flex items-center gap-2">
                <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse inline-block" />
                In progress · Q{(currentQ?.questionOrder ?? 0) + 1} of {safeQuestions.length || '...'}
              </p>
            </div>
            <button onClick={completeSession} className="btn-secondary text-sm">End session</button>
          </div>

          {/* Progress bar */}
          <div className="w-full bg-gray-100 rounded-full h-1.5 mb-5">
            <div className="bg-primary-600 h-1.5 rounded-full transition-all"
              style={{ width: safeQuestions.length ? `${((currentQ?.questionOrder ?? 0) / safeQuestions.length) * 100}%` : '0%' }} />
          </div>

          {currentQ && !evalResult && (
            <div className="card">
              <p className="text-xs text-gray-400 mb-2">Question {currentQ.questionOrder + 1}</p>
              <p className="text-lg font-medium text-gray-900 mb-4">{currentQ.question}</p>
              <textarea value={answer} onChange={e => setAnswer(e.target.value)}
                rows={4} className="input mb-3" placeholder="Type your answer here..." />
              <button onClick={submitAnswer}
                disabled={loading || !answer.trim()}
                className="btn-primary flex items-center gap-2">
                <ChevronRight size={16} />
                {loading ? 'Evaluating with AI...' : 'Submit answer'}
              </button>
            </div>
          )}

          {evalResult && (
            <div className="space-y-3">
              <div className="flex items-center gap-2 mb-1">
                <Trophy size={18} className="text-amber-500" />
                <h3 className="font-semibold text-gray-900">AI Feedback</h3>
              </div>
              <ScoreCard result={evalResult} />
              <div className="flex gap-3">
                {hasNextQ ? (
                  <button className="btn-primary flex-1" onClick={nextQuestion}>
                    Next question →
                  </button>
                ) : (
                  <button className="btn-primary flex-1" onClick={completeSession}>
                    Complete interview
                  </button>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}