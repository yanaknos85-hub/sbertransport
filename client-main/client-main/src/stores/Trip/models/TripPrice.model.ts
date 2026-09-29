import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { IPriceDetails, ITripTariff, TTaxiClass } from 'stores/Trip/Trip.interface';

export class TripPriceModel implements ITripTariff {
  id: string;

  cost: number;

  transportType: { id: string; name: TransportTypeEnum };

  taxiClass?: TTaxiClass;

  contractorId?: string;

  bonusCost?: number;

  priceDetails?: IPriceDetails;

  limitAvailable?: boolean;

  constructor(tariff: ITripTariff) {
    this.id = tariff.id;
    this.cost = Math.ceil(tariff.cost);
    this.transportType = tariff.transportType;
    this.taxiClass = tariff?.taxiClass;
    this.contractorId = tariff?.contractorId;
    this.limitAvailable = tariff?.limitAvailable;

    this.bonusCost = tariff?.bonusCost;
    this.priceDetails = tariff?.priceDetails;
  }
}
