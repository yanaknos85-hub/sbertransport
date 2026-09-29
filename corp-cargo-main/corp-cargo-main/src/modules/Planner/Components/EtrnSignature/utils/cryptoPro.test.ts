import {
  createDetachedSignature,
  createHash,
  getUserCertificates,
} from 'crypto-pro-actual-cades-plugin';
import { getCertificates, signFileByCertificate } from './cryptoPro';

jest.mock('crypto-pro-actual-cades-plugin', () => ({
  createDetachedSignature: jest.fn(),
  createHash: jest.fn(),
  getUserCertificates: jest.fn(),
}));

const mockCreateDetachedSignature = createDetachedSignature as jest.MockedFunction<typeof createDetachedSignature>;
const mockCreateHash = createHash as jest.MockedFunction<typeof createHash>;
const mockGetUserCertificates = getUserCertificates as jest.MockedFunction<typeof getUserCertificates>;

describe('cryptoPro utils', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getCertificates', () => {
    const makeCert = (overrides: Record<string, unknown> = {}) => ({
      thumbprint: 'thumbprint',
      subjectName: 'subject',
      issuerName: 'issuer',
      validFrom: new Date(),
      validTo: new Date(),
      isValid: jest.fn(),
      ...overrides,
    });

    it('должен вернуть массив сертификатов с полем active=true для валидных', async () => {
      const cert = makeCert({ isValid: jest.fn().mockResolvedValue(true) });
      mockGetUserCertificates.mockResolvedValue([cert] as never);

      const result = await getCertificates();

      expect(result).toHaveLength(1);
      expect(result[0].active).toBe(true);
      expect(cert.isValid).toHaveBeenCalledTimes(1);
    });

    it('должен вернуть массив сертификатов с полем active=false для невалидных', async () => {
      const cert = makeCert({ isValid: jest.fn().mockResolvedValue(false) });
      mockGetUserCertificates.mockResolvedValue([cert] as never);

      const result = await getCertificates();

      expect(result).toHaveLength(1);
      expect(result[0].active).toBe(false);
    });

    it('должен корректно обработать смешанный список валидных и невалидных сертификатов', async () => {
      const validCert = makeCert({ thumbprint: 'valid', isValid: jest.fn().mockResolvedValue(true) });
      const invalidCert = makeCert({ thumbprint: 'invalid', isValid: jest.fn().mockResolvedValue(false) });
      mockGetUserCertificates.mockResolvedValue([validCert, invalidCert] as never);

      const result = await getCertificates();

      expect(result).toHaveLength(2);
      expect(result[0].active).toBe(true);
      expect(result[1].active).toBe(false);
    });

    it('должен вернуть пустой массив, если getUserCertificates вернул пустой массив', async () => {
      mockGetUserCertificates.mockResolvedValue([] as never);

      const result = await getCertificates();

      expect(result).toEqual([]);
    });

    it('должен передавать resetCache=true в getUserCertificates по умолчанию', async () => {
      mockGetUserCertificates.mockResolvedValue([] as never);

      await getCertificates();

      expect(mockGetUserCertificates).toHaveBeenCalledTimes(1);
      expect(mockGetUserCertificates).toHaveBeenCalledWith(true);
    });

    it('должен передавать resetCache=false в getUserCertificates, если указано', async () => {
      mockGetUserCertificates.mockResolvedValue([] as never);

      await getCertificates(false);

      expect(mockGetUserCertificates).toHaveBeenCalledTimes(1);
      expect(mockGetUserCertificates).toHaveBeenCalledWith(false);
    });

    it('должен пробрасывать ошибку, если getUserCertificates отклоняется', async () => {
      const error = new Error('CSP error');
      mockGetUserCertificates.mockRejectedValue(error);

      await expect(getCertificates()).rejects.toBe(error);
    });
  });

  describe('signFileByCertificate', () => {
    it('должен вызвать createHash с переданным файлом', async () => {
      const file = new ArrayBuffer(8);
      const hash = 'hash-value';
      const signature = 'signature-value';
      mockCreateHash.mockResolvedValue(hash as never);
      mockCreateDetachedSignature.mockResolvedValue(signature);

      await signFileByCertificate('cert-id', file);

      expect(mockCreateHash).toHaveBeenCalledTimes(1);
      expect(mockCreateHash).toHaveBeenCalledWith(file);
    });

    it('должен передавать certId и хэш в createDetachedSignature', async () => {
      const hash = 'computed-hash';
      const signature = 'pkcs7-signature';
      mockCreateHash.mockResolvedValue(hash as never);
      mockCreateDetachedSignature.mockResolvedValue(signature);

      await signFileByCertificate('cert-thumbprint', new ArrayBuffer(4));

      expect(mockCreateDetachedSignature).toHaveBeenCalledTimes(1);
      expect(mockCreateDetachedSignature).toHaveBeenCalledWith('cert-thumbprint', hash);
    });

    it('должен вернуть подпись, полученную из createDetachedSignature', async () => {
      const signature = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA';
      mockCreateHash.mockResolvedValue('hash' as never);
      mockCreateDetachedSignature.mockResolvedValue(signature);

      const result = await signFileByCertificate('cert-id', new ArrayBuffer(8));

      expect(result).toBe(signature);
    });

    it('должен корректно работать со строковым входом', async () => {
      mockCreateHash.mockResolvedValue('hash-for-string' as never);
      mockCreateDetachedSignature.mockResolvedValue('string-signature');

      const result = await signFileByCertificate('cert-id', 'file-content-string');

      expect(mockCreateHash).toHaveBeenCalledWith('file-content-string');
      expect(mockCreateDetachedSignature).toHaveBeenCalledWith('cert-id', 'hash-for-string');
      expect(result).toBe('string-signature');
    });

    it('должен пробрасывать ошибку, если createHash отклоняется', async () => {
      const error = new Error('hash error');
      mockCreateHash.mockRejectedValue(error);

      await expect(signFileByCertificate('cert-id', new ArrayBuffer(8))).rejects.toBe(error);
      expect(mockCreateDetachedSignature).not.toHaveBeenCalled();
    });

    it('должен пробрасывать ошибку, если createDetachedSignature отклоняется', async () => {
      mockCreateHash.mockResolvedValue('hash' as never);
      const error = new Error('sign error');
      mockCreateDetachedSignature.mockRejectedValue(error);

      await expect(signFileByCertificate('cert-id', new ArrayBuffer(8))).rejects.toBe(error);
    });
  });
});
