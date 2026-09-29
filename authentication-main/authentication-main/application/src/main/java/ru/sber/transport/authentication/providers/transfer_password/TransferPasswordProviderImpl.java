package ru.sber.transport.authentication.providers.transfer_password;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.authentication.business.providers.TransferPasswordProvider;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class TransferPasswordProviderImpl implements TransferPasswordProvider {

    @Override
    public char[] generateTransferPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefhijkmnprstuvwxyz23456789!@#$%^*_=.?)";
        char[] otp = new char[length];
        SecureRandom random = new SecureRandom();

        for(int i = 0; i < length; ++i) {
            otp[i] = chars.charAt(random.nextInt(chars.length()));
        }

        return otp;
    }
}
