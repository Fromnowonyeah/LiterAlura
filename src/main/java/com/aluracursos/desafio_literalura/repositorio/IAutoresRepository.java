package com.aluracursos.desafio_literalura.repositorio;

import com.aluracursos.desafio_literalura.models.Autores;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAutoresRepository extends JpaRepository<Autores, Long> {
    Autores findByNameIgnoreCase(String nombre);

    List<Autores> findByAñoNacimientoLessThanEqualAndAñoMuerteGreaterThanEqual(int añoInicial, int añoFinal);
}
