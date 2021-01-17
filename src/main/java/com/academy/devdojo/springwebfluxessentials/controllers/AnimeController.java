package com.academy.devdojo.springwebfluxessentials.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.devdojo.springwebfluxessentials.domain.Anime;
import com.academy.devdojo.springwebfluxessentials.services.AnimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("animes")
@Slf4j
public class AnimeController {

	private final AnimeService animeService;
	
	@GetMapping(path = "{id}")
	@ResponseStatus(HttpStatus.OK)
	public Mono<Anime> find(@PathVariable int id) {
		return animeService.findById(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ADMIN')")
	public Flux<Anime> listAll() {
		return animeService.findAll();
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Anime> save(@Valid @RequestBody Anime anime) {
		return animeService.save(anime);
	}
	
	@PostMapping("batch")
	@ResponseStatus(HttpStatus.CREATED)
	public Flux<Anime> saveBatch(@RequestBody List<Anime> animes) {
		return animeService.saveAll(animes);
	}
	
	@PutMapping(path = "{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> update(@PathVariable int id, @Valid @RequestBody Anime anime) {
		return animeService.update(anime.withId(id));	
	}
	
	@DeleteMapping(path = "{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> delete(@PathVariable int id) {
		return animeService.delete(id);	
	}
}
