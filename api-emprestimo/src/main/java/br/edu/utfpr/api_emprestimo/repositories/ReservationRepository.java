package br.edu.utfpr.api_emprestimo.repositories;

import br.edu.utfpr.api_emprestimo.models.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReservationRepository extends MongoRepository<Reservation, String> {
    List<Reservation> findByIdUsuarioAndActive(String idUsuario, boolean active);
    List<Reservation> findByActive(boolean active);
}
