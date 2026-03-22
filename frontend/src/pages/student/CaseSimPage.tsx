import CaseSimulator from '../../components/ai/CaseSimulator'
import PageHeader from '../../components/common/PageHeader'

export default function CaseSimPage() {
  return (
    <div className="max-w-3xl mx-auto px-6 py-8">
      <PageHeader
        title="Clinical Case Simulator"
        subtitle="AI-generated patient cases for diagnostic practice"
      />
      <CaseSimulator />
    </div>
  )
}