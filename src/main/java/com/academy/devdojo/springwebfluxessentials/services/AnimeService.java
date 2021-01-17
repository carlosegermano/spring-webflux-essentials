package com.academy.devdojo.springwebfluxessentials.services;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.academy.devdojo.springwebfluxessentials.domain.Anime;
import com.academy.devdojo.springwebfluxessentials.repository.AnimeRepository;

import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnimeService {

	public final AnimeRepository animeRepository;
	
	public Mono<Anime> findById(int id) {
		return animeRepository.findById(id)
				.switchIfEmpty(monoResponseStatusNotFoundException());
	}
	
	public Flux<Anime> findAll() {
		return animeRepository.findAll();
	}
	
	public <T> Mono<T> monoResponseStatusNotFoundException() {
		return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found!"));
	}

	public Mono<Anime> save(@Valid Anime anime) {
		return animeRepository.save(anime);
	}
	
	@Transactional
	public Flux<Anime> saveAll(List<Anime> animes) {
		return animeRepository.saveAll(animes)
				.doOnNext(this::throwResponseStatusExceptionWhenEmptyName);
	}
	
	private void throwResponseStatusExceptionWhenEmptyName(Anime anime) {
		if (StringUtil.isNullOrEmpty(anime.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Name");
		}
	}

	public Mono<Void> update(Anime anime) {
		return findById(anime.getId())
				.flatMap(animeRepository::save)
				.then();
	}

	public Mono<Void> delete(int id) {
		return findById(id)
				.flatMap(animeRepository::delete);
	}

}
