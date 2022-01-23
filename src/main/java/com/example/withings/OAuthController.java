package com.example.withings;

import org.springframework.stereotype.Controller;
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
    public OAuthClient.GetAccessTokenResponseDto a(
            @RequestParam String code,
            @RequestParam String state
    ) throws NoSuchAlgorithmException, InvalidKeyException {
        OAuthClient.GetAccessTokenResponseDto a = client.auth(code);
//        OAuthClient.ASD a = client.getNonce().bodyToMono(OAuthClient.ASD.class).block();
        return a;
    }

}
