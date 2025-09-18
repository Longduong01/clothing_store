// API endpoints
export const API_ENDPOINTS = {
  USERS: '/users',
  SIZES: '/sizes',
  COLORS: '/colors',
  PRODUCTS: '/products',
  CATEGORIES: '/categories',
  BRANDS: '/brands',
  ORDERS: '/orders',
  CARTS: '/carts',
} as const;

// Table pagination
export const PAGINATION_CONFIG = {
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: ['10', '20', '50', '100'],
  SHOW_SIZE_CHANGER: true,
  SHOW_QUICK_JUMPER: true,
  SHOW_TOTAL: (total: number, range: [number, number]) =>
    `${range[0]}-${range[1]} of ${total} items`,
} as const;

// Form validation rules
export const VALIDATION_RULES = {
  REQUIRED: { required: true, message: 'This field is required' },
  EMAIL: {
    type: 'email' as const,
    message: 'Please enter a valid email address',
  },
  USERNAME: {
    min: 3,
    max: 50,
    message: 'Username must be between 3 and 50 characters',
  },
  PASSWORD: {
    min: 6,
    message: 'Password must be at least 6 characters',
  },
  PHONE: {
    pattern: /^[0-9+\-\s()]*$/,
    message: 'Phone number should contain only digits, +, -, spaces, and parentheses',
  },
  SIZE_NAME: {
    min: 1,
    max: 10,
    message: 'Size name must be between 1 and 10 characters',
  },
  COLOR_NAME: {
    min: 1,
    max: 50,
    message: 'Color name must be between 1 and 50 characters',
  },
} as const;

// User roles
export const USER_ROLES = [
  { label: 'Customer', value: 'CUSTOMER' },
  { label: 'Admin', value: 'ADMIN' },
] as const;

// Product statuses
export const PRODUCT_STATUSES = [
  { label: 'Active', value: 'ACTIVE', color: 'green' },
  { label: 'Inactive', value: 'INACTIVE', color: 'red' },
  { label: 'Out of Stock', value: 'OUT_OF_STOCK', color: 'orange' },
] as const;

// Order statuses
export const ORDER_STATUSES = [
  { label: 'Pending', value: 'PENDING', color: 'blue' },
  { label: 'Confirmed', value: 'CONFIRMED', color: 'green' },
  { label: 'Shipped', value: 'SHIPPED', color: 'orange' },
  { label: 'Delivered', value: 'DELIVERED', color: 'green' },
  { label: 'Cancelled', value: 'CANCELLED', color: 'red' },
] as const;

// Common colors for color picker
export const COMMON_COLORS = [
  '#FF0000', '#00FF00', '#0000FF', '#FFFF00', '#FF00FF', '#00FFFF',
  '#000000', '#FFFFFF', '#808080', '#FFA500', '#800080', '#008000',
  '#FFC0CB', '#A52A2A', '#000080', '#808000', '#FF6347', '#40E0D0',
];

// Table columns configuration
export const TABLE_COLUMNS = {
  USER: [
    { key: 'userId', title: 'ID', dataIndex: 'userId', width: 80 },
    { key: 'username', title: 'Username', dataIndex: 'username', sorter: true },
    { key: 'email', title: 'Email', dataIndex: 'email', sorter: true },
    { key: 'phone', title: 'Phone', dataIndex: 'phone' },
    { key: 'role', title: 'Role', dataIndex: 'role', sorter: true },
    { key: 'createdAt', title: 'Created', dataIndex: 'createdAt', sorter: true },
  ],
  SIZE: [
    { key: 'sizeId', title: 'ID', dataIndex: 'sizeId', width: 80 },
    { key: 'sizeName', title: 'Size Name', dataIndex: 'sizeName', sorter: true },
    { key: 'createdAt', title: 'Created', dataIndex: 'createdAt', sorter: true },
  ],
  COLOR: [
    { key: 'colorId', title: 'ID', dataIndex: 'colorId', width: 80 },
    { key: 'colorName', title: 'Color Name', dataIndex: 'colorName', sorter: true },
    { key: 'preview', title: 'Preview', dataIndex: 'colorName', width: 100 },
    { key: 'createdAt', title: 'Created', dataIndex: 'createdAt', sorter: true },
  ],
} as const;

// Local storage keys
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  USER_INFO: 'userInfo',
  THEME: 'theme',
  LANGUAGE: 'language',
} as const;

// Date formats
export const DATE_FORMATS = {
  DISPLAY: 'DD/MM/YYYY HH:mm',
  DATE_ONLY: 'DD/MM/YYYY',
  TIME_ONLY: 'HH:mm',
  API: 'YYYY-MM-DD HH:mm:ss',
} as const;
