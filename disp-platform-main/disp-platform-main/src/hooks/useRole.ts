import { useAppStore } from 'ioc';
import { jwtDecode } from 'utils/utils';

export const useRole = (): string[] => {
  const { authStore } = useAppStore();
  const { token } = authStore;

  return token ? jwtDecode<{ roles: string[] }>(token).roles : [];
};
