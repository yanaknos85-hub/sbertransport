import qs from 'qs';
import { useLocation } from 'react-router-dom';

export const useQueryParams = (): qs.ParsedQs => {
  const location = useLocation();
  return qs.parse(location.search, { ignoreQueryPrefix: true });
};
