package com.trodix.clipystream.security.service;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import com.trodix.clipystream.core.exception.BadRequestException;
import com.trodix.clipystream.security.entity.ERole;
import com.trodix.clipystream.security.entity.RefreshToken;
import com.trodix.clipystream.security.entity.Role;
import com.trodix.clipystream.security.entity.User;
import com.trodix.clipystream.security.jwt.JwtService;
import com.trodix.clipystream.security.model.request.LoginRequest;
import com.trodix.clipystream.security.model.request.LogoutRequest;
import com.trodix.clipystream.security.model.request.RefreshTokenRequest;
import com.trodix.clipystream.security.model.request.SignupRequest;
import com.trodix.clipystream.security.model.response.JwtResponse;
import com.trodix.clipystream.security.repository.RoleRepository;
import com.trodix.clipystream.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class AuthService {

    @Value("${app.default-user.username}")
    private String defaultUser;

    @Value("${app.default-user.email}")
    private String defaultEmail;

    @Value("#{'${app.default-user.roles}'.split(',')}")
    private Set<String> defaultRoles;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostConstruct
    public void init() {
		initDefaultUser();
    }

    /**
     * Log the user in
     * 
     * @param loginRequest
     * @return The user tokens pair
     */
    public JwtResponse authenticateUser(final LoginRequest loginRequest) {

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String jwt = jwtUtils.generateJwtToken(authentication);

        final RefreshToken userRefreshToken = this.refreshTokenService.getToken(loginRequest.getUsername());
        String refreshToken = null;

        if (userRefreshToken == null) {
            refreshToken = refreshTokenService.generateRefreshToken(authentication).getToken();
        } else {
            refreshToken = userRefreshToken.getToken();
        }

        final UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        final List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(jwt, refreshToken, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles);
    }

    /**
     * Generate a new JWT Token if the given refreshToken is valid
     * 
     * @param refreshTokenRequest
     * @return The new JWT token and refreshToken pair
     */
    public JwtResponse refreshToken(final RefreshTokenRequest refreshTokenRequest) {

        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken(), refreshTokenRequest.getUsername());

        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(refreshTokenRequest.getUsername());

        final List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        final String jwt = jwtUtils.generateJwtTokenWithUsername(userDetails.getUsername());

        return new JwtResponse(jwt, refreshTokenRequest.getRefreshToken(), userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles);
    }

    /**
     * Logout the given user and destroy his refreshToken
     * 
     * @param logoutRequest
     */
    public void logout(final LogoutRequest logoutRequest) {

        refreshTokenService.deleteRefreshToken(logoutRequest.getRefreshToken());
    }

    /**
     * Create a new user in the database with the given credentials
     * 
     * @param signupRequest
     */
    public void registerUser(final SignupRequest signupRequest) {

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Error: Email is already in use!");
        }

        // Create new user's account
        final User user = new User(signupRequest.getUsername(), signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword()));

        final Set<String> strRoles = signupRequest.getRole();
        final Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            final Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new BadRequestException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {

                    case "admin":
                        final Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new BadRequestException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;

                    case "moderator":
                        final Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new BadRequestException("Error: Role is not found."));
                        roles.add(modRole);
                        break;

                    default:
                        final Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new BadRequestException("Error: Role is not found."));
                        roles.add(userRole);

                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

    }

    /**
     * Initialize the default user in the database
     */
    public void initDefaultUser() {
        try {
            // Create the default roles
            this.roleService.initDefaultRoles();

            final String defaultPassword = UUID.randomUUID().toString();

            final SignupRequest signupRequest = new SignupRequest();
            signupRequest.setEmail(this.defaultEmail);
            signupRequest.setUsername(this.defaultUser);
            signupRequest.setPassword(defaultPassword);
            signupRequest.setRole(this.defaultRoles);

            this.registerUser(signupRequest);

            log.info(MessageFormat.format("\n\n=============== Default credentials are: {0} / {1} ===============\n",
                    this.defaultUser, defaultPassword));
        } catch (final BadRequestException e) {
            // admin user has already been created
            log.info(MessageFormat.format("Error while generating the default user [{0}], skiping... {1}", this.defaultUser, e.getMessage()));
        }
    }

}
