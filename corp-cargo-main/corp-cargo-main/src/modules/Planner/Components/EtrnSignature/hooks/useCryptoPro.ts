import { useState, useEffect } from 'react';
import { getSystemInfo } from 'crypto-pro-actual-cades-plugin';
import type { SystemInfo } from 'crypto-pro-actual-cades-plugin';

import { IS_MOCKED_CRYPTO_PRO } from 'constants/constants.env';
import { Messages } from '../constants/CryptoPro';
import type { CertificateExtended } from '../cryptoPro.interface';
import { getCertificates } from '../utils/cryptoPro';
import { MOCK_CERTIFICATES } from '../utils/cryptoPro.mock';

interface Result {
 messages: Messages[] | null;
 certificates: CertificateExtended[] | null;
 addMessage: (message: Messages) => void;
}

const useCryptoPro = (): Result => {
  const [messages, setMessages] = useState<Messages[] | null>(null);
  const [certificates, setCertificates] = useState<CertificateExtended[] | null>(null);

  useEffect(() => {
    // Локальный mock-режим: возвращаем захардкоженный сертификат без обращения
    // к плагину КриптоПро. Включается флагом REACT_APP_MOCKED_CRYPTO_PRO=TRUE
    // в .env (нужен для локальной разработки без USB-токена).
    // При подписании через signFileByCertificate плагин попытается найти
    // mock-th thumbprint в своём хранилище и выдаст ошибку — это ожидаемо,
    // нужно для проверки сквозного UI-пути.
    if (IS_MOCKED_CRYPTO_PRO) {
      setMessages([]);
      setCertificates(MOCK_CERTIFICATES);
      return;
    }

    (async () => {
      try {
        const systemInfo: SystemInfo = await getSystemInfo();
        const _messages = [] as Messages[];
        if (!systemInfo.cspVersion) _messages.push(Messages.NoCspProvider);
        if (!systemInfo.cadesVersion) _messages.push(Messages.NoCadesPlugin);
        setMessages(_messages);

        if (!_messages.length) {
          try {
            const _certificates: CertificateExtended[] = await getCertificates();
            if (_certificates.length > 0 && _certificates.some(cert => cert.active)) setCertificates(_certificates);
            else setMessages([Messages.NoCertificates]);
          } catch (err) {
            console.log(err);
            setMessages([Messages.GetCertificatesError]);
          }
        }
      } catch (err) {
        console.log(err);
        setMessages([Messages.GetSystemInfoError]);
      }
    })();
  }, []);

  const addMessage = (message: Messages) => setMessages([...(messages ?? []), message]);

  return {
    messages,
    certificates,
    addMessage,
  };
};

export { useCryptoPro };
