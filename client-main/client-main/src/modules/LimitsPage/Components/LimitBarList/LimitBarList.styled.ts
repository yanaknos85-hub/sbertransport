import styled from 'styled-components';
import { List } from 'antd';

export const StyledList = styled(List)`
  div div ul {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;
  }

  @media (max-width: 500px) {
    width: 100%;
    margin-top: 8px;
  }
`;

export const StyledListItem = styled(List.Item)<any>`
  border-bottom: none !important;
  padding: 0;

  @media (max-width: 500px) {
    padding: 4px 0;
    width: 100%;
  }
`;
