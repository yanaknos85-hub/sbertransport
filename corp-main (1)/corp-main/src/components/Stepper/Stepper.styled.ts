import styled from 'styled-components';
import { colors } from 'shared/styles/styles';

export const StepperWrapper = styled.div`
  display: flex;
  gap: 30px;
  align-items: center;
`;

export const StepStyled = styled.div<{ isActive: boolean; isCurrent: boolean; isClickable: boolean; isDisabled: boolean }>`
  display: inline-grid;
  grid-template-columns: 28px auto 28px;
  gap: 15px;
  font-size: 16px;
  align-items: center;
  color: ${props => (props.isActive ? colors.gray10 : colors.gray5)};
  font-weight: 600;
  span {
    width: ${props => (props.isCurrent ? '26px' : '24px')};
    height: ${props => (props.isCurrent ? '26px' : '24px')};
    font-size: 15px;
    line-height: 26px;
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
  cursor: ${props => (props.isDisabled ? 'not-allowed' : props.isClickable ? 'pointer' : 'default')};
`;
