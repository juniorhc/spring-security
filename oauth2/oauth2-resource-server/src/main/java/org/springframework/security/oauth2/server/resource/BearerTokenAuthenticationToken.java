/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.oauth2.server.resource;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * An {@link Authentication} that contains a
 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer Token</a>.
 *
 * Used by {@link BearerTokenAuthenticationFilter} to prepare an authentication attempt and supported
 * by {@link JwtAuthenticationProvider}.
 *
 * @author Josh Cummings
 * @since 5.1
 */
public class BearerTokenAuthenticationToken extends AbstractAuthenticationToken {
	private String token;

	/**
	 * Create a {@code BearerTokenAuthenticationToken} using the provided parameter(s)
	 *
	 * @param token
	 */
	public BearerTokenAuthenticationToken(String token) {
		super(Collections.emptyList());

		Assert.hasText(token, "token cannot be empty");

		this.token = token;
	}

	/**
	 * Get the <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer Token</a>
	 * @return
	 */
	public String getToken() {
		return this.token;
	}

	/**
	 * Get the <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer Token</a>
	 * @return
	 */
	@Override
	public Object getCredentials() {
		return this.getToken();
	}

	/**
	 * Get the <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer Token</a>
	 * @return
	 */
	@Override
	public Object getPrincipal() {
		return this.getToken();
	}
}
