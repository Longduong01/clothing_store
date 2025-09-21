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
  notification,
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
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { userApi } from '../../services/api';
import { User, CreateUserRequest, UpdateUserRequest, UserRole } from '../../types/user';
import { VALIDATION_RULES, USER_ROLES } from '../../utils/constants';
import { useMutation } from '../../hooks/useApi';
import dayjs from 'dayjs';
import { playSound } from '../../utils/sound';

const { Title, } = Typography;
const { Option } = Select;

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [roleFilter, setRoleFilter] = useState<string>('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [showAllRecords, setShowAllRecords] = useState(true); // Mặc định hiển thị tất cả
  const [form] = Form.useForm();

  // Mock data for testing

  // Fetch users
  const fetchUsers = async () => {
    setLoading(true);
    try {
      // Use real API
      const response = await userApi.getUsers({}, showAllRecords); // Sử dụng state để quyết định
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

  // Fetch lại data khi toggle thay đổi
  useEffect(() => {
    fetchUsers();
  }, [showAllRecords]);

  // Create user mutation
  const createUserMutation = useMutation(userApi.createUser);
  const updateUserMutation = useMutation(userApi.updateUser);
  const deleteUserMutation = useMutation(userApi.deleteUser);

  const handleCreateUser = async (values: CreateUserRequest) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận tạo người dùng</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Tạo người dùng "{values.username}"?</div>,
      okText: 'Tạo', cancelText: 'Hủy', okButtonProps: { size: 'large', type: 'primary' }, cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const result = await createUserMutation.mutate(values);
          if (result) {
            notification.success({ message: 'Thành công', description: 'Tạo người dùng thành công' });
            playSound('success');
            setIsModalVisible(false);
            form.resetFields();
            fetchUsers();
          }
        } catch {
          notification.error({ message: 'Lỗi', description: 'Tạo người dùng thất bại' });
          playSound('error');
        }
      },
    });
  };

  const handleUpdateUser = async (values: UpdateUserRequest) => {
    if (!editingUser) return;
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận cập nhật người dùng</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Cập nhật thông tin người dùng "{values.username ?? editingUser.username}"?</div>,
      okText: 'Cập nhật', cancelText: 'Hủy', okButtonProps: { size: 'large', type: 'primary' }, cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const result = await updateUserMutation.mutate(editingUser.userId, values);
          if (result) {
            notification.success({ message: 'Thành công', description: 'Cập nhật người dùng thành công' });
            playSound('success');
            setIsModalVisible(false);
            setEditingUser(null);
            form.resetFields();
            fetchUsers();
          }
        } catch {
          notification.error({ message: 'Lỗi', description: 'Cập nhật người dùng thất bại' });
          playSound('error');
        }
      },
    });
  };

  const handleDeleteUser = async (userId: number) => {
    try {
      await deleteUserMutation.mutate(userId);
      notification.success({ message: 'Thành công', description: 'Xóa người dùng thành công' });
      playSound('success');
      fetchUsers();
    } catch (error) {
      notification.error({ message: 'Lỗi', description: 'Xóa người dùng thất bại' });
      playSound('error');
    }
  };

  const confirmDeleteUser = (record: User) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận xóa</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Xóa người dùng "{record.username}" (ID: {record.userId})?</div>,
      okText: 'Xóa', cancelText: 'Hủy', okButtonProps: { danger: true, size: 'large' }, cancelButtonProps: { size: 'large' }, centered: true,
      onOk: async () => handleDeleteUser(record.userId),
    });
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
      render: (text: string) => (
        <Tag
          color="#e6f4ff"
          style={{
            color: '#1677ff',
            fontSize: 16,
            padding: '6px 14px',
            borderRadius: 10,
            border: '1px solid #91caff',
            minWidth: 96,
            textAlign: 'center',
          }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
      sorter: (a: User, b: User) => a.email.localeCompare(b.email),
      render: (text: string) => (
        <Tag
          color="#f6ffed"
          style={{
            color: '#389e0d',
            fontSize: 14,
            padding: '4px 10px',
            borderRadius: 8,
            border: '1px solid #b7eb8f',
            maxWidth: 240,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
          }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: 'Phone',
      dataIndex: 'phone',
      key: 'phone',
      render: (text: string) => (
        <Tag
          color="#fff7e6"
          style={{
            color: '#d46b08',
            fontSize: 14,
            padding: '4px 10px',
            borderRadius: 8,
            border: '1px solid #ffd591',
          }}
        >
          {text || '-'}
        </Tag>
      ),
    },
    {
      title: 'Role',
      dataIndex: 'role',
      key: 'role',
      render: (role: UserRole) => (
        <Tag
          color={role === UserRole.ADMIN ? '#fff1f0' : '#e6f4ff'}
          style={{
            color: role === UserRole.ADMIN ? '#cf1322' : '#1677ff',
            fontSize: 14,
            padding: '4px 12px',
            borderRadius: 10,
            border: `1px solid ${role === UserRole.ADMIN ? '#ffa39e' : '#91caff'}`,
            minWidth: 90,
            textAlign: 'center',
          }}
        >
          {role}
        </Tag>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const color = status === 'ACTIVE' ? '#f6ffed' : status === 'INACTIVE' ? '#fff1f0' : status === 'SUSPENDED' ? '#fff7e6' : '#f0f0f0';
        const textColor = status === 'ACTIVE' ? '#389e0d' : status === 'INACTIVE' ? '#cf1322' : status === 'SUSPENDED' ? '#d46b08' : '#595959';
        const label = status === 'ACTIVE' ? 'Hoạt động' : status === 'INACTIVE' ? 'Ngừng hoạt động' : status === 'SUSPENDED' ? 'Tạm khóa' : 'Không xác định';
        
        return (
          <Tag color={color} style={{ color: textColor, border: '1px solid #d9d9d9' }}>
            {label}
          </Tag>
        );
      }
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'createdAt',
      key: 'createdAt',
      sorter: (a: User, b: User) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
      render: (date: string) => dayjs(date).format('DD/MM/YYYY HH:mm'),
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 120,
      render: (_: any, record: User) => (
        <Space size="small">
          <Tooltip title="Sửa">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title="Xóa">
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              onClick={() => confirmDeleteUser(record)}
            />
          </Tooltip>
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
                  type={showAllRecords ? "default" : "primary"}
                  onClick={() => setShowAllRecords(!showAllRecords)}
                  style={{ minWidth: 120 }}
                >
                  {showAllRecords ? "Chỉ hiển thị hoạt động" : "Hiển thị tất cả"}
                </Button>
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
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} của ${total} người dùng`,
            pageSizeOptions: ['5', '10', '20', '50', '100'],
            size: 'default'
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