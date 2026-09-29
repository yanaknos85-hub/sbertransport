import { NavLink } from 'react-router-dom';
import styled from 'styled-components';

export const StyledBreadcrumbs = styled.nav`
  display: flex;
  height: 60px;
  align-items: center;
  flex: 0 0 auto;

  @media (max-width: 500px) {
    padding: 0 8px;
  }
`;

export const StyledBreadcrumb = styled(NavLink)`
  font-size: 16px;
  padding-left: 8px;
  padding-right: 8px;

  &:first-child {
    padding-left: 0;
  }
`;

export const Spacer = styled.div`
  height: 16px;
  flex: 0 0 16px;
`;
