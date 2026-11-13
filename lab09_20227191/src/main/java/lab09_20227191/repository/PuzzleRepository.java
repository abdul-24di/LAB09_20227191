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

    @Query(value = "SELECT * FROM stf_puzzle_sv LIMIT 1", nativeQuery = true)
    Optional<StfPuzzle> obtenerFila();

    //para los updates e inserts utilizaremos modifying y transactional
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE stf_puzzle_sv SET stf_game_board_structure =?1", nativeQuery = true)
    int actualizarEstructura(String estructura);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE stf_puzzle_sv SET image =?1", nativeQuery = true)
    int actualizarImagen(byte[] imagen);

}
