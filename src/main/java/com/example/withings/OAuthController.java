package com.example.withings;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
public class OAuthController {

    private final OAuthClient client;

    public OAuthController(OAuthClient client) {
        this.client = client;
    }

    @GetMapping("/get_token")
    public OAuthClient.GetJwtTokenResponseDto a(
            @RequestParam String code,
            @RequestParam String state
    ) throws NoSuchAlgorithmException, InvalidKeyException {
        OAuthClient.GetJwtTokenResponseDto a = client.auth(code);
        OAuthClient.GetJwtTokenResponseDto b = client.refresh(a.body.refresh_token);
        return b;
    }


}
