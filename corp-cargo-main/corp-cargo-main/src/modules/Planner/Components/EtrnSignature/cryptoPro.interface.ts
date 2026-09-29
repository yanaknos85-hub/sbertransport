import type { Certificate } from 'crypto-pro-actual-cades-plugin';

export interface CertificateExtended extends Certificate {
  active: boolean;
}
