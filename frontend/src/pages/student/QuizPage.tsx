import { useState } from 'react'
import { aiService } from '../../services/aiService'
import type { QuizResponse } from '../../types'
import QuizQuestionComponent from '../../components/ai/QuizQuestion'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import toast from 'react-hot-toast'
import { Brain, RefreshCw } from 'lucide-react'
import { scoreColor } from '../../utils/roleGuard'

const TOPICS = [
  'Beta blockers', 'ACE inhibitors', 'Cardiac anatomy',
  'Pharmacokinetics', 'Diabetes management', 'Antibiotic resistance',
  'Renal physiology', 'CNS pharmacology', 'Respiratory physiology',
]

export default function QuizPage() {
  const [topic,      setTopic]      = useState('')
  const [difficulty, setDifficulty] = useState('INTERMEDIATE')
  const [count,      setCount]      = useState(5)
  const [quiz,       setQuiz]       = useState<QuizResponse | null>(null)
  const [answers,    setAnswers]    = useState<Record<number, number>>({})
  const [submitted,  setSubmitted]  = useState(false)
  const [loading,    setLoading]    = useState(false)

  const generate = async () => {
    if (!topic.trim()) { toast.error('Enter a topic first'); return }
    setLoading(true)
    setQuiz(null)
    setAnswers({})
    setSubmitted(false)
    try {
      const q = await aiService.generateQuiz(topic, difficulty, count)
      setQuiz(q)
    } catch {
      toast.error('Failed to generate quiz. Check your OpenAI key.')
    } finally {
      setLoading(false)
    }
  }

  const handleSelect = (questionNumber: number, optionIndex: number) => {
    if (submitted) return
    setAnswers(a => ({ ...a, [questionNumber]: optionIndex }))
  }

  const answeredCount = Object.keys(answers).length
  const totalQ        = quiz?.questions.length ?? 0
  const score         = quiz
    ? quiz.questions.filter(q => answers[q.questionNumber] === q.correctAnswerIndex).length
    : 0
  const pct = totalQ > 0 ? Math.round((score / totalQ) * 100) : 0

  return (
    <div className="max-w-3xl mx-auto px-6 py-8">
      <PageHeader title="AI Quiz Generator" subtitle="GPT-4 powered MCQs on any medical topic" />

      {/* Config */}
      <div className="card mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-4">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Topic</label>
            <input value={topic} onChange={e => setTopic(e.target.value)}
              placeholder="e.g. Beta blockers" className="input" list="topics" />
            <datalist id="topics">
              {TOPICS.map(t => <option key={t} value={t} />)}
            </datalist>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Difficulty</label>
            <select value={difficulty} onChange={e => setDifficulty(e.target.value)} className="input">
              <option value="BEGINNER">Beginner</option>
              <option value="INTERMEDIATE">Intermediate</option>
              <option value="ADVANCED">Advanced</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Questions</label>
            <select value={count} onChange={e => setCount(+e.target.value)} className="input">
              <option value={3}>3 questions</option>
              <option value={5}>5 questions</option>
              <option value={10}>10 questions</option>
            </select>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={generate} disabled={loading} className="btn-primary flex items-center gap-2">
            <Brain size={15} />
            {loading ? 'Generating...' : 'Generate quiz'}
          </button>
          {quiz && !loading && (
            <button onClick={generate} className="btn-secondary flex items-center gap-2 text-sm">
              <RefreshCw size={14} /> New quiz
            </button>
          )}
          {loading && <p className="text-xs text-gray-400">May take 5–15 seconds...</p>}
        </div>
      </div>

      {loading && <Spinner />}

      {!loading && quiz && (
        <div className="space-y-4">
          {/* Quiz header */}
          <div className="flex items-center justify-between">
            <div>
              <h2 className="font-semibold text-gray-900">{quiz.topic}</h2>
              <p className="text-xs text-gray-400 mt-0.5">
                {quiz.difficulty} · Generated in {(quiz.generatedInMs / 1000).toFixed(1)}s
              </p>
            </div>
            {!submitted && (
              <span className="text-sm text-gray-500">{answeredCount}/{totalQ} answered</span>
            )}
          </div>

          {/* Questions */}
          {quiz.questions.map(q => (
            <QuizQuestionComponent
              key={q.questionNumber}
              question={q}
              selected={answers[q.questionNumber]}
              submitted={submitted}
              onSelect={handleSelect}
            />
          ))}

          {/* Submit / Result */}
          {!submitted ? (
            <button
              onClick={() => setSubmitted(true)}
              disabled={answeredCount < totalQ}
              className="btn-primary w-full">
              Submit ({answeredCount}/{totalQ} answered)
            </button>
          ) : (
            <div className="card text-center">
              <div className={`inline-flex items-center justify-center w-20 h-20 rounded-full mb-3 text-3xl font-bold
                ${pct >= 80 ? 'bg-green-100 text-green-600' : pct >= 60 ? 'bg-yellow-100 text-yellow-600' : 'bg-red-100 text-red-600'}`}>
                {pct}%
              </div>
              <p className="text-xl font-bold text-gray-900">{score} / {totalQ} correct</p>
              <p className={`text-sm mt-1 ${scoreColor(pct)}`}>
                {pct === 100 ? 'Perfect score!' : pct >= 80 ? 'Excellent work!' : pct >= 60 ? 'Good effort!' : 'Keep practising!'}
              </p>
              <div className="flex gap-3 justify-center mt-4">
                <button onClick={() => { setSubmitted(false); setAnswers({}) }} className="btn-secondary">
                  Retry
                </button>
                <button onClick={() => { setQuiz(null); setAnswers({}); setSubmitted(false) }} className="btn-primary">
                  New quiz
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}