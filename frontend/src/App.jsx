import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import MapPage from './pages/MapPage'
import NotFoundPage from './pages/NotFoundPage'

export default function App() {
  return (
    <BrowserRouter>
      <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
        <Header />
        <div style={{ flex: 1, minHeight: 0 }}>
          <Routes>
            <Route path="/" element={<MapPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </div>
      </div>
    </BrowserRouter>
  )
}
