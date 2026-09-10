package com.femcoders.tico.exception;

/**
 * Se lanza cuando un usuario autenticado, con su rol vigente, intenta una
 * acción concreta que las reglas de negocio no le permiten sobre ese recurso
 * (p. ej. responder a un ticket que no es suyo). Se mapea a 403, igual que
 * {@link org.springframework.security.access.AccessDeniedException}, pero
 * NO implica que el token o el rol del usuario estén desactualizados: el
 * frontend no debe cerrar la sesión al recibirla, solo mostrar el mensaje.
 */
public class ForbiddenActionException extends RuntimeException {
    public ForbiddenActionException(String message) {
        super(message);
    }
}
