import { Radio as RadioAnt } from 'antd';
import styled from 'styled-components';

export const Radio = styled(RadioAnt)`
  :global(.ant-radio .ant-radio-checked) {
    :global(.ant-radio-inner:after) {
      left: 10px !important;
      top: 10px !important;
      border: 1px solid #10bf6a;
      background-color: #10bf6a;
    }
  }
`;
