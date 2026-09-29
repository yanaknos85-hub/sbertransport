import styled from 'styled-components';
import { colors } from "shared/styles/styles";

export const Icon = styled.span`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 40px;
  padding: 0;
  line-height: 0;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border-radius: 8px;
  box-sizing: border-box;
  border: 1px solid ${colors.solidBodyNormal};
  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
    border: 1px solid ${colors.solidBodyNormal};
  }
  svg {
    width: 20px;
    height: 20px;
  }
`;