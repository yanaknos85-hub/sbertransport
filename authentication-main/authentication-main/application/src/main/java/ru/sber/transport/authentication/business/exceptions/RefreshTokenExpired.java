package ru.sber.transport.authentication.business.exceptions;

/**
 * Исключение, выбрасываемое в случае истечения токена обновления сессии.
 */
public class RefreshTokenExpired extends Throwable {
    
    /**
     * Создать новое искючение.
     *
     * @param refresh токен обновления сессии.
     */
    public RefreshTokenExpired(String refresh) {
        super(String.format("Refresh token '%s' was expired", refresh));
    }
}
