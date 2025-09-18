import React, { useState } from 'react';
import { Layout, Menu, Button, Avatar, Dropdown, Space, Typography, theme } from 'antd';
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

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

interface AppLayoutProps {
  children: React.ReactNode;
}

const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

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

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed}
        width={260}
        collapsedWidth={80}
        style={{
          background: colorBgContainer,
          boxShadow: '2px 0 8px rgba(0,0,0,0.1)',
        }}
      >
        <div style={{ 
          padding: '16px', 
          textAlign: 'center',
          borderBottom: '1px solid #f0f0f0',
          marginBottom: '8px'
        }}>
          <Text strong style={{ fontSize: collapsed ? '14px' : '18px' }}>
            {collapsed ? 'CS' : 'Clothing Store'}
          </Text>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100% - 64px)' }}>
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems.map(item => ({
              ...item,
              label: <span style={{ display: 'inline-block', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: collapsed ? 0 : 180 }}>{item.label as string}</span>,
            }))}
            onClick={handleMenuClick}
            style={{ border: 'none', fontSize: 14, paddingInline: 8, flex: 1, overflowY: 'auto' }}
          />
          <div style={{ padding: '12px 16px', borderTop: '1px solid #f0f0f0', color: '#8c8c8c' }}>
            <div style={{ fontSize: 12 }}>Phiên bản 1.0.0</div>
            <div style={{ fontSize: 12 }}>© Clothing Store</div>
          </div>
        </div>
      </Sider>
      <Layout>
        <Header style={{ 
          padding: '0 24px', 
          background: colorBgContainer,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
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
