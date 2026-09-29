import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { ITripPrice, TaxiClass } from 'stores/Trip/Trip.interface';

export class TripPriceModel implements ITripPrice {
  id: string;

  cost: number;

  transportType: TransportTypes;

  taxiClass?: TaxiClass;

  constructor(tariff: ITripPrice) {
    this.id = tariff.id;
    this.cost = Math.ceil(tariff.cost);
    this.transportType = tariff.transportType;
    this.taxiClass = tariff?.taxiClass;
  }
}
