package com.example.withings;

import org.springframework.stereotype.Service;


@Service
public class OAuthService {

    private final OAuthClient client;

    public OAuthService(OAuthClient client) {
        this.client = client;
    }

    public void getAuthenticationCode() {

    }


}
