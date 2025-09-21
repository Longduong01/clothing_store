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
  status: UserStatus;
}

export enum UserRole {
  CUSTOMER = 'CUSTOMER',
  ADMIN = 'ADMIN'
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
}

// Gender types
export interface Gender extends BaseEntity {
  genderId: number;
  genderName: string;
  genderCode: string;
  description?: string;
  status: string;
}

export enum GenderStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED'
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
  status: SizeStatus;
  productCount: number;
}

export interface CreateSizeRequest {
  sizeName: string;
}

export interface UpdateSizeRequest {
  sizeName: string;
  status?: SizeStatus;
}

export enum SizeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED'
}

// Color types
export interface Color extends BaseEntity {
  colorId: number;
  colorName: string;
  status: ColorStatus;
  productCount: number;
}

export interface CreateColorRequest {
  colorName: string;
}

export interface UpdateColorRequest {
  colorName: string;
  status?: ColorStatus;
}

export enum ColorStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED'
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
  gender?: Gender;
  categoryName?: string;
  brandName?: string;
  categories?: Category[]; // Multiple categories support
  categoriesString?: string; // Comma-separated category names for display
  category?: Category; // Optional for backward compatibility
  brand?: Brand; // Optional for backward compatibility
}

// Product Request types
export interface ProductCreateRequest {
  productName: string;
  description?: string;
  sku: string;
  status: string;
  genderId?: number;
  brandId: number;
  categoryIds?: number[];
  categoryId?: number; // For backward compatibility
  imageUrl?: string;
  thumbnailUrl?: string;
}

export interface ProductUpdateRequest {
  productName?: string;
  sku?: string;
  status?: string;
  genderId?: number;
  description?: string;
  imageUrl?: string;
  thumbnailUrl?: string;
  categoryId?: number;
  brandId?: number;
  categoryIds?: number[];
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

export enum CategoryStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED'
}

export enum BrandStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED'
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
  status: CategoryStatus;
  productCount: number;
}

export interface Brand extends BaseEntity {
  brandId: number;
  brandName: string;
  logoUrl?: string;
  description?: string;
  website?: string;
  status: BrandStatus;
  productCount: number;
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
