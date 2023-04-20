package Ontdekstation013.ClimateChecker.Services;

import Ontdekstation013.ClimateChecker.models.JwsData;
import Ontdekstation013.ClimateChecker.models.dto.JwsDTO;
import Ontdekstation013.ClimateChecker.models.dto.userDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;

/**
 * This {@code Service Layer} is responsible for the JWS tokens. For more information look in the following documentation.
 * <a href="https://github.com/jwtk/jjwt">JsonWebToken Documentation</a>
 */
@Service
public class JWTService {

    Calendar calendar;
    /**
     * {@code SecretKey} is responsible for creating & verifying the JSON Web token.
     *
     * @implNote Front end can decrypt the data without any key, however if they change data. This key can verify its intergrity.
     * At the current state, {@code secretKey} will be (re)generated each time the program is restarted.
     * @see #generateJWS(userDto)
     * @see <a href="https://github.com/jwtk/jjwt#jws-key-create-secret">Creating an secret key</a>
     */
    SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final Claims claims = Jwts.claims();
    private static final String ISSUER = "Ontdekstation013";

    /**
     * @param user The DTO Containing the required data for generating an JWS.
     * @return An JWS Token in String Format signed with an HS256 Algorithm.
     * @implNote At this moment HS256 will be used. This key is a symmetric key.
     * It is possible to upgrade to RS256 later, this will result into getting 2 keys for encryption: Private / Public
     * However, for the current state of the project, this is not a requirement.
     * @see #secretKey
     * @see <a href="https://github.com/jwtk/jjwt#jws-create-key">Signing an JWS</a>
     */
    public userDto generateJWS(@NotNull userDto user) {
        calendar = Calendar.getInstance();
        claims.setIssuer(ISSUER);
        claims.setSubject(String.valueOf(user.getId()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 7);
        Date date = calendar.getTime();
        claims.setExpiration(date);
        user.setJwsString(Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setClaims(claims)
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .signWith(secretKey)
                .compact());
        return user;
    }

    /**
     * @param jwsDTO DTO Containing the String
     * @return JwsData Object with all Data of the token.
     * @throws JwtException Exception is thrown whenever the given data is incorrect. (E.G. : Expired token, Invalid Signature)
     * @see <a href="https://github.com/jwtk/jjwt#jws-read">Reading the JWS</a>
     * @see #fillJWS(Jws)
     */
    public JwsData verifyJWS(JwsDTO jwsDTO) throws JwtException {
        Jws<Claims> claims;
        claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwsDTO.getJwsString());


        return fillJWS(claims);
    }

    private JwsData fillJWS(Jws<Claims> claims) {
        JwsData jwsData = new JwsData();
        jwsData.setExp(claims.getBody().getExpiration());
        jwsData.setFirstName(claims.getBody().get("firstName", String.class));
        jwsData.setLastName(claims.getBody().get("lastName", String.class));
        jwsData.setIss(claims.getBody().getIssuer());
        jwsData.setId(claims.getBody().getSubject());

        return jwsData;
    }
}
