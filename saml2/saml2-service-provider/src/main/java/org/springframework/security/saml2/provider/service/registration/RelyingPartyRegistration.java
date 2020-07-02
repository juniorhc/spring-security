/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.saml2.provider.service.registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.security.saml2.credentials.Saml2X509Credential;
import org.springframework.security.saml2.credentials.Saml2X509Credential.Saml2X509CredentialType;
import org.springframework.util.Assert;

/**
 * Represents a configured relying party (aka Service Provider) and asserting party (aka Identity Provider) pair.
 *
 * <p>
 * Each RP/AP pair is uniquely identified using a {@code registrationId}, an arbitrary string.
 *
 * <p>
 * A fully configured registration may look like:
 *
 * <pre>
 *	String registrationId = "simplesamlphp";
 *
 * 	String relyingPartyEntityId = "{baseUrl}/saml2/service-provider-metadata/{registrationId}";
 *	String assertionConsumerServiceLocation = "{baseUrl}/login/saml2/sso/{registrationId}";
 *	Saml2X509Credential relyingPartySigningCredential = ...;
 *
 *	String assertingPartyEntityId = "https://simplesaml-for-spring-saml.cfapps.io/saml2/idp/metadata.php";
 *	String singleSignOnServiceLocation = "https://simplesaml-for-spring-saml.cfapps.io/saml2/idp/SSOService.php";
 * 	Saml2X509Credential assertingPartyVerificationCredential = ...;
 *
 *
 *	RelyingPartyRegistration rp = RelyingPartyRegistration.withRegistrationId(registrationId)
 * 			.entityId(relyingPartyEntityId)
 * 		 	.signingX509Credentials(c -> c.add(relyingPartySigningCredential))
 * 			.assertionConsumerServiceLocation(assertingConsumerServiceLocation)
 * 			.providerDetails(details -> details
 * 				.entityId(assertingPartyEntityId));
 * 				.verificationX509Credentials(c -> c.add(assertingPartyVerificationCredential))
 * 				.singleSignOnServiceLocation(singleSignOnServiceLocation)
 * 			)
 * 			.build();
 * </pre>
 *
 * @since 5.2
 * @author Filip Hanik
 * @author Josh Cummings
 */
public class RelyingPartyRegistration {

	private final String registrationId;
	private final String assertionConsumerServiceLocation;
	private final List<Saml2X509Credential> credentials;
	private final Collection<Saml2X509Credential> signingX509Credentials;
	private final Collection<Saml2X509Credential> decryptionX509Credentials;
	private final String entityId;
	private final ProviderDetails providerDetails;

	private RelyingPartyRegistration(
			String registrationId,
			String entityId,
			Collection<Saml2X509Credential> credentials,
			Collection<Saml2X509Credential> signingX509Credentials,
			Collection<Saml2X509Credential> decryptionX509Credentials,
			String assertionConsumerServiceLocation,
			ProviderDetails providerDetails) {

		Assert.hasText(registrationId, "registrationId cannot be empty");
		Assert.hasText(entityId, "entityId cannot be empty");
		Assert.hasText(assertionConsumerServiceLocation, "assertionConsumerServiceLocation cannot be empty");
		Assert.notEmpty(credentials, "credentials cannot be empty");
		for (Saml2X509Credential credential : credentials) {
			Assert.notNull(credential, "credentials cannot have null values");
		}
		for (Saml2X509Credential credential : signingX509Credentials) {
			Assert.notNull(credential, "signingX509Credentials cannot have null values");
			Assert.isTrue(credential.isSigningCredential(),
					"All signingX509Credentials must have a usage of SIGNING set");
		}
		for (Saml2X509Credential credential : decryptionX509Credentials) {
			Assert.notNull(credential, "decryptionX509Credentials cannot have null values");
			Assert.isTrue(credential.isDecryptionCredential(),
					"All decryptionX509Credentials must have a usage of DECRYPTION set");
		}
		Assert.notNull(providerDetails, "providerDetails cannot be null");
		Assert.hasText(providerDetails.singleSignOnServiceLocation, "providerDetails.webSsoUrl cannot be empty");
		this.registrationId = registrationId;
		this.entityId = entityId;
		this.credentials = Collections.unmodifiableList(new ArrayList<>(credentials));
		this.signingX509Credentials = Collections.unmodifiableCollection(signingX509Credentials);
		this.decryptionX509Credentials = Collections.unmodifiableCollection(decryptionX509Credentials);
		this.assertionConsumerServiceLocation = assertionConsumerServiceLocation;
		this.providerDetails = providerDetails;
	}

