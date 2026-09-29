import { DatePicker, Select } from 'antd';
import locale from 'antd/es/date-picker/locale/ru_RU';
import moment from 'moment';
import React, { FC } from 'react';

import { MomentRanges, RangeKeys, pickerOptions } from 'shared/MomentRanges';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TransportTypeOptions } from 'stores/Trip/Trip.interface';
import { LabeledValue } from 'utils';

import { CreateRequestFilterTitles, RequestFilterConstants } from './RequestFilter.constants';
import { RequestFilterProps } from './useRequestFilterProps';

export const RequestFilter: FC<RequestFilterProps> = props => {
  const {
    dateRange, setDateRange, setTransportType, datePickerVisible, setDatePickerVisible,
  } = props;

  const _setDateRange = (option: LabeledValue<RangeKeys>): void => {
    const range = MomentRanges[option.value];
    if (option.value === RangeKeys.all) {
      setDateRange(null);
      setDatePickerVisible(false);
    } else {
      setDateRange(range);
      setDatePickerVisible(range === null);
    }
  };

  const _setTransportType = (option: LabeledValue<TransportTypeEnum>): void => {
    setTransportType(option?.value);
  };

  return (
    <>
      <Select
        labelInValue={true}
        placeholder={CreateRequestFilterTitles[RequestFilterConstants.chooseTransportType]}
        onChange={_setTransportType}
        options={TransportTypeOptions}
        style={{ width: '100%' }}
        allowClear={true}
        getPopupContainer={trigger => trigger.parentNode}
      />
      <Select
        labelInValue={true}
        defaultValue={pickerOptions[0]}
        onChange={_setDateRange}
        options={pickerOptions}
        style={{ width: '100%', marginTop: 16 }}
        getPopupContainer={trigger => trigger.parentNode}
      />

      {datePickerVisible && (
        <DatePicker.RangePicker
          defaultValue={null}
          locale={locale}
          style={{ width: '100%', marginTop: 16 }}
          value={dateRange}
          onChange={(dates): void => setDateRange(dates && [dates[0]?.startOf('day') || null, dates[1]?.endOf('day') || null])}
          ranges={{
            вчера: [moment().subtract(1, 'day'), moment().subtract(1, 'day')],
            позавчера: [moment().subtract(2, 'day'), moment().subtract(2, 'day')],
            неделя: [moment().subtract(1, 'weeks'), moment()],
            месяц: [moment().subtract(1, 'month'), moment()],
          }}
        />
      )}
    </>
  );
};
