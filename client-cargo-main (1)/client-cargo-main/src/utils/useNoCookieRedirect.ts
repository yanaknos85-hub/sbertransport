import { useHistory as History } from '@sber-sbertransport/mf-core';
import Cookies from 'universal-cookie';

const useNoCookieRedirect = (name: string): void => {
  const history = History();
  const cookies = new Cookies();
  const token = cookies.get(name);

  if (!token) {
    history.push('/');
  }
};

export default useNoCookieRedirect;
