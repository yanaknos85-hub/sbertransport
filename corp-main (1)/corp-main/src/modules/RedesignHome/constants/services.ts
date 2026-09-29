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
    ['passengers']: Taxi,
    [TransportTypes.PERSONAL]: Personal,
    [TransportTypes.PUBLIC]: Public,
    [TransportTypes.CARSHARING]: Carsharing,
    ['cargo']: Courier,
    [TransportTypes.DEDICATED]: Dedicated,
    ['carService']: Repair,
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

export const services = [
  {
    name: 'passengers',
    title: 'Пассажирские перевозки',
    numberConnectedServices: 0,
    maxServices: 5,
  },
  {
    name: 'cargo',
    title: 'Грузовые перевозки',
    numberConnectedServices: 0,
    maxServices: 4,
  },
  {
    name: 'carService',
    title: 'Содержание транспорта',
    numberConnectedServices: 0,
    maxServices: 8,
  },
];

export const chartsData = {
  budget: {
    dataBudgetMain: [
      {
        type: 'PLANNED',
        value: 60000,
      },
      {
        type: 'SPEND',
        value: 6000,
      },
      {
        type: 'RESERVE',
        value: 245,
      },
    ],
    dataBudgetDetails: [
      {
        serviceType: 'PASSENGER',
        dataService: [
          {
            type: 'PLANNED',
            value: 176000,
          },
          {
            type: 'FACT',
            value: 250000,
          },
          {
            type: 'SPEND',
            value: 0,
          },
          {
            type: 'RESERVE',
            value: 5000,
          },
        ],
      },
      {
        serviceType: 'AUTOSERVICE',
        dataService: [
          {
            type: 'PLANNED',
            value: 50000,
          },
          {
            type: 'FACT',
            value: 250000,
          },
          {
            type: 'SPEND',
            value: 0,
          },
          {
            type: 'RESERVE',
            value: 1000,
          },
        ],
      },
      {
        serviceType: 'CARGO',
        dataService: [
          {
            type: 'PLANNED',
            value: 730000,
          },
          {
            type: 'FACT',
            value: 60000,
          },
          {
            type: 'SPEND',
            value: 30000,
          },
          {
            type: 'RESERVE',
            value: 10000,
          },
        ],
      },
    ],
  },
  sla: {
    dataSla: [
      {
        type: 'WITHOUTVIOLATION',
        value: 85,
      },
      {
        type: 'WITHVIOLATION',
        value: 10,
      },
      {
        type: 'CANCELED',
        value: 5,
      },
      {
        type: 'NORMAL',
        value: 93,
      },
    ],
  },
  csi: {
    dataCsi: [
      {
        type: 'CUSTOMERSATISFACTION',
        value: 76,
      },
      {
        type: 'NORMAL',
        value: 93,
      },
    ],
  },
  mau: {
    dataMau: [
      {
        type: 'UNIQUEUSERS',
        value: 12883,
      },
    ],
  },
};

export const typesServicePassengers = [
  {
    name: 'OFFICIAL',
    title: 'Служебный транспорт',
    checked: false,
    description: 'Сервис для организации заказа такси с подключением действующих контрагентов по API или с подключением собственного автопарка',
  },
  {
    name: 'TAXI',
    title: 'Такси',
    checked: false,
    description: 'Cервис для организации поездок сотрудник с подключенными контрагентами',
  },
  {
    name: 'PUBLIC',
    title: 'Компенсация затрат',
    checked: false,
    description: 'Cервис для компенсации использования личного и общественного транспорта в производственных целях: заказ, фиксация точек маршрута, выплата',
  },
  {
    name: 'GROUP_TRANSFER',
    title: 'Трансфер',
    checked: false,
    description: 'Сервис для организации встреч в комплексе с сопровождением и закрытием через модуль диспетчерская и МП водитель с расширенными тарифами и приемом точных данных',
  },
  {
    name: 'CARSHARING',
    title: 'Каршеринг',
    checked: false,
    description: 'Сервис для организации процесса использования сервиса Каршеринг в производственных целях, который позволяет экономить на такси и повышает мобильность сотрудников',
  },
];

