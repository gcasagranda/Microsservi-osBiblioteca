package br.edu.utfpr.api_emprestimo.controllers;


import br.edu.utfpr.api_emprestimo.dtos.NewReservationDTO;
import br.edu.utfpr.api_emprestimo.dtos.ReservationDTO;
import br.edu.utfpr.api_emprestimo.models.Reservation;
import br.edu.utfpr.api_emprestimo.repositories.ReservationRepository;
import br.edu.utfpr.api_emprestimo.services.SequenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody NewReservationDTO newReservationDTO) {
        if (newReservationDTO.dataEmprestimo().isAfter(newReservationDTO.dataDevolucaoEsperada())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de empréstimo não pode ser posterior à data de devolução esperada.");
        }
        Reservation reservation = new Reservation();

        reservation.setId(String.valueOf(sequenceService.generateSequence(Reservation.class.getSimpleName())));
        reservation.setIdUsuario(newReservationDTO.idUsuario());
        reservation.setIdLivro(newReservationDTO.idLivro());
        reservation.setDataEmprestimo(newReservationDTO.dataEmprestimo());
        reservation.setDataDevolucaoEsperada(newReservationDTO.dataDevolucaoEsperada());

        reservationRepository.save(reservation);

        return ResponseEntity.ok(reservation);
    }

    @GetMapping
    public ResponseEntity<?> getAllReservations() {
        List<ReservationDTO> list = new ArrayList<>();
        for (Reservation reservation : reservationRepository.findAll()) {
            ReservationDTO reservationDTO = new ReservationDTO(
                    reservation.getId(),
                    reservation.getIdUsuario(),
                    reservation.getIdLivro(),
                    reservation.getDataEmprestimo(),
                    reservation.getDataDevolucaoEsperada()
            );
            list.add(reservationDTO);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping(path="/{id}")
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
                reservation.getDataDevolucaoEsperada()
        );
        return ResponseEntity.ok(reservationDTO);
    }

    @PutMapping(path="/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable("id") String id, @Valid @RequestBody NewReservationDTO newReservationDTO) {
        Optional<Reservation> optReservation = reservationRepository.findById(id);
        if (optReservation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva não encontrada");
        }
        if (newReservationDTO.dataEmprestimo().isAfter(newReservationDTO.dataDevolucaoEsperada())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de empréstimo não pode ser posterior à data de devolução esperada.");
        }
        Reservation reservation = optReservation.get();
        reservation.setIdUsuario(newReservationDTO.idUsuario());
        reservation.setIdLivro(newReservationDTO.idLivro());
        reservation.setDataEmprestimo(newReservationDTO.dataEmprestimo());
        reservation.setDataDevolucaoEsperada(newReservationDTO.dataDevolucaoEsperada());
        reservationRepository.save(reservation);
        return ResponseEntity.ok(new ReservationDTO(
                reservation.getId(),
                reservation.getIdUsuario(),
                reservation.getIdLivro(),
                reservation.getDataEmprestimo(),
                reservation.getDataDevolucaoEsperada()
        ));
    }

    @DeleteMapping(path="/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable("id") String id) {
        Optional<Reservation> optReservation = reservationRepository.findById(id);
        if (optReservation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva não encontrada");
        }
        reservationRepository.deleteById(id);
        return ResponseEntity.ok("Reserva deletada com sucesso");
    }
}
