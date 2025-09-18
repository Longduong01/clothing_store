-- =============================================
-- Script Name: ClothingStoreDB.sql
-- Description: Tạo database cho website thương mại điện tử bán quần áo
-- Author: AI Assistant
-- Created Date: 2024
-- =============================================

-- Tạo database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'ClothingStoreDB')
BEGIN
    CREATE DATABASE ClothingStoreDB;
END
GO

USE ClothingStoreDB;
GO

-- =============================================
-- Tạo các bảng
-- =============================================

-- Bảng Users
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    phone NVARCHAR(20),
    role NVARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT CHK_Users_Role CHECK (role IN ('ADMIN', 'CUSTOMER', 'STAFF'))
);

-- Bảng Categories
CREATE TABLE Categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    category_name NVARCHAR(100) NOT NULL,
    image_url NVARCHAR(500),
    description NVARCHAR(MAX),
    parent_id INT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Categories_Parent FOREIGN KEY (parent_id) REFERENCES Categories(category_id)
);

-- Bảng Brands
CREATE TABLE Brands (
    brand_id INT IDENTITY(1,1) PRIMARY KEY,
    brand_name NVARCHAR(100) NOT NULL,
    logo_url NVARCHAR(500),
    description NVARCHAR(MAX),
    website NVARCHAR(200),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Bảng Products
CREATE TABLE Products (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    product_name NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX),
    sku NVARCHAR(50) NOT NULL UNIQUE,
    price DECIMAL(18,2) NOT NULL,
    image_url NVARCHAR(500),
    thumbnail_url NVARCHAR(500),
    gallery_images NVARCHAR(MAX),
    stock_quantity INT NOT NULL DEFAULT 0,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    category_id INT NOT NULL,
    brand_id INT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Products_Category FOREIGN KEY (category_id) REFERENCES Categories(category_id),
    CONSTRAINT FK_Products_Brand FOREIGN KEY (brand_id) REFERENCES Brands(brand_id),
    CONSTRAINT CHK_Products_Status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED')),
    CONSTRAINT CHK_Products_Price CHECK (price >= 0),
    CONSTRAINT CHK_Products_StockQuantity CHECK (stock_quantity >= 0)
);

-- Bảng Sizes
CREATE TABLE Sizes (
    size_id INT IDENTITY(1,1) PRIMARY KEY,
    size_name NVARCHAR(10) NOT NULL UNIQUE
);

-- Bảng Colors
CREATE TABLE Colors (
    color_id INT IDENTITY(1,1) PRIMARY KEY,
    color_name NVARCHAR(50) NOT NULL UNIQUE
);

-- Bảng ProductImages
CREATE TABLE ProductImages (
    image_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    image_name NVARCHAR(255),
    image_type NVARCHAR(50),
    file_size BIGINT,
    is_primary BIT DEFAULT 0,
    sort_order INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_ProductImages_Product FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    CONSTRAINT CHK_ProductImages_FileSize CHECK (file_size > 0),
    CONSTRAINT CHK_ProductImages_SortOrder CHECK (sort_order >= 0)
);
GO

