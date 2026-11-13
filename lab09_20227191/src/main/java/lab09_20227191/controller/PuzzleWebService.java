package lab09_20227191.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import lab09_20227191.service.PuzzleService;
import lab09_20227191.service.PuzzleService.Movimiento;

// @RestController = @Controller + @ResponseBody (clase 9.2)
@RestController
@RequestMapping("/api/puzzle")
public class PuzzleWebService {


    final PuzzleService puzzleService;

    public PuzzleWebService(PuzzleService puzzleService) {
        this.puzzleService = puzzleService;
    }

    // ---------- GET /api/puzzle -> estado actual (retomar el juego) ----------
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> obtenerEstado() {
        HashMap<String, Object> responseMap = new HashMap<>();
        List<String> tablero = puzzleService.leer(puzzleService.estructuraActual());

        responseMap.put("estado", "ok");
        responseMap.put("estructura", puzzleService.formatear(tablero));
        responseMap.put("n", puzzleService.dimension(tablero));
        // Tablero ordenado = no hay juego pendiente
        responseMap.put("enJuego", !puzzleService.resuelto(tablero));
        return ResponseEntity.ok(responseMap);
    }

    // ---------- POST /api/puzzle/iniciar -> nuevo juego desordenado ----------
    @PostMapping(value = "/iniciar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> iniciarJuego(@RequestBody HashMap<String, String> body) {
        HashMap<String, Object> responseMap = new HashMap<>();

        Integer n = puzzleService.leerDimension(body.get("dimension"));
        if (n == null) {
            responseMap.put("estado", "error");
            responseMap.put("msg", "Ingrese la dimensión en formato NxN (de 3x3 a 9x9)");
            return ResponseEntity.badRequest().body(responseMap);
        }

        responseMap.put("estado", "iniciado");
        responseMap.put("estructura", puzzleService.iniciar(n));
        responseMap.put("n", n);
        return ResponseEntity.ok(responseMap);
    }

    // ---------- PUT /api/puzzle/mover -> mover una ficha ----------
    @PutMapping(value = "/mover", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> moverFicha(@RequestBody HashMap<String, String> body) {
        HashMap<String, Object> responseMap = new HashMap<>();
        try {
            Movimiento mov = puzzleService.mover(body.get("ficha"));
            responseMap.put("estado", mov.terminado() ? "terminado" : "movido");
            responseMap.put("estructura", mov.estructura());
            responseMap.put("n", puzzleService.dimension(puzzleService.leer(mov.estructura())));
            return ResponseEntity.ok(responseMap);
        } catch (IllegalArgumentException e) {
            responseMap.put("estado", "error");
            responseMap.put("msg", e.getMessage());
            return ResponseEntity.badRequest().body(responseMap);
        }
    }

    // ---------- GET /api/puzzle/imagen -> mostrar la imagen ----------
    // Igual que la clase 5.3: los bytes no los entiende el navegador por si solos,
    // hay que devolverlos con la cabecera Content-Type correcta
    @GetMapping("/imagen")
    public ResponseEntity<byte[]> mostrarImagen() {
        byte[] imagen = puzzleService.imagenActual();

        if (imagen == null) {
            // Aun no suben personaje: imagen por defecto de static/img
            try (InputStream in = new ClassPathResource("static/img/default.jpg").getInputStream()) {
                imagen = in.readAllBytes();
            } catch (IOException e) {
                return ResponseEntity.notFound().build();
            }
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);
        httpHeaders.setCacheControl(CacheControl.noStore());   // que el navegador no muestre una imagen vieja
        return new ResponseEntity<>(imagen, httpHeaders, HttpStatus.OK);
    }

    // ---------- POST /api/puzzle/imagen -> subir personaje (solo .jpg) ----------
    @PostMapping(value = "/imagen", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> subirImagen(@RequestParam("archivo") MultipartFile archivo) {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("estado", "error");

        // Validacion 1 (clase 5.3): archivo no vacio
        if (archivo.isEmpty()) {
            responseMap.put("msg", "Debe seleccionar una imagen");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Validacion 2 (clase 5.3): nombre sin ".." para evitar ataques LFI
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || nombre.contains("..")) {
            responseMap.put("msg", "Nombre de archivo no válido");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Validacion 3: solo formato jpg (extension .jpg o .jpeg)
        String nombreMin = nombre.toLowerCase();
        if (!nombreMin.endsWith(".jpg") && !nombreMin.endsWith(".jpeg")) {
            responseMap.put("msg", "Solo se permiten imágenes en formato jpg");
            return ResponseEntity.badRequest().body(responseMap);
        }

        try {
            byte[] bytes = archivo.getBytes();

            // Validacion 4: el contenido realmente es JPG (todo JPG empieza con FF D8 FF).
            // Evita que suban un .png renombrado a .jpg
            if (bytes.length < 3 || (bytes[0] & 0xFF) != 0xFF
                    || (bytes[1] & 0xFF) != 0xD8 || (bytes[2] & 0xFF) != 0xFF) {
                responseMap.put("msg", "El archivo no es una imagen jpg válida");
                return ResponseEntity.badRequest().body(responseMap);
            }

            puzzleService.guardarImagen(bytes);
            responseMap.put("estado", "actualizado");
            return ResponseEntity.ok(responseMap);

        } catch (IOException e) {
            responseMap.put("msg", "No se pudo leer el archivo");
            return ResponseEntity.badRequest().body(responseMap);
        }
    }

    // ---------- Body JSON vacio o mal formado (clase 9.2, diapo 39) ----------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<HashMap<String, String>> gestionExcepcion() {
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("estado", "error");
        responseMap.put("msg", "Debe enviar un JSON válido");
        return ResponseEntity.badRequest().body(responseMap);
    }

    // ---------- POST /imagen sin el campo "archivo" ----------
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<HashMap<String, String>> sinArchivo() {
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("estado", "error");
        responseMap.put("msg", "Debe enviar la imagen en el campo 'archivo'");
        return ResponseEntity.badRequest().body(responseMap);
    }
}
