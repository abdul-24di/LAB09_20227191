package lab09_20227191.service;

import java.util.*;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lab09_20227191.entity.StfPuzzle;
import lab09_20227191.repository.PuzzleRepository;

@Service
public class PuzzleService {

    public static final String VACIO = "X";

    private static final String[] SECUENCIA = {"U", "L", "U","L", "D","R","D","R"};

    private static final Pattern DIMENSION = Pattern.compile("^([3-9])[xX]\\1$");

    public record Movimiento(String estructura, boolean terminado){
    }

    private final PuzzleRepository puzzleRepository;
    public PuzzleService(PuzzleRepository puzzleRepository){
        this.puzzleRepository = puzzleRepository;
    }
}
