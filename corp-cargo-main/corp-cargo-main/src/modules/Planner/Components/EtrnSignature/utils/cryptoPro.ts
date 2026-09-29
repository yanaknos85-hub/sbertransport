import type { Certificate } from 'crypto-pro-actual-cades-plugin';
import { createDetachedSignature, createHash, getUserCertificates } from 'crypto-pro-actual-cades-plugin';

import { IS_MOCKED_CRYPTO_PRO } from 'constants/constants.env';
import { CertificateExtended } from '../cryptoPro.interface';

const getCertificates = (resetCache = true): Promise<CertificateExtended[]> => getUserCertificates(resetCache)
  .then((certs: Certificate[]) => Promise.all(certs.map(async cert => ({
    ...cert,
    active: await cert.isValid(),
  }) as CertificateExtended))
  );

/**
 * Генерирует хэш по ГОСТ Р 34.11-2012 и создает отсоединенную подпись хеша по отпечатку сертификата.
 *
 * В mock-режиме (`IS_MOCKED_CRYPTO_PRO=true`, см. .env → `REACT_APP_MOCKED_CRYPTO_PRO`)
 * плагин КриптоПро не вызывается: возвращается предсказуемая заглушка-строка
 * `'ЭТРН ТИТУЛ 3 ПОДПИСАН'`, чтобы было видно в payload запроса / логах бэкенда,
 * что подписание тестовое. Это позволяет пройти сквозной UI-путь подписания
 * (выбор мок-сертификата → POST на бэкенд через `useSendEtrn`) без USB-токена,
 * который нужен только для локальной разработки и проверки сквозного сценария.
 *
 * @param certId - отпечаток сертификата
 * @param file - файл в формате bytearray
 * @returns подпись в формате PKCS#7 (или строка-маркер `'ЭТРН ТИТУЛ 3 ПОДПИСАН'` в mock-режиме)
 */
const signFileByCertificate = (certId: string, file: ArrayBuffer | string): Promise<string> => {
  if (IS_MOCKED_CRYPTO_PRO) {
    return Promise.resolve('ЭТРН ТИТУЛ 3 ПОДПИСАН');
  }

  return createHash(file)
    .then(hash => createDetachedSignature(certId, hash));
};

export {
  getCertificates,
  signFileByCertificate
};
