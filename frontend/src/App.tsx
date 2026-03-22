import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { useAuthStore } from './store/authStore'
import { useNotifications } from './hooks/useNotifications'

import Navbar from './components/common/Navbar'
import ProtectedRoute from './components/common/ProtectedRoute'

import LoginPage      from './pages/auth/LoginPage'
import RegisterPage   from './pages/auth/RegisterPage'

import Dashboard        from './pages/student/Dashboard'
import CoursesPage      from './pages/student/CoursesPage'
import InterviewsPage   from './pages/student/InterviewsPage'
import QuizPage         from './pages/student/QuizPage'
import CaseSimPage      from './pages/student/CaseSimPage'
import LiveInterviewPage from './pages/student/LiveInterviewPage'

import TeacherDashboard  from './pages/teacher/TeacherDashboard'
import CreateCoursePage  from './pages/teacher/CreateCoursePage'
import ManageCoursePage  from './pages/teacher/ManageCoursePage'

function AppShell() {
  const { isAuthenticated } = useAuthStore()
  useNotifications()          // connects WebSocket once logged in

  return (
    <>
      <Toaster position="top-right" />
      {isAuthenticated && <Navbar />}
      <Routes>
        {/* ── Public ───────────────────────────────────────────── */}
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* ── Student ──────────────────────────────────────────── */}
        <Route path="/" element={
          <ProtectedRoute><Dashboard /></ProtectedRoute>
        } />
        <Route path="/courses" element={
          <ProtectedRoute><CoursesPage /></ProtectedRoute>
        } />
        <Route path="/interviews" element={
          <ProtectedRoute><InterviewsPage /></ProtectedRoute>
        } />
        <Route path="/interviews/live/:roomId" element={
          <ProtectedRoute><LiveInterviewPage /></ProtectedRoute>
        } />
        <Route path="/quiz" element={
          <ProtectedRoute><QuizPage /></ProtectedRoute>
        } />
        <Route path="/cases" element={
          <ProtectedRoute><CaseSimPage /></ProtectedRoute>
        } />

        {/* ── Teacher ──────────────────────────────────────────── */}
        <Route path="/teacher" element={
          <ProtectedRoute role="TEACHER"><TeacherDashboard /></ProtectedRoute>
        } />
        <Route path="/teacher/courses/new" element={
          <ProtectedRoute role="TEACHER"><CreateCoursePage /></ProtectedRoute>
        } />
        <Route path="/teacher/courses/:id" element={
          <ProtectedRoute role="TEACHER"><ManageCoursePage /></ProtectedRoute>
        } />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AppShell />
    </BrowserRouter>
  )
}