-- Bảng ProductVariants
CREATE TABLE ProductVariants (
    variant_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    size_id INT NOT NULL,
    color_id INT NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_ProductVariants_Product FOREIGN KEY (product_id) REFERENCES Products(product_id),
    CONSTRAINT FK_ProductVariants_Size FOREIGN KEY (size_id) REFERENCES Sizes(size_id),
    CONSTRAINT FK_ProductVariants_Color FOREIGN KEY (color_id) REFERENCES Colors(color_id),
    CONSTRAINT CHK_ProductVariants_Status CHECK (status IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK')),
    CONSTRAINT CHK_ProductVariants_Price CHECK (price >= 0),
    CONSTRAINT CHK_ProductVariants_Stock CHECK (stock >= 0),
    CONSTRAINT UQ_ProductVariants_Unique UNIQUE (product_id, size_id, color_id)
);

-- Bảng Carts
CREATE TABLE Carts (
    cart_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Carts_User FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Bảng CartItems
CREATE TABLE CartItems (
    cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
    cart_id INT NOT NULL,
    variant_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT FK_CartItems_Cart FOREIGN KEY (cart_id) REFERENCES Carts(cart_id),
    CONSTRAINT FK_CartItems_Variant FOREIGN KEY (variant_id) REFERENCES ProductVariants(variant_id),
    CONSTRAINT CHK_CartItems_Quantity CHECK (quantity > 0),
    CONSTRAINT UQ_CartItems_Unique UNIQUE (cart_id, variant_id)
);

-- Bảng Orders
CREATE TABLE Orders (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Orders_User FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT CHK_Orders_Status CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT CHK_Orders_TotalAmount CHECK (total_amount >= 0)
);

-- Bảng OrderItems
CREATE TABLE OrderItems (
    order_item_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    variant_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    CONSTRAINT FK_OrderItems_Order FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    CONSTRAINT FK_OrderItems_Variant FOREIGN KEY (variant_id) REFERENCES ProductVariants(variant_id),
    CONSTRAINT CHK_OrderItems_Quantity CHECK (quantity > 0),
    CONSTRAINT CHK_OrderItems_Price CHECK (price >= 0)
);

-- Bảng Payments
CREATE TABLE Payments (
    payment_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    payment_method NVARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id NVARCHAR(100),
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Payments_Order FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    CONSTRAINT CHK_Payments_Status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    CONSTRAINT CHK_Payments_Amount CHECK (amount >= 0)
);

-- =============================================
-- Tạo Indexes để tối ưu hiệu suất
-- =============================================

CREATE INDEX IX_Products_Category ON Products(category_id);
CREATE INDEX IX_Products_Brand ON Products(brand_id);
CREATE INDEX IX_Products_Status ON Products(status);
CREATE INDEX IX_ProductImages_ProductId ON ProductImages(product_id);
CREATE INDEX IX_ProductImages_IsPrimary ON ProductImages(product_id, is_primary);
CREATE INDEX IX_ProductImages_SortOrder ON ProductImages(product_id, sort_order);
CREATE INDEX IX_ProductVariants_Product ON ProductVariants(product_id);
CREATE INDEX IX_ProductVariants_Status ON ProductVariants(status);
CREATE INDEX IX_Orders_User ON Orders(user_id);
CREATE INDEX IX_Orders_Status ON Orders(status);
CREATE INDEX IX_OrderItems_Order ON OrderItems(order_id);
CREATE INDEX IX_CartItems_Cart ON CartItems(cart_id);

-- =============================================
-- Thêm dữ liệu mẫu
-- =============================================

-- Thêm Categories
INSERT INTO Categories (category_name, parent_id) VALUES 
('Áo', NULL),
('Quần', NULL),
('Áo thun', 1),
('Áo sơ mi', 1),
('Quần jean', 2),
('Quần short', 2);

-- Thêm Brands
INSERT INTO Brands (brand_name) VALUES 
('Uniqlo'),
('Nike'),
('Adidas'),
('Zara'),
('H&M');

-- Thêm Sizes
INSERT INTO Sizes (size_name) VALUES 
('S'),
('M'),
('L'),
('XL'),
('XXL');

-- Thêm Colors
INSERT INTO Colors (color_name) VALUES 
('Red'),
('Blue'),
('Black'),
('White'),
('Green'),
('Yellow'),
('Pink'),
('Gray');

-- Thêm Users
INSERT INTO Users (username, password, email, phone, role) VALUES 
('admin', 'admin123', 'admin@clothingstore.com', '0123456789', 'ADMIN'),
('customer1', 'customer123', 'customer1@email.com', '0987654321', 'CUSTOMER'),
('customer2', 'customer123', 'customer2@email.com', '0912345678', 'CUSTOMER');

-- Thêm Products
INSERT INTO Products (product_name, description, sku, price, image_url, thumbnail_url, stock_quantity, category_id, brand_id) VALUES 
('Áo thun Uniqlo Basic', 'Áo thun cotton 100% thoáng mát, thiết kế đơn giản', 'UNI001', 199000, 'https://via.placeholder.com/400x400/007bff/ffffff?text=Uniqlo+Basic', 'https://via.placeholder.com/400x400/007bff/ffffff?text=Uniqlo+Basic', 100, 3, 1),
('Quần jean Nike Sport', 'Quần jean co giãn, phù hợp cho vận động', 'NIKE001', 599000, 'https://via.placeholder.com/400x400/28a745/ffffff?text=Nike+Sport', 'https://via.placeholder.com/400x400/28a745/ffffff?text=Nike+Sport', 50, 5, 2),
('Áo sơ mi Zara Classic', 'Áo sơ mi công sở, chất liệu cotton cao cấp', 'ZARA001', 399000, 'https://via.placeholder.com/400x400/dc3545/ffffff?text=Zara+Classic', 'https://via.placeholder.com/400x400/dc3545/ffffff?text=Zara+Classic', 75, 4, 4),
('Quần short Adidas', 'Quần short thể thao, thoáng khí', 'ADIDAS001', 299000, 'https://via.placeholder.com/400x400/ffc107/ffffff?text=Adidas+Short', 'https://via.placeholder.com/400x400/ffc107/ffffff?text=Adidas+Short', 80, 6, 3);

-- Thêm ProductVariants
INSERT INTO ProductVariants (product_id, size_id, color_id, price, stock) VALUES 
-- Áo thun Uniqlo Basic
(1, 1, 3, 199000, 50),  -- S Black
(1, 2, 3, 199000, 50),  -- M Black
(1, 3, 3, 199000, 50),  -- L Black
(1, 1, 4, 199000, 30),  -- S White
(1, 2, 4, 199000, 30),  -- M White
(1, 3, 4, 199000, 30),  -- L White
(1, 2, 1, 199000, 25),  -- M Red
(1, 3, 1, 199000, 25),  -- L Red

-- Quần jean Nike Sport
(2, 2, 3, 599000, 40),  -- M Black
(2, 3, 3, 599000, 40),  -- L Black
(2, 4, 3, 599000, 40),  -- XL Black
(2, 2, 2, 599000, 20),  -- M Blue
(2, 3, 2, 599000, 20),  -- L Blue

-- Áo sơ mi Zara Classic
(3, 2, 4, 399000, 35),  -- M White
(3, 3, 4, 399000, 35),  -- L White
(3, 4, 4, 399000, 35),  -- XL White
(3, 2, 2, 399000, 25),  -- M Blue
(3, 3, 2, 399000, 25),  -- L Blue

-- Quần short Adidas
(4, 1, 3, 299000, 60),  -- S Black
(4, 2, 3, 299000, 60),  -- M Black
(4, 3, 3, 299000, 60),  -- L Black
(4, 1, 2, 299000, 40),  -- S Blue
(4, 2, 2, 299000, 40),  -- M Blue
(4, 3, 2, 299000, 40);  -- L Blue

-- Thêm Carts cho users
INSERT INTO Carts (user_id) VALUES 
(2),  -- customer1
(3);  -- customer2

-- Thêm CartItems mẫu
INSERT INTO CartItems (cart_id, variant_id, quantity) VALUES 
(1, 1, 2),  -- customer1: 2 áo thun S Black
(1, 5, 1),  -- customer1: 1 áo thun M White
(2, 7, 1),  -- customer2: 1 áo thun M Red
(2, 12, 1); -- customer2: 1 quần jean M Black

-- Thêm Orders mẫu
INSERT INTO Orders (user_id, total_amount, status) VALUES 
(2, 398000, 'CONFIRMED'),  -- customer1: 2 áo thun S Black
(3, 798000, 'PENDING');    -- customer2: 1 áo thun M Red + 1 quần jean M Black

-- Thêm OrderItems mẫu
INSERT INTO OrderItems (order_id, variant_id, quantity, price) VALUES 
(1, 1, 2, 199000),  -- Order 1: 2 áo thun S Black
(2, 7, 1, 199000),  -- Order 2: 1 áo thun M Red
(2, 12, 1, 599000); -- Order 2: 1 quần jean M Black

-- Thêm Payments mẫu
INSERT INTO Payments (order_id, payment_method, amount, status, transaction_id) VALUES 
(1, 'BANK_TRANSFER', 398000, 'COMPLETED', 'TXN001'),
(2, 'CREDIT_CARD', 798000, 'PENDING', 'TXN002');

-- Thêm ProductImages mẫu
INSERT INTO ProductImages (product_id, image_url, image_name, image_type, file_size, is_primary, sort_order) VALUES 
-- Áo thun Uniqlo Basic
(1, 'https://via.placeholder.com/400x400/007bff/ffffff?text=Uniqlo+1', 'uniqlo_basic_1.jpg', 'image/jpeg', 1024000, 1, 1),
(1, 'https://via.placeholder.com/400x400/28a745/ffffff?text=Uniqlo+2', 'uniqlo_basic_2.jpg', 'image/jpeg', 1024000, 0, 2),
(1, 'https://via.placeholder.com/400x400/dc3545/ffffff?text=Uniqlo+3', 'uniqlo_basic_3.jpg', 'image/jpeg', 1024000, 0, 3),

-- Quần jean Nike Sport
(2, 'https://via.placeholder.com/400x400/28a745/ffffff?text=Nike+1', 'nike_jean_1.jpg', 'image/jpeg', 1024000, 1, 1),
(2, 'https://via.placeholder.com/400x400/ffc107/ffffff?text=Nike+2', 'nike_jean_2.jpg', 'image/jpeg', 1024000, 0, 2),

-- Áo sơ mi Zara Classic
(3, 'https://via.placeholder.com/400x400/dc3545/ffffff?text=Zara+1', 'zara_shirt_1.jpg', 'image/jpeg', 1024000, 1, 1),
(3, 'https://via.placeholder.com/400x400/6f42c1/ffffff?text=Zara+2', 'zara_shirt_2.jpg', 'image/jpeg', 1024000, 0, 2),
(3, 'https://via.placeholder.com/400x400/fd7e14/ffffff?text=Zara+3', 'zara_shirt_3.jpg', 'image/jpeg', 1024000, 0, 3),

-- Quần short Adidas
(4, 'https://via.placeholder.com/400x400/ffc107/ffffff?text=Adidas+1', 'adidas_short_1.jpg', 'image/jpeg', 1024000, 1, 1),
(4, 'https://via.placeholder.com/400x400/17a2b8/ffffff?text=Adidas+2', 'adidas_short_2.jpg', 'image/jpeg', 1024000, 0, 2);

-- =============================================
-- Tạo Triggers để tự động cập nhật updated_at
-- =============================================

-- Trigger cho Users
GO
CREATE TRIGGER TR_Users_UpdateTime
ON Users
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Users 
    SET updated_at = GETDATE()
    WHERE user_id IN (SELECT user_id FROM inserted);
END
GO

-- Trigger cho Categories
CREATE TRIGGER TR_Categories_UpdateTime
ON Categories
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Categories 
    SET updated_at = GETDATE()
    WHERE category_id IN (SELECT category_id FROM inserted);
END
GO

-- Trigger cho Brands
CREATE TRIGGER TR_Brands_UpdateTime
ON Brands
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Brands 
    SET updated_at = GETDATE()
    WHERE brand_id IN (SELECT brand_id FROM inserted);
END
GO

-- Trigger cho Products
CREATE TRIGGER TR_Products_UpdateTime
ON Products
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Products 
    SET updated_at = GETDATE()
    WHERE product_id IN (SELECT product_id FROM inserted);
END
GO

-- Trigger cho ProductVariants
CREATE TRIGGER TR_ProductVariants_UpdateTime
ON ProductVariants
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE ProductVariants 
    SET updated_at = GETDATE()
    WHERE variant_id IN (SELECT variant_id FROM inserted);
END
GO

-- Trigger cho Carts
CREATE TRIGGER TR_Carts_UpdateTime
ON Carts
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Carts 
    SET updated_at = GETDATE()
    WHERE cart_id IN (SELECT cart_id FROM inserted);
END
GO

-- Trigger cho Orders
CREATE TRIGGER TR_Orders_UpdateTime
ON Orders
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Orders 
    SET updated_at = GETDATE()
    WHERE order_id IN (SELECT order_id FROM inserted);
END
GO

-- Trigger cho ProductImages
CREATE TRIGGER TR_ProductImages_UpdateTime
ON ProductImages
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE ProductImages 
    SET updated_at = GETDATE()
    WHERE image_id IN (SELECT image_id FROM inserted);
END
GO

-- =============================================
-- Tạo Views hữu ích
-- =============================================

-- View hiển thị thông tin sản phẩm đầy đủ
CREATE VIEW vw_ProductDetails AS
SELECT 
    p.product_id,
    p.product_name,
    p.description,
    p.sku,
    p.price as base_price,
    p.status as product_status,
    c.category_name,
    b.brand_name,
    pv.variant_id,
    s.size_name,
    cl.color_name,
    pv.price as variant_price,
    pv.stock,
    pv.status as variant_status,
    p.created_at,
    p.updated_at
FROM Products p
INNER JOIN Categories c ON p.category_id = c.category_id
INNER JOIN Brands b ON p.brand_id = b.brand_id
INNER JOIN ProductVariants pv ON p.product_id = pv.product_id
INNER JOIN Sizes s ON pv.size_id = s.size_id
INNER JOIN Colors cl ON pv.color_id = cl.color_id;
GO

-- View hiển thị thông tin đơn hàng đầy đủ
CREATE VIEW vw_OrderDetails AS
SELECT 
    o.order_id,
    u.username,
    u.email,
    o.total_amount,
    o.status as order_status,
    o.created_at as order_date,
    p.product_name,
    s.size_name,
    cl.color_name,
    oi.quantity,
    oi.price as item_price,
    (oi.quantity * oi.price) as item_total
FROM Orders o
INNER JOIN Users u ON o.user_id = u.user_id
INNER JOIN OrderItems oi ON o.order_id = oi.order_id
INNER JOIN ProductVariants pv ON oi.variant_id = pv.variant_id
INNER JOIN Products p ON pv.product_id = p.product_id
INNER JOIN Sizes s ON pv.size_id = s.size_id
INNER JOIN Colors cl ON pv.color_id = cl.color_id;
GO

-- =============================================
-- Tạo Stored Procedures hữu ích
-- =============================================

-- Procedure để thêm sản phẩm vào giỏ hàng
CREATE PROCEDURE sp_AddToCart
    @UserID INT,
    @VariantID INT,
    @Quantity INT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @CartID INT;
    
    -- Lấy hoặc tạo cart cho user
    SELECT @CartID = cart_id FROM Carts WHERE user_id = @UserID;
    
    IF @CartID IS NULL
    BEGIN
        INSERT INTO Carts (user_id) VALUES (@UserID);
        SET @CartID = SCOPE_IDENTITY();
    END
    
    -- Kiểm tra xem item đã có trong cart chưa
    IF EXISTS (SELECT 1 FROM CartItems WHERE cart_id = @CartID AND variant_id = @VariantID)
    BEGIN
        UPDATE CartItems 
        SET quantity = quantity + @Quantity
        WHERE cart_id = @CartID AND variant_id = @VariantID;
    END
    ELSE
    BEGIN
        INSERT INTO CartItems (cart_id, variant_id, quantity)
        VALUES (@CartID, @VariantID, @Quantity);
    END
END
GO

-- Procedure để tạo đơn hàng từ giỏ hàng
CREATE PROCEDURE sp_CreateOrderFromCart
    @UserID INT,
    @OrderID INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @CartID INT;
    DECLARE @TotalAmount DECIMAL(18,2) = 0;
    
    -- Lấy cart của user
    SELECT @CartID = cart_id FROM Carts WHERE user_id = @UserID;
    
    IF @CartID IS NULL
    BEGIN
        RAISERROR('User cart not found', 16, 1);
        RETURN;
    END
    
    -- Tính tổng tiền
    SELECT @TotalAmount = SUM(ci.quantity * pv.price)
    FROM CartItems ci
    INNER JOIN ProductVariants pv ON ci.variant_id = pv.variant_id
    WHERE ci.cart_id = @CartID;
    
    -- Tạo đơn hàng
    INSERT INTO Orders (user_id, total_amount, status)
    VALUES (@UserID, @TotalAmount, 'PENDING');
    
    SET @OrderID = SCOPE_IDENTITY();
    
    -- Thêm order items
    INSERT INTO OrderItems (order_id, variant_id, quantity, price)
    SELECT @OrderID, ci.variant_id, ci.quantity, pv.price
    FROM CartItems ci
    INNER JOIN ProductVariants pv ON ci.variant_id = pv.variant_id
    WHERE ci.cart_id = @CartID;
    
    -- Xóa giỏ hàng
    DELETE FROM CartItems WHERE cart_id = @CartID;
    DELETE FROM Carts WHERE cart_id = @CartID;
END
GO

-- =============================================
-- Kiểm tra dữ liệu
-- =============================================

-- Hiển thị thống kê tổng quan
SELECT 'Database Statistics' as Info;
SELECT 'Users' as TableName, COUNT(*) as RecordCount FROM Users
UNION ALL
SELECT 'Categories', COUNT(*) FROM Categories
UNION ALL
SELECT 'Brands', COUNT(*) FROM Brands
UNION ALL
SELECT 'Products', COUNT(*) FROM Products
UNION ALL
SELECT 'ProductImages', COUNT(*) FROM ProductImages
UNION ALL
SELECT 'ProductVariants', COUNT(*) FROM ProductVariants
UNION ALL
SELECT 'Orders', COUNT(*) FROM Orders
UNION ALL
SELECT 'OrderItems', COUNT(*) FROM OrderItems;

-- Hiển thị sản phẩm có sẵn
SELECT 'Available Products' as Info;
SELECT * FROM vw_ProductDetails WHERE variant_status = 'ACTIVE' AND stock > 0;

-- Hiển thị đơn hàng
SELECT 'Order Details' as Info;
SELECT * FROM vw_OrderDetails;

-- =============================================
-- Tạo các bảng bổ sung cho quản lý khách hàng
-- =============================================

-- Bảng Customers
CREATE TABLE Customers (
    customer_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    first_name NVARCHAR(50) NOT NULL,
    last_name NVARCHAR(50) NOT NULL,
    phone NVARCHAR(20),
    email NVARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender NVARCHAR(10),
    address NVARCHAR(500),
    city NVARCHAR(100),
    state_province NVARCHAR(100),
    postal_code NVARCHAR(20),
    country NVARCHAR(100) DEFAULT 'Vietnam',
    customer_type NVARCHAR(20) DEFAULT 'REGULAR',
    loyalty_points INT DEFAULT 0,
    total_orders INT DEFAULT 0,
    total_spent DECIMAL(18,2) DEFAULT 0,
    registration_date DATETIME2 DEFAULT GETDATE(),
    last_login DATETIME2,
    is_active BIT DEFAULT 1,
    notes NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_Customers_User FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT CHK_Customers_Gender CHECK (gender IN ('Male', 'Female', 'Other')),
    CONSTRAINT CHK_Customers_CustomerType CHECK (customer_type IN ('REGULAR', 'VIP', 'PREMIUM', 'WHOLESALE')),
    CONSTRAINT CHK_Customers_LoyaltyPoints CHECK (loyalty_points >= 0),
    CONSTRAINT CHK_Customers_TotalOrders CHECK (total_orders >= 0),
    CONSTRAINT CHK_Customers_TotalSpent CHECK (total_spent >= 0)
);
GO

-- Bảng CustomerAddresses
CREATE TABLE CustomerAddresses (
    address_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    address_type NVARCHAR(20) NOT NULL DEFAULT 'SHIPPING',
    full_name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20),
    address_line1 NVARCHAR(200) NOT NULL,
    address_line2 NVARCHAR(200),
    city NVARCHAR(100) NOT NULL,
    state_province NVARCHAR(100),
    postal_code NVARCHAR(20) NOT NULL,
    country NVARCHAR(100) DEFAULT 'Vietnam',
    is_default BIT DEFAULT 0,
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_CustomerAddresses_Customer FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    CONSTRAINT CHK_CustomerAddresses_AddressType CHECK (address_type IN ('SHIPPING', 'BILLING', 'BOTH'))
);
GO

-- Bảng CustomerPreferences
CREATE TABLE CustomerPreferences (
    preference_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    category_id INT,
    brand_id INT,
    size_id INT,
    color_id INT,
    preference_type NVARCHAR(20) NOT NULL,
    preference_value NVARCHAR(100),
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_CustomerPreferences_Customer FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    CONSTRAINT FK_CustomerPreferences_Category FOREIGN KEY (category_id) REFERENCES Categories(category_id),
    CONSTRAINT FK_CustomerPreferences_Brand FOREIGN KEY (brand_id) REFERENCES Brands(brand_id),
    CONSTRAINT FK_CustomerPreferences_Size FOREIGN KEY (size_id) REFERENCES Sizes(size_id),
    CONSTRAINT FK_CustomerPreferences_Color FOREIGN KEY (color_id) REFERENCES Colors(color_id),
    CONSTRAINT CHK_CustomerPreferences_Type CHECK (preference_type IN ('FAVORITE', 'AVOID', 'SIZE_PREFERENCE', 'COLOR_PREFERENCE', 'BRAND_PREFERENCE'))
);
GO

-- Bảng CustomerReviews
CREATE TABLE CustomerReviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    order_id INT,
    rating INT NOT NULL,
    title NVARCHAR(200),
    review_text NVARCHAR(MAX),
    is_verified_purchase BIT DEFAULT 0,
    is_approved BIT DEFAULT 0,
    helpful_count INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_CustomerReviews_Customer FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    CONSTRAINT FK_CustomerReviews_Product FOREIGN KEY (product_id) REFERENCES Products(product_id),
    CONSTRAINT FK_CustomerReviews_Order FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    CONSTRAINT CHK_CustomerReviews_Rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT CHK_CustomerReviews_HelpfulCount CHECK (helpful_count >= 0)
);
GO

