package com.github.quorum;

import com.github.quorum.component.dto.APIRequest;
import com.github.quorum.web.Router;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class PublicTransactionsTests {

	private static final List<String> PRIVATE_FOR = new ArrayList<>();
	private static final String USER = "Public Test User";

	@BeforeClass
	public static void setUp() {
		PRIVATE_FOR.add("public");
	}

	@Autowired
	private Router router;

	@Test
	public void test_1_deployPrivateContract() {
		final WebTestClient webTestClient = WebTestClient
				.bindToRouterFunction(this.router.composedRouter())
				.build();

		final APIRequest apiRequest = new APIRequest();
		apiRequest.setPrivateFor(PRIVATE_FOR);

		webTestClient
				.post()
				.uri("/")
				.body(Mono.just(apiRequest), APIRequest.class)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@Test
	public void test_2_updateUser() {
		final WebTestClient webTestClient = WebTestClient
				.bindToRouterFunction(this.router.composedRouter())
				.build();

		final APIRequest apiRequest = new APIRequest();
		apiRequest.setPrivateFor(PRIVATE_FOR);
		apiRequest.setUser(USER);

		webTestClient
				.put()
				.uri("/")
				.body(Mono.just(apiRequest), APIRequest.class)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@Test
	public void test_3_getUser() {
		final WebTestClient webTestClient = WebTestClient
				.bindToRouterFunction(this.router.composedRouter())
				.build();

		webTestClient
				.get()
				.uri("/")
				.exchange()
				.expectStatus()
				.isOk();
	}

}
