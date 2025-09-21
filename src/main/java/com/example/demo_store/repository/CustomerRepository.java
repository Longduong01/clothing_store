package com.example.demo_store.repository;

import com.example.demo_store.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // Find customer by user ID
    Optional<Customer> findByUserUserId(Long userId);
    
    // Find customer by email
    Optional<Customer> findByEmail(String email);
    
    // Find customers by customer type
    Page<Customer> findByCustomerType(Customer.CustomerType customerType, Pageable pageable);
    
    // Find customers by gender
    Page<Customer> findByGender(Customer.CustomerGender gender, Pageable pageable);
    
    // Find customers by customer type and gender
    Page<Customer> findByCustomerTypeAndGender(Customer.CustomerType customerType, 
                                               Customer.CustomerGender gender, Pageable pageable);
    
    // Find active customers
    List<Customer> findByIsActiveTrue();
    
    // Find inactive customers
    List<Customer> findByIsActiveFalse();
    
    // Count customers by customer type
    long countByCustomerType(Customer.CustomerType customerType);
    
    // Count customers by gender
    long countByGender(Customer.CustomerGender gender);
    
    // Count active customers
    long countByIsActive(Boolean isActive);
    
    // Find customers by city
    List<Customer> findByCity(String city);
    
    // Find customers by country
    List<Customer> findByCountry(String country);
    
    // Find VIP customers
    List<Customer> findByCustomerType(Customer.CustomerType customerType);
    
    // Find customers with loyalty points greater than
    @Query("SELECT c FROM Customer c WHERE c.loyaltyPoints >= :minPoints")
    List<Customer> findByLoyaltyPointsGreaterThanEqual(@Param("minPoints") Integer minPoints);
    
    // Find customers by total spent range
    @Query("SELECT c FROM Customer c WHERE c.totalSpent BETWEEN :minAmount AND :maxAmount")
    List<Customer> findByTotalSpentBetween(@Param("minAmount") Double minAmount, 
                                         @Param("maxAmount") Double maxAmount);
    
    // Get top customers by total spent
    @Query("SELECT c FROM Customer c ORDER BY c.totalSpent DESC")
    List<Customer> findTopCustomersByTotalSpent(Pageable pageable);
    
    // Get customers by registration date range
    @Query("SELECT c FROM Customer c WHERE c.registrationDate BETWEEN :startDate AND :endDate")
    List<Customer> findByRegistrationDateBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                                @Param("endDate") java.time.LocalDateTime endDate);
}