import styled from 'styled-components';

import { Select as AntDesignSelect } from 'antd';

export const SelectStyled = styled(AntDesignSelect)<any>`
  width: 100%;
  padding: 11px 16px;
  height: 46px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: white;

  :global(.ant-select-selector) {
    :global(.ant-select-selection-search) {
      display: flex;
      align-items: center;
      outline: none;

      .ant-select-selection-search-input {
        height: 22px;
      }
    }

    .ant-select-selection-placeholder {
      font-size: 14px;
      line-height: 22px;
      font-weight: 400;
      letter-spacing: -0.3px;
      color: #909090;
    }

    .ant-select-selection-item {
      font-size: 14px;
      line-height: 22px;
      font-weight: 400;
      letter-spacing: -0.3px;
      color: #262626;
      padding-right: 55px;
    }

    .ant-select-selection-item:focus-visible {
      outline: none;
      border: none;
    }

    &:before {
      content: '';
      position: absolute;
      right: 45px;
      top: calc((100% - 38px) / 2);
      width: 1px;
      height: 28px;
      background-color: rgba(38, 38, 38, 0.08);
    }
  }

  .ant-select-clear {
    right: 27px;
  }

  .ant-select-arrow {
    width: 16px;
    height: 16px;
    right: 24px;
    top: 16px;
    margin-top: 0;
  }

  &:global(.ant-select-open) {
    :global(.ant-select-arrow) {
      transform: rotate(180deg);
    }
  }
`;
