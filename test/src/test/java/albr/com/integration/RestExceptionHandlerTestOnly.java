package albr.com.integration;

import albr.com.tax.exception.ProductCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class RestExceptionHandlerTestOnly {

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorDto("BAD_REQUEST", ex.getMessage())
        );
    }

    @ExceptionHandler(ProductCreationException.class)
    ResponseEntity<?> handleProductCreation(ProductCreationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorDto("CREATION_FAILED", ex.getMessage())
        );
    }


    record ErrorDto(String code, String message) {
    }
}
