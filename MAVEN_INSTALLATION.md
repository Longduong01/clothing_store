# 🔧 Hướng dẫn cài đặt Maven

## 🚨 **Vấn đề hiện tại:**
- Lỗi: `mvn : The term 'mvn' is not recognized`
- Maven chưa được cài đặt hoặc chưa được thêm vào PATH

## 📥 **Cách cài đặt Maven:**

### **Phương án 1: Tải Maven từ trang chủ**
1. Truy cập: https://maven.apache.org/download.cgi
2. Tải file `apache-maven-3.9.6-bin.zip`
3. Giải nén vào thư mục `C:\Program Files\Apache\maven`
4. Thêm vào PATH: `C:\Program Files\Apache\maven\bin`

### **Phương án 2: Sử dụng Chocolatey (nếu có)**
```bash
choco install maven
```

### **Phương án 3: Sử dụng Scoop (nếu có)**
```bash
scoop install maven
```

## 🚀 **Giải pháp tạm thời:**

### **Chạy Spring Boot bằng Java trực tiếp:**
```bash
# Chạy script đơn giản
start_backend_simple.bat

# Hoặc chạy thủ công
java -cp "target\classes" com.example.demo_store.DemoStoreApplication
```

## ✅ **Kiểm tra sau khi cài đặt:**
```bash
mvn -version
```

## 🎯 **Sau khi cài đặt Maven:**
```bash
# Build project
mvn clean compile

# Chạy application
mvn spring-boot:run

# Hoặc tạo JAR file
mvn clean package
java -jar target\demo-store-0.0.1-SNAPSHOT.jar
```

## 📝 **Lưu ý:**
- Java 17 đã được cài đặt ✅
- Classes đã được compile ✅
- Chỉ cần Maven để quản lý dependencies và build
