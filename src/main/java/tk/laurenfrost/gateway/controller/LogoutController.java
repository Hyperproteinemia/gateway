package tk.laurenfrost.gateway.controller;

import tk.laurenfrost.gateway.config.JwtConfig;
import tk.laurenfrost.gateway.service.InvalidJwtService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

    @Lazy
    private final InvalidJwtService invalidJwtService;

    @Lazy
    private final JwtConfig jwtConfig;

    public LogoutController(InvalidJwtService invalidJwtService, JwtConfig jwtConfig) {
        this.invalidJwtService = invalidJwtService;
        this.jwtConfig = jwtConfig;
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logOut(@RequestHeader(value = "Authorization") String header) {
        try {
            invalidJwtService.logout(header);
            return ResponseEntity.status(HttpStatus.OK).body("Logged out");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
