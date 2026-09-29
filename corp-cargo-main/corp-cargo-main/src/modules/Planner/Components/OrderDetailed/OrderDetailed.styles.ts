import styled from 'styled-components';
import { Tabs } from 'antd';

export const TabsStyled = styled(Tabs)`
  width: 100%;

  .ant-tabs-content-holder {
    height: 100%;
    overflow: auto;
  }
`;
