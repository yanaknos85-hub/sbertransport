import React, { FC } from 'react';
import locale from 'antd/es/date-picker/locale/ru_RU';
import { DatePickerProps } from 'antd/lib/date-picker';

import { DATE_FORMAT } from 'constants/constants.app';

import { StyledDatePicker } from './DatePicker.style';
import { ReactComponent as Icon } from './images/calendar.svg';

const DatePicker: FC<DatePickerProps> = ({ ...props }) => (
  <StyledDatePicker
    format={DATE_FORMAT.DATE_WITH_TIME}
    suffixIcon={<Icon />}
    {...props}
    locale={locale}
  />
);

export default DatePicker;
