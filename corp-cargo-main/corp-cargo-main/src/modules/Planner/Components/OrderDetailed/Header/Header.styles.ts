import styled from 'styled-components';
import { colors, fontFamily } from 'shared/styles/styles';

export const Header = styled.div`
  width: 100%;
`;

export const TitleBlock = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
`;

export const TextBlock = styled.div`
  display: flex;
  flex-direction: column;
`;

export const HeaderTitle = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  cursor: pointer;
  span {
    margin-left: 16px;
  }
`;

export const TextLight = styled.p`
  margin-bottom: 0;
  font-family: ${fontFamily.SBSansTextRegular};
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  color: ${colors.gray8};
`;
