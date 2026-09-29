import styled from 'styled-components';
import { Progress as ProgressAnt } from 'antd';
import { colors, fontFamily } from 'shared/styles/styles';

export const ProgressInfo = styled.div`
  display: flex;
  justify-content: space-between;
`;

export const Title = styled.p`
  margin: 0;
  padding: 0;
  font-family: ${fontFamily.SBSansTextRegular};
  font-size: 14px;
  font-weight: 400;
  color: ${colors.gray10};
`;

export const Info = styled.div`
  font-size: 12px;
  color: ${colors.gray8};
`;

export const ProgressBarStyled = styled(ProgressAnt)`
  width: 100%;
  margin-bottom: 16px;

  &:nth-last-child {
    margin-bottom: 0;
  }
`;