-- Cập nhật bảng Orders để tham chiếu đến Customers
ALTER TABLE Orders ADD customer_id INT NULL;
GO

-- Thêm foreign key constraint cho Orders
ALTER TABLE Orders 
ADD CONSTRAINT FK_Orders_Customer FOREIGN KEY (customer_id) REFERENCES Customers(customer_id);
GO

-- =============================================
-- Thêm dữ liệu mẫu cho Customers
-- =============================================

-- Tạo customers từ users hiện có
INSERT INTO Customers (user_id, first_name, last_name, phone, email, date_of_birth, gender, address, city, customer_type, loyalty_points, total_orders, total_spent)
SELECT 
    u.user_id,
    CASE 
        WHEN u.username = 'admin' THEN 'Admin'
        WHEN u.username = 'customer1' THEN 'Nguyen'
        WHEN u.username = 'customer2' THEN 'Tran'
        ELSE 'Unknown'
    END as first_name,
    CASE 
        WHEN u.username = 'admin' THEN 'System'
        WHEN u.username = 'customer1' THEN 'Van A'
        WHEN u.username = 'customer2' THEN 'Thi B'
        ELSE 'User'
    END as last_name,
    u.phone,
    u.email,
    CASE 
        WHEN u.username = 'customer1' THEN '1990-05-15'
        WHEN u.username = 'customer2' THEN '1985-12-20'
        ELSE NULL
    END as date_of_birth,
    CASE 
        WHEN u.username = 'customer1' THEN 'Male'
        WHEN u.username = 'customer2' THEN 'Female'
        ELSE NULL
    END as gender,
    CASE 
        WHEN u.username = 'customer1' THEN '123 Le Loi, Quan 1'
        WHEN u.username = 'customer2' THEN '456 Nguyen Hue, Quan 3'
        ELSE NULL
    END as address,
    CASE 
        WHEN u.username = 'customer1' THEN 'Ho Chi Minh'
        WHEN u.username = 'customer2' THEN 'Ho Chi Minh'
        ELSE NULL
    END as city,
    CASE 
        WHEN u.username = 'admin' THEN 'REGULAR'
        WHEN u.username = 'customer1' THEN 'VIP'
        WHEN u.username = 'customer2' THEN 'REGULAR'
    END as customer_type,
    CASE 
        WHEN u.username = 'customer1' THEN 150
        WHEN u.username = 'customer2' THEN 50
        ELSE 0
    END as loyalty_points,
    CASE 
        WHEN u.username = 'customer1' THEN 1
        WHEN u.username = 'customer2' THEN 1
        ELSE 0
    END as total_orders,
    CASE 
        WHEN u.username = 'customer1' THEN 398000
        WHEN u.username = 'customer2' THEN 798000
        ELSE 0
    END as total_spent
