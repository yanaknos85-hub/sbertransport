import InputNumber from 'shared/form/InputNumber/InputNumber';
import styled from 'styled-components';

export const Wrapper = styled.div`
  padding: 0 16px;
  width: 100%;
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 16px;
  background: #ffffff;
  border-radius: 12px;
`;

export const Title = styled.div`
  font-size: 16px;
  margin-bottom: 16px;
`;

export const StyledInputNumber = styled(InputNumber)`
  width: 100px;
  height: 30px;

  .ant-input-number-handler-wrap {
    width: 55px;
  }
  .ant-input-number-input {
    height: 30px;
    padding-right: 0;
  }
`;
