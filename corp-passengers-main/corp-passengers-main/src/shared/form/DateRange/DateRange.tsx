import React from 'react';
import locale from 'antd/es/date-picker/locale/ru_RU';
import { DATE_FORMAT } from '../../../constants/constants.app';
import { ReactComponent as Icon } from './images/calendar.svg';
import { StyledDateRange } from './DateRange.style';

const DateRange = ({ ...props }) => (
  <StyledDateRange
    format={DATE_FORMAT.BASE_REVERTED}
    suffixIcon={<Icon />}
    {...props}
    locale={locale}
  />
);

export default DateRange;
