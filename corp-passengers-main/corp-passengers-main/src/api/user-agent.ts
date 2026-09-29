import { Quantity, QuantityFilters } from 'stores/UserAgent/UserAgent.interface';
import { APIQueryResult, useAPI } from 'api';
import { QueryConfig } from 'react-query';
import { QUANTITY } from 'constants/constants.api';

declare module 'api' {
  interface Cache {
    quantity: {
      key: ['quantity', QuantityFilters];
      value: Quantity;
    };
  }
}

export const useQuantity = (filters: QuantityFilters, config?: QueryConfig<Quantity>): APIQueryResult<Quantity> => {
  const { updater, ...params } = filters;

  return useAPI(
    ['quantity', filters],
    ({ http, process }) => http
      .get<Quantity>(QUANTITY, { params })
      .then(process.decodeResponseData(Quantity)),
    {
      cacheTime: 500,
      ...config,
    }
  );
};
