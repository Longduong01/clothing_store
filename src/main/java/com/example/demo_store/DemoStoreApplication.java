package com.example.demo_store;

import com.example.demo_store.entity.User;
import com.example.demo_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoStoreApplication {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(DemoStoreApplication.class, args);
    }

    @Bean
    public CommandLineRunner testDatabaseConnection() {
        return args -> {
            try {
                System.out.println("\n=== TESTING DATABASE CONNECTION ===");
                
                // Test counting users
                long userCount = userRepository.count();
                System.out.println("✅ Database connection successful!");
                System.out.println("📊 Total users in database: " + userCount);
                
                // Test fetching all users
                Iterable<User> users = userRepository.findAll();
                System.out.println("👥 Users in database:");
                for (User user : users) {
                    System.out.println("  - " + user.getUsername() + " (" + user.getEmail() + ") - Role: " + user.getRole());
                }
                
                System.out.println("🎉 Database connection test completed successfully!");
                System.out.println("🌐 Application is ready at: http://localhost:8080");
                System.out.println("📡 Test API endpoints:");
                System.out.println("   - http://localhost:8080/api/test/connection");
                System.out.println("   - http://localhost:8080/api/test/users");
                System.out.println("=====================================\n");
                
            } catch (Exception e) {
                System.err.println("❌ Database connection failed!");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
    
}
