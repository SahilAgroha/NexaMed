interface CaseData { patientProfile:string; chiefComplaint:string; history:string; vitals:string[]; physicalExam:string[]; labResults:string[]; differentials:string[]; teachingPoints:string }
function Section({ title, items }: { title:string; items:string[] }) {
  if (!items?.length) return null
  return <div><h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1">{title}</h4><ul className="space-y-0.5">{items.map((item,i)=><li key={i} className="text-sm text-gray-700">• {item}</li>)}</ul></div>
}
export default function CaseViewer({ data }: { data: CaseData }) {
  return (
    <div className="card space-y-5">
      <div className="p-3 bg-blue-50 rounded-lg">
        <h4 className="text-xs font-semibold text-blue-600 uppercase tracking-wide mb-1">Patient</h4>
        <p className="text-sm text-gray-800">{data.patientProfile}</p>
        <p className="text-sm font-medium text-gray-900 mt-1">CC: {data.chiefComplaint}</p>
      </div>
      <div><h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1">History</h4><p className="text-sm text-gray-700">{data.history}</p></div>
      <Section title="Vitals" items={data.vitals} />
      <Section title="Physical exam" items={data.physicalExam} />
      <Section title="Lab results" items={data.labResults} />
      <div>
        <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1">Differentials</h4>
        {data.differentials?.map((d,i)=><div key={i} className={`text-sm px-3 py-1.5 rounded-lg mb-1 ${i===0?'bg-green-50 text-green-800':'bg-gray-50 text-gray-700'}`}>{d}</div>)}
      </div>
      <div className="p-3 bg-amber-50 rounded-lg"><h4 className="text-xs font-semibold text-amber-700 uppercase tracking-wide mb-1">Teaching points</h4><p className="text-sm text-amber-900">{data.teachingPoints}</p></div>
    </div>
  )
}
