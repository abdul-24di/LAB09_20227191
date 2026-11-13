package lab09_20227191.repository;

import java.util.Optional;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import lab09_20227191.entity.StfPuzzle;

import java.beans.Transient;

@Repository
public interface PuzzleRepository extends JpaRepository<StfPuzzle, String> {

    // La tabla tiene una sola fila
    @Query(value = "SELECT * FROM stf_puzzle_sv LIMIT 1", nativeQuery = true)
    Optional<StfPuzzle> obtenerFila();

    // @Modifying + @Transactional: obligatorios para un UPDATE/INSERT con @Query (clase 3.1)
    // clearAutomatically: limpia la cache de JPA para que la siguiente lectura vea el cambio
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE stf_puzzle_sv SET stf_game_board_structure = ?1", nativeQuery = true)
    int actualizarEstructura(String estructura);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE stf_puzzle_sv SET image = ?1", nativeQuery = true)
    int actualizarImagen(byte[] imagen);

    // Por si alguien vacio la tabla: se vuelve a crear la unica fila
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "INSERT INTO stf_puzzle_sv (stf_game_board_structure, image) VALUES (?1, NULL)", nativeQuery = true)
    int insertarFila(String estructura);

}
