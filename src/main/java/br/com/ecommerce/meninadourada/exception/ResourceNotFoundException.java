package br.com.ecommerce.meninadourada.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção customizada para indicar que um recurso não foi encontrado.
 * Mapeada para o status HTTP 404 (Not Found) automaticamente pelo Spring.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Mapeia esta exceção para o status HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construtor que aceita uma mensagem de erro.
     * @param message A mensagem detalhando o recurso não encontrado.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construtor que aceita uma mensagem de erro e uma causa.
     * @param message A mensagem detalhando o recurso não encontrado.
     * @param cause A causa raiz da exceção.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

