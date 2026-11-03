package com.example.spring_security_demo.controller;

import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;

@RestController
public class JwksController {

    private final PublicKey publicKey;

    public JwksController(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {

        if (!(publicKey instanceof ECPublicKey ecPublicKey)) {
            throw new IllegalStateException(
                    "ES256 requires an EC public key"
            );
        }

        ECKey ecKey = new ECKey.Builder(
                Curve.P_256,
                ecPublicKey
        )
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.ES256)
                .keyID("spring-security-es256-v1")
                .build();

        return new JWKSet(
                ecKey.toPublicJWK()
        ).toJSONObject();
    }
}