/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';
import { DatePicker } from 'antd';

import { baseInput, shadowFocused } from '../styles';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const StyledDatePicker = styled(DatePicker)<any>`
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

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const StyledRangePicker = styled(DatePicker.RangePicker)<any>`
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
