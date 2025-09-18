-- Insert sample data for Sizes and Colors
USE ClothingStoreDB;

-- Check if tables exist
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Sizes')
BEGIN
    CREATE TABLE Sizes (
        size_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        size_name NVARCHAR(10) NOT NULL UNIQUE
    );
END

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Colors')
BEGIN
    CREATE TABLE Colors (
        color_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        color_name NVARCHAR(50) NOT NULL UNIQUE
    );
END

-- Insert Sizes if not exists
IF NOT EXISTS (SELECT 1 FROM Sizes WHERE size_name = 'S')
    INSERT INTO Sizes (size_name) VALUES ('S');

IF NOT EXISTS (SELECT 1 FROM Sizes WHERE size_name = 'M')
    INSERT INTO Sizes (size_name) VALUES ('M');

IF NOT EXISTS (SELECT 1 FROM Sizes WHERE size_name = 'L')
    INSERT INTO Sizes (size_name) VALUES ('L');

IF NOT EXISTS (SELECT 1 FROM Sizes WHERE size_name = 'XL')
    INSERT INTO Sizes (size_name) VALUES ('XL');

IF NOT EXISTS (SELECT 1 FROM Sizes WHERE size_name = 'XXL')
    INSERT INTO Sizes (size_name) VALUES ('XXL');

IF NOT EXISTS (SELECT 1 FROM Sizes WHERE size_name = 'XS')
    INSERT INTO Sizes (size_name) VALUES ('XS');

IF NOT EXISTS (SELECT 1 FROM Sizes WHERE size_name = 'XXXL')
    INSERT INTO Sizes (size_name) VALUES ('XXXL');

-- Insert Colors if not exists
IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Red')
    INSERT INTO Colors (color_name) VALUES ('Red');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Blue')
    INSERT INTO Colors (color_name) VALUES ('Blue');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Green')
    INSERT INTO Colors (color_name) VALUES ('Green');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Black')
    INSERT INTO Colors (color_name) VALUES ('Black');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'White')
    INSERT INTO Colors (color_name) VALUES ('White');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Yellow')
    INSERT INTO Colors (color_name) VALUES ('Yellow');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Pink')
    INSERT INTO Colors (color_name) VALUES ('Pink');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Gray')
    INSERT INTO Colors (color_name) VALUES ('Gray');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Orange')
    INSERT INTO Colors (color_name) VALUES ('Orange');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Purple')
    INSERT INTO Colors (color_name) VALUES ('Purple');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Brown')
    INSERT INTO Colors (color_name) VALUES ('Brown');

IF NOT EXISTS (SELECT 1 FROM Colors WHERE color_name = 'Navy')
    INSERT INTO Colors (color_name) VALUES ('Navy');

-- Check inserted data
SELECT 'Sizes' as TableName, COUNT(*) as Count FROM Sizes
UNION ALL
SELECT 'Colors', COUNT(*) FROM Colors;

-- Show sample data
SELECT 'Sizes Data:' as Info;
SELECT * FROM Sizes ORDER BY size_name;

SELECT 'Colors Data:' as Info;
SELECT * FROM Colors ORDER BY color_name;
