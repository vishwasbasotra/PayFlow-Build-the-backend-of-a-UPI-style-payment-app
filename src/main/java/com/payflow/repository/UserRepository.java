package com.payflow.repository;

import com.payflow.entity.User;
import com.payflow.payload.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByPhoneNumber(@NotBlank(message = "Phone Number is required") @Size(min=10, max=10, message = "Phone number should be 10 characters.") String phoneNumber);

    User findByUpiId(String upiId);

    @Query("SELECT u FROM User u WHERE u.balance > :balance")
    List<User> findUsersByBalanceGreaterThan(@Param("balance") Double balance);
}
