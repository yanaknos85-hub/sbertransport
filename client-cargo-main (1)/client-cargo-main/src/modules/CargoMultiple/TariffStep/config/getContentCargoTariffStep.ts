import CargoCourier from 'shared/components/Images/transportTypes/courier.png';
import CargoDedicated from 'shared/components/Images/transportTypes/dedicated.png';
import CargoInterregional from 'shared/components/Images/transportTypes/interregional.png';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';

import { DeliveryUrgencyEnum, TransportTypeEnum, transportTypeTitles } from '../../../../types/Cargo';

interface TariffField {
  name: TransportTypeEnum;
  title: string;
  description: string;
  image: string;
}

interface Content {
  tariff: TariffField[];
  tariffMulti: Record<TransportTypeEnum, TariffField>;
  field: any;
}

export const getContentCargoTariffStep = (): Content => {
  const tariff = [
    {
      name: TransportTypeEnum.COURIER,
      title: transportTypeTitles[TransportTypeEnum.COURIER],
      description: 'Для грузов до 30 кг',
      image: CargoCourier,
    },
    {
      name: TransportTypeEnum.DEDICATED,
      title: transportTypeTitles[TransportTypeEnum.DEDICATED],
      description: 'Для грузов более 30 кг',
      image: CargoDedicated,
    },
    {
      name: TransportTypeEnum.INDIVIDUAL,
      title: transportTypeTitles[TransportTypeEnum.INDIVIDUAL],
      description: 'Для грузов более 30 кг',
      image: CargoDedicated,
    },
    {
      name: TransportTypeEnum.INTERREGIONAL,
      title: transportTypeTitles[TransportTypeEnum.INTERREGIONAL],
      description: '',
      image: CargoInterregional,
    },
    {
      name: TransportTypeEnum.DOMESTIC_COURIER,
      title: transportTypeTitles[TransportTypeEnum.DOMESTIC_COURIER],
      description: 'Для грузов до 20 кг',
      image: CargoCourier,
    },
  ];

  const tariffMulti = {
    [TransportTypeEnum.COURIER]: {
      name: TransportTypeEnum.COURIER,
      title: transportTypeTitles[TransportTypeEnum.COURIER],
      description: 'Для грузов до 30 кг',
      image: CargoCourier,
    },
    [TransportTypeEnum.DEDICATED]: {
      name: TransportTypeEnum.DEDICATED,
      title: transportTypeTitles[TransportTypeEnum.DEDICATED],
      description: 'Для грузов более 30 кг',
      image: CargoDedicated,
    },
    [TransportTypeEnum.INDIVIDUAL]: {
      name: TransportTypeEnum.INDIVIDUAL,
      title: transportTypeTitles[TransportTypeEnum.INDIVIDUAL],
      description: 'Для грузов более 30 кг',
      image: CargoDedicated,
    },
    [TransportTypeEnum.INTERREGIONAL]: {
      name: TransportTypeEnum.INTERREGIONAL,
      title: transportTypeTitles[TransportTypeEnum.INTERREGIONAL],
      description: '',
      image: CargoInterregional,
    },
    [TransportTypeEnum.DOMESTIC_COURIER]: {
      name: TransportTypeEnum.DOMESTIC_COURIER,
      title: transportTypeTitles[TransportTypeEnum.DOMESTIC_COURIER],
      description: 'Для грузов до 20 кг',
      image: CargoCourier,
    },
  };

  const field = {
    deliveryUrgency: {
      label: 'Срочность',
      name: 'deliveryUrgency',
      type: FieldType.radio,
      rules: [ValidationRules.general.required],
      params: {
        options: [
          { label: 'Стандарт', value: DeliveryUrgencyEnum.standart },
          { label: 'Экспресс', value: DeliveryUrgencyEnum.express },
        ],
      },
    },
    comment: {
      label: 'Комментарий к заказу',
      name: 'comment',
      type: FieldType.textarea,
      rules: [ValidationRules.general.maxLength(500)],
      params: {
        maxLength: 500,
        rows: 3,
      },
    },
  };

  return {
    tariff,
    tariffMulti,
    field,
  };
};
