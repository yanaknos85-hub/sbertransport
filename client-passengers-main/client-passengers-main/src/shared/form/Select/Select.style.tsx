import { Select } from 'antd';
import styled from 'styled-components';

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
    display: none;
  }

  .ant-select-arrow {
    right: 16px;
    margin-top: -8px;
  }
`;
