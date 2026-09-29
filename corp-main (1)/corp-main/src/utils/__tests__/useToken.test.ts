/* eslint-disable @typescript-eslint/no-explicit-any */
import { renderHook } from '@testing-library/react-hooks';

import { useToken, tokenCookieExpiration } from '../useToken';

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(),
}));

import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

const mockUseAppStoreContext = useAppStoreContext as jest.Mock;

const b64EncodeUnicode = (str: string): string => btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (_, p1) => String.fromCharCode(parseInt(p1, 16))));

const makeJwt = (payload: object): string => {
  const header = b64EncodeUnicode(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = b64EncodeUnicode(JSON.stringify(payload));

  return `${header}.${body}.signature`;
};

const setToken = (token: string | null) => {
  mockUseAppStoreContext.mockReturnValue({
    authStore: { token },
  });
};

describe('useToken', () => {
  it('возвращает null, когда token === null', () => {
    setToken(null);

    const { result } = renderHook(() => useToken());

    expect(result.current).toBeNull();
  });

  it('возвращает строку токена из стора', () => {
    setToken('abc.def.ghi');

    const { result } = renderHook(() => useToken());

    expect(result.current).toBe('abc.def.ghi');
  });
});

describe('tokenCookieExpiration', () => {
  let cookieSetter: jest.Mock;

  beforeEach(() => {
    cookieSetter = jest.fn();
    // jsdom поддерживает setter document.cookie — перехватываем его,
    // чтобы проверить, что в него передаётся и отсеять особенности jsdom
    // (он не сохраняет expires/path, но setter всё равно вызывается).
    Object.defineProperty(document, 'cookie', {
      configurable: true,
      get: () => '',
      set: value => cookieSetter(value),
    });
  });

  afterAll(() => {
    Object.defineProperty(document, 'cookie', {
      configurable: true,
      writable: true,
      value: '',
    });
  });

  it('возвращает exp - iat - 30 для токена', () => {
    const token = makeJwt({ iat: 1000, exp: 2000 });

    // exp - iat - 30 = 2000 - 1000 - 30 = 970
    expect(tokenCookieExpiration(token, false)).toBe(970);
  });

  it('не пишет cookie при isSigma=false', () => {
    const token = makeJwt({ iat: 0, exp: 1000 });

    tokenCookieExpiration(token, false);

    expect(cookieSetter).not.toHaveBeenCalled();
  });

  it('пишет cookie с token при isSigma=true (по умолчанию)', () => {
    const token = makeJwt({ iat: 0, exp: 1000 });

    // По коду функции isSigma по умолчанию = !!1 = true.
    tokenCookieExpiration(token);

    expect(cookieSetter).toHaveBeenCalledTimes(1);
    const cookieValue = cookieSetter.mock.calls[0][0];
    expect(cookieValue).toContain(`token=${token}`);
    expect(cookieValue).toContain('expires=');
    expect(cookieValue).toContain('path=/');
  });

  it('пишет cookie при явном isSigma=true', () => {
    const token = makeJwt({ iat: 0, exp: 1000 });

    tokenCookieExpiration(token, true);

    expect(cookieSetter).toHaveBeenCalledTimes(1);
  });
});
