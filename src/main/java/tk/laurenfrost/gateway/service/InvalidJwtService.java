package tk.laurenfrost.gateway.service;

import org.springframework.context.annotation.Lazy;
import tk.laurenfrost.gateway.config.JwtConfig;
import tk.laurenfrost.gateway.entity.InvalidJwt;
import tk.laurenfrost.gateway.repository.InvalidJwtRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service("invalidJwtService")
public class InvalidJwtService {
    private final InvalidJwtRepository invalidJwtRepository;

    private final JwtConfig jwtConfig;

    public InvalidJwtService(InvalidJwtRepository invalidJwtRepository, JwtConfig jwtConfig) {
        this.invalidJwtRepository = invalidJwtRepository;
        this.jwtConfig = jwtConfig;
    }

    public void addToken(InvalidJwt invalidJwt) {
        invalidJwtRepository.insert(invalidJwt);
    }

    public InvalidJwt findInvalidJwt(String token) {
        return invalidJwtRepository.findByToken(token);
    }

    public void logout(String header) {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(jwtConfig.getPublicKeyPath())) {    // exceptions might be thrown in creating the claims if for example the token is expired

            if (header == null || !header.startsWith(jwtConfig.getPrefix())) {
                throw new IllegalArgumentException("Invalid jwt");
            }
            String token = header.replace(jwtConfig.getPrefix(), "");


            // Check if already logged out
            InvalidJwt invalidJwt = invalidJwtRepository.findByToken(token);
            if (invalidJwt != null) {
                throw new IllegalArgumentException("Already logged out");
            }

            // Validate the token

            String publicKeyString = IOUtils.toString(inputStream, "UTF-8");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] byteKey = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            PublicKey publicKey = kf.generatePublic(X509publicKey);


            Claims claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            if (username == null) {
                throw new IllegalArgumentException("Invalid jwt");
            }

            // Add token to the blacklist
            invalidJwtRepository.insert(new InvalidJwt(token, claims.getExpiration()));


        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}
