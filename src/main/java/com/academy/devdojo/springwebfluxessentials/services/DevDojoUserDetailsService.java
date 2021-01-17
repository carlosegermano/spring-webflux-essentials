package com.academy.devdojo.springwebfluxessentials.services;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.academy.devdojo.springwebfluxessentials.repository.DevDojoUserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class DevDojoUserDetailsService implements ReactiveUserDetailsService {

	private final DevDojoUserRepository devDojoUserRepository;
	
	@Override
	public Mono<UserDetails> findByUsername(String username) {
		return devDojoUserRepository.findByUsername(username)
				.cast(UserDetails.class);
	}

	
}
