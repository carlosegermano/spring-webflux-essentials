package com.academy.devdojo.springwebfluxessentials.domain;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@With
@Table("anime")
public class Anime {

	@Id
	private Integer id;
	
	@NotNull
	@NotEmpty(message = "The name of this anime cannot is empty!")
	private String name;
	
	
}
