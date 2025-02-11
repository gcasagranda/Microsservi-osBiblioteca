package br.edu.utfpr.api_emprestimo.repositories;

import br.edu.utfpr.api_emprestimo.models.Loan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends MongoRepository<Loan, String> {
    Optional<Loan> findByidUsuarioAndIdLivroAndEmprestimoAtivo(String idUsuario, String idLivro, boolean emprestimoAtivo);
    List<Loan> findByidUsuario(String idUsuario);
    List<Loan> findByEmprestimoAtivo(boolean emprestimoAtivo);
    List<Loan> findByidUsuarioAndEmprestimoAtivo(String idUsuario, boolean emprestimoAtivo);
}
