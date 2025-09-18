import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { message } from 'antd';
import { 
  User, 
  CreateUserRequest, 
  UpdateUserRequest,
  Size,
  CreateSizeRequest,
  UpdateSizeRequest,
  Color,
  CreateColorRequest,
  UpdateColorRequest,
  Brand,
  Category,
  FilterOptions
} from '../types';

// Create axios instance
const api: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
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
  getUsers: async (params?: FilterOptions): Promise<User[]> => {
    const response = await api.get('/users', { params });
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
  getSizes: async (): Promise<Size[]> => {
    const response = await api.get('/sizes');
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
  getColors: async (): Promise<Color[]> => {
    const response = await api.get('/colors');
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
};

// Brand API
export const brandApi = {
  getBrands: async (): Promise<Brand[]> => {
    const response = await api.get('/brands');
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
  createBrand: async (data: { brandName: string; logoUrl?: string; description?: string; website?: string; }): Promise<Brand> => {
    const response = await api.post('/brands', data);
    return response.data;
  },
  updateBrand: async (id: number, data: { brandName: string; logoUrl?: string; description?: string; website?: string; }): Promise<Brand> => {
    const response = await api.put(`/brands/${id}`, data);
    return response.data;
  },
  deleteBrand: async (id: number): Promise<void> => {
    await api.delete(`/brands/${id}`);
  },
};

// Category API
export const categoryApi = {
  getCategories: async (): Promise<Category[]> => {
    const response = await api.get('/categories');
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
};

export default api;
