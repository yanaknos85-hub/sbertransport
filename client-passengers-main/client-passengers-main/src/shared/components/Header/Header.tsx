import React, { FC } from 'react';
import styled from 'styled-components';

import { UserMenu } from 'modules/EmployeeApp/components/UserMenu/UserMenu';

const Logo = styled.div`
  font-size: 120%;
  font-weight: bold;
`;

const Spacer = styled.div`
  flex-grow: 1;
`;

const Header: FC = () => (
  <>
    <Logo>СберТранспорт</Logo>
    <Spacer />
    <UserMenu />
  </>
);

export default Header;
