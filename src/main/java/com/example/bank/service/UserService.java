package com.example.bank.service;

import com.example.bank.dto.RegisterRequest;
import com.example.bank.model.User;

public interface UserService {
    User registerUser(RegisterRequest request);
    User authenticateUser(String email, String password);
    User getUserById(Long id);
}