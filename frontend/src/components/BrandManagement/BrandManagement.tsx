import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Space, Input, Modal, Form, notification, Typography, Row, Col, Statistic, Tooltip, Tag } from 'antd';
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, ShoppingOutlined, ReloadOutlined } from '@ant-design/icons';
import { Brand } from '../../types';
import { brandApi } from '../../services/api';
import { useMutation } from '../../hooks/useApi';
import { playSound } from '../../utils/sound';

const { Title, Text } = Typography;

const BrandManagement: React.FC = () => {
  const [brands, setBrands] = useState<Brand[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editing, setEditing] = useState<Brand | null>(null);
  const [form] = Form.useForm();

  const fetchBrands = async () => {
    setLoading(true);
    try {
      const data = await brandApi.getBrands();
      setBrands(data);
    } catch {
      notification.error({ message: 'Error', description: 'Failed to fetch brands' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchBrands(); }, []);

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

  const handleCreate = async (values: { brandName: string; logoUrl?: string; description?: string; website?: string; }) => {
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
            setIsModalVisible(false); form.resetFields(); fetchBrands();
          }
        } catch { notification.error({ message: 'Lỗi', description: 'Tạo thương hiệu thất bại' }); playSound('error'); }
      },
    });
  };

  const handleUpdate = async (values: { brandName: string; logoUrl?: string; description?: string; website?: string; }) => {
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
            setIsModalVisible(false); setEditing(null); form.resetFields(); fetchBrands();
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
        try { await deleteMut.mutate(record.brandId); notification.success({ message: 'Thành công', description: 'Xóa thương hiệu thành công' }); playSound('success'); setBrands(prev => prev.filter(b => b.brandId !== record.brandId)); fetchBrands(); } 
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
    { title: 'Website', dataIndex: 'website', key: 'website', render: (v: string) => v || '-' },
    { title: 'Thao tác', key: 'actions', width: 120, render: (_: any, record: Brand) => (
      <Space size="small">
        <Tooltip title="Sửa"><Button type="text" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue({ brandName: record.brandName, logoUrl: record.logoUrl, description: record.description, website: record.website }); setIsModalVisible(true); }} /></Tooltip>
        <Tooltip title="Xóa"><Button type="text" danger icon={<DeleteOutlined />} onClick={() => confirmDelete(record)} /></Tooltip>
      </Space>
    ) },
  ];

  const filtered = brands.filter(x => !searchText || x.brandName.toLowerCase().includes(searchText.toLowerCase()));

  return (
    <div>
      <Row gutter={[16,16]} style={{ marginBottom: 24 }}>
        <Col span={8}><Card><Statistic title="Tổng thương hiệu" value={brands.length} prefix={<ShoppingOutlined />} valueStyle={{ color: '#1890ff' }} /></Card></Col>
      </Row>
      <Card>
        <div style={{ marginBottom: 16 }}>
          <Row gutter={[16,16]} align="middle">
            <Col flex="auto"><Title level={4} style={{ margin: 0 }}>Quản lý Thương hiệu</Title><Text type="secondary">Quản lý danh sách thương hiệu sản phẩm</Text></Col>
            <Col>
              <Space>
                <Input placeholder="Tìm kiếm thương hiệu..." prefix={<SearchOutlined />} value={searchText} onChange={(e)=>setSearchText(e.target.value)} style={{ width: 220 }} />
                <Button icon={<ReloadOutlined />} onClick={fetchBrands} loading={loading}>Làm mới</Button>
                <Button type="primary" icon={<PlusOutlined />} onClick={()=>setIsModalVisible(true)}>Thêm Thương hiệu</Button>
              </Space>
            </Col>
          </Row>
        </div>
        <Table columns={columns} dataSource={filtered} rowKey="brandId" loading={loading} pagination={{ pageSize: 10, showSizeChanger: true }} scroll={{ x: 700 }} />
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
          <Form.Item name="website" label="Website" rules={[ { type: 'url', message: 'URL không hợp lệ' } ]}>
            <Input placeholder="https://vi-du.com" />
          </Form.Item>
          <Form.Item name="logoUrl" label="Logo URL" rules={[ { type: 'url', message: 'URL không hợp lệ' } ]}>
            <Input placeholder="https://..." />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={4} placeholder="Mô tả" />
          </Form.Item>
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