	/**
	 * Get the unique registration id for this RP/AP pair
	 *
	 * @return the unique registration id for this RP/AP pair
	 */
	public String getRegistrationId() {
		return this.registrationId;
	}

	/**
	 * Get the relying party's
	 * <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/EntityNaming">EntityID</a>.
	 *
	 * <p>
	 * Equivalent to the value found in the relying party's
	 * &lt;EntityDescriptor EntityID="..."/&gt;
	 *
	 * <p>
	 * This value may contain a number of placeholders, which need to be
	 * resolved before use. They are {@code baseUrl}, {@code registrationId},
	 * {@code baseScheme}, {@code baseHost}, and {@code basePort}.
	 *
	 * @return the relying party's EntityID
	 * @since 5.4
	 */
	public String getEntityId() {
		return this.entityId;
	}

	/**
	 * Get the {@link Collection} of signing {@link Saml2X509Credential}s associated with this relying party
	 *
	 * @return the {@link Collection} of signing {@link Saml2X509Credential}s associated with this relying party
	 * @since 5.4
	 */
	public Collection<Saml2X509Credential> getSigningX509Credentials() {
		return this.signingX509Credentials;
	}

	/**
	 * Get the {@link Collection} of decryption {@link Saml2X509Credential}s associated with this relying party
	 *
	 * @return the {@link Collection} of decryption {@link Saml2X509Credential}s associated with this relying party
	 * @since 5.4
	 */
	public Collection<Saml2X509Credential> getDecryptionX509Credentials() {
		return this.decryptionX509Credentials;
	}

	/**
	 * Get the AssertionConsumerService Location.
	 * Equivalent to the value found in &lt;AssertionConsumerService Location="..."/&gt;
	 * in the relying party's &lt;SPSSODescriptor&gt;.
	 *
	 * This value may contain a number of placeholders, which need to be
	 * resolved before use. They are {@code baseUrl}, {@code registrationId},
	 * {@code baseScheme}, {@code baseHost}, and {@code basePort}.
	 *
	 * @return the AssertionConsumerService Location
	 * @since 5.4
	 */
	public String getAssertionConsumerServiceLocation() {
		return this.assertionConsumerServiceLocation;
	}

	/**
	 * Returns the entity ID of the IDP, the asserting party.
	 * @return entity ID of the asserting party
	 * @deprecated use {@link ProviderDetails#getEntityId()} from {@link #getProviderDetails()}
	 */
	@Deprecated
	public String getRemoteIdpEntityId() {
		return this.providerDetails.getEntityId();
	}

	/**
	 * returns the URL template for which ACS URL authentication requests should contain
	 * Possible variables are {@code baseUrl}, {@code registrationId},
	 * {@code baseScheme}, {@code baseHost}, and {@code basePort}.
	 * @return string containing the ACS URL template, with or without variables present
	 * @deprecated Use {@link #getAssertionConsumerServiceLocation()} instead
	 */
	@Deprecated
	public String getAssertionConsumerServiceUrlTemplate() {
		return this.assertionConsumerServiceLocation;
	}

	/**
	 * Contains the URL for which to send the SAML 2 Authentication Request to initiate
	 * a single sign on flow.
	 * @return a IDP URL that accepts REDIRECT or POST binding for authentication requests
	 * @deprecated use {@link ProviderDetails#getWebSsoUrl()} from {@link #getProviderDetails()}
	 */
	@Deprecated
	public String getIdpWebSsoUrl() {
		return this.getProviderDetails().getSingleSignOnServiceLocation();
	}

	/**
	 * Returns specific configuration around the Identity Provider SSO endpoint
	 * @return the IDP SSO endpoint configuration
	 * @since 5.3
	 */
	public ProviderDetails getProviderDetails() {
		return this.providerDetails;
	}

