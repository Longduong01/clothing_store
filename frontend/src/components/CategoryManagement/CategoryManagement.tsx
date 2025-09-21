import React, { useEffect, useMemo, useState } from 'react';
import { Card, Table, Button, Space, Input, Modal, Form, notification, Typography, Row, Col, Statistic, Tooltip, TreeSelect, Tag, Upload, Select } from 'antd';
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, FileTextOutlined, ReloadOutlined, UploadOutlined, CameraOutlined } from '@ant-design/icons';
import { Category } from '../../types';
import { categoryApi, testApi } from '../../services/api';
import { useMutation } from '../../hooks/useApi';
import { playSound } from '../../utils/sound';

const { Title, Text } = Typography;

const CategoryManagement: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editing, setEditing] = useState<Category | null>(null);
  const [showAllRecords, setShowAllRecords] = useState(true); // Mặc định hiển thị tất cả
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState<any[]>([]);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const data = await categoryApi.getCategories(showAllRecords); // Sử dụng state để quyết định
      setCategories(data);
    } catch {
      notification.error({ message: 'Lỗi', description: 'Không thể tải danh sách danh mục' });
    } finally { setLoading(false); }
  };

  const refreshCategories = async () => {
    setRefreshing(true);
    try {
      const updatedCategories = await categoryApi.getCategories(showAllRecords);
      setCategories(updatedCategories);
      return updatedCategories;
    } catch (e) {
      console.error('Error refreshing categories:', e);
      return [];
    } finally {
      setRefreshing(false);
    }
  };

  useEffect(() => { fetchCategories(); }, []);

  // Fetch lại data khi toggle thay đổi
  useEffect(() => {
    fetchCategories();
  }, [showAllRecords]);

  const createMut = useMutation(categoryApi.createCategory);
  const updateMut = useMutation(categoryApi.updateCategory);
  const deleteMut = useMutation(categoryApi.deleteCategory);
  const uploadImageMut = useMutation(testApi.uploadProductImages);

  // Handle direct image upload from table
  const handleImageUpload = (categoryId: number, file: File) => {
    Modal.confirm({
      title: <span style={{ fontSize: 18, fontWeight: 700 }}>Xác nhận upload ảnh</span>,
      content: <div style={{ fontSize: 16 }}>Upload ảnh cho danh mục này?</div>,
      okText: 'Upload', cancelText: 'Hủy', okButtonProps: { size: 'large', type: 'primary' }, cancelButtonProps: { size: 'large' },
      onOk: async () => {
        try {
          const files = [file];
          const uploadResult = await uploadImageMut.mutate(0, files);
          if (uploadResult && uploadResult.length > 0) {
            const imageUrl = uploadResult[0].imageUrl || uploadResult[0].url;
            
            // Update category with new image
            const category = categories.find(c => c.categoryId === categoryId);
            if (category) {
              await updateMut.mutate(categoryId, {
                categoryName: category.categoryName,
                parentId: category.parentId,
                description: category.description,
                imageUrl: imageUrl
              });
            }
            
            notification.success({ 
              message: 'Thành công', 
              description: 'Upload ảnh danh mục thành công' 
            });
            playSound('success');
            await refreshCategories();
          }
        } catch (error) {
          notification.error({ 
            message: 'Lỗi', 
            description: 'Upload ảnh thất bại' 
          });
          playSound('error');
        }
      },
    });
  };

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
          // Xử lý upload ảnh nếu có
          let finalImageUrl = values.imageUrl;
          if (fileList.length > 0 && fileList[0].originFileObj) {
            try {
              const files = [fileList[0].originFileObj];
              const uploadResult = await uploadImageMut.mutate(0, files); // Sử dụng productId = 0 tạm thời
              if (uploadResult && uploadResult.length > 0) {
                finalImageUrl = uploadResult[0].imageUrl || uploadResult[0].url;
              }
            } catch (uploadError) {
              console.error('Upload image failed:', uploadError);
              // Vẫn tiếp tục tạo category mà không có ảnh
            }
          }

          const categoryData = {
            categoryName: values.categoryName,
            parentId: values.parentId || undefined, // Use undefined instead of null
            description: values.description || '',
            imageUrl: finalImageUrl || ''
          };

          console.log('Creating category with data:', categoryData);

          const res = await createMut.mutate(categoryData);
          if (res) {
            notification.success({ message: 'Thành công', description: 'Tạo danh mục thành công' });
            playSound('success');
            setIsModalVisible(false); 
            form.resetFields(); 
            setFileList([]);
            await refreshCategories();
          }
        } catch (error) { 
          console.error('Create category error:', error);
          notification.error({ message: 'Lỗi', description: 'Tạo danh mục thất bại' }); 
          playSound('error'); 
        }
      },
    });
  };

  const handleUpdate = async (values: { categoryName: string; parentId?: number; description?: string; imageUrl?: string; status?: string; }) => {
    if (!editing) return;
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận cập nhật danh mục</span>,
      content: <div style={{ fontSize: 18 }}>Cập nhật thành "{values.categoryName}"?</div>,
      okText: 'Cập nhật', cancelText: 'Hủy', okButtonProps: { size: 'large', type: 'primary' }, cancelButtonProps: { size: 'large' },
      onOk: async () => {
        playSound('confirm');
        try {
          // Xử lý upload ảnh nếu có
          let finalImageUrl = values.imageUrl || editing.imageUrl;
          if (fileList.length > 0 && fileList[0].originFileObj) {
            try {
              const files = [fileList[0].originFileObj];
              const uploadResult = await uploadImageMut.mutate(0, files);
              if (uploadResult && uploadResult.length > 0) {
                finalImageUrl = uploadResult[0].imageUrl || uploadResult[0].url;
              }
            } catch (uploadError) {
              console.error('Upload image failed:', uploadError);
              // Vẫn tiếp tục cập nhật category mà không có ảnh mới
            }
          }

          const categoryData = {
            categoryName: values.categoryName,
            parentId: values.parentId || undefined, // Use undefined instead of null
            description: values.description || '',
            imageUrl: finalImageUrl || '',
            status: values.status || editing.status // Include status field
          };

          console.log('Updating category with data:', categoryData);
          console.log('Category ID:', editing.categoryId);

          const res = await updateMut.mutate(editing.categoryId, categoryData);
          console.log('Update response:', res);
          
          if (res) {
            notification.success({ message: 'Thành công', description: 'Cập nhật danh mục thành công' });
            playSound('success');
            setIsModalVisible(false); 
            setEditing(null); 
            form.resetFields(); 
            setFileList([]);
            await refreshCategories();
          }
        } catch (error) { 
          console.error('Update category error:', error);
          notification.error({ message: 'Lỗi', description: 'Cập nhật danh mục thất bại' }); 
          playSound('error'); 
        }
      },
    });
  };

  const confirmDelete = (record: Category) => {
    Modal.confirm({
      title: <span style={{ fontSize: 20, fontWeight: 700 }}>Xác nhận xóa</span>,
      content: <div style={{ fontSize: 18 }}>Xóa danh mục "{record.categoryName}" (ID: {record.categoryId})?</div>,
      okText: 'Xóa', cancelText: 'Hủy', okButtonProps: { danger: true, size: 'large' }, cancelButtonProps: { size: 'large' }, centered: true,
      onOk: async () => {
        try { 
          await deleteMut.mutate(record.categoryId); 
          notification.success({ message: 'Thành công', description: 'Xóa danh mục thành công' }); 
          playSound('success'); 
          setCategories(prev => prev.filter(c => c.categoryId !== record.categoryId)); 
          await refreshCategories(); 
        }
        catch { notification.error({ message: 'Lỗi', description: 'Xóa danh mục thất bại' }); playSound('error'); }
      },
    });
  };

  const columns = [
    { title: 'ID', dataIndex: 'categoryId', key: 'categoryId', width: 80, sorter: (a: Category, b: Category) => a.categoryId - b.categoryId },
    { 
      title: 'Tên danh mục', 
      dataIndex: 'categoryName', 
      key: 'categoryName', 
      sorter: (a: Category, b: Category) => a.categoryName.localeCompare(b.categoryName), 
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
      ) 
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
      title: 'Danh mục cha', 
      dataIndex: 'parent', 
      key: 'parent', 
      render: (parent: any) => parent ? parent.categoryName : '-' 
    },
    { 
      title: 'Ảnh', 
      dataIndex: 'imageUrl', 
      key: 'imageUrl',
      width: 120,
      render: (imageUrl: string, record: Category) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {imageUrl ? (
            <img 
              src={imageUrl} 
              alt="Category" 
              style={{ 
                width: 50, 
                height: 50, 
                objectFit: 'cover', 
                borderRadius: 4,
                border: '1px solid #d9d9d9'
              }} 
            />
          ) : (
            <div style={{ 
              width: 50, 
              height: 50, 
              backgroundColor: '#f5f5f5', 
              borderRadius: 4, 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center',
              color: '#999',
              fontSize: 10,
              textAlign: 'center',
              lineHeight: 1.2,
              padding: 4,
              border: '1px solid #d9d9d9'
            }}>
              Chưa có ảnh
            </div>
          )}
          <Upload
            beforeUpload={(file) => {
              handleImageUpload(record.categoryId, file);
              return false;
            }}
            accept="image/*"
            showUploadList={false}
          >
            <Button 
              size="small" 
              icon={<CameraOutlined />} 
              type="text"
              style={{ 
                padding: '4px 8px',
                height: 'auto',
                fontSize: 12
              }}
            >
              Upload
            </Button>
          </Upload>
        </div>
      )
    },
    { 
      title: 'Đang được sử dụng', 
      dataIndex: 'productCount', 
      key: 'productCount', 
      width: 120,
      sorter: (a: Category, b: Category) => a.productCount - b.productCount,
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
    { 
      title: 'Thao tác', 
      key: 'actions', 
      width: 120, 
      render: (_: any, record: Category) => (
        <Space size="small">
          <Tooltip title="Sửa">
            <Button 
              type="text" 
              icon={<EditOutlined />} 
              onClick={() => { 
                setEditing(record); 
                console.log('Editing record:', record);
                form.setFieldsValue({ 
                  categoryName: record.categoryName, 
                  parentId: record.parentId || undefined, 
                  description: record.description || '', 
                  imageUrl: record.imageUrl || '',
                  status: record.status
                }); 
                setFileList([]); // Reset file list when editing
                setIsModalVisible(true); 
              }} 
            />
          </Tooltip>
          <Tooltip title="Xóa">
            <Button 
              type="text" 
              danger 
              icon={<DeleteOutlined />} 
              onClick={() => confirmDelete(record)} 
            />
          </Tooltip>
        </Space>
      ) 
    },
  ];

  const filtered = categories.filter(category => {
    const matchesSearch = !searchText || category.categoryName.toLowerCase().includes(searchText.toLowerCase());
    const matchesStatus = !statusFilter || category.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div>
      <Row gutter={[16,16]} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic 
              title="Tổng danh mục" 
              value={categories.length} 
              prefix={<FileTextOutlined />} 
              valueStyle={{ color: '#1890ff' }} 
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="Đang hoạt động" 
              value={categories.filter(c => c.status === 'ACTIVE').length} 
              prefix={<FileTextOutlined />} 
              valueStyle={{ color: '#52c41a' }} 
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="Ngừng hoạt động" 
              value={categories.filter(c => c.status === 'INACTIVE').length} 
              prefix={<FileTextOutlined />} 
              valueStyle={{ color: '#ff4d4f' }} 
            />
          </Card>
        </Col>
      </Row>
      <Card>
        <div style={{ marginBottom: 16 }}>
          <Row gutter={[16,16]} align="middle">
            <Col flex="auto">
              <Title level={4} style={{ margin: 0 }}>Quản lý danh mục</Title>
              <Text type="secondary">Quản lý các danh mục sản phẩm</Text>
            </Col>
            <Col>
              <Space>
                <Input 
                  placeholder="Tìm kiếm danh mục..." 
                  prefix={<SearchOutlined />} 
                  value={searchText} 
                  onChange={(e)=>setSearchText(e.target.value)} 
                  style={{ width: 240 }} 
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
                  onClick={fetchCategories} 
                  loading={loading || refreshing}
                >
                  Làm mới
                </Button>
                <Button 
                  type="primary" 
                  icon={<PlusOutlined />} 
                  onClick={()=>{
                    setIsModalVisible(true);
                    setEditing(null);
                    form.resetFields();
                    setFileList([]);
                  }}
                >
                  Thêm danh mục
                </Button>
              </Space>
            </Col>
          </Row>
        </div>
        <Table 
          columns={columns} 
          dataSource={filtered} 
          rowKey="categoryId" 
          loading={loading || refreshing} 
          pagination={{ 
            pageSize: 10, 
            showSizeChanger: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} của ${total} danh mục`
          }} 
          scroll={{ x: 800 }} 
        />
      </Card>

      <Modal 
        title={
          <span style={{ fontSize: 22, fontWeight: 700 }}>
            {editing ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới'}
          </span>
        } 
        open={isModalVisible} 
        onCancel={()=>{ 
          setIsModalVisible(false); 
          setEditing(null); 
          form.resetFields(); 
          setFileList([]);
        }} 
        footer={null} 
        width={780} 
        centered
      >
        <Form 
          form={form} 
          layout="vertical" 
          onFinish={(values) => {
            console.log('Form submitted with values:', values);
            if (editing) {
              handleUpdate(values);
            } else {
              handleCreate(values);
            }
          }}
        >
          <Form.Item 
            name="categoryName" 
            label="Tên danh mục" 
            rules={[
              { required: true, message: 'Tên danh mục là bắt buộc' },
              { min: 1, max: 100, message: 'Độ dài từ 1-100 ký tự' },
              { pattern: new RegExp('^[\\\p{L}\\\p{N}\\- .&()]+$','u'), message: 'Chỉ cho phép chữ (có dấu), số, khoảng trắng và - . & ( )' },
              {
                validator: async (_, value) => {
                  const val = (value || '').toString().trim();
                  if (!val) return Promise.resolve();
                  const taken = await isCategoryNameTaken(val, editing?.categoryId);
                  if (taken) return Promise.reject(new Error('Tên danh mục đã tồn tại'));
                  return Promise.resolve();
                },
              },
            ]}
          >
            <Input placeholder="Nhập tên danh mục" />
          </Form.Item>
          
          <Form.Item name="parentId" label="Danh mục cha">
            <TreeSelect
              allowClear
              treeData={treeData}
              placeholder="Chọn danh mục cha (tùy chọn)"
              style={{ width: '100%' }}
              onChange={(value) => {
                console.log('ParentId changed to:', value);
              }}
            />
          </Form.Item>
          
          <Form.Item label="Ảnh danh mục">
            <Upload
              beforeUpload={() => false}
              fileList={fileList}
              onChange={({ fileList }) => setFileList(fileList)}
              listType="picture-card"
              maxCount={1}
              accept="image/*"
            >
              {fileList.length >= 1 ? null : (
                <div>
                  <UploadOutlined />
                  <div style={{ marginTop: 8 }}>Upload ảnh</div>
                </div>
              )}
            </Upload>
            <div style={{ marginTop: 8, fontSize: 12, color: '#666' }}>
              Chọn ảnh đại diện cho danh mục (JPG, PNG, GIF)
            </div>
          </Form.Item>
          
          <Form.Item name="imageUrl" label="Hoặc nhập link ảnh" rules={[ { type: 'url', message: 'Nhập URL hợp lệ' } ]}>
            <Input placeholder="https://..." />
          </Form.Item>
          
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea 
              rows={4} 
              placeholder="Mô tả về danh mục này..." 
            />
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
              <Button 
                size="large" 
                onClick={()=>{ 
                  setIsModalVisible(false); 
                  setEditing(null); 
                  form.resetFields(); 
                  setFileList([]);
                }}
              >
                Hủy
              </Button>
              <Button 
                type="primary" 
                htmlType="submit" 
                size="large" 
                loading={createMut.isLoading || updateMut.isLoading}
              >
                {editing ? 'Cập nhật' : 'Tạo mới'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CategoryManagement;


