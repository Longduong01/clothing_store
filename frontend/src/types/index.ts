// Base types
export interface BaseEntity {
  createdAt: string;
  updatedAt: string;
}

// User types
export interface User extends BaseEntity {
  userId: number;
  username: string;
  email: string;
  phone?: string;
  role: UserRole;
}

export enum UserRole {
  CUSTOMER = 'CUSTOMER',
  ADMIN = 'ADMIN'
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  phone?: string;
  role: UserRole;
}

export interface UpdateUserRequest {
  username?: string;
  email?: string;
  phone?: string;
  role?: UserRole;
}

// Size types
export interface Size extends BaseEntity {
  sizeId: number;
  sizeName: string;
}

export interface CreateSizeRequest {
  sizeName: string;
}

export interface UpdateSizeRequest {
  sizeName: string;
}

// Color types
export interface Color extends BaseEntity {
  colorId: number;
  colorName: string;
}

export interface CreateColorRequest {
  colorName: string;
}

export interface UpdateColorRequest {
  colorName: string;
}

// Product types
export interface Product extends BaseEntity {
  productId: number;
  productName: string;
  description?: string;
  sku: string;
  price?: number; // Optional for parent products
  imageUrl?: string;
  thumbnailUrl?: string;
  stockQuantity?: number; // Optional for parent products
  totalStock?: number; // Tổng tồn kho từ variants
  status: ProductStatus;
  categoryName?: string;
  brandName?: string;
  category?: Category; // Optional for backward compatibility
  brand?: Brand; // Optional for backward compatibility
}

// Product Variant types
export interface ProductVariant extends BaseEntity {
  variantId: number;
  sku: string;
  price: number;
  stock: number;
  status: VariantStatus;
  imagePath?: string;
  product: SimpleRef;
  size: SimpleRef;
  color: SimpleRef;
}

export enum VariantStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  OUT_OF_STOCK = 'OUT_OF_STOCK'
}

export interface SimpleRef {
  id: number;
  name: string;
}

export interface CreateVariantRequest {
  productId: number;
  sizeId: number;
  colorId: number;
  sku: string;
  price: number;
  stock: number;
  status: VariantStatus;
}

export interface UpdateVariantRequest {
  sku: string;
  sizeId: number;
  colorId: number;
  price: number;
  stock: number;
  status: VariantStatus;
}

export interface BulkVariantItem {
  sizeId: number;
  colorId: number;
  price: number;
  stock: number;
  status: VariantStatus;
}

export interface BulkVariantCreateRequest {
  productId: number;
  variants: BulkVariantItem[];
}

export enum ProductStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  DISCONTINUED = 'DISCONTINUED'
}

export interface Category extends BaseEntity {
  categoryId: number;
  categoryName: string;
  imageUrl?: string;
  description?: string;
  parentId?: number;
}

export interface Brand extends BaseEntity {
  brandId: number;
  brandName: string;
  logoUrl?: string;
  description?: string;
  website?: string;
}

// API Response types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Table types
export interface TableColumn {
  key: string;
  title: string;
  dataIndex: string;
  sorter?: boolean;
  filterable?: boolean;
  render?: (value: any, record: any) => React.ReactNode;
}

// Form types
export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'email' | 'password' | 'select' | 'number' | 'textarea';
  required?: boolean;
  options?: { label: string; value: any }[];
  placeholder?: string;
  rules?: any[];
}

// Error types
export interface ApiError {
  message: string;
  errors?: Record<string, string>;
  status: number;
  timestamp: string;
  path: string;
}

// Loading states
export interface LoadingState {
  isLoading: boolean;
  error?: string;
}

// Filter types
export interface FilterOptions {
  search?: string;
  status?: string;
  role?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}
