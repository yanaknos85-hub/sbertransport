import styled from 'styled-components';
import { Select } from 'antd';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const StyledSelectMultiple = styled(Select)<any>`
  min-width: 212px;
  width: 100%;
  min-height: 47px;
  position: relative;
  box-sizing: border-box;

  .ant-select-selector {
    border: 1px solid #e0e0e0;
    border-radius: 8px !important;
    width: 100%;
    padding-top: 10px;
    padding-bottom: 8px;
    box-sizing: border-box;
  }

  .ant-select-selection-search-input {
    border: none;
    outline: none;
  }

  .ant-select-arrow {
    right: 16px;
    margin-top: -8px;
  }

  .ant-select-item .ant-select-item-option .ant-select-item-option-active {
    background-color: white !important;
  }
`;
