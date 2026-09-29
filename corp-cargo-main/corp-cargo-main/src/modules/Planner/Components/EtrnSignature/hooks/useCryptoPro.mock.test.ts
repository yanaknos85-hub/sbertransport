// Мок модуля env-констант: подменяем IS_MOCKED_CRYPTO_PRO=true.
// Хук useCryptoPro импортирует IS_MOCKED_CRYPTO_PRO на уровне модуля,
// поэтому jest.mock ниже сработает при первом импорте useCryptoPro.
jest.mock('constants/constants.env', () => ({
  IS_MOCKED_CRYPTO_PRO: true,
}));

const mockGetSystemInfo = jest.fn();

jest.mock('crypto-pro-actual-cades-plugin', () => ({
  getSystemInfo: (...args: unknown[]) => mockGetSystemInfo(...args),
}));

import { renderHook, act } from '@testing-library/react-hooks';
import { useCryptoPro } from './useCryptoPro';
import { MOCK_CERTIFICATES } from '../utils/cryptoPro.mock';

describe('useCryptoPro (IS_MOCKED_CRYPTO_PRO=true, mock-режим)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockGetSystemInfo.mockReset();
  });

  it('должен сразу выставить пустой массив сообщений и мок-сертификаты', () => {
    const { result } = renderHook(() => useCryptoPro());

    expect(result.current.messages).toEqual([]);
    expect(result.current.certificates).toEqual(MOCK_CERTIFICATES);
  });

  it('НЕ должен вызывать getSystemInfo из плагина КриптоПро', () => {
    renderHook(() => useCryptoPro());

    expect(mockGetSystemInfo).not.toHaveBeenCalled();
  });

  it('addMessage должен работать так же, как в обычном режиме', () => {
    const { result } = renderHook(() => useCryptoPro());

    expect(result.current.messages).toEqual([]);

    act(() => {
      result.current.addMessage('signingError' as any);
    });

    expect(result.current.messages).toEqual(['signingError']);
  });
});
