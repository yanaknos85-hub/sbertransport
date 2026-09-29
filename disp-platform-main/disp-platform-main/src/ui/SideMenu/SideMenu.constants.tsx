import React from 'react';
import { AppTitles } from 'constants/app.constants';
import * as routes from 'constants/routes.constants';

import { faGlobeAmericas as prod } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IconProp } from '@fortawesome/fontawesome-svg-core';

export const items = [
  {
    path: routes.TRIPS,
    name: AppTitles.Disp,
    icon: <FontAwesomeIcon icon={prod as IconProp} />,
  },
  {
    path: routes.SCHEDULE,
    name: AppTitles.Crews,
    icon: <FontAwesomeIcon icon={prod as IconProp} />,
  },
  {
    path: routes.STAFF,
    name: AppTitles.Staff,
    icon: <FontAwesomeIcon icon={prod as IconProp} />,
  },
  {
    path: routes.REPORTS,
    name: AppTitles.Reports,
    icon: <FontAwesomeIcon icon={prod as IconProp} />,
  },
  {
    path: routes.SUPPORT,
    name: AppTitles.Support,
    icon: <FontAwesomeIcon icon={prod as IconProp} />,
  },
];
