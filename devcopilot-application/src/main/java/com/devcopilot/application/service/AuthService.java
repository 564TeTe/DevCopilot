package com.devcopilot.application.service;

import com.devcopilot.application.dto.AuthResult;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.common.security.JwtTokenService;
import com.devcopilot.domain.model.UserAccount;
import com.devcopilot.domain.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResult register(String username, String password, String displayName) {
        if (userAccountRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        String safeDisplayName = displayName == null || displayName.isBlank() ? username : displayName;
        UserAccount account = userAccountRepository.save(new UserAccount(username, passwordEncoder.encode(password), safeDisplayName));
        return toAuthResult(account);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String username, String password) {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误"));
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        return toAuthResult(account);
    }

    private AuthResult toAuthResult(UserAccount account) {
        return new AuthResult(
                account.getId(),
                account.getUsername(),
                account.getDisplayName(),
                jwtTokenService.createToken(account.getId(), account.getUsername())
        );
    }
}
