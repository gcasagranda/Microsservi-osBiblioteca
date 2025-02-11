package br.edu.utfpr.api_emprestimo.controllers;


import br.edu.utfpr.api_emprestimo.dtos.BookDTO;
import br.edu.utfpr.api_emprestimo.dtos.NewReservationDTO;
import br.edu.utfpr.api_emprestimo.dtos.ReservationDTO;
import br.edu.utfpr.api_emprestimo.models.Reservation;
import br.edu.utfpr.api_emprestimo.repositories.ReservationRepository;
import br.edu.utfpr.api_emprestimo.services.BookFeignClient;
import br.edu.utfpr.api_emprestimo.services.SequenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BookFeignClient bookFeignClient;

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<?> createReservation(@Valid @RequestBody NewReservationDTO newReservationDTO, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getClaimAsString("sub");
        ResponseEntity<BookDTO> response = bookFeignClient.getBooksById(newReservationDTO.idLivro());
        BookDTO bookDTO = response.getBody();
        if (bookDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livro não encontrado");
        }
        if (bookDTO.availableCopies() == 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Livro não possui cópias disponíveis");
        }
        if (newReservationDTO.dataEmprestimo().isAfter(newReservationDTO.dataDevolucaoEsperada())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de empréstimo não pode ser posterior à data de devolução esperada.");
        }
        ResponseEntity<?> responseCopies = bookFeignClient.updateReservedBook(newReservationDTO.idLivro(), true);
        if(responseCopies.getStatusCode() != HttpStatus.OK){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar cópias disponíveis");
        }

        Reservation reservation = new Reservation();

        reservation.setId(String.valueOf(sequenceService.generateSequence(Reservation.class.getSimpleName())));
        reservation.setIdUsuario(userId);
        reservation.setIdLivro(newReservationDTO.idLivro());
        reservation.setDataEmprestimo(newReservationDTO.dataEmprestimo());
        reservation.setDataDevolucaoEsperada(newReservationDTO.dataDevolucaoEsperada());
        reservation.setActive(true);

        reservationRepository.save(reservation);

        return ResponseEntity.ok(reservation);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> getAllReservations() {
        List<ReservationDTO> list = new ArrayList<>();
        for (Reservation reservation : reservationRepository.findAll()) {
            ReservationDTO reservationDTO = new ReservationDTO(
                    reservation.getId(),
                    reservation.getIdUsuario(),
                    reservation.getIdLivro(),
                    reservation.getDataEmprestimo(),
                    reservation.getDataDevolucaoEsperada(),
                    reservation.isActive()
            );
            list.add(reservationDTO);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping(path="/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> getReservationById(@PathVariable("id") String id) {
        Optional<Reservation> optReservation = reservationRepository.findById(id);
        if (optReservation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva não encontrada");
        }
        Reservation reservation = optReservation.get();
        ReservationDTO reservationDTO = new ReservationDTO(
                reservation.getId(),
                reservation.getIdUsuario(),
                reservation.getIdLivro(),
                reservation.getDataEmprestimo(),
                reservation.getDataDevolucaoEsperada(),
                reservation.isActive()
        );
        return ResponseEntity.ok(reservationDTO);
    }


    @DeleteMapping(path="/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> deleteReservation(@PathVariable("id") String id) {
        Optional<Reservation> optReservation = reservationRepository.findById(id);
        if (optReservation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva não encontrada");
        }
        Reservation reservation = optReservation.get();
        if (reservation.isActive()) {
            ResponseEntity<?> responseCopies = bookFeignClient.updateReservedBook(reservation.getIdLivro(), false);
            if(responseCopies.getStatusCode() != HttpStatus.OK){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar cópias disponíveis");
            }
        }
        reservationRepository.deleteById(id);
        return ResponseEntity.ok("Reserva deletada com sucesso");
    }

    @GetMapping(path="/myreservations")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<?> getMyReservationsbyUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getClaimAsString("sub");
        List<ReservationDTO> list = new ArrayList<>();
        for (Reservation reservation : reservationRepository.findByIdUsuarioAndActive(userId, true)) {
            ReservationDTO reservationDTO = new ReservationDTO(
                    reservation.getId(),
                    reservation.getIdUsuario(),
                    reservation.getIdLivro(),
                    reservation.getDataEmprestimo(),
                    reservation.getDataDevolucaoEsperada(),
                    reservation.isActive()
            );
            list.add(reservationDTO);
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping(path = "/inactivateReservations")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> inactivateReservations() {
        LocalDate now = LocalDate.now();
        for (Reservation reservation : reservationRepository.findByActive(true)) {
            if (reservation.getDataEmprestimo().isAfter(now)){
                reservation.setActive(false);
                reservationRepository.save(reservation);
            }
        }
        return ResponseEntity.ok("Reservas atualizadas com sucesso");
    }
}
