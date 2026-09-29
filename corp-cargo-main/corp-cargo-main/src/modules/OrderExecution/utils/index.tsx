import { AvailableStatus } from 'stores/StatusTypes/StatusTypes.interface';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { TransportStatuses } from 'modules/ServiceMetrics/TransportStatuses';
import { DetailViewStatuses, TripFinishedStatuses } from '../types/DetailedViewStatuses';


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
      if (!TransportStatuses.cargo.includes(item.name)) {
        return false;
      }
      const currentIndex = TransportStatuses.cargo.indexOf(currentStatus.status);
      const itemIndex = TransportStatuses.cargo.indexOf(item.name);
      if (currentIndex > itemIndex) {
        return false;
      }
    }
    if (currentStatus.status && TransportStatuses.cargo.includes(currentStatus.status)) {
      if (TripFinishedStatuses.includes(currentStatus.status) && item.name === 'CARGO_CANCELED') {
        return false;
      }
    }
    return item.index && currentStatus.index ? item.index >= currentStatus.index : false;
  };

  const availableStatuses = indexedStatuses.filter(item => showedStatuses(item));

  return availableStatuses.reduce((acc: { label: string; value: string }[], { name, rusName }) => {
    const item = acc.find(el => el.label === rusName);
    item ? (item.value += `,${name}`) : acc.push({ label: rusName, value: name });
    return acc;
  }, []);
};

