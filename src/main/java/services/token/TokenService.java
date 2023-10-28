package services.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Clase que implementa la interfaz de token service
 */
public class TokenService {
    // Atributos
    private static TokenService instance;
    private final Logger logger = LoggerFactory.getLogger(TokenService.class);

    /**
     * Constructor de la clase
     */
    private TokenService() {
    }

    /**
     * Singleton de la clase
     * @return una instancia de la clase TokenService
     */
    public synchronized static TokenService getInstance(){
        if (instance == null) {
            instance = new TokenService();
        }
        return instance;
    }

    /**
     * Método que crea un token
     * @param user Usuario
     * @param tokenSecret Secreto del token
     * @param tokenExpiration Tiempo de expiración del token
     * @return String de token
     */
    public String createToken(User user, String tokenSecret, long tokenExpiration){
        logger.debug("Creando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        return JWT.create()
                .withClaim("userid", user.id())
                .withClaim("username", user.username())
                .withClaim("rol", user.role().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpiration))
                .sign(algorithm);
    }

    /**
     * Método que verifica un token
     * @param token Token
     * @param tokenSecret Secreto del token
     * @param user Usuario
     * @return boolean de verificación
     */
    public boolean verifyToken(String token,String tokenSecret, User user) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try{
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            return decodedJWT.getClaim("userid").asLong() == user.id() &&
                    decodedJWT.getClaim("username").asString().equals(user.username()) &&
                    decodedJWT.getClaim("rol").asString().equals(user.role().toString());
        } catch (Exception e) {
           logger.debug("Error al verificar el token: " + e.getMessage());
              return false;
        }
    }

    /**
     * Método que verifica un token
     * @param token Token
     * @param tokenSecret Secreto del token
     * @return boolean de verificación
     */
    public boolean verifyToken(String token, String tokenSecret) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Método que devuelve los claims de un token
     * @param token Token
     * @param tokenSecret Secreto del token
     * @return Map de claims
     */
    public java.util.Map<String, com.auth0.jwt.interfaces.Claim> getClaims(String token, String tokenSecret) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            return decodedJWT.getClaims();
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return null;
        }
    }
}
