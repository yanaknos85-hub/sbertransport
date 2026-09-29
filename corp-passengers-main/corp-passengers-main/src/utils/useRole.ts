import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { jwtDecode } from './Misc';

export const useRole = (): string[] => {
  const { authStore } = useAppStoreContext();
  const { token } = authStore;

  return token ? jwtDecode<{ roles: string[] }>(token).roles : [];
};
