import { Button as ButtonAnt } from 'antd';

import styled from 'styled-components';
import { fontFamily, colors } from 'shared/styles/styles';

export const Button = styled(ButtonAnt)`
  display: flex;
  align-items: center;
  justify-content: flex-start;
`;
export const DropdownDiv = styled.div`
  justify-content: flex-end;
  padding-right: 0;
  align-content: center;
  margin: 8px 24px;
`;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const DropdownButton = styled.div<any>`
  font-family: ${fontFamily.SBSansTextRegular};
  font-size: 14px;
  line-height: 20px;
  color: ${colors.gray8};
`;
