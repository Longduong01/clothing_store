# 🔧 Khắc phục sự cố Frontend

## 🚨 Vấn đề: Trang trắng khi truy cập http://localhost:3000/

### ✅ Giải pháp 1: Kiểm tra Console
1. Mở Developer Tools (F12)
2. Kiểm tra tab Console để xem lỗi
3. Kiểm tra tab Network để xem request có thành công không

### ✅ Giải pháp 2: Restart Development Server
```bash
# Dừng server (Ctrl + C)
# Sau đó chạy lại
cd frontend
npm run dev
```

### ✅ Giải pháp 3: Clear Cache và Reinstall
```bash
# Xóa node_modules và package-lock.json
rm -rf node_modules package-lock.json

# Cài đặt lại dependencies
npm install

# Chạy lại
npm run dev
```

### ✅ Giải pháp 4: Kiểm tra Port
```bash
# Kiểm tra port 3000 có bị chiếm không
npx kill-port 3000

# Hoặc chạy trên port khác
npm run dev -- --port 3001
```

## 🎯 Truy cập các trang quản lý

Sau khi frontend chạy thành công, bạn có thể truy cập:

### 📱 Các URL chính:
- **Dashboard**: `http://localhost:3000/`
- **User Management**: `http://localhost:3000/users`
- **Size Management**: `http://localhost:3000/sizes`
- **Color Management**: `http://localhost:3000/colors`

### 🔍 Kiểm tra Backend Connection
Đảm bảo Spring Boot backend đang chạy trên `http://localhost:8080`

Test API connection:
```bash
curl http://localhost:8080/api/test/connection
```

## 🐛 Các lỗi thường gặp

### 1. **Module not found errors**
```bash
# Lỗi: Cannot resolve module '@/...'
# Giải pháp: Đã sửa import paths thành relative imports
```

### 2. **TypeScript errors**
```bash
# Chạy type check
npx tsc --noEmit
```

### 3. **Ant Design theme errors**
```bash
# Kiểm tra version compatibility
npm list antd
```

### 4. **React Router errors**
```bash
# Kiểm tra React Router version
npm list react-router-dom
```

## 🚀 Các bước khắc phục chi tiết

### Bước 1: Kiểm tra Dependencies
```bash
cd frontend
npm list
```

### Bước 2: Kiểm tra Build
```bash
npm run build
```

### Bước 3: Kiểm tra Development Server
```bash
npm run dev
```

### Bước 4: Kiểm tra Browser Console
- Mở F12
- Kiểm tra Console tab
- Kiểm tra Network tab
- Kiểm tra Sources tab

## 📞 Nếu vẫn gặp vấn đề

1. **Kiểm tra Node.js version**:
   ```bash
   node --version  # Cần >= 18
   npm --version   # Cần >= 8
   ```

2. **Kiểm tra file structure**:
   ```
   frontend/
   ├── src/
   │   ├── App.tsx
   │   ├── main.tsx
   │   ├── index.css
   │   └── ...
   ├── package.json
   ├── vite.config.ts
   └── ...
   ```

3. **Kiểm tra Vite config**:
   - File `vite.config.ts` có đúng không
   - Port 3000 có available không

4. **Restart toàn bộ**:
   ```bash
   # Dừng tất cả processes
   # Xóa node_modules
   rm -rf node_modules package-lock.json
   
   # Cài đặt lại
   npm install
   
   # Chạy lại
   npm run dev
   ```

## 🎉 Khi đã chạy thành công

Bạn sẽ thấy:
- Trang chủ với thông tin về các trang quản lý
- Navigation menu bên trái
- Các trang Size, Color, User hoạt động bình thường

## 📱 Test các tính năng

1. **User Management** (`/users`):
   - Xem danh sách users
   - Thêm user mới
   - Sửa/xóa user

2. **Size Management** (`/sizes`):
   - Xem danh sách sizes
   - Thêm size mới (S, M, L, XL...)
   - Sửa/xóa size

3. **Color Management** (`/colors`):
   - Xem danh sách colors
   - Thêm color mới
   - Color picker
   - Sửa/xóa color

Chúc bạn thành công! 🚀
