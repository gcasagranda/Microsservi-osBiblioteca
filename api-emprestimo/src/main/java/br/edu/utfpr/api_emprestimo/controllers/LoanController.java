package br.edu.utfpr.api_emprestimo.controllers;

import br.edu.utfpr.api_emprestimo.dtos.LoanDTO;
import br.edu.utfpr.api_emprestimo.dtos.NewLoanDTO;
import br.edu.utfpr.api_emprestimo.models.Loan;
import br.edu.utfpr.api_emprestimo.repositories.LoanRepository;
import br.edu.utfpr.api_emprestimo.services.SequenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/loan")
public class LoanController {


    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private LoanRepository loanRepository;

    @PostMapping
    public ResponseEntity<?> createLoan(@Valid @RequestBody NewLoanDTO newLoanDTO) {
        Loan loan = new Loan();

        loan.setId(String.valueOf(sequenceService.generateSequence(Loan.class.getSimpleName())));
        loan.setIdUsuario(newLoanDTO.idUsuario());
        loan.setIdLivro(newLoanDTO.idLivro());
        loan.setDataEmprestimo(LocalDate.now(java.time.ZoneOffset.UTC));
        loan.setDataDevolucaoEsperada(newLoanDTO.dataDevolucaoEsperada());
        loan.setEmprestimoAtivo(true);
        loan.setDataRealDevolucao(null);
        loanRepository.save(loan);

        return ResponseEntity.status(HttpStatus.CREATED).body("Livro #" + newLoanDTO.idLivro() + " emprestado ao usuário #" + newLoanDTO.idUsuario());
    }

    @GetMapping
    public ResponseEntity<?> getAllLoans() {
        List<LoanDTO> list = new ArrayList<>();
        for (Loan loan : loanRepository.findAll()) {
            LoanDTO loanDTO = new LoanDTO(
                    loan.getId(),
                    loan.getIdUsuario(),
                    loan.getIdLivro(),
                    loan.getDataEmprestimo(),
                    loan.getDataDevolucaoEsperada(),
                    loan.getDataRealDevolucao(),
                    loan.isEmprestimoAtivo()
            );
            list.add(loanDTO);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getLoanById(@PathVariable("id") String id) {
        Optional<Loan> optLoan = loanRepository.findById(id);
        if (optLoan.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empréstimo não encontrado");
        }
        Loan loan = optLoan.get();
        LoanDTO loanDTO = new LoanDTO(
                loan.getId(),
                loan.getIdUsuario(),
                loan.getIdLivro(),
                loan.getDataEmprestimo(),
                loan.getDataDevolucaoEsperada(),
                loan.getDataRealDevolucao(),
                loan.isEmprestimoAtivo()
        );
        return ResponseEntity.ok(loanDTO);
    }

    @PutMapping(path="/{id}")
    public ResponseEntity<?> updateLoan(@PathVariable("id") String id, @Valid @RequestBody NewLoanDTO newLoanDTO) {
        Optional<Loan> optLoan = loanRepository.findById(id);
        if (optLoan.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empréstimo não encontrado");
        }
        Loan loan = optLoan.get();
        loan.setIdUsuario(newLoanDTO.idUsuario());
        loan.setIdLivro(newLoanDTO.idLivro());
        loan.setDataEmprestimo(newLoanDTO.dataDevolucaoEsperada());
        loanRepository.save(loan);
        return ResponseEntity.ok(new LoanDTO(
                loan.getId(),
                loan.getIdUsuario(),
                loan.getIdLivro(),
                loan.getDataEmprestimo(),
                loan.getDataDevolucaoEsperada(),
                loan.getDataRealDevolucao(),
                loan.isEmprestimoAtivo()
        ));
    }

    @DeleteMapping(path="/{id}")
    public ResponseEntity<?> deleteLoanById(@PathVariable("id") String id) {
        Optional<Loan> optLoan = loanRepository.findById(id);
        if (optLoan.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empréstimo não encontrado");
        }
        loanRepository.deleteById(id);
        return ResponseEntity.ok("Empréstimo deletado com sucesso");
    }
}
