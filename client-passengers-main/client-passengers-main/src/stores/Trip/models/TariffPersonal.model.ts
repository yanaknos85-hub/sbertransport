/* eslint-disable @typescript-eslint/no-explicit-any */
import { TTariffPersonal, TTariffPriceDetails } from 'stores/Trip/Trip.interface';

import { TariffBaseModel } from './TariffBase.model';

export class TariffPersonal extends TariffBaseModel implements TTariffPersonal {
  seasonStart: string;

  seasonEnd: string;

  priceDetails?: TTariffPriceDetails;

  rewardForPassenger?: number;

  constructor(tariff: TTariffPersonal) {
    super(tariff);
    // Todo:Когда будут глобальные изменения поменять на обязательные
    this.seasonStart = tariff.regionId ?? '';
    this.seasonEnd = tariff.seasonEnd;
    this.priceDetails = tariff.priceDetails;
    this.rewardForPassenger = tariff.rewardForPassenger;
  }
}
