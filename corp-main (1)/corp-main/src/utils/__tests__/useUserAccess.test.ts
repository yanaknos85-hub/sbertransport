/* eslint-disable @typescript-eslint/no-explicit-any */
import { renderHook } from '@testing-library/react-hooks';
import { AcceptableUserRoles } from 'constants/constants.app';

import { useVerifyUserAccess } from '../useUserAccess';

jest.mock('../useToken', () => ({
  useToken: jest.fn(),
}));

import { useToken } from '../useToken';

const mockUseToken = useToken as jest.Mock;

const b64EncodeUnicode = (str: string): string => btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (_, p1) => String.fromCharCode(parseInt(p1, 16))));

const makeJwt = (payload: object): string => {
  const header = b64EncodeUnicode(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = b64EncodeUnicode(JSON.stringify(payload));

  return `${header}.${body}.signature`;
};

const setToken = (token: string | null) => {
  mockUseToken.mockReturnValue(token);
};

describe('useVerifyUserAccess', () => {
  it('возвращает false, когда token === null', () => {
    setToken(null);

    const { result } = renderHook(() => useVerifyUserAccess());

    expect(result.current).toBe(false);
  });

  it('возвращает false, когда в токене нет ни одной роли из AcceptableUserRoles', () => {
    setToken(makeJwt({ roles: ['ROLE_UNKNOWN', 'ROLE_RANDOM'] }));

    const { result } = renderHook(() => useVerifyUserAccess());

    expect(result.current).toBe(false);
  });

  it('возвращает true, когда в токене есть хотя бы одна роль из AcceptableUserRoles', () => {
    setToken(makeJwt({ roles: ['ROLE_UNKNOWN', AcceptableUserRoles[0]] }));

    const { result } = renderHook(() => useVerifyUserAccess());

    expect(result.current).toBe(true);
  });

  it('возвращает true для первой роли из списка AcceptableUserRoles', () => {
    setToken(makeJwt({ roles: [AcceptableUserRoles[0]] }));

    const { result } = renderHook(() => useVerifyUserAccess());

    expect(result.current).toBe(true);
  });

  it('возвращает true для последней роли из списка AcceptableUserRoles', () => {
    const last = AcceptableUserRoles[AcceptableUserRoles.length - 1];

    setToken(makeJwt({ roles: [last] }));

    const { result } = renderHook(() => useVerifyUserAccess());

    expect(result.current).toBe(true);
  });

  it('возвращает true для нескольких ролей из AcceptableUserRoles', () => {
    setToken(makeJwt({
      roles: [
        AcceptableUserRoles[0],
        AcceptableUserRoles[1],
        AcceptableUserRoles[2],
      ],
    }));

    const { result } = renderHook(() => useVerifyUserAccess());

    expect(result.current).toBe(true);
  });

  it('возвращает обновлённое значение при изменении токена', () => {
    setToken(null);

    const { result, rerender } = renderHook(() => useVerifyUserAccess());

    expect(result.current).toBe(false);

    setToken(makeJwt({ roles: [AcceptableUserRoles[0]] }));
    rerender();

    expect(result.current).toBe(true);
  });
});
