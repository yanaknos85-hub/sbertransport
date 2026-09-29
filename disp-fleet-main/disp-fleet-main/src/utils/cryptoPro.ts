import type { Certificate } from 'crypto-pro-actual-cades-plugin';
import { createDetachedSignature, createHash, getUserCertificates } from 'crypto-pro-actual-cades-plugin';
import { CertificateExtended } from 'types/cryptoPro.interface';

/**
 * Получает список сертификатов пользователя
 * @param resetCache - сбросить кэш сертификатов (по умолчанию true)
 * @returns массив расширенных сертификатов с информацией об активности
 */
const getCertificates = (resetCache = true): Promise<CertificateExtended[]> => getUserCertificates(resetCache)
  .then((certs: Certificate[]) => Promise.all(certs.map(async cert => ({
    ...cert,
    active: await cert.isValid(),
  }) as CertificateExtended))
  );

/**
 * Генерирует хэш по ГОСТ Р 34.11-2012 и создает отсоединенную подпись хеша по отпечатку сертификата
 * @param certId - отпечаток сертификата
 * @param file - файл в формате bytearray
 * @returns подпись в формате PKCS#7
 */
const signFileByCertificate = (certId: string, file: ArrayBuffer | string): Promise<string> => createHash(file)
  .then(hash => createDetachedSignature(certId, hash));

export {
  getCertificates,
  signFileByCertificate
};
