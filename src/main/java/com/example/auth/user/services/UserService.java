package com.example.auth.user.services;


import com.example.auth.user.DTOs.UpdateDTO;
import com.example.auth.user.User;
import com.example.auth.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findById(Integer id){
       User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found"));
       return user;
    }

    public boolean isEmailRegistered(String email){
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public UserDetails save(User user){
        return userRepository.save(user);
    }

    public UserDetails findByEmail(String email) {
        UserDetails user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with such email not found"));
        return user;
    }

    @Transactional
    public User updateUser(Integer userId, UpdateDTO dto){
        User user = this.findById(userId);

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPhoneNumber(dto.phoneNumber());

        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(Integer id, String oldPassword, String newPassword){
        User user = this.findById(id);

        if(!passwordEncoder.matches(oldPassword, user.getPassword())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Given old password is wrong!");
        }else{
            if(Objects.equals(oldPassword, newPassword)){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "New and old passwords cannot be the same");
            }
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
}
