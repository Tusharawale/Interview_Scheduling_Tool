package com.example.authadmin.controller;

import com.example.authadmin.dto.UserDtos;
import com.example.authadmin.entity.User;
import com.example.authadmin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody UserDtos.RegisterRequest request) {
        User user = userService.register(request);
        UserDtos.UserResponse resp = toResponse(user);
        return ResponseEntity.status(HttpStatus.CREATED).body((Object) resp);
    }

	@GetMapping("/verify")
	public ResponseEntity<Void> verify(@RequestParam("token") String token) {
		boolean ok = userService.verify(token);
		HttpHeaders headers = new HttpHeaders();
		String location = "/login.html";
		headers.setLocation(URI.create(location));
		return new ResponseEntity<>(headers, ok ? HttpStatus.FOUND : HttpStatus.FOUND);
	}

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody UserDtos.LoginRequest request) {
		Optional<User> optional = userService.authenticate(request.getEmail(), request.getPassword());
		if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or email not verified");
		}
		return ResponseEntity.ok(toResponse(optional.get()));
	}

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable("id") Integer id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok((Object) toResponse(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<Object> updateEmail(@PathVariable("id") Integer id,
                                              @Valid @RequestBody UserDtos.UpdateEmailRequest request) {
        try {
            User updated = userService.updateEmail(id, request.getEmail());
            return ResponseEntity.ok((Object) toResponse(updated));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

	private static UserDtos.UserResponse toResponse(User u) {
		UserDtos.UserResponse resp = new UserDtos.UserResponse();
		resp.setId(u.getId());
		resp.setUsername(u.getUsername());
		resp.setEmail(u.getEmail());
		resp.setVerified(u.isVerified());
		return resp;
	}
}
