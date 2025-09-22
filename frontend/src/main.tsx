import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import 'antd/dist/reset.css';
import App from './App';
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
