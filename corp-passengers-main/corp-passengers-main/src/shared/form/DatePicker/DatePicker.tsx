import React, { FC } from 'react';
import { DatePickerProps, RangePickerProps } from 'antd/lib/date-picker';
import locale from 'antd/es/date-picker/locale/ru_RU';

import { DATE_FORMAT } from 'constants/constants.app';

import { ReactComponent as Icon } from './images/calendar.svg';
import { StyledDatePicker, StyledRangePicker } from './DatePicker.style';

export const RangePicker: FC<RangePickerProps> = ({ ...props }) => (
  <StyledRangePicker
    format={DATE_FORMAT.BASE_REVERTED_DOTS}
    suffixIcon={<Icon />}
    {...props}
    locale={locale}
  />
);

const DatePicker: FC<DatePickerProps> = ({ ...props }) => (
  <StyledDatePicker
    format={DATE_FORMAT.BASE_REVERTED_DOTS}
    suffixIcon={<Icon />}
    {...props}
    locale={locale}
  />
);

export default DatePicker;
