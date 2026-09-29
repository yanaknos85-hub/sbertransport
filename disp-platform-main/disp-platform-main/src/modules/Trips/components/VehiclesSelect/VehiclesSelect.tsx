import React, { FC } from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import { SelectEndlessScroll } from 'components/SelectEndlessScroll';
import { PaginationParams } from 'utils/io-ts/pagination';
import { useTripTransport } from 'api/dispatchers/dispatchers.api';
import { TripTransportFilters } from 'api/dispatchers/dispatchers.types';

interface VehiclesSelectProps extends React.ComponentProps<typeof Select> {
  query: Omit<TripTransportFilters, keyof PaginationParams>;
}

const VehiclesSelect: FC<VehiclesSelectProps> = props => {
  const [fetchTransport] = useTripTransport();

  return (
    <SelectEndlessScroll
      fetch={fetchTransport}
      placeholder="Выберите автомобиль"
      labelField={data => [data.stateNumber, '|', data.brand, data.model].filter(Boolean).join(' ')}
      showSearch
      size="small"
      searchField="search"
      {...props}
    />
  );
};

export default VehiclesSelect;
