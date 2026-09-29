import { colors } from 'shared/styles/styles';
import styled from 'styled-components';

export const OrderInfo = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
`;

export const Checkbox = styled.div`
  display: flex;
  align-items: center;

  .ant-checkbox-wrapper {
    margin-left: 10px;
  }

  .ant-checkbox-inner {
    width: 16px;
    height: 16px;
    border: ${`1px solid ${colors.gray4}`};
    border-radius: 4px;
  }
`;