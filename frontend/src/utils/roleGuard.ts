export function requireRole(userRole: string | undefined, allowed: string[]): boolean {
  return !!userRole && allowed.includes(userRole)
}

export function scoreColor(score: number): string {
  if (score >= 80) return 'text-green-600'
  if (score >= 60) return 'text-yellow-600'
  return 'text-red-600'
}

export function scoreBg(score: number): string {
  if (score >= 80) return 'bg-green-50 text-green-700'
  if (score >= 60) return 'bg-yellow-50 text-yellow-700'
  return 'bg-red-50 text-red-700'
}

export function difficultyColor(d: string): string {
  return { BEGINNER: 'bg-green-100 text-green-700', INTERMEDIATE: 'bg-yellow-100 text-yellow-700', ADVANCED: 'bg-red-100 text-red-700' }[d] ?? 'bg-gray-100 text-gray-700'
}