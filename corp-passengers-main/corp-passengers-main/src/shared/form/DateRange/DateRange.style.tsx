/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';
import { DatePicker } from 'antd';

import { baseInput, shadowFocused } from '../styles';

const { RangePicker: DateRange } = DatePicker;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const StyledDateRange = styled(DateRange)<any>`
  ${baseInput}

  &.ant-picker-focused {
    box-shadow: ${shadowFocused};
  }

  .ant-picker-suffix {
    margin-top: -4px;
  }

  .ant-picker-clear {
    display: none;
  }
`;
