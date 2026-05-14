package com.example.copilot.service.impl;

import com.example.copilot.service.AuthService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.copilot.common.ErrorCode;
import com.example.copilot.dto.request.LoginRequest;
import com.example.copilot.dto.request.RegisterRequest;
import com.example.copilot.dto.response.AuthResponse;
import com.example.copilot.entity.User;
import com.example.copilot.exception.BusinessException;
import com.example.copilot.mapper.UserMapper;
import com.example.copilot.security.JwtTokenProvider;
import com.example.copilot.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.email()));
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已注册");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER"); // default role for new registration
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole()),
                null,
                new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole()).getAuthorities()
        );
        String token = jwtTokenProvider.generateToken(authentication);
        return new AuthResponse(user.getId(), user.getEmail(), user.getName(), token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        String token = jwtTokenProvider.generateToken(authentication);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.email()));
        return new AuthResponse(user.getId(), user.getEmail(), user.getName(), token);
    }

    @Override
    public AuthResponse me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return new AuthResponse(user.getId(), user.getEmail(), user.getName(), null);
    }

    @Override
    public AuthResponse refresh(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new AuthResponse(user.getId(), user.getEmail(), user.getName(), token);
    }
}
