import { useState } from 'react'
import { ChevronRight, Loader } from 'lucide-react'

interface Props {
  question: string
  questionNumber: number
  totalQuestions: number
  onSubmit: (answer: string) => Promise<void>
}

/**
 * Single interview question card with textarea + submit.
 * Manages its own answer state and loading.
 * Parent receives the answer via onSubmit callback.
 */
export default function QuestionCard({ question, questionNumber, totalQuestions, onSubmit }: Props) {
  const [answer,  setAnswer]  = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async () => {
    if (!answer.trim() || loading) return
    setLoading(true)
    try {
      await onSubmit(answer)
      // Don't clear answer — parent navigates away after eval
    } finally {
      setLoading(false)
    }
  }

  const wordCount = answer.trim().split(/\s+/).filter(Boolean).length
  const minWords  = 20
  const ready     = wordCount >= minWords

  return (
    <div className="card space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <span className="text-xs font-medium text-gray-400 uppercase tracking-wide">
          Question {questionNumber} of {totalQuestions}
        </span>
        <div className="flex gap-1">
          {Array.from({ length: totalQuestions }).map((_, i) => (
            <div key={i} className={`h-1.5 rounded-full transition-all ${
              i < questionNumber ? 'w-6 bg-primary-500' :
              i === questionNumber - 1 ? 'w-6 bg-primary-300' : 'w-3 bg-gray-200'
            }`} />
          ))}
        </div>
      </div>

      {/* Question text */}
      <p className="text-base font-medium text-gray-900 leading-relaxed">{question}</p>

      {/* Answer textarea */}
      <div>
        <textarea
          value={answer}
          onChange={e => setAnswer(e.target.value)}
          rows={5}
          placeholder="Type your answer here. Cover the mechanism, clinical relevance, and any exceptions you know..."
          className="input resize-none"
        />
        <div className="flex justify-between mt-1">
          <span className={`text-xs ${ready ? 'text-green-600' : 'text-gray-400'}`}>
            {wordCount} words {!ready && `(aim for ${minWords}+)`}
          </span>
          <span className="text-xs text-gray-300">{answer.length} chars</span>
        </div>
      </div>

      {/* Submit */}
      <button
        onClick={handleSubmit}
        disabled={!answer.trim() || loading}
        className="btn-primary w-full flex items-center justify-center gap-2">
        {loading
          ? <><Loader size={15} className="animate-spin" /> Evaluating with AI...</>
          : <><ChevronRight size={15} /> Submit answer</>
        }
      </button>

      {!ready && answer.length > 0 && (
        <p className="text-xs text-amber-600 text-center">
          Try to write a more complete answer for better AI feedback
        </p>
      )}
    </div>
  )
}