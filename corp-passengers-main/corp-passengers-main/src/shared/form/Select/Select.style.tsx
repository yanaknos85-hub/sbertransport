import styled, { css } from 'styled-components';
import { Select } from 'antd';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const StyledSelect = styled(Select)<any>`
  min-width: 212px;
  width: 100%;
  height: 48px;
  position: relative;

  .ant-select-selector {
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    height: 100%;
    width: 100%;

    ${({ disabled }) => disabled
    && css`
        background: #f5f5f5;
        border-color: #d9d9d9;
        cursor: not-allowed;
      `}
  }

  .ant-select-selection-placeholder,
  .ant-select-selection-item {
    line-height: 44px !important;
    padding: 0 16px !important;
    font-family: SB Sans Text;
    font-size: 14px;
    color: #262626;
  }

  .ant-select-selection-search {
    position: absolute !important;
    top: 10px !important;
    width: 80% !important;
    height: 60% !important;
  }

  .ant-select-selection-search-input {
    border: none;
    outline: none;
    height: 90%;
  }

  .ant-select-arrow {
    right: 16px;
    margin-top: -8px;
  }

  .ant-select-item .ant-select-item-option .ant-select-item-option-active {
    background-color: white !important;
  }

  .ant-picker-disabled {
    background: #f5f5f5;
    border-color: #d9d9d9;
    cursor: not-allowed;
  }
  .ant-select-clear {
    right: 31px;
  }
`;
