import { useAppStore } from 'ioc';
import { UUID } from 'utils/io-ts';
import { jwtDecode } from 'utils/utils';

export const useToken = (): string | null => {
  const { authStore } = useAppStore();

  return authStore.token;
};

interface UserToken {
  jti: UUID;
}

export const useDecodedToken = (): UserToken => {
  const { authStore } = useAppStore();
  const { token } = authStore;

  return jwtDecode<UserToken>(token);
};