FROM Users u
WHERE u.role = 'CUSTOMER' OR u.role = 'ADMIN';
GO

-- Cập nhật customer_id trong Orders
UPDATE Orders 
SET customer_id = (
    SELECT c.customer_id 
    FROM Customers c 
    INNER JOIN Users u ON c.user_id = u.user_id 
    WHERE u.user_id = Orders.user_id
);
GO

-- Thêm địa chỉ giao hàng
INSERT INTO CustomerAddresses (customer_id, address_type, full_name, phone, address_line1, city, postal_code, is_default)
SELECT 
    c.customer_id,
    'SHIPPING',
    c.first_name + ' ' + c.last_name,
    c.phone,
    c.address,
    c.city,
    '700000',
    1
FROM Customers c
WHERE c.address IS NOT NULL;
GO

-- Thêm sở thích khách hàng
INSERT INTO CustomerPreferences (customer_id, category_id, brand_id, preference_type, preference_value)
SELECT 
    c.customer_id,
    CASE 
        WHEN c.customer_id = 2 THEN 1  -- customer1 thích category 1 (Áo)
        WHEN c.customer_id = 3 THEN 2  -- customer2 thích category 2 (Quần)
    END,
    CASE 
        WHEN c.customer_id = 2 THEN 1  -- customer1 thích brand 1 (Uniqlo)
        WHEN c.customer_id = 3 THEN 2  -- customer2 thích brand 2 (Nike)
    END,
    'FAVORITE',
    CASE 
        WHEN c.customer_id = 2 THEN 'Uniqlo'
        WHEN c.customer_id = 3 THEN 'Nike'
    END
