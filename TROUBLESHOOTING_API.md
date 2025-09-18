# 🔧 Troubleshooting API 500 Error

## 🚨 **Vấn đề hiện tại:**
- Frontend gọi API `/api/sizes` và `/api/colors` bị lỗi 500 Internal Server Error
- Backend có thể không chạy hoặc có lỗi trong database connection

## 🔍 **Các bước kiểm tra:**

### 1. **Kiểm tra Backend:**
```bash
# Chạy script khởi động backend
start_backend.bat

# Hoặc chạy thủ công
mvn spring-boot:run
```

### 2. **Kiểm tra API:**
```bash
# Chạy script test API
test_api.bat

# Hoặc test thủ công
curl http://localhost:8080/api/test/connection
curl http://localhost:8080/api/sizes
curl http://localhost:8080/api/colors
```

### 3. **Kiểm tra Database:**
- Đảm bảo SQL Server đang chạy
- Đảm bảo database `ClothingStoreDB` tồn tại
- Đảm bảo tables `Sizes` và `Colors` có dữ liệu

## 🛠️ **Các sửa lỗi đã thực hiện:**

### **Backend Controllers:**
- ✅ Thêm error details trong `SizeController.getAllSizes()`
- ✅ Thêm error details trong `ColorController.getAllColors()`
- ✅ Thêm test endpoint `/api/test/sizes`

### **Error Handling:**
- ✅ Controllers giờ trả về chi tiết lỗi thay vì chỉ 500
- ✅ Có thể debug được nguyên nhân lỗi

## 🎯 **Kết quả mong đợi:**

Sau khi sửa lỗi, API sẽ trả về:
- **Thành công**: Danh sách sizes/colors từ database
- **Lỗi**: Chi tiết lỗi để debug

## 📝 **Các lỗi có thể gặp:**

1. **Database Connection Error**: SQL Server không chạy
2. **Table Not Found**: Tables `Sizes`/`Colors` không tồn tại
3. **Entity Mapping Error**: Lỗi mapping giữa entity và table
4. **Repository Error**: Lỗi trong repository methods

## 🚀 **Cách khắc phục:**

1. **Khởi động Backend**: Chạy `start_backend.bat`
2. **Test API**: Chạy `test_api.bat`
3. **Kiểm tra logs**: Xem console output để tìm lỗi
4. **Kiểm tra database**: Đảm bảo database và tables tồn tại
