import { useCallback, useEffect, useState } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

export interface TRouteParamSub {
  initValue?: string;
  options?: string[];
  isStrict?: boolean;
}

export const useRouteParamSub = (name: string, config: TRouteParamSub = {}): [string, (value: string) => void] => {
  const {
    options, initValue, isStrict,
  } = config;

  const match = useRouteMatch<Record<string, string>>();
  const history = useHistory();

  const urlValue = match.params[name];
  const _initValue = initValue || options?.[0] || '';

  const [param, setParam] = useState<string>(_initValue);

  const updateParam = useCallback(
    (value: string): void => {
      const pathIndex = match.path.split('/').findIndex(x => x === `:${name}`);

      const urlArr = match.url.split('/');
      urlArr[pathIndex] = value;

      history.push(urlArr.join('/'));
    },
    [history, match.path, match.url, name]
  );

  useEffect(() => {
    if (options && isStrict) {
      if (options.includes(urlValue)) {
        setParam(urlValue);
      } else {
        updateParam(_initValue);
      }
    } else {
      setParam(urlValue ?? _initValue);
    }
  }, [urlValue, name, _initValue, isStrict, options, updateParam]);

  return [param, updateParam];
};
