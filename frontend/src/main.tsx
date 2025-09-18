import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import { setSoundUrls } from './utils/sound';

// Configure custom sounds
setSoundUrls({
  success: '/sounds/success_sound.m4a',
}, 0.5);

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
