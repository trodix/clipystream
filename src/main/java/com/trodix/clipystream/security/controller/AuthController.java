package com.trodix.clipystream.security.controller;

import javax.validation.Valid;
import com.trodix.clipystream.security.model.request.LoginRequest;
import com.trodix.clipystream.security.model.request.LogoutRequest;
import com.trodix.clipystream.security.model.request.RefreshTokenRequest;
import com.trodix.clipystream.security.model.request.SignupRequest;
import com.trodix.clipystream.security.model.response.JwtResponse;
import com.trodix.clipystream.security.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	@PostMapping("/signin")
	public JwtResponse authenticateUser(@Valid @RequestBody final LoginRequest loginRequest) {
		return authService.authenticateUser(loginRequest);
	}

	@PostMapping("/refresh-token")
	public JwtResponse refreshToken(@Valid @RequestBody final RefreshTokenRequest refreshTokenRequest) {
		return authService.refreshToken(refreshTokenRequest);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@Valid @RequestBody final LogoutRequest logoutRequest) {
		authService.logout(logoutRequest);
	}

	@PostMapping("/signup")
	public void registerUser(@Valid @RequestBody final SignupRequest signupRequest) {
		authService.registerUser(signupRequest);
	}
}
