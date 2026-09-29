import { Button, Switch as SwitchAnt } from 'antd';
import InputNumber from 'shared/form/InputNumber/InputNumber';
import styled from 'styled-components';

export const Wrapper = styled.div`
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  background-color: #ffffff;
  border-radius: 15px;
`;

export const Title = styled.div`
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
`;

export const Item = styled.div<{ isShadow: boolean }>`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  width: 100%;
  opacity: ${({ isShadow }) => isShadow ? 0.5 : 1};
`;

export const Label = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: start;
  flex: 2 1 20%;
  font-size: 16px;
  color: #333;
`;

export const LabelTitle = styled.div`
  font-size: 14px;
  color: #262626;
`;

export const LabelDescriptionWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
`;

export const LabelDescription = styled.div`
  font-size: 12px;
  color: #737373;
`;

export const Price = styled.span`
  padding: 4px 12px;
  width: fit-content;
  flex-grow: 0;
  background: #E7F9F0;
  color: #10bf6a;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
`;

export const ButtonContainer = styled.div<{ isLiftAvailable: boolean }>`
  width: 100px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex: 1 0 20%;
`;

export const StyledButton = styled(Button)`
  width: 128px;
  height: 40px;
`;

export const StyledInputNumber = styled(InputNumber)`
  width: 130px;
  height: 40px;

  .ant-input-number-handler-wrap {
    width: 55px;
  }
  .ant-input-number-input {
    height: 40px;
    padding-right: 0;
  }
`;

export const LiftButtonContainer = styled.div<{ isLiftAvailable: boolean }>`
  width: 100px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-grow: ${({ isLiftAvailable }) => isLiftAvailable ? 0 : 2};
`;

export const Switch = styled(SwitchAnt)`
  margin-right: 12px;
  color: #262626;
`;

export const RadioItemWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;
