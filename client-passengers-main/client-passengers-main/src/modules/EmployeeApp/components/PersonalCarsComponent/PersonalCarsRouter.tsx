import React, { FC } from 'react';
import { Route, Switch, useRouteMatch } from 'react-router-dom';

import { PersonalCarsCard } from './PersonalCarsCard';
import { PersonalCarsList } from './PersonalCarsList';

const PersonalCarsRouter: FC = () => {
  const match = useRouteMatch();
  return (
    <Switch>
      <Route
        exact={true}
        path={`${match.path}`}
        component={PersonalCarsList}
      />
      <Route path={`${match.path}/:personalCarId`} component={PersonalCarsCard} />
    </Switch>
  );
};

export default PersonalCarsRouter;
