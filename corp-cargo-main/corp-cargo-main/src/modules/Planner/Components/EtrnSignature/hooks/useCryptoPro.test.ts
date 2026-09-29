import { renderHook, act } from '@testing-library/react-hooks';
import { Messages } from '../constants/CryptoPro';
import type { CertificateExtended } from '../cryptoPro.interface';

// ========================
//        Моки
// ========================

jest.mock('crypto-pro-actual-cades-plugin', () => ({
  getSystemInfo: jest.fn(),
}));

jest.mock('../utils/cryptoPro', () => ({
  getCertificates: jest.fn(),
}));

jest.mock('react', () => ({
  ...jest.requireActual('react'),
}));

import { getSystemInfo } from 'crypto-pro-actual-cades-plugin';
import { getCertificates } from '../utils/cryptoPro';
import { useCryptoPro } from './useCryptoPro';

const mockGetSystemInfo = getSystemInfo as jest.Mock;
const mockGetCertificates = getCertificates as jest.Mock;

// ========================
//  Тестовые данные
// ========================

const makeSystemInfo = (overrides: Partial<{ cspVersion: string; cadesVersion: string }> = {}) => ({
  cspVersion: '5.0',
  cadesVersion: '2.0',
  ...overrides,
});

const makeCert = (overrides: Partial<CertificateExtended> = {}): CertificateExtended => ({
  name: 'Test',
  issuerName: 'Issuer',
  subjectName: 'Subject',
  thumbprint: 'thumb-1',
  validFrom: '2026-01-01T00:00:00Z',
  validTo: '2026-12-31T23:59:00Z',
  active: true,
  ...overrides,
} as CertificateExtended);

// Подавляем console.log от catch-блоков в хуке
let consoleLogSpy: jest.SpyInstance;
beforeEach(() => {
  consoleLogSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
});

afterEach(() => {
  consoleLogSpy.mockRestore();
  jest.clearAllMocks();
});

// ========================
//      Тесты
// ========================

