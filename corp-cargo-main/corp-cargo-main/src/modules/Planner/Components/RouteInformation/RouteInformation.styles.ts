import styled from 'styled-components';
import { Divider as DividerAnt } from 'antd';
import { colors, fontFamily } from 'shared/styles/styles';

export const Wrapper = styled.div`
  position: sticky;
  top: 0;
  background-color: white;
  z-index: 1000;
`;

export const Divider = styled(DividerAnt)`
  margin: 24px 0;
`;

export const Price = styled.div`
  display: flex;
  justify-content: space-between;

  p {
    margin: 0;
    padding: 0;
    font-family: ${fontFamily.SBSansInterface};
  }

  p:nth-child(1) {
    margin-right: 6px;
    font-size: 16px;
    font-weight: 400;
  }

  p:nth-child(2) {
    font-size: 22px;
    font-weight: 700;
    line-height: 32px;
    color: ${colors.black90Alpha};
  }
`;

export const Title = styled.div`
  display: flex;
  align-items: center;
`
