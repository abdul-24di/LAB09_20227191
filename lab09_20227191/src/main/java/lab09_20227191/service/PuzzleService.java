package lab09_20227191.service;

import java.util.*;
import java.util.regex.Matcher;
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

    // ---------- Formato del tablero ----------

    // "3x3" -> 3 ; formato invalido -> null
    public Integer leerDimension(String texto) {
        if (texto == null) {
            return null;
        }
        Matcher m = DIMENSION.matcher(texto.trim());
        return m.matches() ? Integer.parseInt(m.group(1)) : null;
    }

    // Tablero resuelto: 1, 2, ..., n*n-1, X
    public List<String> ordenado(int n) {
        List<String> tablero = new ArrayList<>();
        for (int i = 1; i < n * n; i++) {
            tablero.add(String.valueOf(i));
        }
        tablero.add(VACIO);
        return tablero;
    }

    // "41328576X" -> [4,1,3,2,8,5,7,6,X] ; "1-2-...-X" -> [1,2,...,X]
    public List<String> leer(String estructura) {
        if (estructura.contains("-")) {
            return new ArrayList<>(Arrays.asList(estructura.split("-")));
        }
        List<String> tablero = new ArrayList<>();
        for (char c : estructura.toCharArray()) {
            tablero.add(String.valueOf(c));
        }
        return tablero;
    }

    // 3x3 sin separador (igual al ejemplo del enunciado); desde 4x4 con "-",
    // porque las fichas 10, 11... tienen dos digitos
    public String formatear(List<String> tablero) {
        return tablero.size() <= 9 ? String.join("", tablero) : String.join("-", tablero);
    }

    public int dimension(List<String> tablero) {
        return (int) Math.round(Math.sqrt(tablero.size()));
    }

    public boolean resuelto(List<String> tablero) {
        return tablero.equals(ordenado(dimension(tablero)));
    }

    // Dos casillas son contiguas si estan a distancia 1 (misma fila o misma columna)
    public boolean sonContiguas(int i, int j, int n) {
        int distancia = Math.abs(i / n - j / n) + Math.abs(i % n - j % n);
        return distancia == 1;
    }

    // ---------- Desordenar con la secuencia fija ----------

    public List<String> desordenar(int n) {
        List<String> tablero = ordenado(n);
        for (String paso : SECUENCIA) {
            int vacio = tablero.indexOf(VACIO);
            int destino = switch (paso) {
                case "U" -> vacio - n;
                case "D" -> vacio + n;
                case "L" -> vacio - 1;
                default -> vacio + 1;
            };
            Collections.swap(tablero, vacio, destino);
        }
        return tablero;
    }

    // ---------- Operaciones sobre la BD ----------

    public String estructuraActual() {
        return puzzleRepository.obtenerFila()
                .map(StfPuzzle::getStfGameBoardStructure)
                .orElseGet(() -> {
                    String inicial = formatear(ordenado(3));
                    puzzleRepository.insertarFila(inicial);
                    return inicial;
                });
    }

    public byte[] imagenActual() {
        return puzzleRepository.obtenerFila().map(StfPuzzle::getImage).orElse(null);
    }

    public void guardarImagen(byte[] imagen) {
        estructuraActual();                       // asegura que exista la fila
        puzzleRepository.actualizarImagen(imagen);
    }

    // Nuevo juego: tablero desordenado guardado en BD
    public String iniciar(int n) {
        estructuraActual();
        String estructura = formatear(desordenar(n));
        puzzleRepository.actualizarEstructura(estructura);
        return estructura;
    }

    // Mueve una ficha contigua a la casilla vacia y guarda el tablero.
    // Si con ese movimiento se resuelve, se inicia un juego nuevo en BD.
    public Movimiento mover(String ficha) {
        List<String> tablero = leer(estructuraActual());
        int n = dimension(tablero);

        if (resuelto(tablero)) {
            throw new IllegalArgumentException("No hay un juego en curso. Presione Iniciar el juego");
        }
        if (ficha == null || ficha.equals(VACIO) || !tablero.contains(ficha)) {
            throw new IllegalArgumentException("La ficha " + ficha + " no existe en el tablero");
        }

        int iFicha = tablero.indexOf(ficha);
        int iVacio = tablero.indexOf(VACIO);
        if (!sonContiguas(iFicha, iVacio, n)) {
            throw new IllegalArgumentException("Solo se pueden mover fichas contiguas a la casilla vacía");
        }

        Collections.swap(tablero, iFicha, iVacio);

        if (resuelto(tablero)) {
            return new Movimiento(iniciar(n), true);
        }
        String estructura = formatear(tablero);
        puzzleRepository.actualizarEstructura(estructura);   // se guarda CADA movimiento
        return new Movimiento(estructura, false);
    }
}
