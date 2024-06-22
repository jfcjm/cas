package org.apereo.cas.web.saml2;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.attributes.GroovyAttributeConverter;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.converter.AttributeConverter;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.metadata.DefaultSAML2MetadataSigner;
import org.pac4j.saml.store.HttpSessionStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedSaml2IdentityProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */

@Tag("SAML2Web")
public class DelegatedSaml2IdentityProviderTests {

    @SpringBootTest(
        classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class,
        properties = "cas.custom.properties.delegation-test.enabled=false")
    abstract static class BaseTests {
        @Autowired
        @Qualifier("pac4jDelegatedClientFactory")
        protected DelegatedIdentityProviderFactory delegatedIdentityProviderFactory;
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].message-store-factory=org.pac4j.saml.store.unknown",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    @Import(SamlMessageStoreTestConfiguration.class)
    class Saml2ClientsWithCustomMessageStore extends BaseTests {
        @Test
        void verifyClient() throws Throwable {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());
            val client = (SAML2Client) clients.iterator().next();
            assertNotNull(client.getConfiguration().getSamlMessageStoreFactory());
            assertInstanceOf(DefaultSAML2MetadataSigner.class, client.getConfiguration().getMetadataSigner());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].message-store-factory=org.pac4j.saml.store.unknown",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class Saml2ClientsWithUnknownMessageStore extends BaseTests {
        @Test
        void verifyClient() throws Throwable {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].saml2-attribute-converter=classpath:/SAMLAttributeConverter.groovy",
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].metadata-signer-strategy=xmlsec",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class Saml2ClientsWithGroovyAttributeConverter extends BaseTests {
        @Test
        void verifyClient() throws Throwable {
            val saml2clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, saml2clients.size());
            val client = (SAML2Client) saml2clients.stream().findFirst().orElseThrow();
            assertInstanceOf(GroovyAttributeConverter.class, client.getConfiguration().getSamlAttributeConverter());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].saml2-attribute-converter=org.apereo.cas.web.saml2.DelegatedSaml2IdentityProviderTests.CustomAttributeConverterForTest",
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].metadata-signer-strategy=xmlsec",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class Saml2ClientsWithCustomAttributeConverter extends BaseTests {
        @Test
        void verifyClient() throws Throwable {

            val saml2clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, saml2clients.size());

            val client = (SAML2Client) saml2clients.stream().findFirst().get();
            assertInstanceOf(CustomAttributeConverterForTest.class, client.getConfiguration().getSamlAttributeConverter());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].callback-url-type=NONE",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].message-store-factory=org.pac4j.saml.store.HttpSessionStoreFactory",
        "cas.authn.pac4j.saml[0].name-id-policy-format=transient",
        "cas.authn.pac4j.saml[0].mapped-attributes[0]=attr1->givenName",
        "cas.authn.pac4j.saml[0].requested-attributes[0].name=requestedAttribute",
        "cas.authn.pac4j.saml[0].requested-attributes[0].friendly-name=friendlyRequestedName",
        "cas.authn.pac4j.saml[0].blocked-signature-signing-algorithms[0]=sha-1",
        "cas.authn.pac4j.saml[0].signature-algorithms[0]=sha-256",
        "cas.authn.pac4j.saml[0].signature-reference-digest-methods[0]=sha-256",
        "cas.authn.pac4j.saml[0].authn-context-class-ref[0]=classRef1",
        "cas.authn.pac4j.saml[0].assertion-consumer-service-index=1",
        "cas.authn.pac4j.saml[0].principal-id-attribute=givenName",
        "cas.authn.pac4j.saml[0].force-keystore-generation=true",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class Saml2Clients extends BaseTests {
        @Test
        void verifyClient() throws Throwable {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());
            val client = (SAML2Client) clients.iterator().next();
            assertInstanceOf(HttpSessionStoreFactory.class, client.getConfiguration().getSamlMessageStoreFactory());
        }
    }

    public static class CustomAttributeConverterForTest implements AttributeConverter {
        @Override
        public Object convert(final Object o) {
            return null;
        }
    }

    @TestConfiguration(value = "SamlMessageStoreTestConfiguration", proxyBeanMethods = false)
    static class SamlMessageStoreTestConfiguration {
        @Bean
        public SAMLMessageStoreFactory delegatedSaml2ClientSAMLMessageStoreFactory() {
            return mock(SAMLMessageStoreFactory.class);
        }
    }
}
