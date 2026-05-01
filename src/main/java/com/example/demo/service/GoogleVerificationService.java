package com.example.demo.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.example.demo.dto.GoogleUserInfo;

import java.util.Collections;

import org.springframework.stereotype.Service;

@Service
public class GoogleVerificationService {
    private static final String GOOGLE_CLIENT_ID = "301039942357-glghj754l5dval84g1d3kd5psv4tm4s8.apps.googleusercontent.com";

    private final NetHttpTransport transport = new NetHttpTransport();
    private final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public GoogleUserInfo verify(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                return null;
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String sub = payload.getSubject();
            String picture = (String) payload.get("picture");

            picture = normalizeGooglePhotoSize(picture, 512);

            return new GoogleUserInfo(email, name, sub, picture);
        } catch (Exception e) {
            return null;
        }
    }

    public String getPictureFromIdToken(String idToken) {
        var info = verify(idToken);
        if (info == null)
            return null;

        return info.getPicture();
    }

    private String normalizeGooglePhotoSize(String url, int size) {
        if (url == null)
            return null;
        return url.replace("=s\\d+-c$", "=s" + size + "-c");
    }
}