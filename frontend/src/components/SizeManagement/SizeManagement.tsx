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
import { PAGINATION_CONFIG, VALIDATION_RULES } from '../../utils/constants';
import { useMutation } from '../../hooks/useApi';
import { playSound } from '../../utils/sound';

const { Title, Text } = Typography;

const SizeManagement: React.FC = () => {
  const [sizes, setSizes] = useState<Size[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingSize, setEditingSize] = useState<Size | null>(null);
  const [form] = Form.useForm();


  // Fetch sizes
  const fetchSizes = async () => {
    setLoading(true);
    try {
      const response = await sizeApi.getSizes();
      setSizes(response);
    } catch (error) {
      console.error('API Error:', error);
      message.error('Failed to fetch sizes from database');
      setSizes([]); // Show empty state when API fails
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSizes();
  }, []);

  // Mutations
  const createSizeMutation = useMutation(sizeApi.createSize);
  const updateSizeMutation = useMutation(sizeApi.updateSize);
  const deleteSizeMutation = useMutation(sizeApi.deleteSize);

  const handleCreateSize = async (values: CreateSizeRequest) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Confirm create size</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Create size "{values.sizeName}"?</div>,
      okText: 'Create',
      cancelText: 'Cancel',
      okButtonProps: { size: 'large', type: 'primary' },
      cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const result = await createSizeMutation.mutate(values);
          if (result) {
            notification.success({ message: <span style={{ fontSize: 20, fontWeight: 600 }}>Success</span>, description: <div style={{ fontSize: 18 }}>Size created successfully</div>, duration: 1.8, placement: 'top', style: { padding: 12 } });
            playSound('success');
            setIsModalVisible(false);
            form.resetFields();
            fetchSizes();
          }
        } catch (error) {
          console.error('Create Error:', error);
          notification.error({ message: <span style={{ fontSize: 20, fontWeight: 600 }}>Error</span>, description: <div style={{ fontSize: 18 }}>Failed to create size in database</div>, duration: 2.2, placement: 'top', style: { padding: 12 } });
          playSound('error');
        }
      },
    });
  };

  const handleUpdateSize = async (values: UpdateSizeRequest) => {
    if (!editingSize) return;
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Confirm update size</span>,
      icon: <ExclamationCircleOutlined />,
      content: <div style={{ fontSize: 18 }}>Update size to "{values.sizeName}"?</div>,
      okText: 'Update',
      cancelText: 'Cancel',
      okButtonProps: { size: 'large', type: 'primary' },
      cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const result = await updateSizeMutation.mutate(editingSize.sizeId, values);
          if (result) {
            notification.success({ message: <span style={{ fontSize: 20, fontWeight: 600 }}>Success</span>, description: <div style={{ fontSize: 18 }}>Size updated successfully</div>, duration: 1.8, placement: 'top', style: { padding: 12 } });
            playSound('success');
            setIsModalVisible(false);
            setEditingSize(null);
            form.resetFields();
            fetchSizes();
          }
        } catch (error) {
          notification.error({ message: <span style={{ fontSize: 20, fontWeight: 600 }}>Error</span>, description: <div style={{ fontSize: 18 }}>Failed to update size</div>, duration: 2.2, placement: 'top', style: { padding: 12 } });
          playSound('error');
        }
      },
    });
  };

  const handleDeleteSize = async (sizeId: number) => {
    try {
      await deleteSizeMutation.mutate(sizeId);
      notification.success({ message: <span style={{ fontSize: 20, fontWeight: 600 }}>Success</span>, description: <div style={{ fontSize: 18 }}>Size deleted successfully</div>, duration: 1.8, placement: 'top', style: { padding: 12 } });
      playSound('success');
      // Optimistic update
      setSizes(prev => prev.filter(s => s.sizeId !== sizeId));
      // Background refresh to stay in sync
      fetchSizes();
    } catch (error) {
      notification.error({ message: <span style={{ fontSize: 20, fontWeight: 600 }}>Error</span>, description: <div style={{ fontSize: 18 }}>Failed to delete size</div>, duration: 2.2, placement: 'top', style: { padding: 12 } });
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
      title: 'Size Name',
      dataIndex: 'sizeName',
      key: 'sizeName',
      sorter: (a: Size, b: Size) => a.sizeName.localeCompare(b.sizeName),
      render: (text: string) => (
        <Tag color="blue" style={{ fontSize: '14px', padding: '4px 12px' }}>
          {text}
        </Tag>
      ),
    },
    {
      // Removed Created At column
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      render: (_: any, record: Size) => (
        <Space size="small">
          <Tooltip title="Edit">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title="Delete">
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

  const filteredSizes = sizes.filter(size =>
    !searchText || size.sizeName.toLowerCase().includes(searchText.toLowerCase())
  );

  return (
    <div>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="Total Sizes"
              value={sizes.length}
              prefix={<TagsOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Active Sizes"
              value={sizes.length}
              prefix={<TagsOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Most Used"
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
                Size Management
              </Title>
              <Text type="secondary">
                Manage product sizes for your clothing store
              </Text>
            </Col>
            <Col>
              <Space>
                <Input
                  placeholder="Search sizes..."
                  prefix={<SearchOutlined />}
                  value={searchText}
                  onChange={(e) => setSearchText(e.target.value)}
                  style={{ width: 200 }}
                />
                <Button
                  icon={<ReloadOutlined />}
                  onClick={fetchSizes}
                  loading={loading}
                >
                  Refresh
                </Button>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={() => setIsModalVisible(true)}
                >
                  Add Size
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
          dataSource={filteredSizes}
          rowKey="sizeId"
          loading={loading}
          pagination={{
            ...PAGINATION_CONFIG,
            total: filteredSizes.length,
            showTotal: (total, range) =>
              `${range[0]}-${range[1]} of ${total} sizes`,
          }}
          scroll={{ x: 600 }}
        />
      </Card>

      <Modal
        title={<span style={{ fontSize: 22, fontWeight: 700 }}>{editingSize ? 'Edit Size' : 'Add New Size'}</span>}
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
            label="Size Name"
            rules={[VALIDATION_RULES.REQUIRED, VALIDATION_RULES.SIZE_NAME]}
          >
            <Input 
              placeholder="Enter size name (e.g., S, M, L, XL)" 
              style={{ textTransform: 'uppercase' }}
              onChange={(e) => {
                e.target.value = e.target.value.toUpperCase();
              }}
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button size="large" onClick={handleModalClose}>
                Cancel
              </Button>
              <Button
                type="primary"
                htmlType="submit"
                size="large"
                loading={createSizeMutation.isLoading || updateSizeMutation.isLoading}
              >
                {editingSize ? 'Update' : 'Create'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SizeManagement;