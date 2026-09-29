import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

const convertParamType = (param: string | null) => {
  if (param === 'true' || param === 'false') {
    return param === 'true';
  }

  if (param && !Number.isNaN(+param)) {
    return +param;
  }

  try {
    if (param && (param.startsWith('{') || param.startsWith('['))) {
      return JSON.parse(param);
    }
  } catch {
    // not valid JSON, return as string
  }

  return param;
};

const getParams = (urlParams: URLSearchParams) => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const params: any = {};

  // @ts-ignore
  for (const key of urlParams.keys()) {
    if (params[key]) {
      continue;
    }

    if (key.endsWith('[]')) {
      params[key.replace('[]', '')] = urlParams.getAll(key).map(convertParamType);
    } else {
      params[key] = convertParamType(urlParams.get(key));
    }
  }

  return params;
};

const useUrl = () => {
  const location = useLocation();

  const [state, setState] = useState(getParams(new URLSearchParams(location.search)));

  useEffect(() => {
    setState(getParams(new URLSearchParams(location.search)));
  }, [location.search]);

  return state;
};

export default useUrl;
