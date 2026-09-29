import { useCallback } from 'react';
import { useParams, useRouteMatch, useHistory } from 'react-router-dom';

/**
 * Хук для роутинга при использовании табов. В примере ниже в url будет заменятсья параметр :type на нужный
 * @param paramName - имя переменной для таба в url. По умолчанию 'tab'
 * @returns [tab, setTab]
 * @example
 * const [tab, setTab] = useTab<TripTypes>('type');
 *
 * <Tabs
 *   activeKey={tab}
 *   onChange={setTab}
 * />
 */
export const useTab = <TTab extends string>(paramName = 'tab'): [TTab, (tab: string) => void] => {
  const { [paramName]: tab } = useParams<Record<string, TTab>>();
  const { path, params } = useRouteMatch();
  const { replace } = useHistory();

  const setTab = useCallback((value: string) => {
    // Заменяем нужный параметр новым значением
    let newPath = path.replace(`:${paramName}`, value);

    // Возвращаем значения всех остальных параметров
    Object.entries(params).map(([key, val]) => {
      newPath = newPath.replace(`:${key}`, val);
    });

    replace(newPath);
  }, [replace, path, params, paramName]);

  return [tab, setTab];
};
