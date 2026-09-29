import React from 'react';
import styled from 'styled-components';
import { ReactComponent as Logo } from 'shared/assets/svg/logo.svg';
import { Link } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import { AdminMenu } from '../AdminMenu';
import { Organization } from '../../shared/components/Organization';

const Spacer = styled.div`
  flex-grow: 1;
`;

const Header = () => (
  <>
    <Link to={routes.MAIN}>
      <Logo />
    </Link>
    <Organization />
    <Spacer />
    <AdminMenu />
  </>
);

export default Header;
