import { useMemo } from 'react';
import { useLocation } from 'react-router-dom';
import {
  ORDER_EXECUTION_CARGO,
  ORDER_EXECUTION_CARGO_TEMPLATE,
  REGISTRY_CARGO_ORDERS,
  REGISTRY_CARGO_ROUTES,
  MULTI_LOGISTICS
} from 'constants/constants.routes';

export interface ICargoRoute {
  /** true — страница раздела Исполнение заявок(Монитор) */
  isCargoOrderExecution: boolean;
  /** true — страница раздела Реестр(заявки) */
  isCargoOrders: boolean;
  /** true — страница раздела Логистика(Планировщик) */
  isCargoMultiLogistics: boolean;
  /** true — страница раздела Реестр(маршруты) */
  isCargoRoutes: boolean;
  /** true — любой из грузовых разделов */
  isCargoAny: boolean;
  /** true — таб «Подписание ЭТрН» в разделе Логистика, не зависим от планировщика */
  isEtrnTab: boolean;
}

export const useCargoRoute = (): ICargoRoute => {
  const location = useLocation();
  const pathname = location?.pathname ?? '';
  const search = location?.search ?? '';

  return useMemo(() => {
    const isCargoOrderExecution = pathname.includes(ORDER_EXECUTION_CARGO)
      && !pathname.includes(ORDER_EXECUTION_CARGO_TEMPLATE);

    const isCargoOrders = pathname.includes(REGISTRY_CARGO_ORDERS);
    const isCargoMultiLogistics = pathname.includes(MULTI_LOGISTICS);
    const isCargoRoutes = pathname.includes(REGISTRY_CARGO_ROUTES);

    const isCargoAny = isCargoOrderExecution || isCargoOrders || isCargoMultiLogistics || isCargoRoutes;

    const isEtrnTab = isCargoMultiLogistics
      && new URLSearchParams(search).get('tab') === 'etrn';

    return {
      isCargoOrderExecution,
      isCargoOrders,
      isCargoMultiLogistics,
      isCargoRoutes,
      isCargoAny,
      isEtrnTab,
    };
  }, [pathname, search]);
};
