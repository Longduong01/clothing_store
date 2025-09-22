import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { message } from 'antd';
import { 
  User, 
  CreateUserRequest, 
  UpdateUserRequest,
  Product,
  Size,
  CreateSizeRequest,
  UpdateSizeRequest,
  Color,
  CreateColorRequest,
  UpdateColorRequest,
  Brand,
  Category,
  Gender,
  ProductVariant,
  CreateVariantRequest,
  UpdateVariantRequest,
  BulkVariantCreateRequest,
  FilterOptions,
  ProductImage,
  ColorImage
} from '../types';

// Create axios instance
const api: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    } else if (error.response?.status >= 500) {
      message.error('Server error. Please try again later.');
    } else if (error.response?.data?.message) {
      message.error(error.response.data.message);
    } else {
      message.error('An unexpected error occurred.');
    }
    return Promise.reject(error);
  }
);

// User API
export const userApi = {
  // Get all users with pagination
  getUsers: async (params?: FilterOptions, includeInactive: boolean = false): Promise<User[]> => {
    const queryParams = { ...params, includeInactive };
    const response = await api.get('/users', { params: queryParams });
    return response.data;
  },

  // Get user by ID
  getUserById: async (id: number): Promise<User> => {
    const response = await api.get(`/users/${id}`);
    return response.data;
  },

  // Get user by username
  getUserByUsername: async (username: string): Promise<User> => {
    const response = await api.get(`/users/username/${username}`);
    return response.data;
  },

  // Create new user
  createUser: async (userData: CreateUserRequest): Promise<User> => {
    const response = await api.post('/users', userData);
    return response.data;
  },

  // Update user
  updateUser: async (id: number, userData: UpdateUserRequest): Promise<User> => {
    const response = await api.put(`/users/${id}`, userData);
    return response.data;
  },

  // Delete user
  deleteUser: async (id: number): Promise<void> => {
    await api.delete(`/users/${id}`);
  },

  // Get user count
  getUserCount: async (): Promise<number> => {
    const response = await api.get('/users/count');
    return response.data;
  },
};

// Size API
export const sizeApi = {
  // Get all sizes
  getSizes: async (includeInactive: boolean = false): Promise<Size[]> => {
    const response = await api.get('/sizes', { params: { includeInactive } });
    return response.data;
  },

  // Get size by ID
  getSizeById: async (id: number): Promise<Size> => {
    const response = await api.get(`/sizes/${id}`);
    return response.data;
  },

  // Get size by name
  getSizeByName: async (name: string): Promise<Size | null> => {
    const encoded = encodeURIComponent(name);
    const response = await api.get(`/sizes/name/${encoded}`, {
      validateStatus: (status) => (status >= 200 && status < 300) || status === 404,
    });
    if (response.status === 404) return null;
    return response.data;
  },

  // Create new size
  createSize: async (sizeData: CreateSizeRequest): Promise<Size> => {
    const response = await api.post('/sizes', sizeData);
    return response.data;
  },

  // Update size
  updateSize: async (id: number, sizeData: UpdateSizeRequest): Promise<Size> => {
    const response = await api.put(`/sizes/${id}`, sizeData);
    return response.data;
  },

  // Delete size
  deleteSize: async (id: number): Promise<void> => {
    await api.delete(`/sizes/${id}`);
  },

  // Get size count
  getSizeCount: async (): Promise<number> => {
    const response = await api.get('/sizes/count');
    return response.data;
  },
};

// Color API
export const colorApi = {
  // Get all colors
  getColors: async (includeInactive: boolean = false): Promise<Color[]> => {
    const response = await api.get('/colors', { params: { includeInactive } });
    return response.data;
  },

  // Get color by ID
  getColorById: async (id: number): Promise<Color> => {
    const response = await api.get(`/colors/${id}`);
    return response.data;
  },

  // Get color by name
  getColorByName: async (name: string): Promise<Color | null> => {
    const encoded = encodeURIComponent(name);
    const response = await api.get(`/colors/name/${encoded}`, {
      validateStatus: (status) => (status >= 200 && status < 300) || status === 404,
    });
    if (response.status === 404) return null;
    return response.data;
  },

  // Create new color
  createColor: async (colorData: CreateColorRequest): Promise<Color> => {
    const response = await api.post('/colors', colorData);
    return response.data;
  },

  // Update color
  updateColor: async (id: number, colorData: UpdateColorRequest): Promise<Color> => {
    const response = await api.put(`/colors/${id}`, colorData);
    return response.data;
  },

  // Delete color
  deleteColor: async (id: number): Promise<void> => {
    await api.delete(`/colors/${id}`);
  },

  // Get color count
  getColorCount: async (): Promise<number> => {
    const response = await api.get('/colors/count');
    return response.data;
  },

  // Color Images API
  getColorImages: async (colorId: number): Promise<ColorImage[]> => {
    const response = await api.get(`/colors/${colorId}/images`);
    return response.data;
  },
  uploadColorImages: async (colorId: number, images: FormData): Promise<ColorImage[]> => {
    const response = await api.post(`/colors/${colorId}/images`, images);
    return response.data;
  },
  deleteColorImage: async (colorId: number, imageId: number): Promise<void> => {
    await api.delete(`/colors/${colorId}/images/${imageId}`);
  },
  setPrimaryColorImage: async (colorId: number, imageId: number): Promise<ColorImage> => {
    const response = await api.put(`/colors/${colorId}/images/${imageId}/primary`);
    return response.data;
  },
  deleteColorImages: async (colorId: number): Promise<void> => {
    await api.delete(`/colors/${colorId}/images`);
  },
};

