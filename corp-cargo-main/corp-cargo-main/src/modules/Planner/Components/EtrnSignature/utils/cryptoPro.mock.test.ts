// Мок модуля env-констант: подменяем IS_MOCKED_CRYPTO_PRO=true.
// Импорт signFileByCertificate триггерит загрузку cryptoPro.ts,
// который импортирует IS_MOCKED_CRYPTO_PRO на уровне модуля,
// поэтому jest.mock ниже сработает при первом импорте.
jest.mock('constants/constants.env', () => ({
  IS_MOCKED_CRYPTO_PRO: true,
}));

const mockCreateHash = jest.fn();
const mockCreateDetachedSignature = jest.fn();

jest.mock('crypto-pro-actual-cades-plugin', () => ({
  createHash: (...args: unknown[]) => mockCreateHash(...args),
  createDetachedSignature: (...args: unknown[]) => mockCreateDetachedSignature(...args),
}));

import { signFileByCertificate } from './cryptoPro';

describe('signFileByCertificate (IS_MOCKED_CRYPTO_PRO=true, mock-режим)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('должен вернуть строку-маркер без обращения к плагину', async () => {
    const result = await signFileByCertificate('MOCK_THUMBPRINT_001', '<xml>title</xml>');

    expect(result).toBe('ЭТРН ТИТУЛ 3 ПОДПИСАН');
    expect(mockCreateHash).not.toHaveBeenCalled();
    expect(mockCreateDetachedSignature).not.toHaveBeenCalled();
  });

  it('должен корректно работать со строковым входом', async () => {
    const result = await signFileByCertificate('MOCK_THUMBPRINT_002', 'plain-string');

    expect(result).toBe('ЭТРН ТИТУЛ 3 ПОДПИСАН');
    expect(mockCreateHash).not.toHaveBeenCalled();
  });
});
