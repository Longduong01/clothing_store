# 🔧 Khắc phục vấn đề kết nối Database

## 🚨 **Vấn đề hiện tại:**
- Frontend không thể tải dữ liệu từ database
- API `/api/sizes` và `/api/colors` trả về lỗi 500
- Cần sử dụng dữ liệu thật từ database, không phải mock data

## ✅ **Giải pháp:**

### **Bước 1: Khởi động Backend**
```bash
# Chạy Spring Boot application từ IDE hoặc:
mvn spring-boot:run
```

### **Bước 2: Kiểm tra Database Connection**
```bash
# Test database connection
curl http://localhost:8080/api/test/database
```

### **Bước 3: Tạo dữ liệu mẫu trong Database**
```bash
# Seed data vào database
curl -X POST http://localhost:8080/api/test/seed-data
```

### **Bước 4: Test API với dữ liệu thật**
```bash
# Test sizes API
curl http://localhost:8080/api/sizes

# Test colors API  
curl http://localhost:8080/api/colors
```

## 🎯 **Sau khi khắc phục:**

### **Frontend sẽ hiển thị:**
- ✅ Dữ liệu thật từ database
- ✅ Không có mock data
- ✅ Có thể thêm/sửa/xóa dữ liệu thật

### **Các trang quản lý:**
- `http://localhost:3000/users` - Quản lý users (dữ liệu thật)
- `http://localhost:3000/sizes` - Quản lý sizes (dữ liệu thật)
- `http://localhost:3000/colors` - Quản lý colors (dữ liệu thật)

## 📝 **Lưu ý:**
- ✅ Frontend đã được cập nhật để chỉ sử dụng dữ liệu thật
- ✅ Không còn mock data fallback
- ✅ Hiển thị empty state khi API lỗi
- ✅ Cần khởi động backend và seed data trước

## 🔍 **Troubleshooting:**

### **Nếu vẫn lỗi 500:**
1. Kiểm tra database connection trong `application.properties`
2. Đảm bảo SQL Server đang chạy
3. Kiểm tra tables `Sizes` và `Colors` có tồn tại không
4. Xem logs backend để tìm lỗi cụ thể

### **Nếu không có dữ liệu:**
1. Chạy endpoint seed data: `POST /api/test/seed-data`
2. Hoặc chạy file SQL: `insert_sample_data.sql`
3. Kiểm tra database có dữ liệu không
