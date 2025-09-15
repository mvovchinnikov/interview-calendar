import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './index'           // your OutlookLikeCalendarMock default export
import './index.css'                // Tailwind styles

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
