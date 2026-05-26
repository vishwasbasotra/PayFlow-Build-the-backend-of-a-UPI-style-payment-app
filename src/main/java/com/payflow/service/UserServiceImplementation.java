package com.payflow.service;

import com.payflow.entity.User;
import com.payflow.exceptions.APIException;
import com.payflow.payload.UserDTO;
import com.payflow.payload.UserResponse;
import com.payflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImplementation implements UserService{

    @Autowired
    UserRepository userRepository;

    private UserDTO userToUserDTO(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setName(user.getName());
        userDTO.setUpiId(user.getUpiId());
        userDTO.setBalance(user.getBalance());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        return userDTO;
    }

    private User userDtoToUser(UserDTO userDTO){
        User user = new User();
        user.setName(userDTO.getName());
        user.setUpiId(userDTO.getUpiId());
        user.setBalance(userDTO.getBalance());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        return user;
    }

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        User newUser = userDtoToUser(userDTO);
        User existingUser = userRepository.findByPhoneNumber(userDTO.getPhoneNumber());

        if(existingUser != null){
            throw new APIException("User '"+existingUser.getName()+"' already exist!");
        }
        User savedUser = userRepository.save(newUser);
        return userToUserDTO(savedUser);
    }

    @Override
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<User> userPage = userRepository.findAll(pageDetails);
        List<User> userList = userPage.getContent();

        if(userList.isEmpty())  throw new APIException("Users doesn't exist yet!");

        List<UserDTO> userDTOList = userList.stream()
                .map(this::userToUserDTO)
                .toList();

        UserResponse userResponse = new UserResponse();

        userResponse.setContent(userDTOList);
        userResponse.setPageNumber(userPage.getNumber());
        userResponse.setPageSize(userPage.getSize());
        userResponse.setTotalElements(userPage.getTotalElements());
        userResponse.setTotalPages(userPage.getTotalPages());
        userResponse.setLastPage(userPage.isLast());

        return userResponse;
    }
}
