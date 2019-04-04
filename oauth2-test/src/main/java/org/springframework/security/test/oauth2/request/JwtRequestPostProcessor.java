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
package org.springframework.security.test.oauth2.request;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.oauth2.support.JwtAuthenticationBuilder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.SecurityContextRequestPostProcessorSupport;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * @author Jérôme Wacongne &lt;ch4mp@c4-soft.com&gt;
 * @since 5.2.0
 */
public class JwtRequestPostProcessor extends JwtAuthenticationBuilder<JwtRequestPostProcessor>
		implements
		RequestPostProcessor {

	@Override
	public MockHttpServletRequest postProcessRequest(final MockHttpServletRequest request) {
		SecurityContextRequestPostProcessorSupport.save(build(), request);
		return request;
	}

}
