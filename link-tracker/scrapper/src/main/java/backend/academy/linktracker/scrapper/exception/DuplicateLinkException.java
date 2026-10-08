package backend.academy.linktracker.scrapper.exception;

import java.io.Serial;

public class DuplicateLinkException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicateLinkException() {
        super();
    }

    public DuplicateLinkException(String message) {
        super(message);
    }
}
