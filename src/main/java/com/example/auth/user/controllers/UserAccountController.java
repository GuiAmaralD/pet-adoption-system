package com.example.auth.user.controllers;


import com.example.auth.user.DTOs.ChangePasswordDTO;
import com.example.auth.user.DTOs.DeleteAccountDTO;
import com.example.auth.user.DTOs.UpdateDTO;
import com.example.auth.user.DTOs.UserResponseDTO;
import com.example.auth.user.User;
import com.example.auth.user.UserMapper;
import com.example.auth.user.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("account")
@CrossOrigin("*")
@Tag(name = "Account", description = "Authenticated user account operations")
public class UserAccountController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserAccountController(UserService userService, UserMapper userMapper){
        this.userService = userService;
        this.userMapper = userMapper;
    }


    @GetMapping("/me")
    @Operation(summary = "Get logged-in user", description = "Returns the authenticated user's profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDTO> getLoggedUserInfo(Principal principal){
        User user = (User) userService.findByEmail(principal.getName());
        return ResponseEntity.ok().body(userMapper.toDTO(user));
    }

    @PutMapping
    @Operation(summary = "Update account data", description = "Updates the user's name, email, and phone number.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody @Valid UpdateDTO updateDTO,
                                                  Principal principal){
        User user = (User) userService.findByEmail(principal.getName());

        User updated = userService.updateUser(user.getId(), updateDTO);
        return ResponseEntity.ok().body(userMapper.toDTO(updated));

    }

    @PutMapping("/password")
    @Operation(summary = "Update password", description = "Updates the authenticated user's password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password updated"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Old password invalid or equals new password")
    })
    public ResponseEntity<String> updatePassword(@RequestBody @Valid ChangePasswordDTO dto, Principal principal){

        User user = (User) userService.findByEmail(principal.getName());
        Integer id = user.getId();

        userService.updatePassword(id, dto.oldPassword(), dto.newPassword());

        return ResponseEntity.ok().body("Password updated.");
    }

    @DeleteMapping
    @Operation(summary = "Delete account", description = "Removes the authenticated user's account.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account removed"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Invalid password")
    })
    public ResponseEntity<Void> deleteAccount(@RequestBody @Valid DeleteAccountDTO dto, Principal principal) {
        User user = (User) userService.findByEmail(principal.getName());
        Integer id = user.getId();

        userService.deleteAccount(id, dto.password());

        return ResponseEntity.noContent().build();
    }
}
