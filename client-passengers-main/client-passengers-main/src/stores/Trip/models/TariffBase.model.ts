/* eslint-disable @typescript-eslint/no-explicit-any */
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TTariffBase } from 'stores/Trip/Trip.interface';

export abstract class TariffBaseModel implements TTariffBase {
  transportType?: TransportTypeEnum;

  name: string | undefined;

  regionId?: string;

  rideCostPerKm: number;

  organizationId?: string;

  constructor(tariff: TTariffBase) {
    // Todo: уточнение у бэка и добавление реиона id в модель
    this.name = tariff.name;
    this.regionId = tariff.regionId ?? '';
    this.rideCostPerKm = tariff.rideCostPerKm;
    this.transportType = tariff.transportType;
    this.organizationId = tariff.organizationId;
  }
}
