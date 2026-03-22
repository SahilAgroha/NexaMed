import api from './api'
import type { Course, Enrollment } from '../types'

export const courseService = {
  getAll: () => api.get<Course[]>('/courses').then(r => r.data),
  getById: (id: string) => api.get<Course>(`/courses/${id}`).then(r => r.data),
  search: (keyword: string) => api.get<Course[]>(`/courses/search?keyword=${keyword}`).then(r => r.data),
  getMyCourses: () => api.get<Course[]>('/courses/my').then(r => r.data),
  create: (data: Partial<Course>) => api.post<Course>('/courses', data).then(r => r.data),
  publish: (id: string) => api.put<Course>(`/courses/${id}/publish`).then(r => r.data),
  enroll: (id: string) => api.post<Enrollment>(`/courses/${id}/enroll`).then(r => r.data),
  getMyEnrollments: () => api.get<Enrollment[]>('/courses/enrollments/my').then(r => r.data),
  updateProgress: (id: string, percent: number) =>
    api.put<Enrollment>(`/courses/${id}/progress?percent=${percent}`).then(r => r.data),
}