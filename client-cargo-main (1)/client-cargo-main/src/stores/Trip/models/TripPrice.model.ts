import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITripTariff, TTaxiClass } from 'stores/Trip/Trip.interface';

export class TripPriceModel implements ITripTariff {
  id: string;

  cost: number;

  transportType: { id: string; name: TransportTypeEnum };

  taxiClass?: TTaxiClass;

  contractorId?: string;

  bonusCost?: number;

  constructor(tariff: ITripTariff) {
    this.id = tariff.id;
    this.cost = Math.ceil(tariff.cost);
    this.transportType = tariff.transportType;
    this.taxiClass = tariff?.taxiClass;
    this.contractorId = tariff?.contractorId;

    this.bonusCost = tariff?.bonusCost;
  }
}
