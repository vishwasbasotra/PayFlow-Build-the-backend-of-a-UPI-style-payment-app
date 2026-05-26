package com.payflow.controller;

import com.payflow.config.AppConstants;
import com.payflow.entity.User;
import com.payflow.payload.UserDTO;
import com.payflow.payload.UserResponse;
import com.payflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/public/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO){
        UserDTO newUserDTO = userService.registerUser(userDTO);
        return new ResponseEntity<>(newUserDTO,HttpStatus.CREATED);
    }

    @GetMapping("/public/users")
    public ResponseEntity<UserResponse> getAllUsers(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USER_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        return new ResponseEntity<>(userService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder), HttpStatus.OK);
    }
}
