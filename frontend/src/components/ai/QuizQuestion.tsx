import type { QuizQuestion as QuizQuestionType } from '../../types'
import { CheckCircle, XCircle } from 'lucide-react'

interface Props {
  question: QuizQuestionType
  selected: number | undefined        // index the user picked
  submitted: boolean
  onSelect: (questionNumber: number, optionIndex: number) => void
}

/**
 * Single MCQ question card.
 * Shows option buttons — highlights correct/wrong after submit.
 * Renders explanation block after submission.
 */
export default function QuizQuestion({ question, selected, submitted, onSelect }: Props) {
  return (
    <div className="card">
      <p className="font-medium text-gray-900 mb-3">
        <span className="text-primary-600 mr-2">{question.questionNumber}.</span>
        {question.question}
      </p>

      <div className="space-y-2 mb-3">
        {question.options.map((opt, i) => {
          const isSelected = selected === i
          const isCorrect  = i === question.correctAnswerIndex

          const base   = 'w-full text-left px-4 py-2.5 rounded-lg border text-sm transition-all'
          const style  = submitted
            ? isCorrect
              ? `${base} bg-green-50 border-green-400 text-green-800 font-medium`
              : isSelected
                ? `${base} bg-red-50 border-red-400 text-red-800`
                : `${base} border-gray-200 text-gray-500 opacity-60`
            : isSelected
              ? `${base} bg-primary-50 border-primary-400 text-primary-800`
              : `${base} border-gray-200 hover:bg-gray-50 hover:border-gray-300`

          return (
            <button key={i} disabled={submitted} onClick={() => onSelect(question.questionNumber, i)}
              className={style}>
              <span className="font-medium mr-2">{String.fromCharCode(65 + i)}.</span>
              {opt}
              {submitted && isCorrect && (
                <CheckCircle size={14} className="inline ml-2 text-green-600" />
              )}
              {submitted && isSelected && !isCorrect && (
                <XCircle size={14} className="inline ml-2 text-red-500" />
              )}
            </button>
          )
        })}
      </div>

      {submitted && (
        <div className="p-3 bg-blue-50 rounded-lg">
          <p className="text-xs font-medium text-blue-700 mb-1">Explanation</p>
          <p className="text-sm text-blue-800">{question.explanation}</p>
        </div>
      )}
    </div>
  )
}