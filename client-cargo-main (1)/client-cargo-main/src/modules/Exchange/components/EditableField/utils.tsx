import {
  AvailableStatus,
  RouteStatusEnum,
  TransportStatuses,
  TransportTypes
} from './constants';
import { DetailViewStatuses } from './DetailedViewStatuses';

export const getAvailableStatuses = (
  transportType: string | undefined,
  status: string | undefined,
  statuses: AvailableStatus[]
): { label: string; value: string }[] => {
  const indexedStatuses = statuses.map(item => {
    DetailViewStatuses.forEach((element, index) => {
      if (element.indexOf(item.name) !== -1) {
        item.index = index;
      }
    });

    return item;
  });

  const currentStatus: { status?: string; index?: number } = {
    status,
    index: indexedStatuses.find(({ name }) => status === name)?.index || 0,
  };

  const showedStatuses = (item: AvailableStatus) => {
    if (
      currentStatus.status
      && (TransportTypes.COURIER === transportType
      || TransportTypes.DEDICATED === transportType
      || TransportTypes.INTERREGIONAL === transportType
      || TransportTypes.DOMESTIC_COURIER === transportType
      || TransportTypes.INDIVIDUAL === transportType)
    ) {
      if (
        !TransportStatuses.cargo.includes(item.name)
        || item.name === RouteStatusEnum.CARGO_CANCELED
      ) {
        return false;
      }
      const currentIndex = TransportStatuses.cargo.indexOf(currentStatus.status);
      const itemIndex = TransportStatuses.cargo.indexOf(item.name);
      if (currentIndex > itemIndex) {
        return false;
      }
    }

    if (currentStatus.status === RouteStatusEnum.CARGO_SHIPMENT_FINISHED || item.name === RouteStatusEnum.CARGO_CANCELED) {
      return false;
    }

    return item.index && currentStatus.index ? item.index >= currentStatus.index : false;
  };
  /* Логика отображения статусов в журнале маршрутов */
  let availableStatuses = indexedStatuses.filter(item => showedStatuses(item));
  availableStatuses = availableStatuses.slice(1);

  return availableStatuses.reduce((acc: { label: string; value: string }[], { name, rusName }) => {
    const item = acc.find(el => el.label === rusName);
    item ? (item.value += `,${name}`) : acc.push({ label: rusName, value: name });
    return acc;
  }, []);
};
