package de.daschi.javalanguageapi.api;

public class LanguageException extends RuntimeException {

    private static final long serialVersionUID = 6219489708951517148L;

    public LanguageException() {
    }

    public LanguageException(final String message) {
        super(message);
    }

    public LanguageException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LanguageException(final Throwable cause) {
        super(cause);
    }

    public LanguageException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