	/**
	 * The local relying party, or Service Provider, can generate it's entity ID based on
	 * possible variables of {@code baseUrl}, {@code registrationId},
	 * {@code baseScheme}, {@code baseHost}, and {@code basePort}, for example
	 * {@code {baseUrl}/saml2/service-provider-metadata/{registrationId}}
	 * @return a string containing the entity ID or entity ID template
	 * @deprecated Use {@link #getEntityId()} instead
	 */
	@Deprecated
	public String getLocalEntityIdTemplate() {
		return this.entityId;
	}

	/**
	 * Returns a list of configured credentials to be used in message exchanges between relying party, SP, and
	 * asserting party, IDP.
	 * @return a list of credentials
	 * @deprecated Use {@link #getSigningX509Credentials}, {@link #getDecryptionX509Credentials()},
	 * {@link ProviderDetails#getVerificationX509Credentials()}, or {@link ProviderDetails#getEncryptionX509Credentials()}
	 * instead
	 */
	@Deprecated
	public List<Saml2X509Credential> getCredentials() {
		return this.credentials;
	}

	/**
	 * @return a filtered list containing only credentials of type
	 * {@link Saml2X509CredentialType#VERIFICATION}.
	 * Returns an empty list of credentials are not found
	 * @deprecated Use {@link ProviderDetails#getVerificationX509Credentials} instead
	 */
	@Deprecated
	public List<Saml2X509Credential> getVerificationCredentials() {
		return new ArrayList<>(this.providerDetails.getVerificationX509Credentials());
	}

	/**
	 * @return a filtered list containing only credentials of type
	 * {@link Saml2X509CredentialType#SIGNING}.
	 * Returns an empty list of credentials are not found
	 * @deprecated Use {@link #getSigningX509Credentials} instead
	 */
	@Deprecated
	public List<Saml2X509Credential> getSigningCredentials() {
		return new ArrayList<>(this.signingX509Credentials);
	}

	/**
	 * @return a filtered list containing only credentials of type
	 * {@link Saml2X509CredentialType#ENCRYPTION}.
	 * Returns an empty list of credentials are not found
	 * @deprecated Use {@link ProviderDetails#getEncryptionX509Credentials} instead
	 */
	@Deprecated
	public List<Saml2X509Credential> getEncryptionCredentials() {
		return new ArrayList<>(this.providerDetails.getEncryptionX509Credentials());
	}

	/**
	 * @return a filtered list containing only credentials of type
	 * {@link Saml2X509CredentialType#DECRYPTION}.
	 * Returns an empty list of credentials are not found
	 * @deprecated Use {@link #getDecryptionX509Credentials} instead
	 */
	@Deprecated
	public List<Saml2X509Credential> getDecryptionCredentials() {
		return new ArrayList<>(this.decryptionX509Credentials);
	}

	/**
	 * Creates a {@code RelyingPartyRegistration} {@link Builder} with a known {@code registrationId}
	 * @param registrationId a string identifier for the {@code RelyingPartyRegistration}
	 * @return {@code Builder} to create a {@code RelyingPartyRegistration} object
	 */
	public static Builder withRegistrationId(String registrationId) {
		Assert.hasText(registrationId, "registrationId cannot be empty");
		return new Builder(registrationId);
	}

	/**
	 * Creates a {@code RelyingPartyRegistration} {@link Builder} based on an existing object
	 * @param registration the {@code RelyingPartyRegistration}
	 * @return {@code Builder} to create a {@code RelyingPartyRegistration} object
	 */
	public static Builder withRelyingPartyRegistration(RelyingPartyRegistration registration) {
		Assert.notNull(registration, "registration cannot be null");
		ProviderDetails details = registration.getProviderDetails();
		return withRegistrationId(registration.getRegistrationId())
				.entityId(registration.getEntityId())
				.signingX509Credentials(c -> c.addAll(registration.getSigningX509Credentials()))
				.decryptionX509Credentials(c -> c.addAll(registration.getDecryptionX509Credentials()))
				.assertionConsumerServiceLocation(registration.getAssertionConsumerServiceUrlTemplate())
				.providerDetails(d -> d
					.entityId(details.getEntityId())
					.wantsAuthnRequestsSigned(details.getWantsAuthnRequestsSigned())
					.verificationX509Credentials(c -> c.addAll(details.getVerificationX509Credentials()))
					.encryptionX509Credentials(c -> c.addAll(details.getEncryptionX509Credentials()))
					.singleSignOnServiceLocation(registration.getProviderDetails().getSingleSignOnServiceLocation())
					.singleSignOnServiceBinding(registration.getProviderDetails().getSingleSignOnServiceBinding())
				);
	}

