package br.edu.utfpr.api_emprestimo.controllers;

import br.edu.utfpr.api_emprestimo.dtos.BookDTO;
import br.edu.utfpr.api_emprestimo.dtos.LoanDTO;
import br.edu.utfpr.api_emprestimo.dtos.NewLoanDTO;
import br.edu.utfpr.api_emprestimo.models.Loan;
import br.edu.utfpr.api_emprestimo.repositories.LoanRepository;
import br.edu.utfpr.api_emprestimo.services.BookFeignClient;
import br.edu.utfpr.api_emprestimo.services.SequenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


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

    @Autowired
    private BookFeignClient bookFeignClient;


    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> createLoan(@Valid @RequestBody NewLoanDTO newLoanDTO) {
        ResponseEntity<BookDTO> response = bookFeignClient.getBooksById(newLoanDTO.idLivro());
        BookDTO bookDTO = response.getBody();
        if (bookDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livro não encontrado");
        }
        if (bookDTO.availableCopies() == 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Livro não possui cópias disponíveis");
        }
        Optional<Loan> existingLoan = loanRepository.findByidUsuarioAndIdLivroAndEmprestimoAtivo(
                newLoanDTO.idUsuario(), newLoanDTO.idLivro(), true);

        if (existingLoan.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Já existe um empréstimo ativo para este livro e usuário.");
        }
        ResponseEntity<?> responseCopies = bookFeignClient.updateAvailableCopiesLoan(newLoanDTO.idLivro());
        if(responseCopies.getStatusCode() != HttpStatus.OK){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar cópias disponíveis");
        }
        Loan loan = new Loan();
        loan.setId(String.valueOf(sequenceService.generateSequence(Loan.class.getSimpleName())));
        loan.setIdUsuario(newLoanDTO.idUsuario());
        loan.setIdLivro(newLoanDTO.idLivro());
        loan.setDataEmprestimo(LocalDate.now(java.time.ZoneOffset.UTC));
        loan.setDataDevolucaoEsperada(newLoanDTO.dataDevolucaoEsperada());
        loan.setEmprestimoAtivo(true);
        loan.setDataRealDevolucao(null);
        loan.setAtrasado(false);
        loanRepository.save(loan);

        return ResponseEntity.status(HttpStatus.CREATED).body("Livro #" + newLoanDTO.idLivro() + " emprestado ao usuário #" + newLoanDTO.idUsuario());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
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
                    loan.isEmprestimoAtivo(),
                    loan.isAtrasado()
            );
            list.add(loanDTO);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
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
                loan.isEmprestimoAtivo(),
                loan.isAtrasado()
        );
        return ResponseEntity.ok(loanDTO);
    }

    @DeleteMapping(path="/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> deleteLoanById(@PathVariable("id") String id) {
        Optional<Loan> optLoan = loanRepository.findById(id);
        if (optLoan.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empréstimo não encontrado");
        }
        Loan loan = optLoan.get();
        if (loan.isEmprestimoAtivo()) {
            ResponseEntity<?> responseCopies = bookFeignClient.updateAvailableCopiesReturn(loan.getIdLivro());
            if(responseCopies.getStatusCode() != HttpStatus.OK){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar cópias disponíveis");
            }
        }
        loanRepository.deleteById(id);
        return ResponseEntity.ok("Empréstimo deletado com sucesso");
    }

    @PostMapping(path="/return/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> returnLoanById(@PathVariable("id") String id) {
        Optional<Loan> optLoan = loanRepository.findById(id);
        if (optLoan.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empréstimo não encontrado");
        }
        Loan loan = optLoan.get();
        if (!loan.isEmprestimoAtivo()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Empréstimo já devolvido");
        }
        loan.setEmprestimoAtivo(false);
        loan.setDataRealDevolucao(LocalDate.now(java.time.ZoneOffset.UTC));
        ResponseEntity<?> responseCopies = bookFeignClient.updateAvailableCopiesReturn(loan.getIdLivro());
        if(responseCopies.getStatusCode() != HttpStatus.OK){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar cópias disponíveis");
        }
        loanRepository.save(loan);
        return ResponseEntity.ok("Empréstimo devolvido com sucesso");
    }

    @GetMapping(path="/user/history")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<?> getUserHistory(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getClaimAsString("sub");
        List<LoanDTO> list = new ArrayList<>();
        for (Loan loan : loanRepository.findByidUsuario(userId)) {
            LoanDTO loanDTO = new LoanDTO(
                    loan.getId(),
                    loan.getIdUsuario(),
                    loan.getIdLivro(),
                    loan.getDataEmprestimo(),
                    loan.getDataDevolucaoEsperada(),
                    loan.getDataRealDevolucao(),
                    loan.isEmprestimoAtivo(),
                    loan.isAtrasado()
            );
            list.add(loanDTO);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping(path = "/active")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> getAllActiveLoans() {
        List<LoanDTO> list = new ArrayList<>();
        for (Loan loan : loanRepository.findByEmprestimoAtivo(true)) { // Find all active loans
            LoanDTO loanDTO = new LoanDTO(
                    loan.getId(),
                    loan.getIdUsuario(),
                    loan.getIdLivro(),
                    loan.getDataEmprestimo(),
                    loan.getDataDevolucaoEsperada(),
                    loan.getDataRealDevolucao(),
                    loan.isEmprestimoAtivo(),
                    loan.isAtrasado()
            );
            list.add(loanDTO);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping(path = "/active/user")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<?> getActiveLoansByUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getClaimAsString("sub");
        List<LoanDTO> list = new ArrayList<>();
        for (Loan loan : loanRepository.findByidUsuarioAndEmprestimoAtivo(userId, true)) { // Active loans by user
            LoanDTO loanDTO = new LoanDTO(
                    loan.getId(),
                    loan.getIdUsuario(),
                    loan.getIdLivro(),
                    loan.getDataEmprestimo(),
                    loan.getDataDevolucaoEsperada(),
                    loan.getDataRealDevolucao(),
                    loan.isEmprestimoAtivo(),
                    loan.isAtrasado()
            );
            list.add(loanDTO);
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping(path="/updateAtraso")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> updateAtraso() {
        LocalDate date = LocalDate.now(java.time.ZoneOffset.UTC);
        for (Loan loan : loanRepository.findByEmprestimoAtivo(true)) { // Find all active loans
            if(loan.getDataDevolucaoEsperada().isBefore(date)){
                loan.setAtrasado(true);
                loanRepository.save(loan);
            }
        }
        return ResponseEntity.ok("Atrasos atualizados");
    }
}
