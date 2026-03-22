import { useAuthStore } from '../store/authStore'

export function useAuth() {
  const { user, isAuthenticated, clearAuth } = useAuthStore()

  const isTeacher    = user?.role === 'TEACHER'
  const isAdmin      = user?.role === 'ADMIN'
  const isStudent    = user?.role === 'STUDENT'
  const isInterviewer= user?.role === 'INTERVIEWER'
  const canCreateCourse = isTeacher || isAdmin

  return { user, isAuthenticated, isTeacher, isAdmin, isStudent, isInterviewer, canCreateCourse, clearAuth }
}