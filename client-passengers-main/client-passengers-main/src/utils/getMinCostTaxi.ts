import { ITripTariff } from 'stores/Trip/Trip.interface';

export const getMinCostTaxi = (arr: ITripTariff[]) => {
  const taxis = arr.filter(item => item.transportType.name === 'TAXI');

  if (taxis.length === 0) {
    return undefined;
  }

  const minCostTaxi = taxis.reduce((min, item) => {
    return item.cost < min.cost ? item : min;
  });

  return {
    cost: minCostTaxi.cost,
    tariffId: minCostTaxi.id,
  };
};
