import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { TTariffBase } from 'stores/Trip/Trip.interface';

export abstract class TariffBaseModel implements TTariffBase {
  name: string;

  region: string;

  rideCostPerKm: number;

  transportType: TransportTypes;

  organizationId?: string;

  protected constructor(tariff: TTariffBase) {
    this.name = tariff.name;
    this.region = tariff.region;
    this.rideCostPerKm = tariff.rideCostPerKm;
    this.transportType = tariff.transportType;
    this.organizationId = tariff.organizationId;
  }
}
