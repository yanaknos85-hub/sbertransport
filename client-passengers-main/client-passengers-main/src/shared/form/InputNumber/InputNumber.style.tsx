/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { InputNumber } from 'antd';
import styled from 'styled-components';

import {
  baseInput, border, inputFocus, inputHeight
} from '../styles';
import downIcon from './images/1.svg';
import upIcon from './images/2.svg';

export const StyledInput = styled(InputNumber)<any>`
  ${baseInput}

  &:hover {
    border: ${border};
  }

  &.ant-input-number-focused {
    ${inputFocus}
  }

  .ant-input-number-input {
    height: ${inputHeight};
    padding-right: 95px;
  }

  .ant-input-number-handler-wrap {
    opacity: 1;
    border: 0;
    display: flex;
    width: 85px;
    top: 0;
    right: 0;
    background: transparent;
    justify-content: center;
    align-items: center;

    &::before {
      content: '';
      left: 0;
      position: absolute;
      width: 1px;
      height: 28px;
      background: rgba(38, 38, 38, 0.08);
    }
  }

  .ant-input-number-handler {
    border: 0;
    width: 24px;
    height: 24px;
    position: relative;
  }

  .ant-input-number-handler-up {
    margin-left: 4px;
    order: 1;
  }

  .ant-input-number-handler-up-inner {
    top: 0;

    &::after {
      background-image: url(${upIcon});
    }
  }

  .ant-input-number-handler-down-inner {
    &::after {
      background-image: url(${downIcon});
    }
  }

  .anticon {
    position: relative !important;
    margin: 0 !important;
    right: 0 !important;
    font-size: 19px !important;
    color: #262626;
    width: 100%;
    height: 100%;

    &::after {
      content: '';
      width: 24px;
      height: 24px;
      background-repeat: no-repeat;
      background-position: center center;
    }
  }

  svg {
    display: none;
  }
`;