	/**
	 * Configuration for the asserting party.
	 *
	 * Intended to represent the asserting party's &lt;IDPSSODescriptor&gt;.
	 *
	 * @since 5.3
	 */
	public final static class ProviderDetails {
		private final String entityId;
		private final boolean signAuthNRequest;
		private final Collection<Saml2X509Credential> verificationX509Credentials;
		private final Collection<Saml2X509Credential> encryptionX509Credentials;
		private final String singleSignOnServiceLocation;
		private final Saml2MessageBinding singleSignOnServiceBinding;

		private ProviderDetails(
				String entityId,
				boolean wantsAuthnRequestsSigned,
				Collection<Saml2X509Credential> verificationX509Credentials,
				Collection<Saml2X509Credential> encryptionX509Credentials,
				String singleSignOnServiceLocation,
				Saml2MessageBinding singleSignOnServiceBinding) {

			Assert.hasText(entityId, "entityId cannot be null or empty");
			for (Saml2X509Credential credential : verificationX509Credentials) {
				Assert.notNull(credential, "verificationX509Credentials cannot have null values");
				Assert.isTrue(credential.isSignatureVerficationCredential(),
						"All verificationX509Credentials must have a usage of VERIFICATION set");
			}
			for (Saml2X509Credential credential : encryptionX509Credentials) {
				Assert.notNull(credential, "encryptionX509Credentials cannot have null values");
				Assert.isTrue(credential.isEncryptionCredential(),
						"All encryptionX509Credentials must have a usage of ENCRYPTION set");
			}
			Assert.notNull(singleSignOnServiceLocation, "singleSignOnServiceLocation cannot be null");
			Assert.notNull(singleSignOnServiceBinding, "singleSignOnServiceBinding cannot be null");
			this.entityId = entityId;
			this.verificationX509Credentials = verificationX509Credentials;
			this.encryptionX509Credentials = encryptionX509Credentials;
			this.signAuthNRequest = wantsAuthnRequestsSigned;
			this.singleSignOnServiceLocation = singleSignOnServiceLocation;
			this.singleSignOnServiceBinding = singleSignOnServiceBinding;
		}

		/**
		 * Get the asserting party's
		 * <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/EntityNaming">EntityID</a>.
		 *
		 * <p>
		 * Equivalent to the value found in the asserting party's
		 * &lt;EntityDescriptor EntityID="..."/&gt;
		 *
		 * <p>
		 * This value may contain a number of placeholders, which need to be
		 * resolved before use. They are {@code baseUrl}, {@code registrationId},
		 * {@code baseScheme}, {@code baseHost}, and {@code basePort}.
		 *
		 * @return the relying party's EntityID
		 * @since 5.4
		 */
		public String getEntityId() {
			return this.entityId;
		}

		/**
		 * Get the WantsAuthnRequestsSigned setting, indicating the asserting party's preference that
		 * relying parties should sign the AuthnRequest before sending.
		 *
		 * @return the WantsAuthnRequestsSigned value
		 * @since 5.4
		 */
		public boolean getWantsAuthnRequestsSigned() {
			return this.signAuthNRequest;
		}

		/**
		 * Get all verification {@link Saml2X509Credential}s associated with this asserting party
		 *
		 * @return all verification {@link Saml2X509Credential}s associated with this asserting party
		 * @since 5.4
		 */
		public Collection<Saml2X509Credential> getVerificationX509Credentials() {
			return this.verificationX509Credentials;
		}

		/**
		 * Get all encryption {@link Saml2X509Credential}s associated with this asserting party
		 *
		 * @return all encryption {@link Saml2X509Credential}s associated with this asserting party
		 * @since 5.4
		 */
		public Collection<Saml2X509Credential> getEncryptionX509Credentials() {
			return this.encryptionX509Credentials;
		}

