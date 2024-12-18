package br.edu.utfpr.api_emprestimo;

import br.edu.utfpr.api_emprestimo.repositories.LoanRepository;
import br.edu.utfpr.api_emprestimo.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
@SpringBootApplication
public class ApiEmprestimoApplication {

	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	public static void main(String[] args) {
		SpringApplication.run(ApiEmprestimoApplication.class, args);
	}

}
