# 🚀 Giải pháp chạy Spring Boot Application

## 🚨 **Vấn đề:**
- Maven không được cài đặt (`mvn` command not found)
- Spring Boot application không khởi động được bằng Java trực tiếp

## ✅ **Giải pháp nhanh:**

### **Phương án 1: Chạy từ IDE (Khuyến nghị)**
1. Mở project trong **IntelliJ IDEA** hoặc **Eclipse**
2. Tìm file `DemoStoreApplication.java`
3. Click chuột phải → **Run 'DemoStoreApplication'**
4. Hoặc click vào icon ▶️ bên cạnh class

### **Phương án 2: Cài đặt Maven**
1. Tải Maven từ: https://maven.apache.org/download.cgi
2. Giải nén vào `C:\Program Files\Apache\maven`
3. Thêm `C:\Program Files\Apache\maven\bin` vào PATH
4. Restart terminal và chạy: `mvn spring-boot:run`

### **Phương án 3: Sử dụng Gradle (nếu có)**
```bash
./gradlew bootRun
```

## 🎯 **Sau khi khởi động thành công:**

### **Test API:**
```bash
# Test connection
curl http://localhost:8080/api/test/connection

# Test sizes API
curl http://localhost:8080/api/sizes

# Test colors API
curl http://localhost:8080/api/colors
```

### **Truy cập Frontend:**
- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`

## 📝 **Lưu ý:**
- Java 17 đã được cài đặt ✅
- Classes đã được compile ✅
- Chỉ cần khởi động Spring Boot application
- IDE sẽ tự động quản lý dependencies

## 🔧 **Nếu vẫn gặp lỗi:**
1. Kiểm tra database connection trong `application.properties`
2. Đảm bảo SQL Server đang chạy
3. Kiểm tra port 8080 có bị chiếm không
4. Xem logs trong console để tìm lỗi cụ thể
