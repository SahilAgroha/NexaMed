import { scoreColor } from '../../utils/roleGuard'
interface EvalResult { overallScore:number; clarityScore:number; accuracyScore:number; completenessScore?:number; strengths?:string[]; improvements?:string[]; detailedFeedback?:string; modelAnswer?:string }
export default function ScoreCard({ result }: { result: EvalResult }) {
  const metrics = [{ label:'Overall', score:result.overallScore },{ label:'Clarity', score:result.clarityScore },{ label:'Accuracy', score:result.accuracyScore },{ label:'Complete', score:result.completenessScore??0 }]
  return (
    <div className="card space-y-4">
      <div className="grid grid-cols-4 gap-3">
        {metrics.map(m => (
          <div key={m.label} className="text-center p-3 bg-gray-50 rounded-lg">
            <p className={`text-2xl font-bold ${scoreColor(m.score)}`}>{m.score}</p>
            <p className="text-xs text-gray-500 mt-0.5">{m.label}</p>
          </div>
        ))}
      </div>
      {!!result.strengths?.length && <div><p className="text-sm font-medium text-green-700 mb-1">Strengths</p>{result.strengths.map((s,i)=><p key={i} className="text-sm text-gray-600">• {s}</p>)}</div>}
      {!!result.improvements?.length && <div><p className="text-sm font-medium text-amber-700 mb-1">Improvements</p>{result.improvements.map((s,i)=><p key={i} className="text-sm text-gray-600">• {s}</p>)}</div>}
      {result.modelAnswer && <div className="p-3 bg-blue-50 rounded-lg"><p className="text-xs font-medium text-blue-700 mb-1">Model answer</p><p className="text-sm text-blue-800">{result.modelAnswer}</p></div>}
    </div>
  )
}