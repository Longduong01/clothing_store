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
  Tooltip,
  ColorPicker,
  Tag,
  Select,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  BgColorsOutlined,
  ReloadOutlined,
  ExportOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { colorApi } from '../../services/api.ts';
import { Color, CreateColorRequest, UpdateColorRequest } from '../../types';
import { VALIDATION_RULES, COMMON_COLORS } from '../../utils/constants.ts';
import { useMutation } from '../../hooks/useApi.ts';
import { playSound } from '../../utils/sound.ts';

const { Title, Text } = Typography;

const ColorManagement: React.FC = () => {
  const [colors, setColors] = useState<Color[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingColor, setEditingColor] = useState<Color | null>(null);
  const [selectedColor, setSelectedColor] = useState('#1890ff');
  const [showAllRecords, setShowAllRecords] = useState(true); // Mặc định hiển thị tất cả
  const [form] = Form.useForm();


  // Fetch colors
  const fetchColors = async () => {
    setLoading(true);
    try {
      const response = await colorApi.getColors(showAllRecords); // Sử dụng state để quyết định
      setColors(response);
    } catch (error) {
      console.error('API Error:', error);
      message.error('Không thể tải dữ liệu màu sắc từ cơ sở dữ liệu');
      setColors([]); // Show empty state when API fails
    } finally {
      setLoading(false);
    }
  };

  const refreshColors = async () => {
    setRefreshing(true);
    try {
      const updatedColors = await colorApi.getColors(showAllRecords);
      setColors(updatedColors);
      return updatedColors;
    } catch (e) {
      console.error('Error refreshing colors:', e);
      return [];
    } finally {
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchColors();
  }, []);

  // Fetch lại data khi toggle thay đổi
  useEffect(() => {
    fetchColors();
  }, [showAllRecords]);

  // Mutations
  const createColorMutation = useMutation(colorApi.createColor);
  const updateColorMutation = useMutation(colorApi.updateColor);
  const deleteColorMutation = useMutation(colorApi.deleteColor);

  // Validation helpers
  const isColorNameTaken = async (name: string, excludeId?: number) => {
    try {
      const found = await colorApi.getColorByName(name);
      if (!found) return false;
      if (excludeId && found.colorId === excludeId) return false;
      return true;
    } catch {
      return false;
    }
  };

  const handleCreateColor = async (values: CreateColorRequest) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận tạo màu</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Tạo màu "{values.colorName}"?</div>,
      okText: 'Tạo',
      cancelText: 'Hủy',
      okButtonProps: { size: 'large', type: 'primary' },
      cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const result = await createColorMutation.mutate(values);
          if (result) {
            notification.success({ message: 'Thành công', description: 'Tạo màu thành công' });
            playSound('success');
            setIsModalVisible(false);
            form.resetFields();
            setSelectedColor('#1890ff');
            await refreshColors();
          }
        } catch (error) {
          notification.error({ message: 'Lỗi', description: 'Tạo màu thất bại' });
          playSound('error');
        }
      },
    });
  };

  const handleUpdateColor = async (values: UpdateColorRequest) => {
    if (!editingColor) return;
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận cập nhật màu</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Cập nhật thành "{values.colorName}"?</div>,
      okText: 'Cập nhật',
      cancelText: 'Hủy',
      okButtonProps: { size: 'large', type: 'primary' },
      cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const result = await updateColorMutation.mutate(editingColor.colorId, values);
          if (result) {
            notification.success({ message: 'Thành công', description: 'Cập nhật màu thành công' });
            playSound('success');
            setIsModalVisible(false);
            setEditingColor(null);
            form.resetFields();
            setSelectedColor('#1890ff');
            await refreshColors();
          }
        } catch (error) {
          notification.error({ message: 'Lỗi', description: 'Cập nhật màu thất bại' });
          playSound('error');
        }
      },
    });
  };

  const handleDeleteColor = async (colorId: number) => {
    try {
      await deleteColorMutation.mutate(colorId);
      notification.success({ message: 'Thành công', description: 'Xóa màu thành công' });
      playSound('success');
      // Optimistic update
      setColors(prev => prev.filter(c => c.colorId !== colorId));
      // Background refresh to stay in sync
      await refreshColors();
    } catch (error) {
      notification.error({ message: 'Lỗi', description: 'Xóa màu thất bại' });
      playSound('error');
    }
  };

  const confirmDeleteColor = (record: Color) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận xóa màu</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Xóa màu "{record.colorName}" (ID: {record.colorId})?</div>,
      okText: 'Xóa',
      cancelText: 'Hủy',
      okButtonProps: { danger: true, size: 'large' },
      cancelButtonProps: { size: 'large' },
      centered: true,
      onOk: async () => handleDeleteColor(record.colorId),
    });
  };

  const handleEdit = (color: Color) => {
    setEditingColor(color);
    form.setFieldsValue({
      colorName: color.colorName,
      status: color.status,
    });
    setIsModalVisible(true);
  };

  const handleModalClose = () => {
    setIsModalVisible(false);
    setEditingColor(null);
    form.resetFields();
    setSelectedColor('#1890ff');
  };


  const columns = [
    {
      title: 'ID',
      dataIndex: 'colorId',
      key: 'colorId',
      width: 80,
      sorter: (a: Color, b: Color) => a.colorId - b.colorId,
    },
    {
      title: 'Tên màu',
      dataIndex: 'colorName',
      key: 'colorName',
      sorter: (a: Color, b: Color) => a.colorName.localeCompare(b.colorName),
      render: (text: string) => (
        <Tag
          color="#e6f4ff"
          style={{
            color: '#1677ff',
            fontSize: 16,
            padding: '6px 14px',
            borderRadius: 10,
            border: '1px solid #91caff',
            minWidth: 64,
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
      sorter: (a: Color, b: Color) => a.productCount - b.productCount,
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
      render: (_: any, record: Color) => (
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
              onClick={() => confirmDeleteColor(record)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const filteredColors = colors.filter(color => {
    const matchesSearch = !searchText || color.colorName.toLowerCase().includes(searchText.toLowerCase());
    const matchesStatus = !statusFilter || color.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="Tổng số màu"
              value={colors.length}
              prefix={<BgColorsOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Đang hoạt động"
              value={colors.filter(c => c.status === 'ACTIVE').length}
              prefix={<BgColorsOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Ngừng hoạt động"
              value={colors.filter(c => c.status === 'INACTIVE').length}
              prefix={<BgColorsOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
      </Row>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Row gutter={[16, 16]} align="middle">
            <Col flex="auto">
              <Title level={4} style={{ margin: 0 }}>
                Quản lý Màu sắc
              </Title>
              <Text type="secondary">
                Quản lý danh sách màu sắc sản phẩm
              </Text>
            </Col>
            <Col>
              <Space>
                <Input
                  placeholder="Tìm kiếm màu..."
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
                  onClick={fetchColors}
                  loading={loading || refreshing}
                >
                  Làm mới
                </Button>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={() => setIsModalVisible(true)}
                >
                  Thêm Màu
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
          dataSource={filteredColors}
          rowKey="colorId"
          loading={loading || refreshing}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} của ${total} màu sắc`,
            pageSizeOptions: ['5', '10', '20', '50', '100'],
            size: 'default'
          }}
          scroll={{ x: 700 }}
        />
      </Card>

      <Modal
        title={<span style={{ fontSize: 22, fontWeight: 700 }}>{editingColor ? 'Sửa Màu' : 'Thêm Màu mới'}</span>}
        open={isModalVisible}
        onCancel={handleModalClose}
        footer={null}
        width={820}
        centered
        styles={{ body: { paddingTop: 16, paddingBottom: 12 } }}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={editingColor ? handleUpdateColor : handleCreateColor}
        >
          <Form.Item
            name="colorName"
            label="Tên màu"
            rules={[
              VALIDATION_RULES.REQUIRED,
              VALIDATION_RULES.COLOR_NAME,
              {
                pattern: new RegExp('^[\\\p{L}\\\p{N}\\- ]+$','u'),
                message: 'Chỉ cho phép chữ (có dấu), số, khoảng trắng và -',
              },
              {
                validator: async (_, value) => {
                  const val = (value || '').toString().trim().toLowerCase();
                  if (!val) return Promise.resolve();
                  const taken = await isColorNameTaken(val, editingColor?.colorId);
                  if (taken) return Promise.reject(new Error('Màu đã tồn tại'));
                  return Promise.resolve();
                },
              },
            ]}
          >
            <Input 
              placeholder="Nhập tên màu (VD: đỏ, xanh, đen)" 
              style={{ textTransform: 'capitalize' }}
              onChange={(e) => {
                e.target.value = e.target.value.toLowerCase();
              }}
            />
          </Form.Item>

          <Form.Item label="Xem trước màu">
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              <ColorPicker
                value={selectedColor}
                onChange={(color) => setSelectedColor(color.toHexString())}
                presets={[
                  {
                    label: 'Màu thông dụng',
                    colors: COMMON_COLORS,
                  },
                ]}
              />
              <div
                style={{
                  width: 40,
                  height: 40,
                  backgroundColor: selectedColor,
                  border: '1px solid #d9d9d9',
                  borderRadius: 4,
                }}
              />
              <Text type="secondary">{selectedColor}</Text>
            </div>
          </Form.Item>

          {editingColor && (
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
                loading={createColorMutation.isLoading || updateColorMutation.isLoading}
              >
                {editingColor ? 'Cập nhật' : 'Tạo'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ColorManagement;