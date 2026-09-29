import Icon from '@ant-design/icons';
import React from 'react';

import Drag from './assets/DragIcon';
import LocationMark from './assets/LocationMarkIcon';
import Route from './assets/RouteIcon';

const LocationMarkIcon = (props: any): JSX.Element => <Icon component={LocationMark} {...props} />;
const RouteIcon = (props: any): JSX.Element => <Icon component={Route} {...props} />;
const DragIcon = (props: any): JSX.Element => <Icon component={Drag} {...props} />;

export { LocationMarkIcon, RouteIcon, DragIcon };
