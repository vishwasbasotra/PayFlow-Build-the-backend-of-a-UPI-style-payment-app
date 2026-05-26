package com.payflow.repository;

import com.payflow.entity.User;
import com.payflow.payload.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByPhoneNumber(@NotBlank(message = "Phone Number is required") @Size(min=10, max=10, message = "Phone number should be 10 characters.") String phoneNumber);

    User findByUpiId(String userId);
}
