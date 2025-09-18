import React from 'react';
import { Card, Row, Col, Statistic, Typography, Space, Button } from 'antd';
import {
  UserOutlined,
  ShoppingOutlined,
  TagsOutlined,
  BgColorsOutlined,
  FileTextOutlined,
  ShoppingCartOutlined,
} from '@ant-design/icons';

const { Title, Text } = Typography;

const Dashboard: React.FC = () => {
  // Mock data - replace with actual data from API
  const stats = {
    totalUsers: 156,
    totalProducts: 89,
    totalOrders: 234,
    totalRevenue: 12500000,
    totalSizes: 8,
    totalColors: 12,
  };

  const recentActivities = [
    { action: 'New user registered', user: 'john_doe', time: '2 minutes ago' },
    { action: 'Order completed', order: '#ORD-001', time: '5 minutes ago' },
    { action: 'Product added', product: 'Summer T-Shirt', time: '10 minutes ago' },
    { action: 'Color updated', color: 'Navy Blue', time: '15 minutes ago' },
  ];

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <Title level={2}>Dashboard</Title>
        <Text type="secondary">
          Welcome to Clothing Store Management System
        </Text>
      </div>

      {/* Statistics Cards */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Users"
              value={stats.totalUsers}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Products"
              value={stats.totalProducts}
              prefix={<ShoppingOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Orders"
              value={stats.totalOrders}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Revenue"
              value={stats.totalRevenue}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#f5222d' }}
              formatter={(value) => `$${value.toLocaleString()}`}
            />
          </Card>
        </Col>
      </Row>

      {/* Management Overview */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Available Sizes"
              value={stats.totalSizes}
              prefix={<TagsOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Available Colors"
              value={stats.totalColors}
              prefix={<BgColorsOutlined />}
              valueStyle={{ color: '#13c2c2' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Active Products"
              value={stats.totalProducts}
              prefix={<ShoppingOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Pending Orders"
              value={12}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Recent Activities */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="Recent Activities" extra={<Button type="link">View All</Button>}>
            <Space direction="vertical" style={{ width: '100%' }}>
              {recentActivities.map((activity, index) => (
                <div key={index} style={{ padding: '8px 0', borderBottom: '1px solid #f0f0f0' }}>
                  <Text strong>{activity.action}</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: '12px' }}>
                    {activity.user || activity.order || activity.product} • {activity.time}
                  </Text>
                </div>
              ))}
            </Space>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Quick Actions" extra={<Button type="link">More</Button>}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Button type="primary" block icon={<UserOutlined />}>
                Add New User
              </Button>
              <Button block icon={<ShoppingOutlined />}>
                Add New Product
              </Button>
              <Button block icon={<TagsOutlined />}>
                Manage Sizes
              </Button>
              <Button block icon={<BgColorsOutlined />}>
                Manage Colors
              </Button>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
