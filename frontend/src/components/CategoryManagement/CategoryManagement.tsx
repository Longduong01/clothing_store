import React, { useEffect, useMemo, useState } from 'react';
import { Card, Table, Button, Space, Input, Modal, Form, notification, Typography, Row, Col, Statistic, Tooltip, TreeSelect, Tag } from 'antd';
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, FileTextOutlined, ReloadOutlined } from '@ant-design/icons';
import { Category } from '../../types';
import { categoryApi } from '../../services/api';
import { useMutation } from '../../hooks/useApi';
import { playSound } from '../../utils/sound';

const { Title, Text } = Typography;

const CategoryManagement: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editing, setEditing] = useState<Category | null>(null);
  const [form] = Form.useForm();

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const data = await categoryApi.getCategories();
      setCategories(data);
    } catch {
      notification.error({ message: 'Error', description: 'Failed to fetch categories' });
    } finally { setLoading(false); }
  };

  useEffect(() => { fetchCategories(); }, []);

  const createMut = useMutation(categoryApi.createCategory);
  const updateMut = useMutation(categoryApi.updateCategory);
  const deleteMut = useMutation(categoryApi.deleteCategory);

  // Validation helpers
  const isCategoryNameTaken = async (name: string, excludeId?: number) => {
    try {
      const found = await categoryApi.getCategoryByName(name);
      if (!found) return false;
      if (excludeId && found.categoryId === excludeId) return false;
      return true;
    } catch {
      return false;
    }
  };

  const treeData = useMemo(() => {
    return categories.map(c => ({ value: c.categoryId, title: c.categoryName }));
  }, [categories]);

  const handleCreate = async (values: { categoryName: string; parentId?: number; description?: string; imageUrl?: string; }) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận tạo danh mục</span>,
      content: <div style={{ fontSize: 18 }}>Tạo danh mục "{values.categoryName}"?</div>,
      okText: 'Tạo', cancelText: 'Hủy', okButtonProps: { size: 'large', type: 'primary' }, cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const res = await createMut.mutate(values);
          if (res) {
            notification.success({ message: 'Thành công', description: 'Tạo danh mục thành công' });
            playSound('success');
            setIsModalVisible(false); form.resetFields(); fetchCategories();
          }
        } catch { notification.error({ message: 'Lỗi', description: 'Tạo danh mục thất bại' }); playSound('error'); }
      },
    });
  };

  const handleUpdate = async (values: { categoryName: string; parentId?: number; description?: string; imageUrl?: string; }) => {
    if (!editing) return;
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận cập nhật danh mục</span>,
      content: <div style={{ fontSize: 18 }}>Cập nhật thành "{values.categoryName}"?</div>,
      okText: 'Cập nhật', cancelText: 'Hủy', okButtonProps: { size: 'large', type: 'primary' }, cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          const res = await updateMut.mutate(editing.categoryId, values);
          if (res) {
            notification.success({ message: 'Thành công', description: 'Cập nhật danh mục thành công' });
            playSound('success');
            setIsModalVisible(false); setEditing(null); form.resetFields(); fetchCategories();
          }
        } catch { notification.error({ message: 'Lỗi', description: 'Cập nhật danh mục thất bại' }); playSound('error'); }
      },
    });
  };

  const confirmDelete = (record: Category) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận xóa</span>,
      content: <div style={{ fontSize: 18 }}>Xóa danh mục "{record.categoryName}" (ID: {record.categoryId})?</div>,
      okText: 'Xóa', cancelText: 'Hủy', okButtonProps: { danger: true, size: 'large' }, cancelButtonProps: { size: 'large' }, centered: true,
      onOk: async () => {
        try { await deleteMut.mutate(record.categoryId); notification.success({ message: 'Thành công', description: 'Xóa danh mục thành công' }); playSound('success'); setCategories(prev => prev.filter(c => c.categoryId !== record.categoryId)); fetchCategories(); }
        catch { notification.error({ message: 'Lỗi', description: 'Xóa danh mục thất bại' }); playSound('error'); }
      },
    });
  };

  const columns = [
    { title: 'ID', dataIndex: 'categoryId', key: 'categoryId', width: 80, sorter: (a: Category, b: Category) => a.categoryId - b.categoryId },
    { title: 'Tên danh mục', dataIndex: 'categoryName', key: 'categoryName', sorter: (a: Category, b: Category) => a.categoryName.localeCompare(b.categoryName), render: (text: string) => (
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
    { title: 'Cha', dataIndex: 'parentId', key: 'parentId', render: (v: number) => v ? categories.find(c => c.categoryId === v)?.categoryName || v : '-' },
    { title: 'Thao tác', key: 'actions', width: 120, render: (_: any, record: Category) => (
      <Space size="small">
        <Tooltip title="Sửa"><Button type="text" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue({ categoryName: record.categoryName, parentId: record.parentId, description: record.description, imageUrl: record.imageUrl }); setIsModalVisible(true); }} /></Tooltip>
        <Tooltip title="Xóa"><Button type="text" danger icon={<DeleteOutlined />} onClick={() => confirmDelete(record)} /></Tooltip>
      </Space>
    ) },
  ];

  const filtered = categories.filter(x => !searchText || x.categoryName.toLowerCase().includes(searchText.toLowerCase()));

  return (
    <div>
      <Row gutter={[16,16]} style={{ marginBottom: 24 }}>
        <Col span={8}><Card><Statistic title="Total Categories" value={categories.length} prefix={<FileTextOutlined />} valueStyle={{ color: '#1890ff' }} /></Card></Col>
      </Row>
      <Card>
        <div style={{ marginBottom: 16 }}>
          <Row gutter={[16,16]} align="middle">
            <Col flex="auto"><Title level={4} style={{ margin: 0 }}>Category Management</Title><Text type="secondary">Manage product categories</Text></Col>
            <Col>
              <Space>
                <Input placeholder="Search categories..." prefix={<SearchOutlined />} value={searchText} onChange={(e)=>setSearchText(e.target.value)} style={{ width: 240 }} />
                <Button icon={<ReloadOutlined />} onClick={fetchCategories} loading={loading}>Refresh</Button>
                <Button type="primary" icon={<PlusOutlined />} onClick={()=>setIsModalVisible(true)}>Add Category</Button>
              </Space>
            </Col>
          </Row>
        </div>
        <Table columns={columns} dataSource={filtered} rowKey="categoryId" loading={loading} pagination={{ pageSize: 10, showSizeChanger: true }} scroll={{ x: 760 }} />
      </Card>

      <Modal title={<span style={{ fontSize: 22, fontWeight: 700 }}>{editing ? 'Edit Category' : 'Add New Category'}</span>} open={isModalVisible} onCancel={()=>{ setIsModalVisible(false); setEditing(null); form.resetFields(); }} footer={null} width={780} centered>
        <Form form={form} layout="vertical" onFinish={editing ? handleUpdate : handleCreate}>
          <Form.Item name="categoryName" label="Category Name" rules={[
            { required: true, message: 'Category name is required' },
            { min: 1, max: 100, message: 'Length 1-100' },
            { pattern: new RegExp('^[\\\p{L}\\\p{N}\\- .&()]+$','u'), message: 'Chỉ cho phép chữ (có dấu), số, khoảng trắng và - . & ( )' },
            {
              validator: async (_, value) => {
                const val = (value || '').toString().trim();
                if (!val) return Promise.resolve();
                const taken = await isCategoryNameTaken(val, editing?.categoryId);
                if (taken) return Promise.reject(new Error('Category name already exists'));
                return Promise.resolve();
              },
            },
          ]}>
            <Input placeholder="Enter category name" />
          </Form.Item>
          <Form.Item name="parentId" label="Parent Category">
            <TreeSelect
              allowClear
              treeData={treeData}
              placeholder="Select parent (optional)"
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item name="imageUrl" label="Image URL" rules={[ { type: 'url', message: 'Enter a valid URL' } ]}>
            <Input placeholder="https://..." />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={4} placeholder="Description" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button size="large" onClick={()=>{ setIsModalVisible(false); setEditing(null); form.resetFields(); }}>Cancel</Button>
              <Button type="primary" htmlType="submit" size="large" loading={createMut.isLoading || updateMut.isLoading}>{editing ? 'Update' : 'Create'}</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CategoryManagement;


