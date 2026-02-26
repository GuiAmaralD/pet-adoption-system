package com.example.auth.user.controllers;


import com.example.auth.user.DTOs.ChangePasswordDTO;
import com.example.auth.user.DTOs.UpdateDTO;
import com.example.auth.user.DTOs.UserResponseDTO;
import com.example.auth.user.User;
import com.example.auth.user.UserMapper;
import com.example.auth.user.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("account")
@CrossOrigin("*")
public class UserAccountController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserAccountController(UserService userService, UserMapper userMapper){
        this.userService = userService;
        this.userMapper = userMapper;
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getLoggedUserInfo(Principal principal){
        User user = (User) userService.findByEmail(principal.getName());
        return ResponseEntity.ok().body(userMapper.toDTO(user));
    }

    @PutMapping
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody @Valid UpdateDTO updateDTO,
                                                  Principal principal){
        User user = (User) userService.findByEmail(principal.getName());

        User updated = userService.updateUser(user.getId(), updateDTO);
        return ResponseEntity.ok().body(userMapper.toDTO(updated));

    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid ChangePasswordDTO dto, Principal principal){

        User user = (User) userService.findByEmail(principal.getName());
        Integer id = user.getId();

        userService.updatePassword(id, dto.oldPassword(), dto.newPassword());

        return ResponseEntity.ok().body("Password updated.");
    }
}