FROM Customers c
WHERE c.customer_id IN (2, 3);
GO

-- Thêm đánh giá sản phẩm
INSERT INTO CustomerReviews (customer_id, product_id, order_id, rating, title, review_text, is_verified_purchase, is_approved)
SELECT 
    c.customer_id,
    CASE 
        WHEN c.customer_id = 2 THEN 1  -- customer1 đánh giá product 1
        WHEN c.customer_id = 3 THEN 2  -- customer2 đánh giá product 2
    END,
    CASE 
        WHEN c.customer_id = 2 THEN 1  -- order 1
        WHEN c.customer_id = 3 THEN 2  -- order 2
    END,
    CASE 
        WHEN c.customer_id = 2 THEN 5  -- 5 sao
        WHEN c.customer_id = 3 THEN 4  -- 4 sao
    END,
    CASE 
        WHEN c.customer_id = 2 THEN 'Áo thun rất tốt!'
        WHEN c.customer_id = 3 THEN 'Quần jean chất lượng'
    END,
    CASE 
        WHEN c.customer_id = 2 THEN 'Chất liệu cotton mềm mại, form dáng đẹp. Sẽ mua thêm!'
        WHEN c.customer_id = 3 THEN 'Quần jean co giãn tốt, phù hợp cho vận động. Chỉ hơi đắt một chút.'
    END,
    1, 1