export const typesServiceCargo = [
  {
    name: 'DEDICATED',
    title: 'Доставка сборного груза',
    checked: false,
    description: 'Сервис для отправления грузов, которые могут быть объединены с другими посылками в рамках одного транспортного средства. Позволяет оптимизировать логистику и снизить затраты на доставку',
  },
  {
    name: 'INDIVIDUAL',
    title: 'Доставка выделенным транспортом',
    checked: false,
    description: 'Сервис для перевозки грузов с использованием отдельного транспортного средства, предназначенного исключительно для вашего заказа. Обеспечивает высокую скорость и безопасность доставки крупных грузов',
  },
  {
    name: 'COURIER',
    title: 'Курьерская доставка',
    checked: false,
    description: 'Сервис для быстрой доставки небольших грузов (до 30 кг) курьером. Подходит для документов, посылок и других мелких отправлений, требующих оперативной доставки',
  },
  {
    name: 'INTERREGIONAL',
    title: 'Межрегиональная доставка',
    checked: false,
    description: 'Сервис для перевозки крупногабаритных грузов (более 30 кг) между регионами. Включает полный комплекс логистических решений для безопасной и своевременной доставки на дальние расстояния',
  },
];

export const typesServiceCarService = [
  {
    name: 'REPAIR',
    title: 'Ремонт',
    checked: false,
    description: 'Сервис по управлению ремонтами автопарка, контроль исполнения заявок и формирование отчетности по выполненным работам',
  },
  {
    name: 'TECHNICAL_MAINTENANCE',
    title: 'Техническое обслуживание',
    checked: false,
    description: 'Сервис по управлению техническим обслуживанием автопарка, контроль исполнения заявок и формирование отчетности по выполненным работам',
  },
  {
    name: 'TIRE_SERVICE',
    title: 'Шиномонтаж',
    checked: false,
    description: 'Сервис по управлению техническим обслуживанием автопарка, контроль исполнения заявок и формирование отчетности по выполненным работам',
  },
  {
    name: 'TOW_TRUCK',
    title: 'Эвакуатор',
    checked: false,
    description: 'Модуль для заказа эвакуатора по запросу автопарка, отслеживания выполнения заказов и подготовки отчетности по предоставленным услугам',
  },
  {
    name: 'WASHING',
    title: 'Мойка',
    checked: false,
    description: 'Модель для заказа услуг мойки по запросу автопарка, отслеживания выполнения заказов и подготовки отчетности по предоставленным услугам',
  },
  {
    name: 'REGISTRATION',
    title: 'Регистрация',
    checked: false,
    description: 'Модуль регистрации транспортного средства в ГИБДД и получения государственного номера',
  },
  {
    name: 'ELECTRONIC_WAYBILL',
    title: 'Электронный путевой лист',
    checked: false,
    description: 'Комплексное решение для организации процесса выпуска автомобилей на линию: создание путевых листов, подтверждение прохождения медицинского и технического контроля, выпуск на линию, формирование QR кода и электронного путевого листа, подписание Титулов согласно законодательству РФ',
  },
  {
    name: 'PRETRIP_INSPECTIONS',
    title: 'Предрейсовые осмотры',
    checked: false,
    description: 'Организация процесса прохождения медицинского и технического осмотров для выпуска на линию транспортных средств Технический осмотр  – модуль, в котором AI модели проверяют готовность авто к выходу на линию через мобильное приложение АС Сбертранспорт. Водитель проходит проверки, отправляя фото в Монитор исполнения заявок «Телемеханик». Модели AI помогают Механику оценивать техническое состояние автомобиля, используя отдельные модели для каждого узла, что сокращает время принятия решения. Медицинский осмотр – модуль для проверки готовности водителей по медицинским показателям к выходу на линию.',
  },
  {
    name: 'ORDER_RECOGNITION',
    title: 'Распознование Заказ-наряда с использованием AI',
    checked: false,
    description: 'Контроль отклонений, классификация и анализ гарантийных и повторяющихся работ',
  },
  {
    name: 'REFILL',
    title: 'Заправка',
    checked: false,
    description: 'Сервис заправки топливом через отображение АЗС на карте и контроль отклонений по расходу топлива по каждому автомобилю',
  },
];
