package lab09_20227191.controller;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class UploadErrorHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<HashMap<String, String>> imagenMuyGrande() {
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("estado", "error");
        responseMap.put("msg", "La imagen supera el tamaño máximo de 10 MB");
        return ResponseEntity.badRequest().body(responseMap);
    }
}
