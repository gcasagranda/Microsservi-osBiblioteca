package br.edu.utfpr.api_emprestimo.repositories;

import br.edu.utfpr.api_emprestimo.models.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationRepository extends MongoRepository<Reservation, String> {
}