// Auth API
export const authApi = {
  login: async (data: { username: string; password: string }): Promise<{ token: string; user: User }> => {
    const response = await api.post('/auth/login', data);
    return response.data;
  },
  register: async (data: { 
    username: string; 
    email: string; 
    phone: string; 
    password: string; 
    role: string; 
  }): Promise<{ message: string; email?: string; requiresVerification?: boolean }> => {
    const response = await api.post('/auth/register', data);
    return response.data;
  },
  verifyEmail: async (data: {
    email: string;
    username: string;
    phone: string;
    password: string;
    role: string;
    code: string;
  }): Promise<{ message: string }> => {
    const response = await api.post('/auth/verify-email', data);
    return response.data;
  },
  resendVerification: async (data: {
    email: string;
    username: string;
  }): Promise<{ message: string }> => {
    const response = await api.post('/auth/resend-verification', data);
    return response.data;
  },
  forgotPassword: async (data: { email: string }): Promise<{ message: string }> => {
    const response = await api.post('/auth/forgot-password', data);
    return response.data;
  },
  resetPassword: async (data: { 
    email: string; 
    token: string; 
    newPassword: string; 
  }): Promise<{ message: string }> => {
    const response = await api.post('/auth/reset-password', data);
    return response.data;
  },
  verifyToken: async (): Promise<User> => {
    const response = await api.get('/auth/verify');
    return response.data;
  },
  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  }
};

// Brand API
export const brandApi = {
  getBrands: async (includeInactive: boolean = false): Promise<Brand[]> => {
    const response = await api.get('/brands', { params: { includeInactive } });
    return response.data;
  },
  getBrandById: async (id: number): Promise<Brand> => {
    const response = await api.get(`/brands/${id}`);
    return response.data;
  },
  getBrandByName: async (name: string): Promise<Brand | null> => {
    const encoded = encodeURIComponent(name);
    const response = await api.get(`/brands/name/${encoded}`, {
      validateStatus: (s) => (s >= 200 && s < 300) || s === 404,
    });
    if (response.status === 404) return null;
    return response.data;
  },
  createBrand: async (data: { brandName: string; logoUrl?: string; description?: string; website?: string; } | FormData): Promise<Brand> => {
    const config = data instanceof FormData ? { headers: { 'Content-Type': 'multipart/form-data' } } : {};
    const response = await api.post('/brands', data, config);
    return response.data;
  },
  updateBrand: async (id: number, data: { brandName: string; logoUrl?: string; description?: string; website?: string; } | FormData): Promise<Brand> => {
    const config = data instanceof FormData ? { headers: { 'Content-Type': 'multipart/form-data' } } : {};
    const response = await api.put(`/brands/${id}`, data, config);
    return response.data;
  },
  deleteBrand: async (id: number): Promise<void> => {
    await api.delete(`/brands/${id}`);
  },
};

// Category API
export const categoryApi = {
  getCategories: async (includeInactive: boolean = false): Promise<Category[]> => {
    const response = await api.get('/categories', { params: { includeInactive } });
    return response.data;
  },
  getCategoryById: async (id: number): Promise<Category> => {
    const response = await api.get(`/categories/${id}`);
    return response.data;
  },
  getCategoryByName: async (name: string): Promise<Category | null> => {
    const encoded = encodeURIComponent(name);
    const response = await api.get(`/categories/name/${encoded}`, {
      validateStatus: (s) => (s >= 200 && s < 300) || s === 404,
    });
    if (response.status === 404) return null;
    return response.data;
  },
  createCategory: async (data: { categoryName: string; parentId?: number; description?: string; imageUrl?: string; }): Promise<Category> => {
    const response = await api.post('/categories', data);
    return response.data;
  },
  updateCategory: async (id: number, data: { categoryName: string; parentId?: number; description?: string; imageUrl?: string; }): Promise<Category> => {
    const response = await api.put(`/categories/${id}`, data);
    return response.data;
  },
  deleteCategory: async (id: number): Promise<void> => {
    await api.delete(`/categories/${id}`);
  },
};

