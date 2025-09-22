import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, theme } from 'antd';
import AppLayout from './components/Layout/AppLayout';
import Dashboard from './pages/Dashboard';
import UserManagement from './components/UserManagement/UserManagement';
import SizeManagement from './components/SizeManagement/SizeManagement';
import ColorManagement from './components/ColorManagement/ColorManagement.tsx';
import BrandManagement from './components/BrandManagement/BrandManagement.tsx';
import ProductManagement from './components/ProductManagement/ProductManagement';
import CategoryManagement from './components/CategoryManagement/CategoryManagement';
import LoginPage from './components/Auth/LoginPage';
import RegisterPage from './components/Auth/RegisterPage';
import ForgotPasswordPage from './components/Auth/ForgotPasswordPage';
import ClientLayout from './components/Client/ClientLayout';
import HomePage from './pages/client/HomePage';
import CategoryPage from './pages/client/CategoryPage';
import ProductDetailPage from './pages/client/ProductDetailPage';
import CheckoutPage from './pages/client/CheckoutPage';

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
        <Routes>
          {/* Client routes */}
          <Route path="/shop" element={<ClientLayout><HomePage /></ClientLayout>} />
          <Route path="/shop/category/:slug" element={<ClientLayout><CategoryPage /></ClientLayout>} />
          <Route path="/shop/product/:id" element={<ClientLayout><ProductDetailPage /></ClientLayout>} />
          <Route path="/shop/checkout" element={<ClientLayout><CheckoutPage /></ClientLayout>} />

          {/* Auth Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          
          {/* Protected Admin Routes */}
          <Route path="/" element={<AppLayout><Navigate to="/dashboard" replace /></AppLayout>} />
          <Route path="/dashboard" element={<AppLayout><Dashboard /></AppLayout>} />
          <Route path="/users" element={<AppLayout><UserManagement /></AppLayout>} />
          <Route path="/sizes" element={<AppLayout><SizeManagement /></AppLayout>} />
          <Route path="/colors" element={<AppLayout><ColorManagement /></AppLayout>} />
          <Route path="/brands" element={<AppLayout><BrandManagement /></AppLayout>} />
          <Route path="/categories" element={<AppLayout><CategoryManagement /></AppLayout>} />
          <Route path="/products" element={<AppLayout><ProductManagement /></AppLayout>} />
          
          {/* Admin Routes */}
          <Route path="/admin" element={<AppLayout><Navigate to="/dashboard" replace /></AppLayout>} />
          
          {/* Fallback */}
          <Route path="*" element={<Navigate to="/shop" replace />} />
        </Routes>
      </Router>
    </ConfigProvider>
  );
};

export default App;