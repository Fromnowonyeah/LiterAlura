package com.aluracursos.desafio_literalura.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosAutores(
        @JsonAlias("name") String nombreAutor,
        @JsonAlias("birth_year") Integer añoNacimiento,
        @JsonAlias("death_year") Integer añoMuerte
) {
}
