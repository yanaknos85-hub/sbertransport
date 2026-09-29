import { useLocation } from 'react-router-dom';
import qs from 'qs';

export const useQueryParams = (): qs.ParsedQs => {
  const location = useLocation();
  return qs.parse(location.search, { ignoreQueryPrefix: true });
};
