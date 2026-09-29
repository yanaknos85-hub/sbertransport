package ru.sber.transport.authentication.business.providers;

import ru.sber.transport.authentication.business.dto.AccessTokenData;
import ru.sber.transport.authentication.business.dto.AccountDto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Провайдер токенов доступа.
 */
public interface AccessTokenProvider {

    /**
     * Раздел токена с ролями.
     */
    String ROLES_CLAIM_NAME = "roles";

    /**
     * Раздел токена с признаком транспортного пароля.
     */
    String TRANSPORT_CLAIM_NAME = "transport";

    /**
     * Раздел токена с признаком транспортного пароля.
     */
    String DATAMASTER_CLAIM_NAME = "data_master";

    /**
     * Идентификатор запроса, выдавшего токен.
     */
    String REQUEST_ID_CLAIM_NAME = "requestId";

    /**
     * Случайное число, обеспечивающее уникальность.
     */
    String RANDOM_CLAIM_NAME = "random";

    /**
     * Фактор авторизации.
     */
    String FACTOR_CLAIM_NAME = "factor";

    /**
     * Область видимости пользователя.
     */
    String SCOPE_CLAIM_NAME = "scope";
    
    /**
     * Сгенерировать токен.
     *
     * @param accountDto УЗ для генерации токена.
     * @param enforceBasic принудительное отключение второго фактора.
     * @return токен.
     */
    AccessTokenData generate(AccountDto accountDto, boolean enforceBasic) throws NoSuchAlgorithmException, InvalidKeySpecException;
    
    /**
     * Отправить токен в черный список.
     *
     * @param token токен.
     */
    void toBlackList(String token);
}
