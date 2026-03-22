import api from './api'
import type { QuizResponse } from '../types'

export const aiService = {
  generateQuiz: (topic: string, difficulty: string, questionCount: number) =>
    api.post<QuizResponse>('/ai/quiz/generate', { topic, difficulty, questionCount }).then(r => r.data),

  generateCase: (specialty: string, difficulty: string) =>
    api.post('/ai/cases/generate', { specialty, difficulty }).then(r => r.data),

  evaluateAnswer: (question: string, answer: string, specialty: string) =>
    api.post('/ai/eval/answer', { question, answer, specialty }).then(r => r.data),
}