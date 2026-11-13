package lab09_20227191.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import lab09_20227191.entity.StfPuzzle;
@Repository
public interface PuzzleRepository extends JpaRepository<StfPuzzle, String> {

    
}
