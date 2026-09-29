import React, { FC, useMemo } from 'react';

import { Select } from '@sber-sbertransport/ui-kit/src';

import moment from 'moment';

import { useShiftsQuery } from 'modules/Schedule2.0/tabs/Shifts/context/shiftsQuery.context';

import { periodOptions, PeriodType } from './PeriodFilter.constants';

import styles from './PeriodFilter.module.scss';

export const PeriodFilter: FC = () => {
  const { query, setQuery } = useShiftsQuery();

  const period = useMemo(
    () => Object.values(PeriodType).find(type => (
      moment(query.startDate).isSame(moment().startOf(type)) && moment(query.endDate).isSame(moment().endOf(type))
    )),
    [query.startDate, query.endDate]
  );

  const onChangePeriod = (value: PeriodType) => {
    setQuery({
      ...query,
      startDate: moment().startOf(value).startOf('day').toISOString(),
      endDate: moment().endOf(value).endOf('day').toISOString(),
    });
  };

  return (
    <Select
      value={period}
      options={periodOptions}
      className={styles.select}
      placeholder="Период"
      size="small"
      showDivider
      showCheckMark
      onChange={onChangePeriod}
    />
  );
};
