import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Space, Input, Modal, Form, notification, Typography, Row, Col, Statistic, Tooltip, Tag, Select } from 'antd';
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, ShoppingOutlined, ReloadOutlined } from '@ant-design/icons';
import { Brand } from '../../types';
import { brandApi } from '../../services/api.ts';
import { useMutation } from '../../hooks/useApi.ts';
import { playSound } from '../../utils/sound.ts';

const { Title, Text } = Typography;

const BrandManagement: React.FC = () => {
  const [brands, setBrands] = useState<Brand[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editing, setEditing] = useState<Brand | null>(null);
  const [showAllRecords, setShowAllRecords] = useState(true); // Mặc định hiển thị tất cả
  const [form] = Form.useForm();

  const fetchBrands = async () => {
    setLoading(true);
    try {
      const data = await brandApi.getBrands(showAllRecords); // Sử dụng state để quyết định
      setBrands(data);
    } catch {
      notification.error({ message: 'Error', description: 'Failed to fetch brands' });
    } finally {
      setLoading(false);
    }
  };

  const refreshBrands = async () => {
    setRefreshing(true);
    try {
      const updatedBrands = await brandApi.getBrands(showAllRecords);
      setBrands(updatedBrands);
      return updatedBrands;
    } catch (e) {
      console.error('Error refreshing brands:', e);
      return [];
    } finally {
      setRefreshing(false);
    }
  };

  useEffect(() => { fetchBrands(); }, []);

  // Fetch lại data khi toggle thay đổi
  useEffect(() => {
    fetchBrands();
  }, [showAllRecords]);

  const createMut = useMutation(brandApi.createBrand);
  const updateMut = useMutation(brandApi.updateBrand);
  const deleteMut = useMutation(brandApi.deleteBrand);

  // Validation helpers
  const isBrandNameTaken = async (name: string, excludeId?: number) => {
    try {
      const found = await brandApi.getBrandByName(name);
      if (!found) return false;
      if (excludeId && found.brandId === excludeId) return false;
      return true;
    } catch {
      return false;
    }
  };

  const handleCreate = async (values: { brandName: string; logoUrl?: string; description?: string; }) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận tạo thương hiệu</span>,
      content: <div style={{ fontSize: 18 }}>Tạo thương hiệu "{values.brandName}"?</div>,
      okText: 'Tạo', cancelText: 'Hủy', okButtonProps: { size: 'large', type: 'primary' }, cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const res = await createMut.mutate(values);
          if (res) {
            notification.success({ message: 'Thành công', description: 'Tạo thương hiệu thành công' });
            playSound('success');
            setIsModalVisible(false); form.resetFields(); await refreshBrands();
          }
        } catch { notification.error({ message: 'Lỗi', description: 'Tạo thương hiệu thất bại' }); playSound('error'); }
      },
    });
  };

  const handleUpdate = async (values: { brandName: string; logoUrl?: string; description?: string; }) => {
    if (!editing) return;
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận cập nhật thương hiệu</span>,
      content: <div style={{ fontSize: 18 }}>Cập nhật thành "{values.brandName}"?</div>,
      okText: 'Cập nhật', cancelText: 'Hủy', okButtonProps: { size: 'large', type: 'primary' }, cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const res = await updateMut.mutate(editing.brandId, values);
          if (res) {
            notification.success({ message: 'Thành công', description: 'Cập nhật thương hiệu thành công' });
            playSound('success');
            setIsModalVisible(false); setEditing(null); form.resetFields(); await refreshBrands();
          }
        } catch { notification.error({ message: 'Lỗi', description: 'Cập nhật thương hiệu thất bại' }); playSound('error'); }
      },
    });
  };

  const confirmDelete = (record: Brand) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận xóa</span>,
      content: <div style={{ fontSize: 18 }}>Xóa thương hiệu "{record.brandName}" (ID: {record.brandId})?</div>,
      okText: 'Xóa', cancelText: 'Hủy', okButtonProps: { danger: true, size: 'large' }, cancelButtonProps: { size: 'large' }, centered: true,
      onOk: async () => {
        try { await deleteMut.mutate(record.brandId); notification.success({ message: 'Thành công', description: 'Xóa thương hiệu thành công' }); playSound('success'); setBrands(prev => prev.filter(b => b.brandId !== record.brandId)); await refreshBrands(); } 
        catch { notification.error({ message: 'Lỗi', description: 'Xóa thương hiệu thất bại' }); playSound('error'); }
      },
    });
  };

  const columns = [
    { title: 'ID', dataIndex: 'brandId', key: 'brandId', width: 80, sorter: (a: Brand, b: Brand) => a.brandId - b.brandId },
    { title: 'Tên thương hiệu', dataIndex: 'brandName', key: 'brandName', sorter: (a: Brand, b: Brand) => a.brandName.localeCompare(b.brandName), render: (text: string) => (
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
    ) },
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
      sorter: (a: Brand, b: Brand) => a.productCount - b.productCount,
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
          {count} sản phẩm
        </Tag>
      )
    },
    { title: 'Thao tác', key: 'actions', width: 120, render: (_: any, record: Brand) => (
      <Space size="small">
        <Tooltip title="Sửa"><Button type="text" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue({ brandName: record.brandName, logoUrl: record.logoUrl, description: record.description, status: record.status }); setIsModalVisible(true); }} /></Tooltip>
        <Tooltip title="Xóa"><Button type="text" danger icon={<DeleteOutlined />} onClick={() => confirmDelete(record)} /></Tooltip>
      </Space>
    ) },
  ];

  const filtered = brands.filter(brand => {
    const matchesSearch = !searchText || brand.brandName.toLowerCase().includes(searchText.toLowerCase());
    const matchesStatus = !statusFilter || brand.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div>
      <Row gutter={[16,16]} style={{ marginBottom: 24 }}>
        <Col span={8}><Card><Statistic title="Tổng thương hiệu" value={brands.length} prefix={<ShoppingOutlined />} valueStyle={{ color: '#1890ff' }} /></Card></Col>
        <Col span={8}><Card><Statistic title="Đang hoạt động" value={brands.filter(b => b.status === 'ACTIVE').length} prefix={<ShoppingOutlined />} valueStyle={{ color: '#52c41a' }} /></Card></Col>
        <Col span={8}><Card><Statistic title="Ngừng hoạt động" value={brands.filter(b => b.status === 'INACTIVE').length} prefix={<ShoppingOutlined />} valueStyle={{ color: '#ff4d4f' }} /></Card></Col>
      </Row>
      <Card>
        <div style={{ marginBottom: 16 }}>
          <Row gutter={[16,16]} align="middle">
            <Col flex="auto"><Title level={4} style={{ margin: 0 }}>Quản lý Thương hiệu</Title><Text type="secondary">Quản lý danh sách thương hiệu sản phẩm</Text></Col>
            <Col>
              <Space>
                <Input placeholder="Tìm kiếm thương hiệu..." prefix={<SearchOutlined />} value={searchText} onChange={(e)=>setSearchText(e.target.value)} style={{ width: 220 }} />
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
                <Button icon={<ReloadOutlined />} onClick={fetchBrands} loading={loading}>Làm mới</Button>
                <Button type="primary" icon={<PlusOutlined />} onClick={()=>setIsModalVisible(true)}>Thêm Thương hiệu</Button>
              </Space>
            </Col>
          </Row>
        </div>
        <Table 
          columns={columns} 
          dataSource={filtered} 
          rowKey="brandId" 
          loading={loading || refreshing} 
          pagination={{ 
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} của ${total} thương hiệu`,
            pageSizeOptions: ['5', '10', '20', '50', '100'],
            size: 'default'
          }} 
          scroll={{ x: 700 }} 
        />
      </Card>

      <Modal title={<span style={{ fontSize: 22, fontWeight: 700 }}>{editing ? 'Sửa Thương hiệu' : 'Thêm Thương hiệu mới'}</span>} open={isModalVisible} onCancel={()=>{ setIsModalVisible(false); setEditing(null); form.resetFields(); }} footer={null} width={760} centered>
        <Form form={form} layout="vertical" onFinish={editing ? handleUpdate : handleCreate}>
          <Form.Item name="brandName" label="Tên thương hiệu" rules={[
            { required: true, message: 'Vui lòng nhập tên thương hiệu' },
            { min: 1, max: 100, message: 'Độ dài 1 - 100 ký tự' },
            { pattern: new RegExp('^[\\\p{L}\\\p{N}\\- .&()]+$','u'), message: 'Chỉ cho phép chữ (có dấu), số, khoảng trắng và - . & ( )' },
            {
              validator: async (_, value) => {
                const val = (value || '').toString().trim();
                if (!val) return Promise.resolve();
                const taken = await isBrandNameTaken(val, editing?.brandId);
                if (taken) return Promise.reject(new Error('Tên thương hiệu đã tồn tại'));
                return Promise.resolve();
              },
            },
          ]}>
            <Input placeholder="Nhập tên thương hiệu" />
          </Form.Item>
          <Form.Item name="logoUrl" label="Logo URL" rules={[ { type: 'url', message: 'URL không hợp lệ' } ]}>
            <Input placeholder="https://..." />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={4} placeholder="Mô tả" />
          </Form.Item>
          
          {editing && (
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
              <Button size="large" onClick={()=>{ setIsModalVisible(false); setEditing(null); form.resetFields(); }}>Hủy</Button>
              <Button type="primary" htmlType="submit" size="large" loading={createMut.isLoading || updateMut.isLoading}>{editing ? 'Cập nhật' : 'Tạo'}</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default BrandManagement;


