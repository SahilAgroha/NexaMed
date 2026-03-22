import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import { authService } from '../../services/authService'
import toast from 'react-hot-toast'
import {
  Home, BookOpen, Mic, Brain, Stethoscope,
  LogOut, LayoutDashboard, Plus
} from 'lucide-react'

interface NavLinkProps { to: string; icon: React.ReactNode; label: string }

function NavLink({ to, icon, label }: NavLinkProps) {
  const { pathname } = useLocation()
  const active = pathname === to || (to !== '/' && pathname.startsWith(to))
  return (
    <Link to={to} className={`flex items-center gap-1.5 px-3 py-2 text-sm rounded-lg transition-colors
      ${active
        ? 'bg-primary-50 text-primary-700 font-medium'
        : 'text-gray-600 hover:text-primary-600 hover:bg-gray-50'}`}>
      {icon}{label}
    </Link>
  )
}

export default function Navbar() {
  const { user, clearAuth } = useAuthStore()
  const navigate = useNavigate()
  const isTeacher = user?.role === 'TEACHER' || user?.role === 'ADMIN'

  const handleLogout = async () => {
    try { await authService.logout() } catch {}
    clearAuth()
    toast.success('Logged out')
    navigate('/login')
  }

  return (
    <nav className="bg-white border-b border-gray-200 px-6 py-2.5 flex items-center justify-between sticky top-0 z-50">
      <div className="flex items-center gap-1">
        <Link to="/" className="text-xl font-bold text-primary-600 mr-4">NexaMed</Link>

        {/* Student nav */}
        <NavLink to="/"          icon={<Home size={15} />}        label="Dashboard" />
        <NavLink to="/courses"   icon={<BookOpen size={15} />}    label="Courses" />
        <NavLink to="/interviews"icon={<Mic size={15} />}         label="Interviews" />
        <NavLink to="/quiz"      icon={<Brain size={15} />}       label="Quiz" />
        <NavLink to="/cases"     icon={<Stethoscope size={15} />} label="Case Sim" />

        {/* Teacher-only nav */}
        {isTeacher && (
          <>
            <div className="w-px h-5 bg-gray-200 mx-1" />
            <NavLink to="/teacher"             icon={<LayoutDashboard size={15} />} label="My Courses" />
            <NavLink to="/teacher/courses/new" icon={<Plus size={15} />}            label="New Course" />
          </>
        )}
      </div>

      <div className="flex items-center gap-3">
        <div className="text-right">
          <p className="text-sm font-medium text-gray-900 leading-none">{user?.fullName}</p>
          <p className="text-xs text-gray-400 mt-0.5">{user?.role}</p>
        </div>
        <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 text-sm font-semibold">
          {user?.fullName?.[0]?.toUpperCase() ?? '?'}
        </div>
        <button onClick={handleLogout}
          className="p-2 text-gray-400 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          title="Logout">
          <LogOut size={17} />
        </button>
      </div>
    </nav>
  )
}
