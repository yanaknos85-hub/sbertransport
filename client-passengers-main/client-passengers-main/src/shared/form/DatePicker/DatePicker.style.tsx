/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { DatePicker } from 'antd';
import styled from 'styled-components';

import { baseInput, shadowFocused } from '../styles';

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
