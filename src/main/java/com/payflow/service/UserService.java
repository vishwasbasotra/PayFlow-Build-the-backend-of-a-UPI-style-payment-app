package com.payflow.service;

import com.payflow.payload.UserDTO;
import com.payflow.payload.UserResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface UserService {
    UserDTO registerUser(@Valid UserDTO userDTO);

    UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