describe('useCryptoPro', () => {
  describe('начальное состояние', () => {
    it('должен вернуть messages=null и certificates=null до резолва getSystemInfo', () => {
      // getSystemInfo ещё не зарезолвился
      mockGetSystemInfo.mockImplementation(() => new Promise(() => {}));

      const { result } = renderHook(() => useCryptoPro());

      // До резолва
      expect(result.current.messages).toBeNull();
      expect(result.current.certificates).toBeNull();
      expect(typeof result.current.addMessage).toBe('function');
    });
  });

  describe('getSystemInfo', () => {
    it('должен вызвать getSystemInfo при mount', async () => {
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo());
      mockGetCertificates.mockResolvedValue([makeCert()]);

      const { waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(mockGetSystemInfo).toHaveBeenCalledTimes(1);
    });

    it('должен выставить GetSystemInfoError если getSystemInfo бросает', async () => {
      mockGetSystemInfo.mockRejectedValue(new Error('CSP not found'));

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(result.current.messages).toEqual([Messages.GetSystemInfoError]);
      expect(result.current.certificates).toBeNull();
      expect(consoleLogSpy).toHaveBeenCalled();
    });

    it('должен НЕ вызывать getCertificates если getSystemInfo упал', async () => {
      mockGetSystemInfo.mockRejectedValue(new Error('CSP not found'));

      const { waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(mockGetCertificates).not.toHaveBeenCalled();
    });
  });

  describe('проверка CSP/CADES версий', () => {
    it('должен выставить NoCspProvider если cspVersion пустой', async () => {
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo({ cspVersion: '' }));
      mockGetCertificates.mockResolvedValue([makeCert()]);

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(result.current.messages).toEqual([Messages.NoCspProvider]);
      // certificates не должны быть установлены, потому что есть ошибки
      expect(result.current.certificates).toBeNull();
      // getCertificates не вызывается, если есть ошибки инициализации
      expect(mockGetCertificates).not.toHaveBeenCalled();
    });

    it('должен выставить NoCadesPlugin если cadesVersion пустой', async () => {
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo({ cadesVersion: '' }));
      mockGetCertificates.mockResolvedValue([makeCert()]);

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(result.current.messages).toEqual([Messages.NoCadesPlugin]);
      expect(result.current.certificates).toBeNull();
      expect(mockGetCertificates).not.toHaveBeenCalled();
    });

    it('должен выставить обе ошибки если обе версии пустые', async () => {
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo({ cspVersion: '', cadesVersion: '' }));

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(result.current.messages).toEqual([Messages.NoCspProvider, Messages.NoCadesPlugin]);
      expect(result.current.certificates).toBeNull();
      expect(mockGetCertificates).not.toHaveBeenCalled();
    });
  });

  describe('getCertificates (успешное окружение)', () => {
    it('должен выставить certificates когда есть хотя бы один активный', async () => {
      const certs = [makeCert({ thumbprint: 'thumb-1', active: true })];
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo());
      mockGetCertificates.mockResolvedValue(certs);

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(result.current.certificates).toEqual(certs);
      expect(result.current.messages).toEqual([]);
    });

    it('должен НЕ устанавливать certificates если нет ни одного активного', async () => {
      const certs = [makeCert({ active: false })];
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo());
      mockGetCertificates.mockResolvedValue(certs);

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(result.current.certificates).toBeNull();
      expect(result.current.messages).toEqual([Messages.NoCertificates]);
    });

    it('должен НЕ устанавливать certificates если список пуст', async () => {
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo());
      mockGetCertificates.mockResolvedValue([]);

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(result.current.certificates).toBeNull();
      expect(result.current.messages).toEqual([Messages.NoCertificates]);
    });

    it('должен выставить GetCertificatesError если getCertificates бросает', async () => {
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo());
      mockGetCertificates.mockRejectedValue(new Error('Cert error'));

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(result.current.certificates).toBeNull();
      expect(result.current.messages).toEqual([Messages.GetCertificatesError]);
      expect(consoleLogSpy).toHaveBeenCalled();
    });

    it('должен устанавливать certificates когда среди многих есть хотя бы один active', async () => {
      const certs = [
        makeCert({ thumbprint: 'thumb-1', active: false }),
        makeCert({ thumbprint: 'thumb-2', active: true }),
        makeCert({ thumbprint: 'thumb-3', active: false }),
      ];
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo());
      mockGetCertificates.mockResolvedValue(certs);

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      expect(result.current.certificates).toEqual(certs);
      expect(result.current.messages).toEqual([]);
    });
  });

  describe('addMessage', () => {
    it('должен добавлять сообщение к пустому массиву', async () => {
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo());
      mockGetCertificates.mockResolvedValue([makeCert()]);

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      act(() => {
        result.current.addMessage(Messages.SigningError);
      });

      expect(result.current.messages).toEqual([Messages.SigningError]);
    });

    it('должен добавлять сообщение к существующему массиву', async () => {
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo());
      mockGetCertificates.mockResolvedValue([makeCert()]);

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      // initial state: []
      act(() => {
        result.current.addMessage(Messages.SigningError);
      });

      act(() => {
        result.current.addMessage(Messages.NoCertificates);
      });

      expect(result.current.messages).toEqual([
        Messages.SigningError,
        Messages.NoCertificates,
      ]);
    });

    it('должен корректно работать когда messages === null', () => {
      // Промис не резолвится — messages остаётся null
      mockGetSystemInfo.mockImplementation(() => new Promise(() => {}));

      const { result } = renderHook(() => useCryptoPro());

      // messages === null на старте
      expect(result.current.messages).toBeNull();

      act(() => {
        result.current.addMessage(Messages.SigningError);
      });

      // addMessage работает через fallback `(messages ?? [])`
      expect(result.current.messages).toEqual([Messages.SigningError]);
    });

    it('должен корректно добавлять сообщение когда messages === [] (пустой массив)', async () => {
      // getSystemInfo вернёт валидное окружение, но сертификаты не найдены → messages=[NoCertificates]
      mockGetSystemInfo.mockResolvedValue(makeSystemInfo());
      mockGetCertificates.mockResolvedValue([]);

      const { result, waitForNextUpdate } = renderHook(() => useCryptoPro());
      await waitForNextUpdate();

      // Сначала messages = [NoCertificates]
      expect(result.current.messages).toEqual([Messages.NoCertificates]);

      act(() => {
        result.current.addMessage(Messages.SigningError);
      });

      // Теперь messages = [NoCertificates, SigningError]
      expect(result.current.messages).toEqual([
        Messages.NoCertificates,
        Messages.SigningError,
      ]);
    });
  });
});
