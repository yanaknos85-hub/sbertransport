package ru.sber.transport.authentication.providers.text;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.sber.transport.authentication.business.providers.TextProvider;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Релизация провайдера текстовок.
 */
@Component
@Slf4j
class TextProviderImpl implements TextProvider {
    
    private static final String DEFAULT_PASSWORD_EMAIL_FILE = "classpath:text/email/resetPassword.html";

    private static final String DEFAULT_PASSWORD_EMAIL_FOR_DISPATCHER_FILE = "classpath:text/email/resetPasswordForDispatcher.html";

    private static final String DEFAULT_PASSWORD_EMAIL_FOR_DRIVER_FILE = "classpath:text/email/resetPasswordForDriver.html";

    private static final String DEFAULT_PASSWORD_EMAIL_FOR_CONTRACTOR_FILE = "classpath:text/email/resetPasswordForContractor.html";

    private static final String DEFAULT_EMAIL_CONFIRMATION_FILE = "classpath:text/email/emailConfirmation.html";

    private static final String DEFAULT_EMAIL_TWO_FA_FILE = "classpath:text/email/twoFactor.html";

    private static final String DEFAULT_PASSWORD_EMAIL_FOR_AUTOSERVICE_FILE = "classpath:text/email/resetPasswordForAutoservice.html";

    private static final String DEFAULT_PASSWORD_EMAIL_FOR_AUTOSERVICE_TA_FILE = "classpath:text/email/resetPasswordForAutoserviceTA.html";

    private static final String DEFAULT_PASSWORD_EMAIL_V2_FILE = "classpath:text/email/resetPasswordV2.html";

    @Setter(AccessLevel.PACKAGE)
    @Value("${text.email.resetPassword:}")
    private String resetPasswordEmailFile;

    @Setter(AccessLevel.PACKAGE)
    @Value("${text.email.resetPasswordForDispatcher:}")
    private String resetPasswordEmailForDispatcherFile;

    @Setter(AccessLevel.PACKAGE)
    @Value("${text.email.resetPasswordForDriver:}")
    private String resetPasswordEmailForDriverFile;

    @Setter(AccessLevel.PACKAGE)
    @Value("${text.email.resetPasswordForContractor:}")
    private String resetPasswordEmailForContractorFile;

    @Setter(AccessLevel.PACKAGE)
    @Value("${text.email.emailConfirmation:}")
    private String emailConfirmationFile;

    @Setter(AccessLevel.PACKAGE)
    @Value("${text.email.twoFactor:}")
    private String twoFactorFile;

    @Setter(AccessLevel.PACKAGE)
    @Value("${text.email.resetPasswordForAutoservice:}")
    private String resetPasswordEmailForAutoserviceFile;

    @Setter(AccessLevel.PACKAGE)
    @Value("${text.email.resetPasswordForAutoserviceTA:}")
    private String resetPasswordEmailForAutoserviceTaFile;

    @Setter(AccessLevel.PACKAGE)
    @Value("${text.email.resetPasswordV2:}")
    private String resetPasswordEmailV2File;
    
    @SneakyThrows(IOException.class)
    @Override
    public String getResetPasswordEmail(UserMessage.Scope scope) {
        if(UserMessage.Scope.DISPATCHER.equals(scope)){
            return readFile(resetPasswordEmailForDispatcherFile, resetPasswordEmailForDispatcherFile, DEFAULT_PASSWORD_EMAIL_FOR_DISPATCHER_FILE);
        }else if(UserMessage.Scope.DRIVER.equals(scope)) {
            return readFile(resetPasswordEmailForDriverFile, resetPasswordEmailForDriverFile, DEFAULT_PASSWORD_EMAIL_FOR_DRIVER_FILE);
        }else if(UserMessage.Scope.CONTRACTOR.equals(scope)){
            return readFile(resetPasswordEmailForContractorFile, resetPasswordEmailForContractorFile, DEFAULT_PASSWORD_EMAIL_FOR_CONTRACTOR_FILE);
        }else if(UserMessage.Scope.AUTOSERVICE.equals(scope)){
            return readFile(resetPasswordEmailForAutoserviceFile, resetPasswordEmailForAutoserviceFile, DEFAULT_PASSWORD_EMAIL_FOR_AUTOSERVICE_FILE);
        }else if(UserMessage.Scope.AUTOSERVICE_TA.equals(scope)){
            return readFile(resetPasswordEmailForAutoserviceTaFile, resetPasswordEmailForAutoserviceTaFile, DEFAULT_PASSWORD_EMAIL_FOR_AUTOSERVICE_TA_FILE);
        } else {
            return readFile(resetPasswordEmailFile, resetPasswordEmailFile, DEFAULT_PASSWORD_EMAIL_FILE);
        }
    }

    @SneakyThrows(IOException.class)
    @Override
    public String getResetPasswordEmailV2() {
        return readFile(resetPasswordEmailV2File, resetPasswordEmailV2File, DEFAULT_PASSWORD_EMAIL_V2_FILE);
    }

    @SneakyThrows(IOException.class)
    @Override
    public String getEmailConfirmation() {
        return readFile(emailConfirmationFile, emailConfirmationFile, DEFAULT_EMAIL_CONFIRMATION_FILE);
    }

    @SneakyThrows(IOException.class)
    @Override
    public String getSecondFactorEmailText() {
        return readFile(twoFactorFile, emailConfirmationFile, DEFAULT_EMAIL_TWO_FA_FILE);
    }

    @NotNull
    private String readFile(String twoFactorFile, String emailConfirmationFile, String defaultEmailTwoFaFile) throws IOException {
        log.debug("Opening file from path " + twoFactorFile);
        var file = getFile(twoFactorFile);
        if (emailConfirmationFile == null) {
            log.debug("File not defined. Opening a default file");
        } else if (file == null || !file.isFile()) {
            log.debug("File " + Paths.get(emailConfirmationFile).toAbsolutePath() + " not found. Opening a default file");
        }
        if (file == null || !file.isFile()) {
            file = getFile(defaultEmailTwoFaFile);
        }
        var stringContent = "";
        try (var fis = new FileInputStream(file)) {
            var rawContent = fis.readAllBytes();
            stringContent = new String(rawContent, StandardCharsets.UTF_8);
        }
        return stringContent;
    }

    private File getFile(String path) {
        if (path != null && path.startsWith("classpath:")) {
            path = path.replace("classpath:", "");
            var resource = getClass().getClassLoader().getResource(path);
            if (resource != null) {
                return new File(resource.getFile());
            }
            return new File(path);
        }
        if (path != null) {
            return new File(path);
        }
        return null;
    }
}
