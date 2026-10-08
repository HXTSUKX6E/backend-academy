package backend.academy.linktracker.scrapper.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<String> chatNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat not found");
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<String> linkNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Link not found");
    }

    @ExceptionHandler(DuplicateLinkException.class)
    public ResponseEntity<String> duplicate() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Link already tracked");
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<String> rateLimitExceeded() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests");
    }
}
