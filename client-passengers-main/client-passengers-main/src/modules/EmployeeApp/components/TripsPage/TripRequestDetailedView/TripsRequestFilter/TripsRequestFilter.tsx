import { DatePicker, Select } from 'antd';
import locale from 'antd/es/date-picker/locale/ru_RU';
import moment from 'moment';
import React from 'react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';

import { DATE_FORMAT } from 'constants/constants.app';
import { LabeledValueTransportTypes, TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TransportTypeOptions } from 'stores/Trip/Trip.interface';

import { SelectTransportType } from 'shared/components/RequestFilter/SelectTransportType/SelectTransportType';
import type { RequestFilterProps } from 'shared/components/RequestFilter/useRequestFilterProps';

import {
  TransportTypeStatuses,
  TripStatusesEnum,
  selectedTransportTypeCompletedStatuses,
  selectedTransportTypeStatuses,
  TripsRangeEnum
} from 'modules/EmployeeApp/TripRequestStatuses.constants';

import styles from './styles.module.scss';

export const TripsRequestFilter: React.FC<RequestFilterProps> = props => {
  const {
    dateRange,
    setDateRange,
    setTransportType,
    setStatus,
    statusFilter,
    setPage,
    transportType,
  } = props;

  const {
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();
  const IS_PERSONAL_DEVICE = configStore.env.IS_PERSONAL_DEVICE;

  const selectTransportType = [{ label: 'Все', value: undefined }, ...JSON.parse(JSON.stringify(TransportTypeOptions))];

  const _setTransportType = (value: TransportTypeEnum): void => {
    setTransportType(value);
  };

  const _setStatus = (value: TripStatusesEnum): void => {
    setStatus(value);
  };

  const options = statusFilter === 'planned' ? selectedTransportTypeStatuses[transportType as keyof TransportTypeStatuses] : selectedTransportTypeCompletedStatuses[transportType as keyof TransportTypeStatuses];

  const changeSelectedTransport = (type: string) => {
    setPage && setPage(1);
    setTransportType(type as TransportTypeEnum);
  };

  return (
    <div className={styles.wrapperFilter}>
      <div className={styles.selectTransportType}>
        {selectTransportType.map((el: LabeledValueTransportTypes) => (
          <SelectTransportType
            selectedTransport={transportType}
            changeSelectedTransport={changeSelectedTransport}
            lable={el.label}
            type={el.value}
            _setTransportType={_setTransportType}
          />
        )
        )}
      </div>
      <div className={styles.filters}>
        {!IS_PERSONAL_DEVICE && (
        <DatePicker.RangePicker
          defaultValue={null}
          locale={locale}
          className={styles.DatePicker}
          value={dateRange}
          onChange={(dates): void => setDateRange(dates && [dates[0]?.startOf('day') || null, dates[1]?.endOf('day') || null])}
          format={DATE_FORMAT.BASE_REVERTED_DOTS}
          ranges={{
            [TripsRangeEnum.YESTERDAY]: [moment().subtract(1, 'day'), moment().subtract(1, 'day')],
            [TripsRangeEnum.BEFORE_YESTERDAY]: [moment().subtract(2, 'day'), moment().subtract(2, 'day')],
            [TripsRangeEnum.WEEK]: [moment().subtract(1, 'weeks'), moment()],
            [TripsRangeEnum.MONTH]: [moment().subtract(1, 'month'), moment()],
          }}
        />
        )}
        {!IS_PERSONAL_DEVICE && transportType
        && (
        <Select
          className={styles.select}
          options={options}
          onChange={_setStatus}
          defaultValue={TripStatusesEnum.ALL}
        />
        )}
      </div>
    </div>
  );
};
