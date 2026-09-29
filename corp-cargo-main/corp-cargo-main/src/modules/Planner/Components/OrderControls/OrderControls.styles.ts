import styled from 'styled-components';
import { colors } from 'shared/styles/styles';

export const ControlsStyled = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: white;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
  padding: 10px 24px 0 24px;
  & .ant-input {
    background-color: ${colors.bgDark};
  }
`;

export const Wrapper = styled('div')`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const ButtonsBlock = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  &:focus,
  &:active {
    color: ${colors.white};
    background-color: ${colors.solidBodyNormal};
  }

  svg path {
    transition: color 0.3s;
  }

  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
    cursor: pointer;

    svg path {
      stroke: ${colors.solidBodyNormal};
      transition: all 0.3s;
      fill: ${colors.solidBodyNormal};
    }
  }
`;
export const GreenTextButton = styled.button`
  background: none;
  border: none;
  color: ${colors.green10};
  cursor: pointer;
  margin-left: auto;
  font-weight: 600;
  font-size: 12px;
  padding-right: 24px;

  &:hover {
    text-decoration: underline;
  }
`;

export const SortMenuBlock = styled.div`
  display: flex;
  justify-content: flex-start;
`;

