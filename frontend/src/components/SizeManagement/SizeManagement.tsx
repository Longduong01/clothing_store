import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Modal,
  Form,
  message,
  notification,
  Typography,
  Row,
  Col,
  Statistic,
  Tag,
  Tooltip,
  Select,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  TagsOutlined,
  ReloadOutlined,
  ExportOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { sizeApi } from '../../services/api';
import { Size, CreateSizeRequest, UpdateSizeRequest } from '../../types';
import { VALIDATION_RULES } from '../../utils/constants';
import { useMutation } from '../../hooks/useApi';
import { playSound } from '../../utils/sound';

const { Title, Text } = Typography;

const SizeManagement: React.FC = () => {
  const [sizes, setSizes] = useState<Size[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingSize, setEditingSize] = useState<Size | null>(null);
  const [showAllRecords, setShowAllRecords] = useState(true); // Mặc định hiển thị tất cả
  const [form] = Form.useForm();


  // Fetch sizes
  const fetchSizes = async () => {
    setLoading(true);
    try {
      const response = await sizeApi.getSizes(showAllRecords); // Sử dụng state để quyết định
      setSizes(response);
    } catch (error) {
      console.error('API Error:', error);
      message.error('Failed to fetch sizes from database');
      setSizes([]); // Show empty state when API fails
    } finally {
      setLoading(false);
    }
  };

  const refreshSizes = async () => {
    setRefreshing(true);
    try {
      const updatedSizes = await sizeApi.getSizes(showAllRecords);
      setSizes(updatedSizes);
      return updatedSizes;
    } catch (e) {
      console.error('Error refreshing sizes:', e);
      return [];
    } finally {
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchSizes();
  }, []);

  // Fetch lại data khi toggle thay đổi
  useEffect(() => {
    fetchSizes();
  }, [showAllRecords]);

  // Mutations
  const createSizeMutation = useMutation(sizeApi.createSize);
  const updateSizeMutation = useMutation(sizeApi.updateSize);
  const deleteSizeMutation = useMutation(sizeApi.deleteSize);

  // Validation helpers
  const isSizeNameTaken = async (name: string, excludeId?: number) => {
    try {
      const found = await sizeApi.getSizeByName(name);
      if (!found) return false;
      if (excludeId && found.sizeId === excludeId) return false;
      return true;
    } catch {
      return false;
    }
  };

  const handleCreateSize = async (values: CreateSizeRequest) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận tạo size</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Tạo size "{values.sizeName}"?</div>,
      okText: 'Tạo',
      cancelText: 'Hủy',
      okButtonProps: { size: 'large', type: 'primary' },
      cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const result = await createSizeMutation.mutate(values);
          if (result) {
            notification.success({ message: 'Thành công', description: 'Tạo size thành công' });
            playSound('success');
            setIsModalVisible(false);
            form.resetFields();
            await refreshSizes();
          }
        } catch (error) {
          console.error('Create Error:', error);
          notification.error({ message: 'Lỗi', description: 'Tạo size thất bại' });
          playSound('error');
        }
      },
    });
  };

  const handleUpdateSize = async (values: UpdateSizeRequest) => {
    if (!editingSize) return;
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận cập nhật size</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Cập nhật thành "{values.sizeName}"?</div>,
      okText: 'Cập nhật',
      cancelText: 'Hủy',
      okButtonProps: { size: 'large', type: 'primary' },
      cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const result = await updateSizeMutation.mutate(editingSize.sizeId, values);
          if (result) {
            notification.success({ message: 'Thành công', description: 'Cập nhật size thành công' });
            playSound('success');
            setIsModalVisible(false);
            setEditingSize(null);
            form.resetFields();
            await refreshSizes();
          }
        } catch (error) {
          notification.error({ message: 'Lỗi', description: 'Cập nhật size thất bại' });
          playSound('error');
        }
      },
    });
  };

  const handleDeleteSize = async (sizeId: number) => {
    try {
      await deleteSizeMutation.mutate(sizeId);
      notification.success({ message: 'Thành công', description: 'Xóa size thành công' });
      playSound('success');
      // Optimistic update
      setSizes(prev => prev.filter(s => s.sizeId !== sizeId));
      // Background refresh to stay in sync
      await refreshSizes();
    } catch (error) {
      notification.error({ message: 'Lỗi', description: 'Xóa size thất bại' });
      playSound('error');
    }
  };

  const confirmDeleteSize = (record: Size) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Confirm delete</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Delete size "{record.sizeName}" (ID: {record.sizeId})?</div>,
      okText: 'Delete',
      cancelText: 'Cancel',
      okButtonProps: { danger: true, size: 'large' },
      cancelButtonProps: { size: 'large' },
      centered: true,
      onOk: async () => handleDeleteSize(record.sizeId),
    });
  };

  const handleEdit = (size: Size) => {
    setEditingSize(size);
    form.setFieldsValue({
      sizeName: size.sizeName,
      status: size.status,
    });
    setIsModalVisible(true);
  };

  const handleModalClose = () => {
    setIsModalVisible(false);
    setEditingSize(null);
    form.resetFields();
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'sizeId',
      key: 'sizeId',
      width: 80,
      sorter: (a: Size, b: Size) => a.sizeId - b.sizeId,
    },
    {
      title: 'Tên size',
      dataIndex: 'sizeName',
      key: 'sizeName',
      sorter: (a: Size, b: Size) => a.sizeName.localeCompare(b.sizeName),
      render: (text: string) => (
        <Tag
          color="#e6f4ff"
          style={{
            color: '#1677ff',
            fontSize: 16,
            padding: '6px 14px',
            borderRadius: 10,
            border: '1px solid #91caff',
            minWidth: 48,
            textAlign: 'center',
          }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const color = status === 'ACTIVE' ? '#f6ffed' : status === 'INACTIVE' ? '#fff1f0' : status === 'DISCONTINUED' ? '#f0f0f0' : '#fff7e6';
        const textColor = status === 'ACTIVE' ? '#389e0d' : status === 'INACTIVE' ? '#cf1322' : status === 'DISCONTINUED' ? '#595959' : '#d46b08';
        const label = status === 'ACTIVE' ? 'Hoạt động' : status === 'INACTIVE' ? 'Ngừng hoạt động' : status === 'DISCONTINUED' ? 'Ngừng sản xuất' : 'Không xác định';
        
        return (
          <Tag color={color} style={{ color: textColor, border: '1px solid #d9d9d9' }}>
            {label}
          </Tag>
        );
      }
    },
    {
      title: 'Đang được sử dụng',
      dataIndex: 'productCount',
      key: 'productCount',
      width: 120,
      sorter: (a: Size, b: Size) => a.productCount - b.productCount,
      render: (count: number) => (
        <Tag
          color={count > 0 ? '#52c41a' : '#d9d9d9'}
          style={{
            color: count > 0 ? '#ffffff' : '#8c8c8c',
            fontSize: 14,
            padding: '4px 12px',
            borderRadius: 8,
            border: count > 0 ? '1px solid #52c41a' : '1px solid #d9d9d9',
            minWidth: 60,
            textAlign: 'center',
            fontWeight: 600
          }}
        >
          {count} biến thể
        </Tag>
      )
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 120,
      render: (_: any, record: Size) => (
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
              onClick={() => confirmDeleteSize(record)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const filteredSizes = sizes.filter(size => {
    const matchesSearch = !searchText || size.sizeName.toLowerCase().includes(searchText.toLowerCase());
    const matchesStatus = !statusFilter || size.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="Tổng size"
              value={sizes.length}
              prefix={<TagsOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Đang hoạt động"
              value={sizes.filter(s => s.status === 'ACTIVE').length}
              prefix={<TagsOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Ngừng hoạt động"
              value={sizes.filter(s => s.status === 'INACTIVE').length}
              prefix={<TagsOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Phổ biến"
              value="M"
              prefix={<TagsOutlined />}
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
                Quản lý Size
              </Title>
              <Text type="secondary">
                Quản lý danh sách size sản phẩm
              </Text>
            </Col>
            <Col>
              <Space>
                <Input
                  placeholder="Tìm kiếm size..."
                  prefix={<SearchOutlined />}
                  value={searchText}
                  onChange={(e) => setSearchText(e.target.value)}
                  style={{ width: 200 }}
                />
                <Select
                  placeholder="Trạng thái"
                  allowClear
                  showSearch
                  filterOption={(input, option) =>
                    String(option?.children || '').toLowerCase().includes(input.toLowerCase())
                  }
                  notFoundContent="Không tìm thấy"
                  style={{ width: 150 }}
                  value={statusFilter}
                  onChange={setStatusFilter}
                >
                  <Select.Option value="ACTIVE">Hoạt động</Select.Option>
                  <Select.Option value="INACTIVE">Ngừng hoạt động</Select.Option>
                  <Select.Option value="DISCONTINUED">Ngừng sản xuất</Select.Option>
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
                  onClick={fetchSizes}
                  loading={loading || refreshing}
                >
                  Làm mới
                </Button>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={() => setIsModalVisible(true)}
                >
                  Thêm Size
                </Button>
                <Button icon={<ExportOutlined />}>
                  Xuất
                </Button>
              </Space>
            </Col>
          </Row>
        </div>

        <Table
          columns={columns}
          dataSource={filteredSizes}
          rowKey="sizeId"
          loading={loading || refreshing}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} của ${total} kích thước`,
            pageSizeOptions: ['5', '10', '20', '50', '100'],
            size: 'default'
          }}
          scroll={{ x: 600 }}
        />
      </Card>

      <Modal
        title={<span style={{ fontSize: 22, fontWeight: 700 }}>{editingSize ? 'Sửa Size' : 'Thêm Size mới'}</span>}
        open={isModalVisible}
        onCancel={handleModalClose}
        footer={null}
        width={720}
        centered
        styles={{ body: { paddingTop: 16, paddingBottom: 12 } }}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={editingSize ? handleUpdateSize : handleCreateSize}
        >
          <Form.Item
            name="sizeName"
            label="Tên size"
            rules={[
              { required: true, message: 'Vui lòng nhập tên size' },
              VALIDATION_RULES.SIZE_NAME,
              {
                pattern: new RegExp('^[\\\p{L}\\\p{N}\\- ]+$','u'),
                message: 'Chỉ cho phép chữ (có dấu), số, khoảng trắng và -',
              },
              {
                validator: async (_, value) => {
                  const val = (value || '').toString().trim().toUpperCase();
                  if (!val) return Promise.resolve();
                  const taken = await isSizeNameTaken(val, editingSize?.sizeId);
                  if (taken) return Promise.reject(new Error('Size đã tồn tại'));
                  return Promise.resolve();
                },
              },
            ]}
          >
            <Input 
              placeholder="Nhập tên size (VD: S, M, L, XL)" 
              style={{ textTransform: 'uppercase' }}
              onChange={(e) => {
                e.target.value = e.target.value.toUpperCase();
              }}
            />
          </Form.Item>

          {editingSize && (
            <Form.Item
              name="status"
              label="Trạng thái"
              rules={[{ required: true, message: 'Vui lòng chọn trạng thái' }]}
            >
              <Select placeholder="Chọn trạng thái">
                <Select.Option value="ACTIVE">Hoạt động</Select.Option>
                <Select.Option value="INACTIVE">Ngừng hoạt động</Select.Option>
                <Select.Option value="DISCONTINUED">Ngừng sản xuất</Select.Option>
              </Select>
            </Form.Item>
          )}

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button size="large" onClick={handleModalClose}>
                Hủy
              </Button>
              <Button
                type="primary"
                htmlType="submit"
                size="large"
                loading={createSizeMutation.isLoading || updateSizeMutation.isLoading}
              >
                {editingSize ? 'Cập nhật' : 'Tạo'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SizeManagement;