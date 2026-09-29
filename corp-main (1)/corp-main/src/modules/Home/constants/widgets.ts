import { LazyExoticComponent, lazy } from 'react';

import { Translation } from 'i18n/ru';

import { Widgets } from '../types/Home.types';
import MFLoader from 'mf/MFLoader';

export const widgets: {
  id: Widgets;
  titleKey: keyof Translation['Widgets'];
}[] = [
  { id: Widgets.BUDGET, titleKey: 'budget' },
  { id: Widgets.INDICATORS, titleKey: 'serviceIndicators' },
];

export const widgetComponents: Record<Widgets, LazyExoticComponent<React.ComponentType<unknown>>> = {
  [Widgets.BUDGET]: lazy(() => MFLoader(import('platform/modules/Home/Budget'))),
  [Widgets.INDICATORS]: lazy(() => MFLoader(import('platform/modules/Home/ServiceIndicators'))),
};
