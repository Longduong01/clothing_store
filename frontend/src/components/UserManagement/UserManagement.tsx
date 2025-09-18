import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  Modal,
  Form,
  message,
  Popconfirm,
  Tag,
  Typography,
  Row,
  Col,
  Statistic,
  Avatar,
  Tooltip,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  UserOutlined,
  ReloadOutlined,
  ExportOutlined,
} from '@ant-design/icons';
import { userApi } from '../../services/api';
import { User, CreateUserRequest, UpdateUserRequest, UserRole } from '../../types';
import { PAGINATION_CONFIG, VALIDATION_RULES, USER_ROLES } from '../../utils/constants';
import { useMutation } from '../../hooks/useApi';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { Option } = Select;

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [roleFilter, setRoleFilter] = useState<string>('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [form] = Form.useForm();

  // Mock data for testing

  // Fetch users
  const fetchUsers = async () => {
    setLoading(true);
    try {
      // Use real API
      const response = await userApi.getUsers();
      setUsers(response);
    } catch (error) {
      message.error('Failed to fetch users');
      setUsers([]); // Fallback to empty array
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [searchText, roleFilter]);

  // Create user mutation
  const createUserMutation = useMutation(userApi.createUser);
  const updateUserMutation = useMutation(userApi.updateUser);
  const deleteUserMutation = useMutation(userApi.deleteUser);

  const handleCreateUser = async (values: CreateUserRequest) => {
    try {
      const result = await createUserMutation.mutate(values);
      if (result) {
        message.success('User created successfully');
        setIsModalVisible(false);
        form.resetFields();
        fetchUsers();
      }
    } catch (error) {
      message.error('Failed to create user');
    }
  };

  const handleUpdateUser = async (values: UpdateUserRequest) => {
    if (!editingUser) return;
    
    try {
      const result = await updateUserMutation.mutate(editingUser.userId, values);
      if (result) {
        message.success('User updated successfully');
        setIsModalVisible(false);
        setEditingUser(null);
        form.resetFields();
        fetchUsers();
      }
    } catch (error) {
      message.error('Failed to update user');
    }
  };

  const handleDeleteUser = async (userId: number) => {
    try {
      const result = await deleteUserMutation.mutate(userId);
      if (result !== undefined) {
        message.success('User deleted successfully');
        fetchUsers();
      }
    } catch (error) {
      message.error('Failed to delete user');
    }
  };

  const handleEdit = (user: User) => {
    setEditingUser(user);
    form.setFieldsValue({
      username: user.username,
      email: user.email,
      phone: user.phone,
      role: user.role,
    });
    setIsModalVisible(true);
  };

  const handleModalClose = () => {
    setIsModalVisible(false);
    setEditingUser(null);
    form.resetFields();
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 80,
      sorter: (a: User, b: User) => a.userId - b.userId,
    },
    {
      title: 'Avatar',
      key: 'avatar',
      width: 80,
      render: () => (
        <Avatar icon={<UserOutlined />} style={{ backgroundColor: '#1890ff' }} />
      ),
    },
    {
      title: 'Username',
      dataIndex: 'username',
      key: 'username',
      sorter: (a: User, b: User) => a.username.localeCompare(b.username),
      render: (text: string) => <Text strong>{text}</Text>,
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
      sorter: (a: User, b: User) => a.email.localeCompare(b.email),
    },
    {
      title: 'Phone',
      dataIndex: 'phone',
      key: 'phone',
      render: (text: string) => text || '-',
    },
    {
      title: 'Role',
      dataIndex: 'role',
      key: 'role',
      render: (role: UserRole) => (
        <Tag color={role === UserRole.ADMIN ? 'red' : 'blue'}>
          {role}
        </Tag>
      ),
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      sorter: (a: User, b: User) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
      render: (date: string) => dayjs(date).format('DD/MM/YYYY HH:mm'),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      render: (_: any, record: User) => (
        <Space size="small">
          <Tooltip title="Edit">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Popconfirm
            title="Are you sure you want to delete this user?"
            onConfirm={() => handleDeleteUser(record.userId)}
            okText="Yes"
            cancelText="No"
          >
            <Tooltip title="Delete">
              <Button
                type="text"
                danger
                icon={<DeleteOutlined />}
              />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const filteredUsers = users.filter(user => {
    const matchesSearch = !searchText || 
      user.username.toLowerCase().includes(searchText.toLowerCase()) ||
      user.email.toLowerCase().includes(searchText.toLowerCase());
    const matchesRole = !roleFilter || user.role === roleFilter;
    return matchesSearch && matchesRole;
  });

  const adminCount = users.filter(user => user.role === UserRole.ADMIN).length;
  const customerCount = users.filter(user => user.role === UserRole.CUSTOMER).length;

  return (
    <div>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="Total Users"
              value={users.length}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Admins"
              value={adminCount}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Customers"
              value={customerCount}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Active Users"
              value={users.length}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Row gutter={[16, 16]} align="middle">
            <Col flex="auto">
              <Title level={4} style={{ margin: 0 }}>
                User Management
              </Title>
            </Col>
            <Col>
              <Space>
                <Input
                  placeholder="Search users..."
                  prefix={<SearchOutlined />}
                  value={searchText}
                  onChange={(e) => setSearchText(e.target.value)}
                  style={{ width: 200 }}
                />
                <Select
                  placeholder="Filter by role"
                  value={roleFilter}
                  onChange={setRoleFilter}
                  style={{ width: 150 }}
                  allowClear
                >
                  <Option value={UserRole.ADMIN}>Admin</Option>
                  <Option value={UserRole.CUSTOMER}>Customer</Option>
                </Select>
                <Button
                  icon={<ReloadOutlined />}
                  onClick={fetchUsers}
                  loading={loading}
                >
                  Refresh
                </Button>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={() => setIsModalVisible(true)}
                >
                  Add User
                </Button>
                <Button icon={<ExportOutlined />}>
                  Export
                </Button>
              </Space>
            </Col>
          </Row>
        </div>

        <Table
          columns={columns}
          dataSource={filteredUsers}
          rowKey="userId"
          loading={loading}
          pagination={{
            ...PAGINATION_CONFIG,
            total: filteredUsers.length,
            showTotal: (total, range) =>
              `${range[0]}-${range[1]} of ${total} users`,
          }}
          scroll={{ x: 800 }}
        />
      </Card>

      <Modal
        title={editingUser ? 'Edit User' : 'Add New User'}
        open={isModalVisible}
        onCancel={handleModalClose}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={editingUser ? handleUpdateUser : handleCreateUser}
        >
          <Form.Item
            name="username"
            label="Username"
            rules={[VALIDATION_RULES.REQUIRED, VALIDATION_RULES.USERNAME]}
          >
            <Input placeholder="Enter username" />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[VALIDATION_RULES.REQUIRED, VALIDATION_RULES.EMAIL]}
          >
            <Input placeholder="Enter email" />
          </Form.Item>

          <Form.Item
            name="phone"
            label="Phone"
            rules={[VALIDATION_RULES.PHONE]}
          >
            <Input placeholder="Enter phone number" />
          </Form.Item>

          <Form.Item
            name="role"
            label="Role"
            rules={[VALIDATION_RULES.REQUIRED]}
          >
            <Select placeholder="Select role">
              {USER_ROLES.map(role => (
                <Option key={role.value} value={role.value}>
                  {role.label}
                </Option>
              ))}
            </Select>
          </Form.Item>

          {!editingUser && (
            <Form.Item
              name="password"
              label="Password"
              rules={[VALIDATION_RULES.REQUIRED, VALIDATION_RULES.PASSWORD]}
            >
              <Input.Password placeholder="Enter password" />
            </Form.Item>
          )}

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={handleModalClose}>
                Cancel
              </Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={createUserMutation.isLoading || updateUserMutation.isLoading}
              >
                {editingUser ? 'Update' : 'Create'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default UserManagement;