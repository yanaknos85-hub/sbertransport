import styled from 'styled-components';
import { List } from 'antd';

export const ListStyled = styled(List)`
  margin-top: 24px;

  @media (max-width: 500px) {
    margin-top: 16px;
  }

  .ant-spin-nested-loading {
    min-height: 300px;
  }

  .ant-list-items {
    display: flex;
    flex-direction: column;
    gap: 16px;

    @media (max-width: 500px) {
      gap: 8px;
    }
  }
`;
