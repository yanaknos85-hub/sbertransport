
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITaxi, ITripTariff } from 'stores/Trip/Trip.interface';

export const useTaxiTariffs = ({
  tariffsInfo,
  option,
}: {
  tariffsInfo: ITripTariff[];
  option: ITaxi;
}): {
  tariff: ITripTariff | undefined;
} => {
  const { taxiClass, transportType } = option;

  const getActualTariff = (): ITripTariff | undefined => tariffsInfo?.find((x: ITripTariff) => {
    if (x.transportType.name !== TransportTypeEnum.TAXI) {
      return x.transportType.name === transportType;
    }
    return x.taxiClass === taxiClass;
  });

  /**
   * В качестве стоимости tariffCost выбирается первый подходящий из списка
   * тарифов, подходящий по типу транспорта
   */
  const tariff = getActualTariff();

  return { tariff };
};
