import React from 'react';
import { AppTitles } from 'constants/app.constants';
import * as routes from 'constants/routes.constants';

import { faGlobeAmericas as prod } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IconProp } from '@fortawesome/fontawesome-svg-core';

export const items = [
  {
    name: AppTitles.Autopark,
    icon: <FontAwesomeIcon icon={prod as IconProp} />,
    subItems: [
      {
        path: routes.AUTOPARKS,
        name: AppTitles.Autoparks,
        icon: <FontAwesomeIcon icon={prod as IconProp} />,
      },
      {
        path: routes.VEHICLES,
        name: AppTitles.Vehicles,
        icon: <FontAwesomeIcon icon={prod as IconProp} />,
      },
      {
        path: routes.RELEASE_ON_LINE_LINK,
        name: AppTitles.ReleaseOnLine,
        icon: <FontAwesomeIcon icon={prod as IconProp} />,
      },
      {
        path: routes.MAINTENANCE_LINK,
        name: AppTitles.Maintenance,
        icon: <FontAwesomeIcon icon={prod as IconProp} />,
      },
    ],
  },
  {
    path: routes.TELEMATICS,
    name: AppTitles.Telematics,
    icon: <FontAwesomeIcon icon={prod as IconProp} />,
  },
];
