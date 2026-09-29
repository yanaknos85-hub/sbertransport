/* eslint-disable @typescript-eslint/no-explicit-any */
import { renderHook } from '@testing-library/react-hooks';

import { useRole } from '../useRole';

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(),
}));

import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

const mockUseAppStoreContext = useAppStoreContext as jest.Mock;

// Структура JWT: header.payload.signature, где payload = b64EncodeUnicode(JSON.stringify(...))
const b64EncodeUnicode = (str: string): string => {
  // Минимальная реализация, повторяющая src/utils/Misc.ts.
  const encoded = btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (_, p1) => String.fromCharCode(parseInt(p1, 16))));

  return encoded.replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
};

const makeJwt = (payload: object): string => {
  const header = b64EncodeUnicode(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = b64EncodeUnicode(JSON.stringify(payload));

  return `${header}.${body}.signature`;
};

const setToken = (token: string | null | undefined) => {
  mockUseAppStoreContext.mockReturnValue({
    authStore: { token },
  });
};

describe('useRole', () => {
  it('возвращает пустой массив, когда token === undefined', () => {
    setToken(undefined);

    const { result } = renderHook(() => useRole());

    expect(result.current).toEqual([]);
  });

  it('возвращает пустой массив, когда token === null', () => {
    setToken(null);

    const { result } = renderHook(() => useRole());

    expect(result.current).toEqual([]);
  });

  it('возвращает роли из JWT-токена', () => {
    const token = makeJwt({ roles: ['ROLE_ADMIN', 'ROLE_USER'] });

    setToken(token);

    const { result } = renderHook(() => useRole());

    expect(result.current).toEqual(['ROLE_ADMIN', 'ROLE_USER']);
  });

  it('возвращает обновлённый список ролей при изменении токена', () => {
    const token1 = makeJwt({ roles: ['ROLE_A'] });
    setToken(token1);

    const { result, rerender } = renderHook(() => useRole());

    expect(result.current).toEqual(['ROLE_A']);

    const token2 = makeJwt({ roles: ['ROLE_B', 'ROLE_C'] });
    setToken(token2);
    rerender();

    expect(result.current).toEqual(['ROLE_B', 'ROLE_C']);
  });
});