		/**
		 * Get the
		 * <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/MetadataForIdP#MetadataForIdP-SingleSign-OnServices">SingleSignOnService</a>
		 * Location.
		 *
		 * <p>
		 * Equivalent to the value found in &lt;SingleSignOnService Location="..."/&gt;
		 * in the asserting party's &lt;IDPSSODescriptor&gt;.
		 *
		 * @return the SingleSignOnService Location
		 * @since 5.4
		 */
		public String getSingleSignOnServiceLocation() {
			return this.singleSignOnServiceLocation;
		}

		/**
		 * Get the
		 * <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/MetadataForIdP#MetadataForIdP-SingleSign-OnServices">SingleSignOnService</a>
		 * Binding.
		 *
		 * <p>
		 * Equivalent to the value found in &lt;SingleSignOnService Binding="..."/&gt;
		 * in the asserting party's &lt;IDPSSODescriptor&gt;.
		 *
		 * @return the SingleSignOnService Location
		 * @since 5.4
		 */
		public Saml2MessageBinding getSingleSignOnServiceBinding() {
			return this.singleSignOnServiceBinding;
		}

		/**
		 * Contains the URL for which to send the SAML 2 Authentication Request to initiate
		 * a single sign on flow.
		 * @return a IDP URL that accepts REDIRECT or POST binding for authentication requests
		 * @deprecated Use {@link #getSingleSignOnServiceLocation()} instead
		 */
		@Deprecated
		public String getWebSsoUrl() {
			return this.singleSignOnServiceLocation;
		}

		/**
		 * @return {@code true} if AuthNRequests from this relying party to the IDP should be signed
		 * {@code false} if no signature is required.
		 * @deprecated Use {@link #getWantsAuthnRequestsSigned()} instead
		 */
		@Deprecated
		public boolean isSignAuthNRequest() {
			return this.signAuthNRequest;
		}

		/**
		 * @return the type of SAML 2 Binding the AuthNRequest should be sent on
		 * @deprecated Use {@link #getSingleSignOnServiceBinding()} instead
		 */
		@Deprecated
		public Saml2MessageBinding getBinding() {
			return this.singleSignOnServiceBinding;
		}

		/**
		 * A builder for the asserting party.
		 *
		 * Intended to represent the asserting party's &lt;EntityDescriptor&gt;
		 *
		 * @since 5.3
		 */
		public final static class Builder {
			private String entityId;
			private boolean wantsAuthnRequestsSigned = true;
			private Collection<Saml2X509Credential> verificationX509Credentials = new HashSet<>();
			private Collection<Saml2X509Credential> encryptionX509Credentials = new HashSet<>();
			private String singleSignOnServiceLocation;
			private Saml2MessageBinding singleSignOnServiceBinding = Saml2MessageBinding.REDIRECT;

			/**
			 * Set the asserting party's
			 * <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/EntityNaming">EntityID</a>.
			 * Equivalent to the value found in the asserting party's
			 * &lt;EntityDescriptor EntityID="..."/&gt;
			 *
			 * @param entityId the asserting party's EntityID
			 * @return the {@link Builder} for further configuration
			 * @since 5.4
			 */
			public Builder entityId(String entityId) {
				this.entityId = entityId;
				return this;
			}

			/**
			 * Set the WantsAuthnRequestsSigned setting, indicating the asserting party's preference that
			 * relying parties should sign the AuthnRequest before sending.
			 *
			 * @param wantsAuthnRequestsSigned the WantsAuthnRequestsSigned setting
			 * @return the {@link Builder} for further configuration
			 * @since 5.4
			 */
			public Builder wantsAuthnRequestsSigned(boolean wantsAuthnRequestsSigned) {
				this.wantsAuthnRequestsSigned = wantsAuthnRequestsSigned;
				return this;
			}

			/**
			 * Apply this {@link Consumer} to the list of {@link Saml2X509Credential}s
			 *
			 * @param credentialsConsumer a {@link Consumer} of the {@link List} of {@link Saml2X509Credential}s
			 * @return the {@link RelyingPartyRegistration.Builder} for further configuration
			 * @since 5.4
			 */
			public Builder verificationX509Credentials(Consumer<Collection<Saml2X509Credential>> credentialsConsumer) {
				credentialsConsumer.accept(this.verificationX509Credentials);
				return this;
			}

