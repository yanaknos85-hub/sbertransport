import styled from 'styled-components';
import { colors, fontFamily } from 'shared/styles/styles';

export const OrderParams = styled.div`
  display: flex;
  justify-content: space-between;
  width: 100%;
`;

export const CargoInfo = styled.div`
  display: flex;

  div:nth-child(2) {
    margin-left: 68px;
  }
`;

export const CargoParams = styled.div`
  text-align: center;
`;

export const SemiBoldText = styled.p<any>`
  margin: 0;
  padding: 0;

  font-size: 16px;
  font-weight: 600;

  font-family: ${props => props.fontFamily};
  color: ${props => props.color};
`;

export const Text = styled.p`
  margin: 0;
  padding: 0;

  color: ${colors.gray6};
`;

export const Price = styled.div`
  text-align: right;

  p {
    margin: 0;
    padding: 0;
  }

  p:nth-child(1) {
    font-size: 14px;
    font-family: ${fontFamily.SBSansTextRegular};
    color: ${colors.gray7};
  }

  p:nth-child(2) {
    font-size: 24px;
    font-family: ${fontFamily.SBSansInterface};
    font-weight: 700;
    color: ${colors.black90Alpha};
  }
`;
