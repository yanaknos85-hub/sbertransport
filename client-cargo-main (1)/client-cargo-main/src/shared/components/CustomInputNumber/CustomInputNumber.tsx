import InputNumber from 'shared/form/InputNumber/InputNumber';
import styled from 'styled-components';

export const CustomInputNumber = styled(InputNumber)<{ width: string; height: string }>`
  width: ${({ width }) => `${width}`};
  height: ${({ height }) => `${height}`};

  .ant-input-number-handler-wrap {
    width: 55px;
  }
  .ant-input-number-input {
    height: ${({ height }) => `${height}`};
    padding-right: 0;
  }

  &:hover:not(.ant-input-number-borderless) .ant-input-number-handler-down,
  &.ant-input-number-focused:not(.ant-input-number-borderless) .ant-input-number-handler-down {
    border: none;
  }
`;
