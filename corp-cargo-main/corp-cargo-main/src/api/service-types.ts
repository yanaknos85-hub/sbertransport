import type { QueryConfig } from 'react-query';

import { useAPI } from 'api';
import type { APIQueryResult } from 'api';
import type { ServiceType } from 'stores/ServiceTypes/ServiceTypes.interface';

const SERVICE_TYPE_KEY = 'serviceTypes';

declare module 'api' {
  interface Cache {
    serviceTypes: { key: [typeof SERVICE_TYPE_KEY]; value: ServiceType[] };
  }
}

// eslint-disable-next-line @stylistic/max-len
export const useServiceTypes = (config?: QueryConfig<ServiceType[]>): APIQueryResult<ServiceType[]> => useAPI([SERVICE_TYPE_KEY], () => [
  { name: 'Перевозка сотрудников', value: 'EMPLOYEE_TRANSPORTATION' },
  { name: 'Грузоперевозки', value: 'CARGO_TRANSPORTATION' }, // TODO: запросить реальные
  { name: 'Автосервис', value: 'CAR_SERVICE_TRANSPORTATION' },
], config);
