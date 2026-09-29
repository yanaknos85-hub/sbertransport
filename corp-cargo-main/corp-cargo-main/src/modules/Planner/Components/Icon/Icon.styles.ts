import styled from 'styled-components';
import { colors } from 'shared/styles/styles';

export const Icon = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-left: 12px;
  padding: 13px 12px;
  line-height: 0;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border-radius: 8px;
  box-sizing: border-box;
  border: 1px solid ${colors.solidBodyNormal};

  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
    box-sizing: border-box;
    border: 1px solid ${colors.solidBodyNormal};

    svg path {
      fill: ${colors.solidBodyNormal};
    };
  }

  @media screen and (max-width: 1600px) {
    margin-left: 0;
    margin-right: 8px;
    height: 40px;

    :nth-last-child(1) {
      margin-right: 0;
    }
  }
`;