			/**
			 * Apply this {@link Consumer} to the list of {@link Saml2X509Credential}s
			 *
			 * @param credentialsConsumer a {@link Consumer} of the {@link List} of {@link Saml2X509Credential}s
			 * @return the {@link RelyingPartyRegistration.Builder} for further configuration
			 * @since 5.4
			 */
			public Builder encryptionX509Credentials(Consumer<Collection<Saml2X509Credential>> credentialsConsumer) {
				credentialsConsumer.accept(this.encryptionX509Credentials);
				return this;
			}

			/**
			 * Set the
			 * <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/MetadataForIdP#MetadataForIdP-SingleSign-OnServices">SingleSignOnService</a>
			 * Location.
			 *
			 * <p>
			 * Equivalent to the value found in &lt;SingleSignOnService Location="..."/&gt;
			 * in the asserting party's &lt;IDPSSODescriptor&gt;.
			 *
			 * @param singleSignOnServiceLocation the SingleSignOnService Location
			 * @return the {@link Builder} for further configuration
			 * @since 5.4
			 */
			public Builder singleSignOnServiceLocation(String singleSignOnServiceLocation) {
				this.singleSignOnServiceLocation = singleSignOnServiceLocation;
				return this;
			}

			/**
			 * Set the
			 * <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/MetadataForIdP#MetadataForIdP-SingleSign-OnServices">SingleSignOnService</a>
			 * Binding.
			 *
			 * <p>
			 * Equivalent to the value found in &lt;SingleSignOnService Binding="..."/&gt;
			 * in the asserting party's &lt;IDPSSODescriptor&gt;.
			 *
			 * @param singleSignOnServiceBinding the SingleSignOnService Binding
			 * @return the {@link Builder} for further configuration
			 * @since 5.4
			 */
			public Builder singleSignOnServiceBinding(Saml2MessageBinding singleSignOnServiceBinding) {
				this.singleSignOnServiceBinding = singleSignOnServiceBinding;
				return this;
			}

			/**
			 * Sets the {@code SSO URL} for the remote asserting party, the Identity Provider.
			 *
			 * @param url - a URL that accepts authentication requests via REDIRECT or POST bindings
			 * @return this object
			 * @deprecated Use {@link #singleSignOnServiceLocation} instead
			 */
			@Deprecated
			public Builder webSsoUrl(String url) {
				this.singleSignOnServiceLocation = url;
				return this;
			}

			/**
			 * Set to true if the AuthNRequest message should be signed
			 *
			 * @param signAuthNRequest true if the message should be signed
			 * @return this object
			 * @deprecated Use {@link #wantsAuthnRequestsSigned} instead
			 */
			@Deprecated
			public Builder signAuthNRequest(boolean signAuthNRequest) {
				this.wantsAuthnRequestsSigned = signAuthNRequest;
				return this;
			}


			/**
			 * Sets the message binding to be used when sending an AuthNRequest message
			 *
			 * @param binding either {@link Saml2MessageBinding#POST} or {@link Saml2MessageBinding#REDIRECT}
			 * @return this object
			 * @deprecated Use {@link #singleSignOnServiceBinding} instead
			 */
			@Deprecated
			public Builder binding(Saml2MessageBinding binding) {
				this.singleSignOnServiceBinding = binding;
				return this;
			}

			/**
			 * Creates an immutable ProviderDetails object representing the configuration for an Identity Provider, IDP
			 * @return immutable ProviderDetails object
			 */
			public ProviderDetails build() {
				return new ProviderDetails(
						this.entityId,
						this.wantsAuthnRequestsSigned,
						this.verificationX509Credentials,
						this.encryptionX509Credentials,
						this.singleSignOnServiceLocation,
						this.singleSignOnServiceBinding
				);
			}
		}
	}

	public final static class Builder {
		private String registrationId;
		private String entityId = "{baseUrl}/saml2/service-provider-metadata/{registrationId}";
		private String assertionConsumerServiceLocation;
		private ProviderDetails.Builder providerDetails = new ProviderDetails.Builder();
		private Collection<Saml2X509Credential> credentials = new HashSet<>();
		private Collection<Saml2X509Credential> signingX509Credentials = new HashSet<>();
		private Collection<Saml2X509Credential> decryptionX509Credentials = new HashSet<>();

