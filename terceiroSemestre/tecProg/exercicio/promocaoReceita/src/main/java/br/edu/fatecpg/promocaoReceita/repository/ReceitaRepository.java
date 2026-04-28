package br.edu.fatecpg.promocaoReceita.repository;

import br.edu.fatecpg.promocaoReceita.model.Receita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {
    List<Receita> findByPromocaoTrue();
}
