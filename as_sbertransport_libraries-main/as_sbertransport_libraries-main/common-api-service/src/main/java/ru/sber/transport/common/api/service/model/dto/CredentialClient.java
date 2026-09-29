package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

/**
 * CredentialClient
 */


public class CredentialClient  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("uri")
  private URI uri = null;

  @JsonProperty("login")
  private String login;

  @JsonProperty("password")
  private String password;

  public CredentialClient uri(URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Get uri
   * @return uri
  */
  @Valid 
  @Schema(name = "uri", required = false)
  public URI getUri() {
    return uri;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public CredentialClient login(String login) {
    this.login = login;
    return this;
  }

  /**
   * Get login
   * @return login
  */
  
  @Schema(name = "login", required = false)
  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public CredentialClient password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Get password
   * @return password
  */
  
  @Schema(name = "password", required = false)
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var credentialClient = (CredentialClient) o;
    return Objects.equals(this.uri, credentialClient.uri) &&
        Objects.equals(this.login, credentialClient.login) &&
        Objects.equals(this.password, credentialClient.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri, login, password);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class CredentialClient {\n");
    sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
    sb.append("    login: ").append(toIndentedString(login)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
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

