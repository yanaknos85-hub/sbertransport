// type-only импорт: в Node-среде (Jest) модуль crypto-pro-actual-cades-plugin
// экспортирует Certificate только как тип (привязка к плагину происходит в браузере).
// runtime-импорт здесь бы упал при загрузке модуля в тестах.
import type { Certificate } from 'crypto-pro-actual-cades-plugin';

import type { CertificateExtended } from '../cryptoPro.interface';

/**
 * Захардкоженный список сертификатов для локального тестирования ЭТрН без флешки.
 *
 * Используется, когда в .env выставлен флаг `REACT_APP_MOCKED_CRYPTO_PRO=TRUE`.
 * Активация — в `useCryptoPro.ts`: при включённом флаге реальные вызовы
 * `getSystemInfo` / `getCertificates` плагина КриптоПро не выполняются,
 * хук сразу возвращает `MOCK_CERTIFICATES`.
 *
 * ⚠️ Только для dev/staging. В проде флаг должен быть выключен.
 */

const createMockCertificate = (params: {
  name: string;
  subjectName: string;
  thumbprint: string;
}): CertificateExtended => ({
  // Поля реального класса Certificate (см. crypto-pro-actual-cades-plugin).
  name: params.name,
  issuerName: 'Test Crypto-Pro CA',
  subjectName: params.subjectName,
  thumbprint: params.thumbprint,
  validFrom: '2026-01-01T00:00:00Z',
  validTo: '2027-12-31T23:59:59Z',
  // Расширение проекта.
  active: true,
  // Заглушки методов Certificate — в мок-режиме они не вызываются,
  // но TypeScript требует их наличия для соответствия типу.
  isValid: () => Promise.resolve(true),
} as unknown as CertificateExtended);

const mockCertificates: CertificateExtended[] = [
  createMockCertificate({
    name: 'Иванов Иван Иванович (тестовый УКЭП)',
    subjectName: 'CN=Ivanov Ivan Ivanovich',
    thumbprint: 'MOCK_THUMBPRINT_001',
  }),
  createMockCertificate({
    name: 'Олегов Олег Олегович (тестовый УКЭП)',
    subjectName: 'CN=Olegov Oleg Olegovich',
    thumbprint: 'MOCK_THUMBPRINT_002',
  }),
];

// Привязываем моки к прототипу реального класса, чтобы при `instanceof Certificate`
// в UI-коде (если такое появится) поведение не ломалось.
// В Node-среде (Jest) класс Certificate из плагина КриптоПро не загружен —
// пропускаем привязку прототипа, чтобы модуль можно было импортировать в тестах.
try {
  // eslint-disable-next-line @typescript-eslint/no-var-requires, global-require
  const CryptoProPlugin = require('crypto-pro-actual-cades-plugin');
  const CertificateCtor: typeof Certificate | undefined = CryptoProPlugin?.Certificate;
  if (CertificateCtor?.prototype) {
    mockCertificates.forEach(cert => Object.setPrototypeOf(cert, CertificateCtor.prototype));
  }
} catch {
  // Плагин недоступен (Node / тесты) — продолжаем без instanceof.
}

export const MOCK_CERTIFICATES: CertificateExtended[] = mockCertificates;
