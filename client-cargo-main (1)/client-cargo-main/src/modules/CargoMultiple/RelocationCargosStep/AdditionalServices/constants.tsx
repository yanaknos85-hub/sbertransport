import React from 'react';
import { FieldType } from 'shared/form/Field/Field';

import { ReactComponent as Info } from '../icons/info.svg';
import { ServiceItems, ServiceItemsName, ServiceItemsNameEnum } from './types';

// Переключатели
export const serviceItems: ServiceItems[] = [
  {
    title: 'Грузчики',
    name: ServiceItemsName[ServiceItemsNameEnum.LOADERS],
    description: 'Помощь грузчиков',
    price: 'от 2000 ₽',
    icon: <Info />,
    infoDescription: 'Расчёт количества грузчиков производится автоматически в зависимости от объема и веса груза',
    value: false,
  },

  /* Отключаем до уточнения требований */
  // {
  //   title: 'Поднять на 4 этаж',
  //   name: ServiceItemsName[ServiceItemsNameEnum.LIFT],
  //   description: 'По лестнице или на лифте',
  //   price: 'от 700 ₽',
  //   button: 'Доступно при заказе грузчиков',
  //   availableWhenLoadersOrdered: true,
  //   value: false,
  // },
  // {
  //   title: 'Перевозка автомобиля',
  //   name: ServiceItemsName[ServiceItemsNameEnum.CAR],
  //   description: 'Доставка автомобиля в город назначения',
  //   price: 'от 15 500 ₽',
  //   value: false,
  // },
];

export const formFields = {
  floorField: {
    label: 'Этаж',
    name: 'address',
    initialValue: null,
    type: FieldType.input,
    index: 1,
  },
};
