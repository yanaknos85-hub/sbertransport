import { useState, useEffect } from 'react';
import { getSystemInfo } from 'crypto-pro-actual-cades-plugin';
import type { SystemInfo } from 'crypto-pro-actual-cades-plugin';

import { getCertificates } from 'utils/cryptoPro';
import { Messages } from 'constants/cryptoPro';
import { CertificateExtended } from 'types/cryptoPro.interface';

interface Result {
  messages: Messages[] | null;
  certificates: CertificateExtended[] | null;
  addMessage: (message: Messages) => void;
}

const useCryptoPro = (): Result => {
  const [messages, setMessages] = useState<Messages[] | null>(null);
  const [certificates, setCertificates] = useState<CertificateExtended[] | null>(null);

  useEffect(() => {
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
            setMessages([Messages.GetCertificatesError]);
          }
        }
      } catch (err) {
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
