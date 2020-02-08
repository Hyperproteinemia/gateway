package tk.laurenfrost.gateway.repository;


import tk.laurenfrost.gateway.entity.InvalidJwt;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface InvalidJwtRepository extends MongoRepository <InvalidJwt, String > {

    InvalidJwt findByToken(final String token);
}