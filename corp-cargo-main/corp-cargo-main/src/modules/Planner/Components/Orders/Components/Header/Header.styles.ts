import styled from 'styled-components';
import { colors, fontFamily } from 'shared/styles/styles';

export const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const HeaderText = styled.div`
  display: flex;
  flex-direction: column;

  p {
    font-family: ${fontFamily.SBSansTextRegular};
    margin: 0;
    padding: 0;
    font-size: 12px;
  }

  p:nth-child(-n + 1) {
    color: ${colors.gray6};
  }

  p:nth-child(2) {
    color: ${colors.black};
    font-size: 14px;
    font-weight: 600;
    font-family: ${fontFamily.SBSansInterface};
    display: flex;
    align-items: center;
  }
  p:nth-child(3) {
    color: ${colors.gray8};
  }
`;

export const Checkbox = styled.div`
  display: flex;
  align-items: center;

  .ant-checkbox-wrapper {
    margin-left: 10px;
  }

  .ant-checkbox-inner {
    width: 24px;
    height: 24px;
    border: ${`1px solid ${colors.gray4}`};
    border-radius: 4px;
  }
`;
