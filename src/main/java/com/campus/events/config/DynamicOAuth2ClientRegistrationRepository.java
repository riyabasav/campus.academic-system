package com.campus.events.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicOAuth2ClientRegistrationRepository implements ClientRegistrationRepository {

    private final Map<String, ClientRegistration> registrations = new ConcurrentHashMap<>();

    public DynamicOAuth2ClientRegistrationRepository(
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String clientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String clientSecret) {

        if (clientId != null && !clientId.trim().isEmpty() && !clientId.contains("YOUR_GOOGLE_CLIENT_ID")) {
            updateGoogleCredentials(clientId, clientSecret);
        }
    }

    public synchronized void updateGoogleCredentials(String clientId, String clientSecret) {
        if (clientId == null || clientId.trim().isEmpty()) {
            return;
        }

        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId(clientId.trim())
                .clientSecret(clientSecret != null ? clientSecret.trim() : "")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();

        registrations.put("google", registration);
    }

    public boolean isGoogleConfigured() {
        ClientRegistration google = registrations.get("google");
        return google != null && !google.getClientId().contains("YOUR_GOOGLE_CLIENT_ID");
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return registrations.get(registrationId);
    }
}
