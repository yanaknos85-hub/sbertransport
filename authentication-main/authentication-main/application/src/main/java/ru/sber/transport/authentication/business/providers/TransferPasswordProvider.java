package ru.sber.transport.authentication.business.providers;

/**
 * Провайдер транспортных паролей.
 */
public interface TransferPasswordProvider {

    /**
     * Генерация транспортного пароля.
     * @param length длина
     */
    char[] generateTransferPassword(int length);

}
