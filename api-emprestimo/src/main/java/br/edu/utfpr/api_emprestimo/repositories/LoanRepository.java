package br.edu.utfpr.api_emprestimo.repositories;

import br.edu.utfpr.api_emprestimo.models.Loan;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoanRepository extends MongoRepository<Loan, String> {
}
