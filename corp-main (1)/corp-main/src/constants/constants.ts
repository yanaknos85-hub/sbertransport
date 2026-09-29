
import { QueryCache, ReactQueryCacheProvider } from 'react-query';

const queryCache = new QueryCache({
  defaultConfig: {
    queries: {
      suspense: true,
      staleTime: Infinity,
    },
    mutations: {
      throwOnError: true,
    },
  },
});

export { queryCache, ReactQueryCacheProvider };
