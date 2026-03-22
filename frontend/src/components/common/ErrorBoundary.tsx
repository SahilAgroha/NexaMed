import { Component, type ReactNode } from 'react'
import { AlertTriangle } from 'lucide-react'

interface Props   { children: ReactNode; fallback?: ReactNode }
interface State   { hasError: boolean; message: string }

/**
 * Catches render errors anywhere in the subtree.
 * Wrap around route-level pages in App.tsx for graceful recovery.
 *
 * Usage:
 *   <ErrorBoundary>
 *     <MyPage />
 *   </ErrorBoundary>
 */
export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false, message: '' }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, message: error.message }
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    console.error('[ErrorBoundary]', error, info.componentStack)
  }

  reset = () => this.setState({ hasError: false, message: '' })

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) return this.props.fallback

      return (
        <div className="flex flex-col items-center justify-center min-h-[60vh] px-6 text-center">
          <div className="p-4 bg-red-50 rounded-full mb-4">
            <AlertTriangle size={32} className="text-red-500" />
          </div>
          <h2 className="text-lg font-semibold text-gray-900 mb-1">Something went wrong</h2>
          <p className="text-sm text-gray-500 mb-2 max-w-sm">{this.state.message || 'An unexpected error occurred.'}</p>
          <p className="text-xs text-gray-400 mb-6">Check the browser console for details.</p>
          <div className="flex gap-3">
            <button onClick={this.reset} className="btn-primary">Try again</button>
            <button onClick={() => window.location.href = '/'} className="btn-secondary">Go home</button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}