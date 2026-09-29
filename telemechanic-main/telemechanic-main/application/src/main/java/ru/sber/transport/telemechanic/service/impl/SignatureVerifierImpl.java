package ru.sber.transport.telemechanic.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.exception.SignatureNotValidException;
import ru.sber.transport.telemechanic.service.SignatureVerifier;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.Security;
import java.security.cert.CertificateException;

@Service
@Slf4j
public class SignatureVerifierImpl implements SignatureVerifier {
    @Override
    public void verify(byte[] fileAsBytes, String signatureString, String userFullName) {
        var certHolder = verifySignatureAndExtractCertificate(fileAsBytes, signatureString);
        var employeeFullName = userFullName.replaceAll("\\s", "").toUpperCase();
        var signer = extractNameFromSignature(certHolder).replaceAll("\\s", "").toUpperCase();
        if (!signer.equals(employeeFullName)) {
            throw new SignatureNotValidException("Подпись не принадлежит текущему пользователю. Убедитесь пожалуйста, что подписываете своей подписью");
        }
    }

    private static X509CertificateHolder verifySignatureAndExtractCertificate(byte[] fileAsBytes, String signatureString) {
        try {
            var signature = Base64.decode(signatureString);
            var signedData = new CMSSignedData(new CMSProcessableByteArray(fileAsBytes), signature);
            var certStoreInSing = signedData.getCertificates();
            var signer = signedData.getSignerInfos().getSigners().iterator().next();
            var certCollection = certStoreInSing.getMatches(signer.getSID());
            var certIt = certCollection.iterator();
            var certHolder = (X509CertificateHolder) certIt.next();
            var certificate = new JcaX509CertificateConverter().getCertificate(certHolder);
            var checkResult = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificate));
            if (!checkResult) {
                throw new SignatureNotValidException("Подпись не валидна, отправка не возможна");
            }
            return certHolder;
        } catch (CMSException | CertificateException | OperatorCreationException ex) {
            throw new SignatureNotValidException("При подписании использован не валидный сертификат");
        }
    }

    @NotNull
    private static String extractNameFromSignature(X509CertificateHolder certHolder) {
        try {
            var x509Certificate = new JcaX509CertificateConverter().getCertificate(certHolder);
            var name = x509Certificate.getSubjectX500Principal().getName();
            var ldapDN = new LdapName(name);
            return ldapDN.getRdns().stream()
                    .filter(rdn -> "CN".equalsIgnoreCase(rdn.getType()))
                    .map(Rdn::getValue)
                    .findFirst()
                    .map(Object::toString)
                    .orElse("");
        } catch (CertificateException | InvalidNameException ex) {
            throw new SignatureNotValidException("Не полученны данные о подписанте из открепленной подписи");
        }

    }

    @PostConstruct
    public void initProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }
}
