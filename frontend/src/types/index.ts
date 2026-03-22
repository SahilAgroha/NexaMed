export interface AuthResponse {
  accessToken: string; refreshToken: string; tokenType: string
  role: string; email: string; fullName: string; userId: string
}
export interface LoginRequest { email: string; password: string }
export interface RegisterRequest { fullName: string; email: string; password: string; role?: string }
export interface UserProfile {
  id: string; userId: string; fullName: string; email: string; role: string
  bio?: string; avatarUrl?: string; specialization?: string; institution?: string
  profileComplete: boolean; createdAt: string
}
export interface Course {
  id: string; title: string; description: string
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
  category: string; thumbnailUrl?: string; teacherId: string
  published: boolean; enrollmentCount: number; moduleCount: number; createdAt: string
}
export interface Enrollment {
  id: string; studentId: string; courseId: string; courseTitle: string
  status: string; progressPercent: number; enrolledAt: string
}
export interface InterviewSession {
  id: string; studentId: string; type: 'AI_MOCK' | 'LIVE'
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  specialty: string; roomId: string; overallScore?: number
  feedbackSummary?: string; createdAt: string; completedAt?: string
}
export interface StudentDashboard {
  studentId: string; totalEnrollments: number; completedCourses: number
  totalInterviews: number; averageInterviewScore: number; bestInterviewScore: number
  totalQuizzesTaken: number; averageQuizScore: number; streakDays: number
  recentActivity: ActivitySummary[]; lastActiveAt: string
}
export interface ActivitySummary { type: string; description: string; score?: number; occurredAt: string }
export interface QuizQuestion {
  questionNumber: number; question: string; options: string[]
  correctAnswerIndex: number; explanation: string
}
export interface QuizResponse {
  topic: string; difficulty: string; questionCount: number
  questions: QuizQuestion[]; generatedInMs: number
}