// Product API
export const productApi = {
  getProducts: async (includeInactive: boolean = false): Promise<Product[]> => {
    const response = await api.get(`/products?includeInactive=${includeInactive}`);
    return response.data;
  },
  getProductById: async (id: number): Promise<Product> => {
    const response = await api.get(`/products/${id}`);
    return response.data;
  },
  getProductByName: async (name: string): Promise<Product | null> => {
    const encoded = encodeURIComponent(name);
    const response = await api.get(`/products/name/${encoded}`, {
      validateStatus: (s) => (s >= 200 && s < 300) || s === 404,
    });
    if (response.status === 404) return null;
    return response.data;
  },
  createProduct: async (data: Partial<Product>): Promise<Product> => {
    const response = await api.post('/products', data);
    return response.data;
  },
  createProductWithImages: async (data: FormData): Promise<Product> => {
    const response = await api.post('/products', data);
    return response.data;
  },
  updateProduct: async (id: number, data: Partial<Product>): Promise<Product> => {
    const response = await api.put(`/products/${id}`, data);
    return response.data;
  },
  deleteProduct: async (id: number): Promise<void> => {
    await api.delete(`/products/${id}`);
  },
  // Product Images API
  getProductImages: async (productId: number): Promise<ProductImage[]> => {
    const response = await api.get(`/products/${productId}/images`);
    return response.data;
  },
  uploadProductImages: async (productId: number, images: FormData): Promise<ProductImage[]> => {
    const response = await api.post(`/products/${productId}/images`, images);
    return response.data;
  },
  setPrimaryProductImage: async (productId: number, imageId: number): Promise<ProductImage> => {
    const response = await api.put(`/products/${productId}/images/${imageId}/primary`);
    return response.data;
  },
  deleteProductImage: async (productId: number, imageId: number): Promise<void> => {
    await api.delete(`/products/${productId}/images/${imageId}`);
  },
};

// Product Variant API
export const variantApi = {
  // Get variants by product ID
  getVariantsByProduct: async (productId: number): Promise<ProductVariant[]> => {
    const response = await api.get(`/variants/product/${productId}`);
    return response.data;
  },

  // Get variant by ID
  getVariantById: async (id: number): Promise<ProductVariant> => {
    const response = await api.get(`/variants/${id}`);
    return response.data;
  },

  // Create new variant
  createVariant: async (data: CreateVariantRequest): Promise<ProductVariant> => {
    const response = await api.post('/variants', data);
    return response.data;
  },

  // Create new variant with file upload
  createVariantWithFile: async (formData: FormData): Promise<ProductVariant> => {
    const response = await api.post('/variants', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Update variant
  updateVariant: async (id: number, data: UpdateVariantRequest): Promise<ProductVariant> => {
    const response = await api.put(`/variants/${id}`, data);
    return response.data;
  },

  // Delete variant
  deleteVariant: async (id: number): Promise<void> => {
    await api.delete(`/variants/${id}`);
  },

  // Get variant by SKU (for validation)
  getVariantBySku: async (sku: string): Promise<ProductVariant | null> => {
    try {
      const response = await api.get(`/variants/sku/${encodeURIComponent(sku)}`, {
        validateStatus: (status) => status === 200 || status === 404
      });
      return response.status === 200 ? response.data : null;
    } catch (error) {
      return null;
    }
  },

  // Update all product statuses based on variants
  updateAllProductStatuses: async (): Promise<string> => {
    const response = await api.post('/variants/update-product-statuses');
    return response.data;
  },

  // Create multiple variants at once
  createBulkVariants: async (data: BulkVariantCreateRequest): Promise<ProductVariant[]> => {
    const response = await api.post('/variants/bulk', data);
    return response.data;
  },

  // Upload image for variant
  uploadVariantImage: async (variantId: number, image: File): Promise<ProductVariant> => {
    const formData = new FormData();
    formData.append('image', image);
    const response = await api.post(`/variants/${variantId}/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};

// Test API
export const testApi = {
  // Test connection
  testConnection: async (): Promise<any> => {
    const response = await api.get('/test/connection');
    return response.data;
  },

  // Get all users for testing
  getTestUsers: async (): Promise<User[]> => {
    const response = await api.get('/test/users');
    return response.data;
  },

  // Upload images for product
  uploadProductImages: async (productId: number, images: File[]): Promise<any[]> => {
    const formData = new FormData();
    images.forEach(image => {
      formData.append('images', image);
    });
    const response = await api.post(`/products/${productId}/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};

// Gender API
export const genderApi = {
  // Get all genders
  getGenders: async (includeInactive: boolean = false): Promise<Gender[]> => {
    const response = await api.get(`/genders?includeInactive=${includeInactive}`);
    return response.data;
  },

  // Get gender by ID
  getGenderById: async (id: number): Promise<Gender> => {
    const response = await api.get(`/genders/${id}`);
    return response.data;
  },

  // Get gender by code
  getGenderByCode: async (code: string): Promise<Gender> => {
    const response = await api.get(`/genders/code/${code}`);
    return response.data;
  },

  // Create gender
  createGender: async (gender: Partial<Gender>): Promise<Gender> => {
    const response = await api.post('/genders', gender);
    return response.data;
  },

  // Update gender
  updateGender: async (id: number, gender: Partial<Gender>): Promise<Gender> => {
    const response = await api.put(`/genders/${id}`, gender);
    return response.data;
  },

  // Soft delete gender
  deleteGender: async (id: number): Promise<void> => {
    await api.delete(`/genders/${id}`);
  },
};

export default api;
