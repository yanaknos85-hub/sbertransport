import React from 'react';
import {
  Route, Switch, useLocation, useRouteMatch
} from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { Breadcrumb } from 'shared/components/Breadcrumbs';
import styled from 'styled-components';

const StyledNotFound = styled.div`
  margin: 2em;
  padding: 2em;
  justify-self: center;
`;

const NotFound: React.FC = () => <StyledNotFound>Страница не найдена</StyledNotFound>;

export const CustomRoute: React.FC<{ bc?: string } & React.ComponentProps<typeof Route>> = ({
  bc: breadcrumb,
  render,
  component: Component,
  children,
  ...routeProps
}) => {
  const history = History();
  const location = useLocation();
  const match = useRouteMatch();

  return (
    <Route {...routeProps}>
      {breadcrumb ? <Breadcrumb title={breadcrumb} /> : null}

      {Component ? (
        <Component {...{
          history, location, match,
        }}
        />
      ) : render ? (
        render({
          history, location, match,
        })
      ) : (
        children
      )}
    </Route>
  );
};

export const CustomSwitch: React.FC<React.ComponentProps<typeof Switch>> = ({ children }) => (
  <Switch>
    {children}
    <CustomRoute component={NotFound} />
  </Switch>
);
