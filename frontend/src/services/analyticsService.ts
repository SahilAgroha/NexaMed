import api from './api'
import type { StudentDashboard } from '../types'

export const analyticsService = {
  getDashboard: () => api.get<StudentDashboard>('/analytics/dashboard').then(r => r.data),
  getTimeline: () => api.get('/analytics/timeline').then(r => r.data),
}