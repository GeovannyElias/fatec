package br.edu.fatecpg.promocaoReceita;

import br.edu.fatecpg.promocaoReceita.model.Receita;
import br.edu.fatecpg.promocaoReceita.repository.ReceitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PromocaoReceitaApplication implements CommandLineRunner {
	@Autowired
	private ReceitaRepository rep;
	public static void main(String[] args) {
		SpringApplication.run(PromocaoReceitaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (rep.count() == 0){
			Receita r1 = new Receita("Feijoada", "Salgado", "13.00", true);
			Receita r2 = new Receita("Bananada", "Doce", "13.00", false);
			Receita r3 = new Receita("Pudim", "Doce", "13.00", true);
			Receita r4 = new Receita("Bolo", "Doce", "13.00", false);
			Receita r5 = new Receita("Coxinha", "Salgado", "13.00", false);

			rep.save(r1);
			rep.save(r2);
			rep.save(r3);
			rep.save(r4);
			rep.save(r5);
		}

		/*BUSCAR RECEITAS EM PROMOCAO*/
		rep.findByPromocaoTrue().forEach(System.out::println);
	}
}
