import api from './api'
import type { InterviewSession } from '../types'

export const interviewService = {
  startMock: (specialty: string) =>
    api.post<InterviewSession>('/interviews/mock/start', { specialty }).then(r => r.data),

  submitAnswer: (sessionId: string, answer: string) =>
    api.post(`/interviews/${sessionId}/answer`, { answer }).then(r => r.data),

  complete: (sessionId: string) =>
    api.post<InterviewSession>(`/interviews/${sessionId}/complete`).then(r => r.data),

  getHistory: () => api.get<InterviewSession[]>('/interviews/history').then(r => r.data),

  getQuestions: (sessionId: string) =>
    api.get(`/interviews/${sessionId}/questions`).then(r =>
      Array.isArray(r.data) ? r.data : r.data?.content ?? r.data?.questions ?? []
    ),
}