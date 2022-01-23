package com.example.withings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.LinkedHashMap;

@Component
public class OAuthClient {

    @Value("${withings.client-id}")
    private String clientId;

    @Value("${withings.client-secret}")
    private String clientSecret;

    public GetAccessTokenResponseDto auth(final String code) throws NoSuchAlgorithmException, InvalidKeyException {
        GetNonceResponseDtoWrapper nonceDto = getNonce();
        GetAccessTokenResponseDto accessToken = getAccessToken(code, nonceDto.signature, nonceDto.nonce);
        return accessToken;
    }

    private GetAccessTokenResponseDto getAccessToken(
            final String code,
            final String nonce,
            final String signature
    ) {
        WebClient client = WebClient.builder()
                .baseUrl("https://wbsapi.withings.net/v2/oauth2")
                .build();

        final String action = "requesttoken";
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("action", action);
        body.put("grant_type", "authorization_code");
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("code", code);
        body.put("redirect_uri", "http://localhost:8080/get_token");

        WebClient.RequestBodyUriSpec request = client.post();
        request.body(BodyInserters.fromValue(body));
        return request.retrieve().bodyToMono(GetAccessTokenResponseDto.class).block();
    }

    static class GetAccessTokenResponseDto {
        Integer status;
        Body body;

        public Integer getStatus() {
            return status;
        }

        public Body getBody() {
            return body;
        }

        private static class Body {
            String userid;
            String access_token;
            String refresh_token;
            Long expires_in;
            String scope;
            String csrf_token;
            String token_type;

            public String getUserid() {
                return userid;
            }

            public String getAccess_token() {
                return access_token;
            }

            public String getRefresh_token() {
                return refresh_token;
            }

            public Long getExpires_in() {
                return expires_in;
            }

            public String getScope() {
                return scope;
            }

            public String getCsrf_token() {
                return csrf_token;
            }

            public String getToken_type() {
                return token_type;
            }
        }
    }

    public GetNonceResponseDtoWrapper getNonce() throws NoSuchAlgorithmException, InvalidKeyException {
        WebClient client = WebClient.builder()
                .baseUrl("https://wbsapi.withings.net/v2/signature")
                .build();

        final String action = "getnonce";
        final String now = String.valueOf(Instant.now().getEpochSecond());

        WebClient.RequestBodyUriSpec requestBuilder = client.post();

        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("action", action);
        body.put("client_id", clientId);
        body.put("timestamp", now);
        final String signature = generateSignature(getJoin(body));
        body.put("signature", signature);

        requestBuilder.body(BodyInserters.fromValue(body));
        GetNonceResponseDto response = requestBuilder.retrieve().bodyToMono(GetNonceResponseDto.class).block();
        return new GetNonceResponseDtoWrapper(signature, response);
    }

    private class GetNonceResponseDtoWrapper {
        String signature;
        String nonce;

        public GetNonceResponseDtoWrapper(String signature, GetNonceResponseDto dto) {
            this.signature = signature;
            this.nonce = dto.body.nonce;
        }
    }

    static class GetNonceResponseDto {
        int status;
        Body body;

        public int getStatus() {
            return status;
        }

        public Body getBody() {
            return body;
        }

        private static class Body {
            String nonce;

            public String getNonce() {
                return nonce;
            }
        }
    }

    private String getJoin(LinkedHashMap<String, String> body) {
        StringBuilder sb = new StringBuilder();
        sb.append(body.get("action")).append(',');
        sb.append(body.get("client_id")).append(',');
        sb.append(body.get("timestamp"));
        return sb.toString();
    }

    private String generateSignature(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac hasher = Mac.getInstance("HmacSHA256");
        hasher.init(new SecretKeySpec("77c6aafbed60d209c9e39ab7cea8500bc5b7f43351c587afa5a6a3d8d0446830".getBytes(), "HmacSHA256"));
        return bytesToHex(hasher.doFinal(data.getBytes()));
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte h : hash) {
            String hex = Integer.toHexString(0xff & h);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
