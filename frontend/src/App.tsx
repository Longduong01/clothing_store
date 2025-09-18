import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, theme } from 'antd';
import AppLayout from './components/Layout/AppLayout';
import Dashboard from './pages/Dashboard';
import UserManagement from './components/UserManagement/UserManagement';
import SizeManagement from './components/SizeManagement/SizeManagement';
import ColorManagement from './components/ColorManagement/ColorManagement';
import BrandManagement from './components/BrandManagement/BrandManagement';
import CategoryManagement from './components/CategoryManagement/CategoryManagement';

// Configure Ant Design theme
const antdTheme = {
  algorithm: theme.defaultAlgorithm,
  token: {
    colorPrimary: '#1890ff',
    borderRadius: 6,
    colorBgContainer: '#ffffff',
  },
  components: {
    Layout: {
      headerBg: '#ffffff',
      siderBg: '#ffffff',
    },
    Card: {
      borderRadius: 8,
    },
    Button: {
      borderRadius: 6,
    },
    Input: {
      borderRadius: 6,
    },
    Table: {
      borderRadius: 8,
    },
  },
};

const App: React.FC = () => {
  return (
    <ConfigProvider theme={antdTheme}>
      <Router>
        <AppLayout>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/users" element={<UserManagement />} />
            <Route path="/sizes" element={<SizeManagement />} />
            <Route path="/colors" element={<ColorManagement />} />
            <Route path="/brands" element={<BrandManagement />} />
            <Route path="/categories" element={<CategoryManagement />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </AppLayout>
      </Router>
    </ConfigProvider>
  );
};

export default App;