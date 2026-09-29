import React, { FC } from 'react';
import { FormInstance } from 'antd/lib/form/Form';
import { FilterButton } from '../../FilterButton';
import { FilterInput } from '../FilterInput/FilterInput';
import { usePlanner } from '../../../context/PlannerContext';
import { MonitorFiltersType } from '../../../types';

import * as S from './Filters.styles';

interface Props {
  filtersForm: FormInstance;
  filterIdForm: FormInstance;
  getFilteredRoutes: (request: MonitorFiltersType) => void;
}

export const Filters: FC<Props> = ({ filtersForm, getFilteredRoutes, filterIdForm }) => {
  const { handleOpenMonitorFilters } = usePlanner();

  return (
    <S.Container>
      <S.Handlers>
        <FilterInput
          getFilteredRoutes={getFilteredRoutes}
          filtersForm={filtersForm}
          showFiltersIcon={false}
          filterIdForm={filterIdForm}
        />
        <FilterButton handleFilters={handleOpenMonitorFilters} />
      </S.Handlers>
    </S.Container>
  );
};