		private Builder(String registrationId) {
			this.registrationId = registrationId;
		}


		/**
		 * Sets the {@code registrationId} template. Often be used in URL paths
		 * @param id registrationId for this object, should be unique
		 * @return this object
		 */
		public Builder registrationId(String id) {
			this.registrationId = id;
			return this;
		}

		/**
		 * Set the relying party's
		 * <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/EntityNaming">EntityID</a>.
		 * Equivalent to the value found in the relying party's
		 * &lt;EntityDescriptor EntityID="..."/&gt;
		 *
		 * This value may contain a number of placeholders.
		 * They are {@code baseUrl}, {@code registrationId},
		 * {@code baseScheme}, {@code baseHost}, and {@code basePort}.
		 *
		 * @return the {@link Builder} for further configuration
		 * @since 5.4
		 */
		public Builder entityId(String entityId) {
			this.entityId = entityId;
			return this;
		}

		/**
		 * Apply this {@link Consumer} to the {@link Collection} of {@link Saml2X509Credential}s
		 * for the purposes of modifying the {@link Collection}
		 *
		 * @param credentialsConsumer - the {@link Consumer} for modifying the {@link Collection}
		 * @return the {@link Builder} for further configuration
		 * @since 5.4
		 */
		public Builder signingX509Credentials(Consumer<Collection<Saml2X509Credential>> credentialsConsumer) {
			credentialsConsumer.accept(this.signingX509Credentials);
			return this;
		}

		/**
		 * Apply this {@link Consumer} to the {@link Collection} of {@link Saml2X509Credential}s
		 * for the purposes of modifying the {@link Collection}
		 *
		 * @param credentialsConsumer - the {@link Consumer} for modifying the {@link Collection}
		 * @return the {@link Builder} for further configuration
		 * @since 5.4
		 */
		public Builder decryptionX509Credentials(Consumer<Collection<Saml2X509Credential>> credentialsConsumer) {
			credentialsConsumer.accept(this.decryptionX509Credentials);
			return this;
		}

		/**
		 * Set the <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/AssertionConsumerService">AssertionConsumerService</a>
		 * Location.
		 *
		 * <p>
		 * Equivalent to the value found in &lt;AssertionConsumerService Location="..."/&gt;
		 * in the relying party's &lt;SPSSODescriptor&gt;
		 *
		 * <p>
		 * This value may contain a number of placeholders.
		 * They are {@code baseUrl}, {@code registrationId},
		 * {@code baseScheme}, {@code baseHost}, and {@code basePort}.
		 *
		 * @param assertionConsumerServiceLocation
		 * @return the {@link Builder} for further configuration
		 * @since 5.4
		 */
		public Builder assertionConsumerServiceLocation(String assertionConsumerServiceLocation) {
			this.assertionConsumerServiceLocation = assertionConsumerServiceLocation;
			return this;
		}

		/**
		 * Configures the IDP SSO endpoint
		 * @param providerDetails a consumer that configures the IDP SSO endpoint
		 * @return this object
		 */
		public Builder providerDetails(Consumer<ProviderDetails.Builder> providerDetails) {
			providerDetails.accept(this.providerDetails);
			return this;
		}

		/**
		 * Modifies the collection of {@link Saml2X509Credential} objects
		 * used in communication between IDP and SP
		 * For example:
		 * <code>
		 *     Saml2X509Credential credential = ...;
		 *     return RelyingPartyRegistration.withRegistrationId("id")
		 *             .credentials(c -> c.add(credential))
		 *             ...
		 *             .build();
		 * </code>
		 * @param credentials - a consumer that can modify the collection of credentials
		 * @return this object'
		 * @deprecated Use {@link #signingX509Credentials} or {@link #decryptionX509Credentials} instead
		 * for relying party keys or {@link ProviderDetails.Builder#verificationX509Credentials} or
		 * {@link ProviderDetails.Builder#encryptionX509Credentials} for asserting party keys
		 */
		@Deprecated
		public Builder credentials(Consumer<Collection<Saml2X509Credential>> credentials) {
			credentials.accept(this.credentials);
			return this;
		}

