import React from 'react';
import {
  Route, Switch, useHistory, useLocation, useRouteMatch
} from 'react-router-dom';
import styled from 'styled-components';

import { Breadcrumb } from 'shared/components/Breadcrumbs';

const StyledNotFound = styled.div`
  margin: 2em;
  padding: 2em;
  justify-self: center;
`;

const NotFound: React.FC = () => <StyledNotFound>Страница не найдена</StyledNotFound>;

export const CustomRoute: React.FC<{ bc?: string; bcPath?: string } & React.ComponentProps<typeof Route>> = ({
  bc: breadcrumb,
  bcPath,
  render,
  component: Component,
  children,
  ...routeProps
}) => {
  const history = useHistory();
  const location = useLocation();
  const match = useRouteMatch();

  return (
    <Route {...routeProps}>
      {breadcrumb ? <Breadcrumb title={breadcrumb} path={bcPath} /> : null}

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
