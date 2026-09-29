import styled from 'styled-components';
import { Tabs } from 'antd';

export const TabsStyled = styled(Tabs)<any>`
  height: 100%;

  .ant-tabs-content-holder {
    height: 100%;
    overflow: auto;
  }

  .ant-tabs {
    height: 100%;
    overflow: auto;
  }

  .ant-tabs-content {
    height: 100%;
  }
`;
