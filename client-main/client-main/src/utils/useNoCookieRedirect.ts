import { useHistory } from 'react-router-dom';
import Cookies from 'universal-cookie';

const useNoCookieRedirect = (name: string): void => {
  const history = useHistory();
  const cookies = new Cookies();
  const token = cookies.get(name);

  if (!token) {
    history.push('/');
  }
};

export default useNoCookieRedirect;
