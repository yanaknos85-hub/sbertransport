import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React from 'react';
import { NavLink } from 'react-router-dom';
import styled from 'styled-components';

const StyledNavLink = styled(NavLink)`
  height: 40px;
  border-radius: 8px;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 10px;
  color: #4c4c4c;
  font-size: 14px;
  letter-spacing: -0.3px;
  line-height: 22px;
  margin-bottom: 10px;

  &.active {
    color: #10bf6a;
    background-color: white;
  }
`;

const Icon = styled(FontAwesomeIcon)`
  margin-right: 10px;
  font-size: 16px;
`;

const MenuItem: React.FC<
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  { icon?: any } & React.ComponentProps<typeof NavLink>
> = ({
  icon,
  children,
  ...navLinkProps
}) => (
  <StyledNavLink {...navLinkProps} activeClassName="active">
    {icon ? <Icon icon={icon} /> : null}
    {children}
  </StyledNavLink>
);

export default MenuItem;
