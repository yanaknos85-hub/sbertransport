import styled from 'styled-components';
import { Tabs } from 'antd';

export const Wrapper = styled.div`
  margin-top: 16px;
`;

export const DepartmentHeadWrapper = styled.div`
  margin-top: -16px;
`;

export const TabsStyled = styled(Tabs)`
  .ant-tabs-tab-btn {
    font-size: 16px;
    font-weight: 400;
    line-height: 24px;
    letter-spacing: -0.3px;
    font-family: SB Sans Text;
    color: #737373;
  }

  .ant-tabs-tab-active .ant-tabs-tab-btn {
    font-weight: 600;
    color: #262626;
  }

  .ant-tabs-tab {
    padding: 0 0 19px 0;
  }
`;
