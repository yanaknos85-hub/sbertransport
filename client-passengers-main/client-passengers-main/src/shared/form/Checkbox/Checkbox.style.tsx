/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { Checkbox } from 'antd';
import styled from 'styled-components';

export const StyledCheckbox = styled(Checkbox)<any>`
  label {
    font-family: SB Sans Text;
    font-size: 14px;
    line-height: 22px;
    letter-spacing: -0.3px;
    color: #262626;
  }

  .ant-checkbox-inner {
    border-radius: 4px;
  }
`;
