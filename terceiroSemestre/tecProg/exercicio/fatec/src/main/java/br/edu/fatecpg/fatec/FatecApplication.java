package br.edu.fatecpg.fatec;

import br.edu.fatecpg.fatec.model.Aluno;
import br.edu.fatecpg.fatec.repository.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class FatecApplication implements CommandLineRunner {

	@Autowired
	private AlunoRepository rep;

	public static void main(String[] args) {
		SpringApplication.run(FatecApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		if (rep.count() == 0) {
			// Adicionando alguns alunos para teste
			Aluno a1 = new Aluno("Ale", "079.866.309-06", "Ale@gmail.com", "01");
			Aluno a2 = new Aluno("Maria", "079.866.309-07", "Maria@gmail.com", "02");
			Aluno a3 = new Aluno("João", "079.866.309-08", "joao@gmail.com", "03");
			Aluno a4 = new Aluno("Caio", "079.866.309-09", "Caio@gmail.com", "04");
			Aluno a5 = new Aluno("GeGe", "079.866.309-10", "GeGe@gmail.com", "05");
			Aluno a6 = new Aluno("Xamis", "079.866.309-11", "Xamis@gmail.com", "06");
			rep.save(a1);
			rep.save(a2);
			rep.save(a3);
			rep.save(a4);
			rep.save(a5);
			rep.save(a6);
		} else {
			System.out.println("Já foi Salvo");
		}


		Optional<Aluno> aluno = rep.findById(1L);
		if (aluno.isPresent()) {
			System.out.println("Aluno com ID 1: " + aluno.get());
		} else {
			System.out.println("Aluno com ID 1 não encontrado.");
		}


		rep.deleteById(2L);
		System.out.println("Aluno com ID 2 foi removido.");


		List<Aluno> alunosRestantes = rep.findAll();
		System.out.println("Alunos restantes:");
		alunosRestantes.forEach(a -> System.out.println(a.getNome() + " - " + a.getEmail()));
	}
}