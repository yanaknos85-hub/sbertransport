import { createBrowserHistory } from 'history';
import * as React from 'react';
import { Router } from 'react-router-dom';

import { Breadcrumb, Breadcrumbs, BreadcrumbsProvider } from '.';

export default {
  title: 'Breadcrumbs',
  component: Breadcrumbs,
};

const history = createBrowserHistory();

export const BreadcrumbsStory: React.FC = () => (
  <Router history={history}>
    <BreadcrumbsProvider>
      <Breadcrumb title="Breadcrumb 1" />
      <Breadcrumb title="Breadcrumb 1" />
      <Breadcrumbs />
    </BreadcrumbsProvider>
  </Router>
);
