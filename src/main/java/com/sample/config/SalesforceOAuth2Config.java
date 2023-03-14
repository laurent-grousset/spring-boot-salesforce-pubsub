package com.sample.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class SalesforceOAuth2Config {
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                 OAuth2AuthorizedClientService authorizedClientService,
                                                                 OAuth2AuthorizationFailureHandler failureHandler) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizationFailureHandler(failureHandler);
        return authorizedClientManager;
    }

    @Bean
    public OAuth2AuthorizationFailureHandler authorizationFailureHandler(OAuth2AuthorizedClientService authorizedClientService) {
        return new RemoveAuthorizedClientOAuth2AuthorizationFailureHandler((clientRegistrationId, principal, attributes) -> authorizedClientService.removeAuthorizedClient(clientRegistrationId, principal.getName()));
    }

    @Bean
    public OAuth2AuthorizedClient salesforceAuthorizedClient(@Value("${spring.security.oauth2.client.registration.salesforce.provider}") String salesforceRegistrationId,
                                                             OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(salesforceRegistrationId)
                .principal(salesforceRegistrationId).build();
        return authorizedClientManager.authorize(authorizeRequest);
    }
}
