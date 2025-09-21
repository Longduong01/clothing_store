import React, { useState, useEffect } from 'react';
import { Layout, Menu, Button, Avatar, Dropdown, Space, Typography, theme, Drawer } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  DashboardOutlined,
  TeamOutlined,
  TagsOutlined,
  BgColorsOutlined,
  ShoppingOutlined,
  FileTextOutlined,
  AppstoreOutlined,
  DeploymentUnitOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { USER_ROLES } from '../../utils/constants';
import './AppLayout.css';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

interface AppLayoutProps {
  children: React.ReactNode;
}

const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  // Check if screen is mobile
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
      if (window.innerWidth >= 768) {
        setMobileOpen(false);
      }
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  // Mock user data - replace with actual user data from context/store
  const currentUser = {
    username: 'admin',
    email: 'admin@clothingstore.com',
    role: USER_ROLES[1].value, // ADMIN
  };

  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: 'Bảng điều khiển',
    },
    {
      key: '/users',
      icon: <TeamOutlined />,
      label: 'Quản lý Người dùng',
    },
    {
      key: '/sizes',
      icon: <TagsOutlined />,
      label: 'Quản lý Size',
    },
    {
      key: '/colors',
      icon: <BgColorsOutlined />,
      label: 'Quản lý Màu sắc',
    },
    {
      key: '/brands',
      icon: <DeploymentUnitOutlined />,
      label: 'Quản lý Thương hiệu',
    },
    {
      key: '/categories',
      icon: <AppstoreOutlined />,
      label: 'Quản lý Danh mục',
    },
    {
      key: '/products',
      icon: <ShoppingOutlined />,
      label: 'Quản lý Sản phẩm',
    },
    {
      key: '/orders',
      icon: <FileTextOutlined />,
      label: 'Quản lý Đơn hàng',
    },
  ];

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Profile',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: 'Settings',
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      danger: true,
    },
  ];

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key);
    // Close mobile drawer when menu item is clicked
    if (isMobile) {
      setMobileOpen(false);
    }
  };

  const handleUserMenuClick = ({ key }: { key: string }) => {
    switch (key) {
      case 'profile':
        navigate('/profile');
        break;
      case 'settings':
        navigate('/settings');
        break;
      case 'logout':
        // Handle logout logic
        localStorage.removeItem('authToken');
        navigate('/login');
        break;
      default:
        break;
    }
  };

  // Sidebar component
  const SidebarContent = () => (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Logo Section */}
      <div style={{ 
        padding: '20px 16px', 
        textAlign: 'center',
        borderBottom: '1px solid #f0f0f0',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white'
      }}>
        <div style={{ 
          fontSize: collapsed ? '16px' : '20px', 
          fontWeight: 'bold',
          marginBottom: collapsed ? 0 : '4px'
        }}>
          {collapsed ? 'CS' : 'Clothing Store'}
        </div>
        {!collapsed && (
          <div style={{ fontSize: '12px', opacity: 0.8 }}>
            Admin Panel
          </div>
        )}
      </div>

      {/* Menu Section */}
      <div style={{ 
        display: 'flex', 
        flexDirection: 'column', 
        height: 'calc(100% - 80px)',
        padding: '8px 0'
      }}>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          onClick={handleMenuClick}
          style={{ 
            border: 'none', 
            background: 'transparent',
            flex: 1, 
            overflowY: 'auto',
            padding: '0 8px'
          }}
          theme="light"
        >
          {menuItems.map(item => (
            <Menu.Item key={item.key} icon={item.icon}>
              {item.label}
            </Menu.Item>
          ))}
        </Menu>
        
        {/* Footer Section */}
        <div style={{ 
          padding: '16px', 
          borderTop: '1px solid #f0f0f0', 
          color: '#8c8c8c',
          textAlign: 'center',
          background: '#fafafa'
        }}>
          <div style={{ 
            fontSize: '11px', 
            marginBottom: '4px',
            fontWeight: '500'
          }}>
            Phiên bản 1.0.0
          </div>
          <div style={{ fontSize: '10px', opacity: 0.7 }}>
            © 2024 Clothing Store
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* Desktop Sidebar */}
      {!isMobile && (
        <Sider 
          trigger={null} 
          collapsible 
          collapsed={collapsed}
          width={260}
          collapsedWidth={80}
          style={{
            background: '#ffffff',
            boxShadow: '2px 0 12px rgba(0,0,0,0.08)',
            borderRight: '1px solid #f0f0f0',
            position: 'fixed',
            left: 0,
            top: 0,
            bottom: 0,
            zIndex: 100,
          }}
        >
          <SidebarContent />
        </Sider>
      )}

      {/* Mobile Drawer */}
      <Drawer
        title={
          <div style={{ 
            fontSize: '18px', 
            fontWeight: 'bold',
            color: 'white'
          }}>
            Clothing Store
          </div>
        }
        placement="left"
        onClose={() => setMobileOpen(false)}
        open={mobileOpen}
        width={260}
        styles={{
          body: { padding: 0 },
          header: { 
            padding: '16px 24px',
            borderBottom: '1px solid #f0f0f0',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            color: 'white'
          }
        }}
      >
        <SidebarContent />
      </Drawer>

      <Layout style={{ 
        marginLeft: isMobile ? 0 : (collapsed ? 80 : 260),
        transition: 'margin-left 0.2s',
      }}>
        <Header style={{ 
          padding: '0 24px', 
          background: colorBgContainer,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          position: 'sticky',
          top: 0,
          zIndex: 99,
        }}>
          <Button
            type="text"
            icon={isMobile ? <MenuUnfoldOutlined /> : (collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />)}
            onClick={() => {
              if (isMobile) {
                setMobileOpen(true);
              } else {
                setCollapsed(!collapsed);
              }
            }}
            style={{
              fontSize: '16px',
              width: 64,
              height: 64,
            }}
          />
          
          <Space>
            <Text type="secondary">
              Welcome, {currentUser.username}
            </Text>
            <Dropdown
              menu={{
                items: userMenuItems,
                onClick: handleUserMenuClick,
              }}
              placement="bottomRight"
            >
              <Avatar 
                style={{ 
                  backgroundColor: '#1890ff',
                  cursor: 'pointer',
                }}
                icon={<UserOutlined />}
              />
            </Dropdown>
          </Space>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
          }}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  );
};

export default AppLayout;
