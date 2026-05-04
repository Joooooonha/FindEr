import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import MapPage from './pages/MapPage'
import CardCreatePage from './pages/CardCreatePage'
import CardViewPage from './pages/CardViewPage'
import CardEditPage from './pages/CardEditPage'

export default function App() {
  return (
    <BrowserRouter>
      <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
        <Header />
        <div style={{ flex: 1, minHeight: 0 }}>
          <Routes>
            <Route path="/" element={<MapPage />} />
            <Route path="/cards/new" element={<CardCreatePage />} />
            <Route path="/cards/:token" element={<CardViewPage />} />
            <Route path="/cards/:token/edit" element={<CardEditPage />} />
          </Routes>
        </div>
      </div>
    </BrowserRouter>
  )
}
