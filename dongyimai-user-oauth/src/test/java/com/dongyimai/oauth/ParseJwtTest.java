package com.dongyimai.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

public class ParseJwtTest {

    @Test
    public void parseToken(){
        // 令牌：
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoid28gc2hpIGRlbmcgbHUgcWlhbiBkZSBsaW5nIHBhaSEiLCJhdXRob3JpdGllcyI6WyJhZG1pbiJdfQ.bMGQYyqM3w-Wbp7SDmsUmhqMzngKGWDCBz7avLKma29YDUXqgKJlS3HZquoUCYH-T9p6NaXUltKFPcqaBK3WUdAk4ezYA_n_L80UF9MsHPM4lvU62DQtdQBRlthXD2gauNWHf7lZQnjT1y9n6oXyrw2OMJhFV7alge5CgcgK2VtbQ-bEYV5-QJvDi0Or7hT3F8_gf2S6YoJPdYA0pr8tE74D29X23b9TOCIjyd4dBsBhL88otoN7OXHEci89pU1z40byr5Y294cnG_5-wWM-4Zf7g6FZ0Ld5OugN2amnUI0fxHWLKFfPZtr4DFzlRMOlImfedbGGa2mGE6GhkQoCGQ";
        // 公钥
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmpPKDYbSzr8dX4EV945KsReK3XbkZ/Hh/fz+d1+sJM45sP/PijELzdC8kw/S4/fEqTC1nMQbEt+mic2H36+mzp6Ysrmt6z3Mim9pbPuatFkRN2rvCMY8Kuu5A+eba7Anw6PHER1b6DdsfgOvJln6QtZxMUbBvcayIQsmuOo6A021acKaEA04hMQ6mM+j5UWkdHi3fDOy+CYfbZ8HLrua4SrYxCw/IVAWNtG5HwkPgIvSOxGGLSlClLMZok8PICYjrfX0GtqUtoacvNI7NrQUe6lnEyfnT3Aw13Su5Ak1mIBq8MEr1WOh8oipapjQ2VY/A5wILs+3RCZttdSkNQOo3QIDAQAB-----END PUBLIC KEY-----";

        // 校验 令牌是否被串改
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));

        // 解析jwt
        String claims = jwt.getClaims();
        System.out.println(claims);


    }
}
