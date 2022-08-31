package sg.darren.kafka.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class LibraryEventControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleRequestBody(MethodArgumentNotValidException ex) {
        List<FieldError> list = ex.getBindingResult().getFieldErrors();
        String errorMessage = list.stream()
                .map(fe -> fe.getField() + " - " + fe.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        log.info("Error message: {}", errorMessage);
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

}
