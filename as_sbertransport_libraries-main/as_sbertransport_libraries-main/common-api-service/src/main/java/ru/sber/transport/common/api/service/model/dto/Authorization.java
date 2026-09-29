package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * Authorization
 */


public class Authorization implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("token")
    private String token;

    @JsonProperty("refreshToken")
    private String refreshToken;

    @JsonProperty("transferPassword")
    private Boolean transferPassword;

    public Authorization token(String token) {
        this.token = token;
        return this;
    }

    /**
     * Токен доступа к ресурсам. Содержит информацию о вошедшем пользователе. Его необходимо отправлять заголовком Authorization: Bearer {token} с каждым запросом
     *
     * @return token
     */

    @Schema(name = "token", description = "Токен доступа к ресурсам. Содержит информацию о вошедшем пользователе. Его необходимо отправлять заголовком Authorization: Bearer {token} с каждым запросом", required = false)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Authorization refreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    /**
     * Токен повторного входа. Отправлять с заголовком Authorization: Token {refreshToken} при истечении срока жизни токена доступа
     *
     * @return refreshToken
     */

    @Schema(name = "refreshToken", description = "Токен повторного входа. Отправлять с заголовком Authorization: Token {refreshToken} при истечении срока жизни токена доступа", required = false)
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Authorization transferPassword(Boolean transferPassword) {
        this.transferPassword = transferPassword;
        return this;
    }

    /**
     * Транспортный пароль
     *
     * @return transferPassword
     */

    @Schema(name = "transferPassword", description = "Транспортный пароль", required = false)
    public Boolean isTransferPassword() {
        return transferPassword;
    }

    public void setTransferPassword(Boolean transferPassword) {
        this.transferPassword = transferPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var authorization = (Authorization) o;
        return Objects.equals(this.token, authorization.token) &&
                Objects.equals(this.refreshToken, authorization.refreshToken) &&
                Objects.equals(this.transferPassword, authorization.transferPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, refreshToken, transferPassword);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("class Authorization {\n");
        sb.append("    token: ").append(toIndentedString(token)).append("\n");
        sb.append("    refreshToken: ").append(toIndentedString(refreshToken)).append("\n");
        sb.append("    transferPassword: ").append(toIndentedString(transferPassword)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

