import styled from 'styled-components';
import { colors, fontFamily } from 'shared/styles/styles';

export const ListItemAddressStyled = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
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
    font-weight: 700;
    font-size: 24px;
    line-height: 32px;
    color: ${colors.black90Alpha};
  }
`;
