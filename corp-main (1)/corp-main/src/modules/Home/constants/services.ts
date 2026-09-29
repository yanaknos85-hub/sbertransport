import { Translation, useTranslation } from 'i18n';
import { useMemo } from 'react';

import {
  Taxi,
  Personal,
  Public,
  Carsharing,
  Courier,
  Dedicated,
  CarDefault,
  TruckDefault,
  Repair
} from '../static';
import { ServicesTypes, TransportTypes } from '../types/Home.types';

const defaultValues = {
  car: CarDefault,
  truck: TruckDefault,
  color: '#eee',
};

export const transportTypesByServices: Record<ServicesTypes, Set<TransportTypes>> = {
  [ServicesTypes.ALL]: new Set(Object.values(TransportTypes)),
  [ServicesTypes.CARGO]: new Set([TransportTypes.DEDICATED, TransportTypes.COURIER, TransportTypes.INTERREGIONAL]),
  [ServicesTypes.PASSENGERS]: new Set([
    TransportTypes.CARSHARING,
    TransportTypes.PERSONAL,
    TransportTypes.PUBLIC,
    TransportTypes.TAXI,
    TransportTypes.WALK,
    TransportTypes.PERSONAL,
    TransportTypes.BICYCLE,
  ]),
  [ServicesTypes.REPAIR]: new Set([TransportTypes.OFFICIAL, TransportTypes.SPECIAL, TransportTypes.PRIVATE]),
};

export const servicesImage = new Proxy(
  {
    [TransportTypes.TAXI]: Taxi,
    [TransportTypes.PERSONAL]: Personal,
    [TransportTypes.PUBLIC]: Public,
    [TransportTypes.CARSHARING]: Carsharing,
    [TransportTypes.COURIER]: Courier,
    [TransportTypes.DEDICATED]: Dedicated,
    [TransportTypes.OFFICIAL]: Repair,
  } as Record<string, string>,
  {
    get(target, name: TransportTypes) {
      return (
        target[name]
        || (transportTypesByServices[ServicesTypes.CARGO].has(name) ? defaultValues.truck : defaultValues.car)
      );
    },
  }
);

export const servicesSelect: { titleKey: keyof Translation['Widgets']['Budget']; value: ServicesTypes }[] = [
  { titleKey: 'allServices', value: ServicesTypes.ALL },
  { titleKey: 'cargo', value: ServicesTypes.CARGO },
  { titleKey: 'passengers', value: ServicesTypes.PASSENGERS },
  { titleKey: 'repair', value: ServicesTypes.REPAIR },
];

export const useLegend = (): Record<
  TransportTypes | Exclude<ServicesTypes, 'ALL'>,
  { color: string; image: string; title: string }
> => {
  const { t } = useTranslation();

  const legendBudget = useMemo(
    () => ({
      [TransportTypes.DEDICATED]: {
        color: '#6979F7',
        image: servicesImage[TransportTypes.DEDICATED],
        title: t.Widgets.Legends.dedicated,
      },
      [TransportTypes.COURIER]: {
        color: '#FF9A32',
        image: servicesImage[TransportTypes.COURIER],
        title: t.Widgets.Legends.courier,
      },
      [TransportTypes.DOMESTIC_COURIER]: {
        color: '#FF9A90',
        image: servicesImage[TransportTypes.COURIER],
        title: t.Widgets.Legends.domesticCourier,
      },
      [TransportTypes.INTERREGIONAL]: {
        color: '#059956',
        image: servicesImage[TransportTypes.INTERREGIONAL],
        title: t.Widgets.Legends.interregional,
      },
      [TransportTypes.OFFICIAL]: {
        color: '#059957',
        image: servicesImage[TransportTypes.OFFICIAL],
        title: t.Widgets.Legends.official,
      },
      [TransportTypes.SPECIAL]: {
        color: '#059958',
        image: servicesImage[TransportTypes.OFFICIAL],
        title: t.Widgets.Legends.special,
      },
      [TransportTypes.PRIVATE]: {
        color: '#059959',
        image: servicesImage[TransportTypes.OFFICIAL],
        title: t.Widgets.Legends.private,
      },
      [TransportTypes.CARSHARING]: {
        color: '#FF9A32',
        image: servicesImage[TransportTypes.CARSHARING],
        title: t.Widgets.Legends.carsharing,
      },
      [TransportTypes.PUBLIC]: {
        color: '#0091FF',
        image: servicesImage[TransportTypes.PUBLIC],
        title: t.Widgets.Legends.public,
      },
      [TransportTypes.TAXI]: {
        color: '#FFC914',
        image: servicesImage[TransportTypes.TAXI],
        title: t.Widgets.Legends.taxi,
      },
      [TransportTypes.WALK]: {
        color: defaultValues.color,
        image: servicesImage[TransportTypes.WALK],
        title: t.Widgets.Legends.walk,
      },
      [TransportTypes.PERSONAL]: {
        color: '#10BF6A',
        image: servicesImage[TransportTypes.PERSONAL],
        title: t.Widgets.Legends.personal,
      },
      [TransportTypes.BICYCLE]: {
        color: defaultValues.color,
        image: servicesImage[TransportTypes.BICYCLE],
        title: t.Widgets.Legends.bicycle,
      },
      [ServicesTypes.CARGO]: {
        color: '#6979F7',
        image: servicesImage[TransportTypes.DEDICATED],
        title: t.Widgets.Budget.cargo,
      },
      [ServicesTypes.PASSENGERS]: {
        color: '#FFC914',
        image: servicesImage[TransportTypes.TAXI],
        title: t.Widgets.Budget.passengers,
      },
      [ServicesTypes.REPAIR]: {
        color: '#059956',
        image: servicesImage[TransportTypes.OFFICIAL],
        title: t.Widgets.Budget.repair,
      },
    }),
    [t]
  );

  return legendBudget;
};
