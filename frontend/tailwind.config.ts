/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: { 50:'#f0f9ff',100:'#e0f2fe',500:'#0ea5e9',600:'#0284c7',700:'#0369a1' },
        nexamed: { 50:'#f0fdf4',500:'#22c55e',600:'#16a34a',700:'#15803d' }
      }
    }
  },
  plugins: [],
}