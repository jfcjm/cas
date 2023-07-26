package org.apereo.cas.oidc.authn;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.BaseOAuth20AuthenticatorTests;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.*;

/**
 * This is {@link OidcClientIdClientSecretAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDC")
@TestPropertySource(properties = {
    "cas.authn.oidc.core.user-defined-scopes.MyScope=uid",
    "cas.authn.oidc.discovery.scopes=openid,profile,email,MyScope",
    "cas.authn.oidc.discovery.claims=sub,name,family_name,given_name,uid"
})
class OidcClientIdClientSecretAuthenticatorTests extends AbstractOidcTests {

    @Autowired
    @Qualifier("oauthClientAuthenticator")
    private Authenticator authenticator;

    @Test
    void verifyWithoutRequestingScopes() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val credentials = new UsernamePasswordCredentials(registeredService.getClientId(), registeredService.getClientSecret());
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret());
        request.addParameter(OAuth20Constants.SCOPE, "openid");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals(1, credentials.getUserProfile().getAttributes().size());
        assertTrue(credentials.getUserProfile().getAttributes().containsKey(OAuth20Constants.CLIENT_ID));
    }

    @Test
    void verifyWithRequestingScopes() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setScopes(Set.of("openid", "MyScope"));
        servicesManager.save(registeredService);
        val credentials = new UsernamePasswordCredentials(registeredService.getClientId(), registeredService.getClientSecret());
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret());
        request.addParameter(OAuth20Constants.SCOPE, "openid MyScope");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, JEESessionStore.INSTANCE), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals(2, credentials.getUserProfile().getAttributes().size());
        assertTrue(credentials.getUserProfile().getAttributes().containsKey(OAuth20Constants.CLIENT_ID));
        assertTrue(credentials.getUserProfile().getAttributes().containsKey("uid"));
    }
}
