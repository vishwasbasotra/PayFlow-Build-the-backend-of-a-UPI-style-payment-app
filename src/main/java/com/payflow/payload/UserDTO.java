package com.payflow.payload;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long userId;

    @NotBlank(message = "Name is required")
    @Size(min=2, max=20, message = "Name should be 2 to 20 characters.")
    private String name;

    @NotBlank(message = "UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$",
            message = "Invalid UPI ID format")
    private String upiId;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "1000", message = "Price must be at least 1000")
    private Double balance;

    @NotBlank(message = "Phone Number is required")
    @Size(min=10, max=10, message = "Phone number should be 10 characters.")
    private String phoneNumber;

}
