import styled from 'styled-components';
import { colors } from 'styles/styles';

export const StepperWrapper = styled.div`
  display: flex;
  gap: 40px;
  align-items: center;
  padding: 20px 0;
`;

export const StepStyled = styled.div<{ isActive: boolean; isCurrent: boolean; isClickable: boolean }>`
  display: inline-grid;
  grid-template-columns: 28px auto 28px;
  gap: 18px;
  font-family: 'SB Sans Text';
  font-size: 16px;
  font-weight: ${props => (props.isCurrent ? 600 : 400)};
  line-height: 24px;
  letter-spacing: -0.3px;
  color: ${props => (props.isActive ? colors.gray10 : colors.gray5)};
  align-items: center;

  span {
    width: 28px;
    height: 28px;
    font-weight: 600;
    line-height: 28px;
    outline: ${props => (props.isCurrent ? '5px' : '2px')} solid
      ${props => (props.isActive ? (props.isCurrent ? colors.green3 : colors.green10) : colors.gray3)};
    text-align: center;
    border-radius: 28px;
    background: ${props => (props.isActive ? colors.green10 : colors.white)};
    color: ${props => (props.isActive ? colors.white : colors.gray3)};
  }

  svg {
    margin-left: 20px;
  }

  cursor: ${props => (props.isClickable ? 'pointer' : 'default')};
`;
