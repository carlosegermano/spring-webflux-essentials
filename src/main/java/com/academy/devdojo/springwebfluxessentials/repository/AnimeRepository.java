package com.academy.devdojo.springwebfluxessentials.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.academy.devdojo.springwebfluxessentials.domain.Anime;

import reactor.core.publisher.Mono;

public interface AnimeRepository extends ReactiveCrudRepository<Anime, Integer> {

	Mono<Anime> findById(int id);
}
