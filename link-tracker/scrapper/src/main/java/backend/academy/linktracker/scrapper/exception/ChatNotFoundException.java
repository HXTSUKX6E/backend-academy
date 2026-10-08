package backend.academy.linktracker.scrapper.exception;

import java.io.Serial;

public class ChatNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ChatNotFoundException() {
        super();
    }

    public ChatNotFoundException(String message) {
        super(message);
    }
}