		/**
		 * <a href="https://wiki.shibboleth.net/confluence/display/CONCEPT/AssertionConsumerService">Assertion Consumer
		 * Service</a> URL template. It can contain variables {@code baseUrl}, {@code registrationId},
		 * {@code baseScheme}, {@code baseHost}, and {@code basePort}.
		 * @param assertionConsumerServiceUrlTemplate the Assertion Consumer Service URL template (i.e.
		 * "{baseUrl}/login/saml2/sso/{registrationId}".
		 * @return this object
		 * @deprecated Use {@link #assertionConsumerServiceLocation} instead.
		 */
		@Deprecated
		public Builder assertionConsumerServiceUrlTemplate(String assertionConsumerServiceUrlTemplate) {
			this.assertionConsumerServiceLocation = assertionConsumerServiceUrlTemplate;
			return this;
		}

		/**
		 * Sets the {@code entityId} for the remote asserting party, the Identity Provider.
		 * @param entityId the IDP entityId
		 * @return this object
		 * @deprecated use {@link #providerDetails(Consumer< ProviderDetails.Builder >)}
		 */
		@Deprecated
		public Builder remoteIdpEntityId(String entityId) {
			this.providerDetails(idp -> idp.entityId(entityId));
			return this;
		}

		/**
		 * Sets the {@code SSO URL} for the remote asserting party, the Identity Provider.
		 * @param url - a URL that accepts authentication requests via REDIRECT or POST bindings
		 * @return this object
		 * @deprecated use {@link #providerDetails(Consumer< ProviderDetails.Builder >)}
		 */
		@Deprecated
		public Builder idpWebSsoUrl(String url) {
			providerDetails(config -> config.singleSignOnServiceLocation(url));
			return this;
		}

		/**
		 * Sets the local relying party, or Service Provider, entity Id template.
		 * can generate it's entity ID based on possible variables of {@code baseUrl}, {@code registrationId},
		 * {@code baseScheme}, {@code baseHost}, and {@code basePort}, for example
		 * {@code {baseUrl}/saml2/service-provider-metadata/{registrationId}}
		 * @return a string containing the entity ID or entity ID template
		 * @deprecated Use {@link #entityId} instead
		 */
		@Deprecated
		public Builder localEntityIdTemplate(String template) {
			this.entityId = template;
			return this;
		}

		/**
		 * Constructs a RelyingPartyRegistration object based on the builder configurations
		 * @return a RelyingPartyRegistration instance
		 */
		public RelyingPartyRegistration build() {
			List<Saml2X509Credential> signingCredentials =
					filterCredentials(this.credentials, c -> c.isSigningCredential());
			List<Saml2X509Credential> decryptionCredentials =
					filterCredentials(this.credentials, c -> c.isDecryptionCredential());
			List<Saml2X509Credential> verificationCredentials =
					filterCredentials(this.credentials, c -> c.isSignatureVerficationCredential());
			List<Saml2X509Credential> encryptionCredentials =
					filterCredentials(this.credentials, c -> c.isEncryptionCredential());

			signingX509Credentials(c -> c.addAll(signingCredentials));
			decryptionX509Credentials(c -> c.addAll(decryptionCredentials));
			this.providerDetails.verificationX509Credentials(c -> c.addAll(verificationCredentials));
			this.providerDetails.encryptionX509Credentials(c -> c.addAll(encryptionCredentials));

			this.credentials.addAll(this.signingX509Credentials);
			this.credentials.addAll(this.decryptionX509Credentials);
			this.credentials.addAll(this.providerDetails.verificationX509Credentials);
			this.credentials.addAll(this.providerDetails.encryptionX509Credentials);

			return new RelyingPartyRegistration(
					this.registrationId,
					this.entityId,
					this.credentials,
					this.signingX509Credentials,
					this.decryptionX509Credentials,
					this.assertionConsumerServiceLocation,
					this.providerDetails.build());
		}
	}

	private static List<Saml2X509Credential> filterCredentials(Collection<Saml2X509Credential> credentials,
			Function<Saml2X509Credential, Boolean> filter) {

		List<Saml2X509Credential> result = new LinkedList<>();
		for (Saml2X509Credential c : credentials) {
			if (filter.apply(c)) {
				result.add(c);
			}
		}
		return result;
	}
}
