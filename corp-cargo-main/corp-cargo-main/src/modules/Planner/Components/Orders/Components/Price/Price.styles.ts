import styled from 'styled-components';
import { colors, fontFamily } from 'shared/styles/styles';

export const Price = styled.div`
  display: flex;
  justify-content: space-between;

  p {
    margin: 0;
    padding: 0;
  }
`;

export const Cost = styled.div`
  display: flex;
  flex-direction: column;
`;

export const CostTitle = styled.p`
  font-size: 14px;
  font-family: ${fontFamily.SBSansTextRegular};
  color: ${colors.gray7};
`;

export const ContractInfo = styled.div`
  display: flex;
  flex-direction: column;

  p {
    margin: 0;
    color: ${colors.gray7};
  }

  span {
    word-break: break-all;
    font-weight: 600;
    color: ${colors.black90Alpha};
  }
`;

export const Contractor = styled.div`
  display: flex;
`;
