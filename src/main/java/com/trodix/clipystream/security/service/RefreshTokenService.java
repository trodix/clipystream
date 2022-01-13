package com.trodix.clipystream.security.service;

import java.time.Instant;
import java.util.UUID;
import javax.transaction.Transactional;
import com.trodix.clipystream.core.exception.UnauthorizedException;
import com.trodix.clipystream.security.entity.RefreshToken;
import com.trodix.clipystream.security.entity.User;
import com.trodix.clipystream.security.repository.RefreshTokenRepository;
import com.trodix.clipystream.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class RefreshTokenService {

    @Autowired
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private final UserRepository userRepository;

    public RefreshToken generateRefreshToken(final Authentication authentication) {

        final UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        final User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + userPrincipal.getUsername()));

        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setUser(user);

        return refreshTokenRepository.save(refreshToken);
    }

    public void validateRefreshToken(final String refreshToken, final String username) {
        refreshTokenRepository.findByTokenAndUsername(refreshToken, username)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
    }

    public RefreshToken getToken(final String username) {
        return refreshTokenRepository.findByUsername(username).orElse(null);
    }

    public void deleteRefreshToken(final String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }
}
