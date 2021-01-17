package com.academy.devdojo.springwebfluxessentials.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.academy.devdojo.springwebfluxessentials.domain.DevDojoUser;

import reactor.core.publisher.Mono;

public interface DevDojoUserRepository extends ReactiveCrudRepository<DevDojoUser, Integer> {

	Mono<DevDojoUser> findByUsername(String username);
}
