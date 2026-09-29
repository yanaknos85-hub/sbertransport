import { useEffect, useState } from 'react';

import { CargoListItem } from 'types/Cargo';

import { TariffCost } from 'stores/CargoTariff/CargoTariff.interface';

interface Props {
  cargoList: CargoListItem[];
  tariffCost: TariffCost | null;
}

interface ReturnProps {
  cost: number;
  baseCost: number;
  loaderCost: number;
  expressCost: number;
  time: number;
  length: number;
  height: number;
  weight: number;
  width: number;
  volume: number;
  occupiedPlacesCount: number;
}

const useTotal = ({ cargoList, tariffCost }: Props): ReturnProps => {
  const [total, setTotal] = useState<ReturnProps>({
    cost: 0,
    baseCost: 0,
    loaderCost: 0,
    expressCost: 0,
    time: 0,
    length: 0,
    height: 0,
    weight: 0,
    width: 0,
    volume: 0,
    occupiedPlacesCount: 0,
  });

  useEffect(() => {
    const time = tariffCost?.time;
    const cost = tariffCost?.cost;
    const baseCost = tariffCost?.priceDetails.baseCost;
    const loaderCost = tariffCost?.priceDetails.loaderCost;
    const expressCost = tariffCost?.priceDetails.expressCost;

    const sum = cargoList.reduce(
      (acc, curr) => ({
        length: acc.length + curr.length,
        height: acc.height + curr.height,
        weight: acc.weight + curr.weight,
        width: acc.width + curr.width,
        volume: acc.volume + curr.volume,
        occupiedPlacesCount: acc.occupiedPlacesCount + curr.occupiedPlacesCount,
      }),
      {
        length: 0,
        height: 0,
        weight: 0,
        width: 0,
        volume: 0,
        occupiedPlacesCount: 0,
      }
    );

    setTotal({
      cost: cost || 0,
      baseCost: baseCost || 0,
      expressCost: expressCost || 0,
      loaderCost: loaderCost || 0,
      time: time || 0,
      length: sum.length,
      height: sum.height,
      weight: sum.weight,
      width: sum.width,
      volume: sum.volume,
      occupiedPlacesCount: sum.occupiedPlacesCount,
    });

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cargoList, tariffCost]);

  return total;
};

export default useTotal;
