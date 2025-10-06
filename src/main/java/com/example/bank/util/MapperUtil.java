package com.example.bank.util;

import com.example.bank.dto.RegisterRequest;
import com.example.bank.model.User;

public class MapperUtil {
    
    public static User mapToUser(RegisterRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return user;
    }
}