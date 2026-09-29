import { DatePicker, Select } from 'antd';
import locale from 'antd/es/date-picker/locale/ru_RU';
import moment from 'moment';
import React, { FC, useEffect, useState } from 'react';

import { LabeledValueTransportTypes, TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TransportTypeOptions } from 'stores/Trip/Trip.interface';

import { RequestFilterProps } from './useRequestFilterProps';
import { SelectTransportType } from './SelectTransportType/SelectTransportType';

import styles from './styles.module.scss';
import {
  TransportTypeStatuses, TripStatusesEnum, selectedTransportTypeCompletedStatuses, selectedTransportTypeStatuses
} from 'modules/EmployeeApp/TripRequestStatuses.constants';

export const RequestFilter: FC<RequestFilterProps> = props => {
  const {
    dateRange, setDateRange, setTransportType, setStatus, statusFilter, setPage,
  } = props;
  const [selectedTransport, setSelectedTransport] = useState<string | undefined>('');
  const selectTransportType = [{ label: 'Все', value: undefined }, ...JSON.parse(JSON.stringify(TransportTypeOptions))];

  useEffect(() => {
    setSelectedTransport(undefined);
  }, []);

  const _setTransportType = (value: TransportTypeEnum): void => {
    setTransportType(value);
  };

  const _setStatus = (value: TripStatusesEnum): void => {
    setStatus(value);
  };

  const changeSelectedTransport = (type: string) => {
    setPage && setPage(1);
    setSelectedTransport(type);
  };

  return (
    <div className={styles.wrapperFilter}>
      <div className={styles.selectTransportType}>
        {selectTransportType.map((el: LabeledValueTransportTypes) => (
          <SelectTransportType
            selectedTransport={selectedTransport}
            changeSelectedTransport={changeSelectedTransport}
            lable={el.label}
            type={el.value}
            _setTransportType={_setTransportType}
          />
        )
        )}
      </div>
      <div className={styles.filters}>
        <DatePicker.RangePicker
          defaultValue={null}
          locale={locale}
          className={styles.DatePicker}
          value={dateRange}
          onChange={(dates): void => setDateRange(dates && [dates[0]?.startOf('day') || null, dates[1]?.endOf('day') || null])}
          ranges={{
            вчера: [moment().subtract(1, 'day'), moment().subtract(1, 'day')],
            позавчера: [moment().subtract(2, 'day'), moment().subtract(2, 'day')],
            неделя: [moment().subtract(1, 'weeks'), moment()],
            месяц: [moment().subtract(1, 'month'), moment()],
          }}
        />
        {selectedTransport
        && (
        <Select
          className={styles.select}
          options={statusFilter === 'planned' ? selectedTransportTypeStatuses[selectedTransport as keyof TransportTypeStatuses] : selectedTransportTypeCompletedStatuses[selectedTransport as keyof TransportTypeStatuses]}
          onChange={_setStatus}
          defaultValue={TripStatusesEnum.ALL}
        />
        )}
      </div>
    </div>
  );
};
