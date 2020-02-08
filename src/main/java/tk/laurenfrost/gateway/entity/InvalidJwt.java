package tk.laurenfrost.gateway.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@Document(collection = "invalidTokens")
public class InvalidJwt {
    @Id
    private ObjectId id;

    @Indexed
    private String token;

    @Indexed(expireAfterSeconds = 0)
    private Date expireAt;

    public InvalidJwt(String token, Date expireAt) {
        this.token = token;
        this.expireAt = expireAt;
    }
}
