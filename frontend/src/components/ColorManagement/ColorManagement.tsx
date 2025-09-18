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
import { colorApi } from '../../services/api';
import { Color, CreateColorRequest, UpdateColorRequest } from '../../types';
import { PAGINATION_CONFIG, VALIDATION_RULES, COMMON_COLORS } from '../../utils/constants';
import { useMutation } from '../../hooks/useApi';
import { playSound } from '../../utils/sound';

const { Title, Text } = Typography;

const ColorManagement: React.FC = () => {
  const [colors, setColors] = useState<Color[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingColor, setEditingColor] = useState<Color | null>(null);
  const [selectedColor, setSelectedColor] = useState('#1890ff');
  const [form] = Form.useForm();


  // Fetch colors
  const fetchColors = async () => {
    setLoading(true);
    try {
      const response = await colorApi.getColors();
      setColors(response);
    } catch (error) {
      console.error('API Error:', error);
      message.error('Failed to fetch colors from database');
      setColors([]); // Show empty state when API fails
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchColors();
  }, []);

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
            fetchColors();
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
            fetchColors();
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
      fetchColors();
    } catch (error) {
      notification.error({ message: 'Lỗi', description: 'Xóa màu thất bại' });
      playSound('error');
    }
  };

  const confirmDeleteColor = (record: Color) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Confirm delete</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Delete color "{record.colorName}" (ID: {record.colorId})?</div>,
      okText: 'Delete',
      cancelText: 'Cancel',
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
    });
    setIsModalVisible(true);
  };

  const handleModalClose = () => {
    setIsModalVisible(false);
    setEditingColor(null);
    form.resetFields();
    setSelectedColor('#1890ff');
  };

  // Convert color name to hex (simplified mapping)
  const getColorHex = (colorName: string): string => {
    const colorMap: Record<string, string> = {
      'red': '#ff0000',
      'blue': '#0000ff',
      'green': '#00ff00',
      'yellow': '#ffff00',
      'black': '#000000',
      'white': '#ffffff',
      'gray': '#808080',
      'pink': '#ffc0cb',
      'orange': '#ffa500',
      'purple': '#800080',
    };
    return colorMap[colorName.toLowerCase()] || '#1890ff';
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
      title: 'Preview',
      key: 'preview',
      width: 120,
      render: (_: any, record: Color) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <div
            style={{
              width: 24,
              height: 24,
              backgroundColor: getColorHex(record.colorName),
              border: '1px solid #d9d9d9',
              borderRadius: 4,
            }}
          />
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {getColorHex(record.colorName)}
          </Text>
        </div>
      ),
    },
    {
      // Removed Created At column
    },
    {
      title: 'Actions',
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

  const filteredColors = colors.filter(color =>
    !searchText || color.colorName.toLowerCase().includes(searchText.toLowerCase())
  );

  return (
    <div>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="Total Colors"
              value={colors.length}
              prefix={<BgColorsOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Active Colors"
              value={colors.length}
              prefix={<BgColorsOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Most Popular"
              value="Black"
              prefix={<BgColorsOutlined />}
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
                <Button
                  icon={<ReloadOutlined />}
                  onClick={fetchColors}
                  loading={loading}
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
          loading={loading}
          pagination={{
            ...PAGINATION_CONFIG,
            total: filteredColors.length,
            showTotal: (total, range) =>
              `${range[0]}-${range[1]} of ${total} colors`,
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

          <Form.Item label="Color Preview">
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              <ColorPicker
                value={selectedColor}
                onChange={(color) => setSelectedColor(color.toHexString())}
                presets={[
                  {
                    label: 'Common Colors',
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