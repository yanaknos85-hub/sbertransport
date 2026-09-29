import { Select } from 'antd';
import { useAutoparks } from 'api/contractors/contractors.api';
import { Autopark, Autoparks, AutoparksFilters } from 'api/contractors/contractors.types';
import { SelectEndlessScroll } from 'components/SelectEndlessScroll';
import React, { FC, useCallback } from 'react';
import { MutateFunction } from 'react-query';

interface AutoparkSelectProps extends React.ComponentProps<typeof Select> {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onFetch?: (page: number, data: Autopark[]) => void;
}

const AutoparkSelect: FC<AutoparkSelectProps> = ({ onFetch, ...props }) => {
  const [_fetchAutoparks] = useAutoparks();

  const fetchAutoparks: MutateFunction<Autoparks, unknown, AutoparksFilters, unknown> = (
    useCallback(async (filters?: AutoparksFilters) => {
      const result = await _fetchAutoparks(filters);
      onFetch?.(filters?.page ?? 0, result?.content ?? []);

      return result;
    }, [_fetchAutoparks, onFetch])
  );

  return (
    <SelectEndlessScroll
      fetch={fetchAutoparks}
      placeholder="Выберите автопарк"
      {...props}
    />
  );
};

export default AutoparkSelect;
