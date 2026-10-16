package com.group5.MyMemories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MyMemoriesApplicationTests {
	private static final String TEST_JWT_SECRET = createTestJwtSecret();

	@Autowired
	private MockMvc mockMvc;

	@DynamicPropertySource
	static void testProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", () -> "jdbc:h2:mem:mymemories;DB_CLOSE_DELAY=-1");
		registry.add("spring.datasource.username", () -> "sa");
		registry.add("spring.datasource.password", () -> "");
		registry.add("jwt.secret", () -> TEST_JWT_SECRET);
		registry.add("jwt.expiration", () -> "86400000");
	}

	private static String createTestJwtSecret() {
		byte[] secret = new byte[32];
		new SecureRandom().nextBytes(secret);
		return Base64.getEncoder().encodeToString(secret);
	}

	@Test
	void contextLoads() {
	}

	@Test
	void registerAndLoginReturnJwtWithoutPassword() throws Exception {
		String suffix = UUID.randomUUID().toString();
		String username = "test-" + suffix;
		String email = suffix + "@example.test";
		String password = createTestJwtSecret();

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","email":"%s","password":"%s"}
						""".formatted(username, email, password)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.token").isNotEmpty())
			.andExpect(jsonPath("$.password").doesNotExist());

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"%s"}
						""".formatted(email, password)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void malformedBearerTokenReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/private-test")
				.header("Authorization", "Bearer not-a-valid-token"))
			.andExpect(status().isUnauthorized());
	}

}
