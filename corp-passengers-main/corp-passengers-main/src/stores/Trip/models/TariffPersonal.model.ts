import { TTariffPersonal, TTariffPriceDetails } from 'stores/Trip/Trip.interface';

import { TariffBaseModel } from './TariffBase.model';

export class TariffPersonal extends TariffBaseModel implements TTariffPersonal {
  coefficient: number;

  seasonStart: string;

  seasonEnd: string;

  priceDetails?: TTariffPriceDetails;

  rewardForPassenger?: number;

  constructor(tariff: TTariffPersonal) {
    super(tariff);

    this.coefficient = tariff.coefficient;
    this.seasonStart = tariff.region;
    this.seasonEnd = tariff.seasonEnd;
    this.priceDetails = tariff.priceDetails;
    this.rewardForPassenger = tariff.rewardForPassenger;
  }
}
