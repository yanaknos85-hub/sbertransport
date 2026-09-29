package ru.sber.transport.authsb.api.service.client;

public class ParamRequest {

    private ParamRequest() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CODE = "code";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String ERROR = "error";
    public static final String NONCE = "nonce";
    public static final String SUB = "sub";
    public static final String AUD = "aud";
    public static final String ISS = "iss";
    public static final String ORG_OGRN = "orgOgrn";
    public static final String ORG_KPP = "orgKpp";
    public static final String NAME = "name";
    public static final String INN = "inn";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String ORG_LAW_FORM = "orgLawForm";
    public static final String EMAIL = "email";
    public static final String ORG_FULL_NAME = "orgFullName";
    public static final String OFFER_EXPIRATION_DATE = "offerExpirationDate";
    public static final String ORG_OKTMO = "orgOktmo";
    public static final String INDIVIDUAL_EXECUTIVE_AGENCY = "individualExecutiveAgency";


    public static final String TOKEN_TYPE = "token_type";
    public static final String NOT_FOUND_FIELDS_MSG_FORMAT = "Получен ответ от сервиса %s, в котором отсутствуют поля: %s";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EXPIRES_IN = "expires_in";
    public static final String ID_TOKEN = "id_token";
}
