/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.security.oauth2.jwt;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.util.Assert;

/**
 * An implementation of an {@link AbstractOAuth2Token} representing a JSON Web Token (JWT).
 *
 * <p>
 * JWTs represent a set of &quot;claims&quot; as a JSON object that may be encoded in a
 * JSON Web Signature (JWS) and/or JSON Web Encryption (JWE) structure.
 * The JSON object, also known as the JWT Claims Set, consists of one or more claim name/value pairs.
 * The claim name is a {@code String} and the claim value is an arbitrary JSON object.
 *
 * @author Joe Grandja
 * @since 5.0
 * @see AbstractOAuth2Token
 * @see JwtClaimAccessor
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc7519">JSON Web Token (JWT)</a>
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc7515">JSON Web Signature (JWS)</a>
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc7516">JSON Web Encryption (JWE)</a>
 */
public class Jwt extends AbstractOAuth2Token implements JwtClaimAccessor {
	private final Map<String, Object> headers;
	private final Map<String, Object> claims;

	public Jwt(String tokenValue, Map<String, Object> headers, Map<String, Object> claims) {
		this(tokenValue, getIssuedAt(claims), getExpiresAt(claims), headers, claims);
	}

	private static Instant getIssuedAt(Map<String, Object> claims) {
		Assert.notEmpty(claims, "claims cannot be empty");
		return (Instant) claims.get(JwtClaimNames.IAT);
	}

	private static Instant getExpiresAt(Map<String, Object> claims) {
		Assert.notEmpty(claims, "claims cannot be empty");
		return (Instant) claims.get(JwtClaimNames.EXP);
	}

	/**
	 * Constructs a {@code Jwt} using the provided parameters.
	 *
	 * @param tokenValue the token value
	 * @param issuedAt the time at which the JWT was issued
	 * @param expiresAt the expiration time on or after which the JWT MUST NOT be accepted
	 * @param headers the JOSE header(s)
	 * @param claims the JWT Claims Set
	 */
	public Jwt(String tokenValue, Instant issuedAt, Instant expiresAt,
				Map<String, Object> headers, Map<String, Object> claims) {
		super(tokenValue, issuedAt, expiresAt);
		Assert.notEmpty(headers, "headers cannot be empty");
		Assert.notEmpty(claims, "claims cannot be empty");
		this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
		this.claims = Collections.unmodifiableMap(new LinkedHashMap<>(claims));
	}

	/**
	 * Returns the JOSE header(s).
	 *
	 * @return a {@code Map} of the JOSE header(s)
	 */
	public Map<String, Object> getHeaders() {
		return this.headers;
	}

	/**
	 * Returns the JWT Claims Set.
	 *
	 * @return a {@code Map} of the JWT Claims Set
	 */
	@Override
	public Map<String, Object> getClaims() {
		return this.claims;
	}

	public static JwtBuilder withTokenValue(String tokenValue) {
		return new JwtBuilder(tokenValue);
	}

	public static final class JwtBuilder {
		private String tokenValue;
		private Map<String, Object> buildingClaims = new HashMap<>();
		private Map<String, Object> buildingHeaders = new HashMap<>();
		private Map<String, Object> claims;
		private Map<String, Object> headers;

		public JwtBuilder(String tokenValue) {
			this.tokenValue = tokenValue;
		}

		public JwtBuilder header(String name, Object value) {
			Assert.hasLength(name, "header name cannot be empty");
			Assert.notNull(value, "header value cannot be null");
			this.buildingHeaders.put(name, value);
			return this;
		}

		public JwtBuilder headers(Map<String, Object> headers) {
			Assert.notEmpty(headers, "headers cannot be empty");
			this.headers = headers;
			return this;
		}

		public JwtBuilder claim(String name, Object value) {
			Assert.hasLength(name, "claim name cannot be empty");
			Assert.notNull(value, "claim value cannot be null");
			this.buildingClaims.put(name, value);
			return this;
		}

		public JwtBuilder claims(Map<String, Object> claims) {
			Assert.notEmpty(claims, "claims cannot be empty");
			this.claims = claims;
			return this;
		}

		private Map<String, Object> getHeaders() {
			if (this.headers == null) {
				return this.buildingHeaders;
			}
			return this.headers;
		}

		private Map<String, Object> getClaims() {
			if (this.claims == null) {
				return this.buildingClaims;
			}
			return this.claims;
		}

		public Jwt build() {
			return new Jwt(this.tokenValue, getHeaders(), getClaims());
		}
	}
}
