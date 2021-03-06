package com.academy.devdojo.springwebfluxessentials.integration;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.academy.devdojo.springwebfluxessentials.domain.Anime;
import com.academy.devdojo.springwebfluxessentials.exceptions.CustomAttributes;
import com.academy.devdojo.springwebfluxessentials.repository.AnimeRepository;
import com.academy.devdojo.springwebfluxessentials.services.AnimeService;
import com.academy.devdojo.springwebfluxessentials.utils.AnimeCreator;
import com.academy.devdojo.springwebfluxessentials.utils.WebTestClientUtil;

import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
@Import({ AnimeService.class, CustomAttributes.class })
public class AnimeControllerIT {
	
	@Autowired
	private WebTestClientUtil webTestClientUtil;

	@MockBean
	private AnimeRepository animeRepositoryMock;

	private WebTestClient testClientUser;
	
	private WebTestClient testClientAdmin;
	
	private WebTestClient testClientInvalid;

	private final Anime anime = AnimeCreator.createValidAnime();

	@BeforeAll
	public static void blockHoundSetup() {
		BlockHound.install(builder -> 
		builder.allowBlockingCallsInside("java.util.UUID", "randomUUID"));
	}

	@BeforeEach
	public void setUp() {
		
		testClientUser = webTestClientUtil.authenticateClient("david", "devdojo");
		
		testClientAdmin = webTestClientUtil.authenticateClient("carlosegermano", "devdojo");
		
		testClientInvalid = webTestClientUtil.authenticateClient("João", "senhaInvalida");
		
		BDDMockito.when(animeRepositoryMock.findAll()).thenReturn(Flux.just(anime));

		BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.just(anime));

		BDDMockito.when(animeRepositoryMock.save(AnimeCreator.createAnimeToBeSaved())).thenReturn(Mono.just(anime));
		
		BDDMockito.when(animeRepositoryMock.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
		.thenReturn(Flux.just(anime, anime));

		BDDMockito.when(animeRepositoryMock.delete(ArgumentMatchers.any(Anime.class))).thenReturn(Mono.empty());

		BDDMockito.when(animeRepositoryMock.save(AnimeCreator.createValidAnime())).thenReturn(Mono.empty());
	}

	@Test
	public void blockHoundWorks() {
		try {
			FutureTask<?> task = new FutureTask<>(() -> {
				Thread.sleep(0);
				return "";
			});
			Schedulers.parallel().schedule(task);

			task.get(10, TimeUnit.SECONDS);
			Assertions.fail("should fail");
		} catch (Exception e) {
			Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
		}
	}

	@Test
	@DisplayName("listAll returns unauthorized when user is not authenticated")
	public void listAll_ReturnUnauthorized_WhenUserIsNotAuthenticated() {
		testClientInvalid.get()
			.uri("/animes")
			.exchange()
			.expectStatus()
			.isUnauthorized();
	}
	
	@Test
	@DisplayName("listAll returns forbidden when user is successfully authenticated and does note have role ADMIN")
	public void listAll_ReturnForbidden_WhenUserDoesNotHaveRoleAdmin() {
		testClientUser.get()
			.uri("/animes")
			.exchange()
			.expectStatus()
			.isForbidden();
	}
	
	@Test
	@DisplayName("listAll returns a flux of anime when user is successfully authenticated and has role ADMIN")
	public void listAll_ReturnFluxOfAnime_WhenSuccessful() {
		testClientAdmin.get().uri("/animes").exchange().expectStatus().is2xxSuccessful().expectBody()
			.jsonPath("$.[0].id").isEqualTo(anime.getId())
			.jsonPath("$.[0].name").isEqualTo(anime.getName());
	}

	@Test
	@DisplayName("listAll returns a flux of anime when user is successfully authenticated and has role ADMIN")
	public void listAll_Flavor2_ReturnFluxOfAnime_WhenSuccessful() {
		testClientAdmin.get().uri("/animes").exchange().expectStatus().isOk().expectBodyList(Anime.class).hasSize(1)
				.contains(anime);
	}

	@Test
	@DisplayName("findById returns Mono with anime when it exists and user is successfully authenticated and has role USER")
	public void findById_ReturnMonoAnime_WhenSuccessful() {
		testClientUser.get().uri("/animes/{id}", 1).exchange().expectStatus().isOk().expectBody(Anime.class)
				.isEqualTo(anime);
	}

	@Test
	@DisplayName("findById returns Mono error when anime does not exist and user is successfully authenticated and has role USER")
	public void findById_ReturnMonoError_WhenEmptyMonoIsReturned() {
		testClientUser.get().uri("/animes{id}", 1).exchange().expectStatus().isNotFound().expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
	}

	@Test
	@DisplayName("save creates an anime when successful and user is successfully authenticated and has role ADMIN")
	public void save_CreatesAnime_WhenSuccessful() {
		Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

		testClientAdmin.post().uri("/animes").contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(animeToBeSaved)).exchange().expectStatus().isCreated()
				.expectBody(Anime.class).isEqualTo(anime);
	}

	@Test
	@DisplayName("save returns mono error with bad request when name is empty and user is successfully authenticated and has role ADMIN")
	public void save_ReturnsError_WhenNameIsEmpty() {
		Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");

		testClientAdmin.post().uri("/animes").contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(animeToBeSaved)).exchange().expectStatus().isBadRequest().expectBody()
				.jsonPath("$.status").isEqualTo(400);
	}
	
	@Test
	@DisplayName("saveBatch creates a list of anime when successful and user is successfully authenticated and has role ADMIN")
	public void saveBatch_CreatesListOfAnime_WhenSuccessful() {
		Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
		
		testClientAdmin.post().uri("/animes/batch").contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved))).exchange().expectStatus().isCreated()
			.expectBodyList(Anime.class).hasSize(2).contains(anime);
	}
	
	@Test
	@DisplayName("saveBatch returns mono error when one of objects in the list contains null or empty name and user is successfully authenticated and has role ADMIN")
	public void saveBatch_ReturnsMonoError_whenContainsInvalidName() {
		Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
		
		BDDMockito.when(animeRepositoryMock.saveAll(ArgumentMatchers.anyIterable()))
		.thenReturn(Flux.just(anime, anime.withName("")));
		
		testClientAdmin.post().uri("/animes/batch").contentType(MediaType.APPLICATION_JSON)
		.body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved))).exchange().expectStatus().isBadRequest().expectBody()
		.jsonPath("$.status").isEqualTo(400);
	}

	@Test
	@DisplayName("delete removes the anime when successful and user is successfully authenticated and has role ADMIN")
	public void delete_DeleteAnime_WhenSuccessful() {
		testClientAdmin.delete().uri("/animes/{id}", 1).exchange().expectStatus().isNoContent();
	}

	@Test
	@DisplayName("delete returns Mono error when anime does not exist and user is successfully authenticated and has role ADMIN")
	public void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
		BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.empty());

		testClientAdmin.delete().uri("/animes{id}", 1).exchange().expectStatus().isNotFound().expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
	}

	@Test
	@DisplayName("update save updated anime and returns empty mono when successful and user is successfully authenticated and has role ADMIN")
	public void update_SaveUpdatedAnime_WhenSuccessful() {
		testClientAdmin.put().uri("/animes/{id}", 1).contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(anime)).exchange().expectStatus().isNoContent();
	}

	@Test
	@DisplayName("update returns Mono error when anime does not exist and user is successfully authenticated and has role ADMIN")
	public void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
		BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.empty());

		testClientAdmin.put().uri("/animes/{id}", 1).contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(anime)).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status")
			.isEqualTo(404).jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
	}
}
