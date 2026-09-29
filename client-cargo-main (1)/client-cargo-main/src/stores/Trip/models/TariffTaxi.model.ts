import { TaxiClassEnum, TTariffTaxi } from 'stores/Trip/Trip.interface';

import { TariffBaseModel } from './TariffBase.model';

export class TariffTaxi extends TariffBaseModel implements TTariffTaxi {
  id?: string;

  taxiClass: TaxiClassEnum;

  rideCostPerMin: number;

  waitCostPerMin: number;

  freeWaitingTime: number;

  priceDetails?: any;

  deptStruct?: string;

  deptName?: string;

  vehicleType?: string;

  minRideCost?: number;

  savingsDeviationPct?: number;

  distanceDeviationKm?: number;

  timeDeviationMin?: number;

  maxCapacity?: number;

  minCancelTimeMin?: number;

  tollRoads?: boolean;

  historicalTraffic?: boolean;

  minServiceTime?: number;

  tariffId?: string;

  contractorId?: string;

  constructor(tariff: TTariffTaxi) {
    super(tariff);
    this.id = tariff.id;
    this.taxiClass = tariff.taxiClass;
    this.rideCostPerMin = tariff.rideCostPerMin;
    this.priceDetails = tariff.priceDetails;
    this.waitCostPerMin = tariff.waitCostPerMin;
    this.freeWaitingTime = tariff.freeWaitingTime;
    this.priceDetails = tariff.priceDetails;
    this.deptStruct = tariff.deptStruct;
    this.deptName = tariff.deptName;
    this.vehicleType = tariff.vehicleType;
    this.minRideCost = tariff.minRideCost;
    this.savingsDeviationPct = tariff.savingsDeviationPct;
    this.distanceDeviationKm = tariff.distanceDeviationKm;
    this.timeDeviationMin = tariff.timeDeviationMin;
    this.maxCapacity = tariff.maxCapacity;
    this.minCancelTimeMin = tariff.minCancelTimeMin;
    this.tollRoads = tariff.tollRoads;
    this.historicalTraffic = tariff.historicalTraffic;
    this.minServiceTime = tariff.minServiceTime;
    this.tariffId = tariff.tariffId;
    this.contractorId = tariff?.contractorId;
  }
}
