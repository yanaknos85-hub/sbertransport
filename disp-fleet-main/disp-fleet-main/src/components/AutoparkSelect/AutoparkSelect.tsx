import React, { FC, useCallback } from 'react';
import { MutateFunction } from 'react-query';
import { Select } from '@sber-sbertransport/ui-kit/src';

import { useAutopark, useAutoparks } from 'api/contractors/contractors.api';
import { Autopark, Autoparks, AutoparksFilters } from 'api/contractors/contractors.types';
import { SelectEndlessScroll } from 'components/SelectEndlessScroll';
import { UUID } from 'utils/io-ts';

interface AutoparkSelectProps extends React.ComponentProps<typeof Select> {
  onFetch?: (page: number, data: Autopark[]) => void;
}

const AutoparkSelect: FC<AutoparkSelectProps> = ({ onFetch, ...props }) => {
  const [_fetchAutopark] = useAutopark();
  const [_fetchAutoparks] = useAutoparks();

  const fetchAutopark: MutateFunction<Autopark, unknown, UUID, unknown> = useCallback(
    async (autoparkId?: UUID) => {
      const result = await _fetchAutopark(autoparkId);

      return result;
    },
    [_fetchAutopark]
  );

  const fetchAutoparks: MutateFunction<Autoparks, unknown, AutoparksFilters, unknown> = useCallback(
    async (filters?: AutoparksFilters) => {
      const result = await _fetchAutoparks(filters);
      onFetch?.(filters?.page ?? 0, result?.content ?? []);

      return result;
    },
    [_fetchAutoparks, onFetch]
  );

  return (
    <SelectEndlessScroll
      fetch={fetchAutoparks}
      fetchOne={fetchAutopark}
      placeholder="Выберите автопарк"
      searchField="name"
      showSearch
      showDivider
      size="small"
      {...props}
    />
  );
};

export default AutoparkSelect;
