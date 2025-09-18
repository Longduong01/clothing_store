package com.example.demo_store.controller;

import com.example.demo_store.entity.Customer;
import com.example.demo_store.entity.User;
import com.example.demo_store.repository.CustomerRepository;
import com.example.demo_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    // GET /api/customers - Lấy tất cả khách hàng với pagination
    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String customerType,
            @RequestParam(required = false) String gender
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Customer> customers;
            if (customerType != null && gender != null) {
                customers = customerRepository.findByCustomerTypeAndGender(
                    Customer.CustomerType.valueOf(customerType), 
                    Customer.Gender.valueOf(gender), 
                    pageable
                );
            } else if (customerType != null) {
                customers = customerRepository.findByCustomerType(
                    Customer.CustomerType.valueOf(customerType), 
                    pageable
                );
            } else if (gender != null) {
                customers = customerRepository.findByGender(
                    Customer.Gender.valueOf(gender), 
                    pageable
                );
            } else {
                customers = customerRepository.findAll(pageable);
            }
            
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch customers: " + e.getMessage()));
        }
    }

    // GET /api/customers/{id} - Lấy khách hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        try {
            Optional<Customer> customer = customerRepository.findById(id);
            if (customer.isPresent()) {
                return ResponseEntity.ok(customer.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch customer: " + e.getMessage()));
        }
    }

    // GET /api/customers/user/{userId} - Lấy khách hàng theo User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCustomerByUserId(@PathVariable Long userId) {
        try {
            Optional<Customer> customer = customerRepository.findByUserUserId(userId);
            if (customer.isPresent()) {
                return ResponseEntity.ok(customer.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch customer: " + e.getMessage()));
        }
    }

    // GET /api/customers/email/{email} - Lấy khách hàng theo email
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getCustomerByEmail(@PathVariable String email) {
        try {
            Optional<Customer> customer = customerRepository.findByEmail(email);
            if (customer.isPresent()) {
                return ResponseEntity.ok(customer.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch customer: " + e.getMessage()));
        }
    }

    // POST /api/customers - Tạo khách hàng mới
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CustomerCreateRequest request) {
        try {
            // Validate user exists
            if (!userRepository.existsById(request.getUserId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("User not found"));
            }

            // Check if customer already exists for this user
            if (customerRepository.findByUserUserId(request.getUserId()).isPresent()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Customer already exists for this user"));
            }

            User user = userRepository.findById(request.getUserId()).get();

            // Create customer
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setFirstName(request.getFirstName());
            customer.setLastName(request.getLastName());
            customer.setPhone(request.getPhone());
            customer.setEmail(request.getEmail());
            customer.setDateOfBirth(request.getDateOfBirth());
            customer.setGender(request.getGender());
            customer.setAddress(request.getAddress());
            customer.setCity(request.getCity());
            customer.setStateProvince(request.getStateProvince());
            customer.setPostalCode(request.getPostalCode());
            customer.setCountry(request.getCountry());
            customer.setCustomerType(request.getCustomerType());
            customer.setLoyaltyPoints(0);
            customer.setTotalOrders(0);
            customer.setTotalSpent(0.0);
            customer.setRegistrationDate(LocalDateTime.now());
            customer.setIsActive(true);
            customer.setNotes(request.getNotes());

            Customer savedCustomer = customerRepository.save(customer);
            return ResponseEntity.ok(savedCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to create customer: " + e.getMessage()));
        }
    }

    // PUT /api/customers/{id} - Cập nhật khách hàng
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody CustomerUpdateRequest request) {
        try {
            Optional<Customer> customerOptional = customerRepository.findById(id);
            if (customerOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Customer customer = customerOptional.get();
            if (request.getFirstName() != null) {
                customer.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                customer.setLastName(request.getLastName());
            }
            if (request.getPhone() != null) {
                customer.setPhone(request.getPhone());
            }
            if (request.getEmail() != null) {
                customer.setEmail(request.getEmail());
            }
            if (request.getDateOfBirth() != null) {
                customer.setDateOfBirth(request.getDateOfBirth());
            }
            if (request.getGender() != null) {
                customer.setGender(request.getGender());
            }
            if (request.getAddress() != null) {
                customer.setAddress(request.getAddress());
            }
            if (request.getCity() != null) {
                customer.setCity(request.getCity());
            }
            if (request.getStateProvince() != null) {
                customer.setStateProvince(request.getStateProvince());
            }
            if (request.getPostalCode() != null) {
                customer.setPostalCode(request.getPostalCode());
            }
            if (request.getCountry() != null) {
                customer.setCountry(request.getCountry());
            }
            if (request.getCustomerType() != null) {
                customer.setCustomerType(request.getCustomerType());
            }
            if (request.getIsActive() != null) {
                customer.setIsActive(request.getIsActive());
            }
            if (request.getNotes() != null) {
                customer.setNotes(request.getNotes());
            }

            Customer updatedCustomer = customerRepository.save(customer);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to update customer: " + e.getMessage()));
        }
    }

    // DELETE /api/customers/{id} - Xóa khách hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            if (!customerRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            customerRepository.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse("Customer deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to delete customer: " + e.getMessage()));
        }
    }

    // GET /api/customers/stats - Thống kê khách hàng
    @GetMapping("/stats")
    public ResponseEntity<?> getCustomerStats() {
        try {
            long totalCustomers = customerRepository.count();
            long activeCustomers = customerRepository.countByIsActive(true);
            long vipCustomers = customerRepository.countByCustomerType(Customer.CustomerType.VIP);
            long premiumCustomers = customerRepository.countByCustomerType(Customer.CustomerType.PREMIUM);

            CustomerStats stats = new CustomerStats();
            stats.setTotalCustomers(totalCustomers);
            stats.setActiveCustomers(activeCustomers);
            stats.setVipCustomers(vipCustomers);
            stats.setPremiumCustomers(premiumCustomers);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch customer stats: " + e.getMessage()));
        }
    }

    // Response classes
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class CustomerCreateRequest {
        private Long userId;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private java.time.LocalDate dateOfBirth;
        private Customer.Gender gender;
        private String address;
        private String city;
        private String stateProvince;
        private String postalCode;
        private String country;
        private Customer.CustomerType customerType;
        private String notes;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(java.time.LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        
        public Customer.Gender getGender() { return gender; }
        public void setGender(Customer.Gender gender) { this.gender = gender; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getStateProvince() { return stateProvince; }
        public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
        
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public Customer.CustomerType getCustomerType() { return customerType; }
        public void setCustomerType(Customer.CustomerType customerType) { this.customerType = customerType; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class CustomerUpdateRequest {
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private java.time.LocalDate dateOfBirth;
        private Customer.Gender gender;
        private String address;
        private String city;
        private String stateProvince;
        private String postalCode;
        private String country;
        private Customer.CustomerType customerType;
        private Boolean isActive;
        private String notes;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(java.time.LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        
        public Customer.Gender getGender() { return gender; }
        public void setGender(Customer.Gender gender) { this.gender = gender; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getStateProvince() { return stateProvince; }
        public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
        
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public Customer.CustomerType getCustomerType() { return customerType; }
        public void setCustomerType(Customer.CustomerType customerType) { this.customerType = customerType; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class CustomerStats {
        private long totalCustomers;
        private long activeCustomers;
        private long vipCustomers;
        private long premiumCustomers;

        // Getters and setters
        public long getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
        
        public long getActiveCustomers() { return activeCustomers; }
        public void setActiveCustomers(long activeCustomers) { this.activeCustomers = activeCustomers; }
        
        public long getVipCustomers() { return vipCustomers; }
        public void setVipCustomers(long vipCustomers) { this.vipCustomers = vipCustomers; }
        
        public long getPremiumCustomers() { return premiumCustomers; }
        public void setPremiumCustomers(long premiumCustomers) { this.premiumCustomers = premiumCustomers; }
    }
}
