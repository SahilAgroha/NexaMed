import { useParams, useNavigate } from 'react-router-dom'
import LiveVideoRoom from '../../components/interview/LiveVideoRoom'
import PageHeader from '../../components/common/PageHeader'

export default function LiveInterviewPage() {
  const { roomId } = useParams<{ roomId: string }>()
  const navigate   = useNavigate()

  if (!roomId) return <div className="p-8 text-gray-500">Invalid room ID</div>

  return (
    <div className="max-w-5xl mx-auto px-6 py-8">
      <PageHeader
        title="Live Interview"
        subtitle={`Room: ${roomId}`}
      />
      <LiveVideoRoom
        roomId={roomId}
        onEnd={() => navigate('/interviews')}
      />
    </div>
  )
}