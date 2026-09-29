import { useCallback, useMemo } from 'react';
import { useLocation, useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';

/**
 *
 * @param steps массив ключей шагов в виде строк. Эти ключи записываются в url
 * @returns аналогично useState
 */
export const useStep = (steps: string[]): [number, (step: number) => void] => {
  const { replace } = useHistory();
  const { pathname: currentPath } = useLocation();
  const { path } = useRouteMatch();

  const step = useMemo(() => steps.findIndex(x => x === currentPath.replace(`${path}/`, '').split('/')[0]), [
    steps,
    currentPath,
    path,
  ]);

  const setStep = useCallback(
    (val: number) => {
      replace(`${path}/${steps[val]}`);
    },
    [steps, replace, path]
  );

  return [step, setStep];
};