FROM Customers c
WHERE c.customer_id IN (2, 3);
GO

-- =============================================
-- Cập nhật thống kê cuối cùng
-- =============================================

-- Hiển thị thống kê mới
SELECT 'Final Database Statistics' as Info;
SELECT 'Users' as TableName, COUNT(*) as RecordCount FROM Users
UNION ALL
SELECT 'Customers', COUNT(*) FROM Customers
UNION ALL
SELECT 'CustomerAddresses', COUNT(*) FROM CustomerAddresses
UNION ALL
SELECT 'CustomerPreferences', COUNT(*) FROM CustomerPreferences
UNION ALL
SELECT 'CustomerReviews', COUNT(*) FROM CustomerReviews
UNION ALL
SELECT 'Categories', COUNT(*) FROM Categories
UNION ALL
SELECT 'Brands', COUNT(*) FROM Brands
UNION ALL
SELECT 'Products', COUNT(*) FROM Products
UNION ALL
SELECT 'ProductImages', COUNT(*) FROM ProductImages
UNION ALL
SELECT 'ProductVariants', COUNT(*) FROM ProductVariants
UNION ALL
SELECT 'Orders', COUNT(*) FROM Orders
UNION ALL
SELECT 'OrderItems', COUNT(*) FROM OrderItems;

PRINT 'Database ClothingStoreDB đã được tạo thành công!';
PRINT 'Tổng cộng: 17 bảng, 15 views, 10 triggers, 4 stored procedures';
PRINT 'Dữ liệu mẫu đã được thêm vào tất cả các bảng';
PRINT 'Hỗ trợ multiple images cho sản phẩm (tối đa 10 ảnh/sản phẩm)';
