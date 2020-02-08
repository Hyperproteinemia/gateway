package tk.laurenfrost.gateway.filter;                //e.printStackTrace();


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.io.IOUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import tk.laurenfrost.gateway.config.JwtConfig;
import tk.laurenfrost.gateway.service.InvalidJwtService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;


@Component
public class UsernameProxyFilter extends AbstractGatewayFilterFactory<UsernameProxyFilter.Config> {

    private final InvalidJwtService invalidJwtService;

    private final JwtConfig jwtConfig;

    public UsernameProxyFilter(InvalidJwtService invalidJwtService, JwtConfig jwtConfig) {
        super(Config.class);
        this.invalidJwtService = invalidJwtService;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(jwtConfig.getPublicKeyPath())) {    // exceptions might be thrown in creating the claims if for example the token is expired
                String header = exchange.getRequest().getHeaders().get(jwtConfig.getHeader()).get(0);

                if (header == null || !header.startsWith(jwtConfig.getPrefix())) {
                    return chain.filter(exchange);
                }
                String token = header.replace(jwtConfig.getPrefix(), "");

                if (invalidJwtService.findInvalidJwt(token) != null) {
                    return chain.filter(exchange);
                }

                // 4. Validate the token

                String publicKeyString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
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
                    return chain.filter(exchange);
                }
                ServerHttpRequest request = exchange.getRequest().mutate().
                        header("username", username).
                        build();

                return chain.filter(exchange.mutate().request(request).build());

            } catch (Exception e) {
                return chain.filter(exchange);
            }
        };
    }

    public static class Config {
        //Put the configuration properties for your filter here
    }
}
