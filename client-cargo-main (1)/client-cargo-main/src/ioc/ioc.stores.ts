import { IRootStore } from '@sber-sbertransport/mf-core';
import { interfaces } from 'inversify';

import { IAddressService, IAddressStore } from 'stores/Address/Address.interface';
import { DIAddressService } from 'stores/Address/DIAddress.service';
import { DIAddressStore } from 'stores/Address/DIAddress.store';
import { ICargoService, ICargoStore } from 'stores/Cargo/Cargo.interface';
import { DICargoService } from 'stores/Cargo/DICargo.service';
import { DICargoStore } from 'stores/Cargo/DICargo.store';
import { ICargoMassServiceMultiple, ICargoMassStoreMultiple } from 'stores/CargoMassMultiple/CargoMassMultiple.interface';
import { DICargoMassServiceMultiple } from 'stores/CargoMassMultiple/DICargoMassMultiple.service';
import { DICargoMassStoreMultiple } from 'stores/CargoMassMultiple/DICargoMassMultiple.store';
import { ICargosService, ICargosStore } from 'stores/Cargos/Cargos.interface';
import { DICargosService } from 'stores/Cargos/DICargos.service';
import { DICargosStore } from 'stores/Cargos/DICargos.store';
import { ICargoTariffService, ICargoTariffStore } from 'stores/CargoTariff/CargoTariff.interface';
import { DICargoTariffService } from 'stores/CargoTariff/DICargoTariff.service';
import { DICargoTariffStore } from 'stores/CargoTariff/DICargoTariff.store';
import { ICargoTypeService, ICargoTypeStore } from 'stores/CargoType/CargoType.interface';
import { DICargoTypeService } from 'stores/CargoType/DICargoType.service';
import { DICargoTypeStore } from 'stores/CargoType/DICargoType.store';
import { ICompensationService, ICompensationStore } from 'stores/Compensations/Compensation.interface';
import { DICompensationStore } from 'stores/Compensations/DICompensation.store';
import { ICorporateService, ICorporateStore } from 'stores/Corporate/Corporate.interface';
import { DICorporateService } from 'stores/Corporate/DICorporate.service';
import { DICorporateStore } from 'stores/Corporate/DICorporate.store';
import { IDelegatesService, IDelegatesStore } from 'stores/Delegates/Delegates.interface';
import { DIDelegatesService } from 'stores/Delegates/DIDelegates.service';
import { DIDelegatesStore } from 'stores/Delegates/DIDelegates.store';
import { DIExchangeStore } from 'stores/Exchange/DIExchange.store';
import { IExchangeStore } from 'stores/Exchange/Exchange.interface';
import { DIFilesService } from 'stores/Files/DIFiles.service';
import { DIFilesStore } from 'stores/Files/DIFiles.store';
import { IFilesService, IFilesStore } from 'stores/Files/Files.interface';
import { DIGeoService } from 'stores/Geo/DIGeo.service';
import { DIGeoStore } from 'stores/Geo/DIGeo.store';
import { IGeoService, IGeoStore } from 'stores/Geo/Geo.interface';
import { DILimitsService } from 'stores/Limits/DILimits.service';
import { DILimitsStore } from 'stores/Limits/DILimits.store';
import { DILimitsRequestService } from 'stores/Limits/DILimitsRequest.service';
import { DILimitsRequestStore } from 'stores/Limits/DILimitsRequest.store';
import { ILimitsService, ILimitsStore } from 'stores/Limits/Limit.interface';
import { ILimitsRequestService, ILimitsRequestStore } from 'stores/Limits/LimitsRequest.interface';
import { MappedStore } from 'stores/Mapped/DIMapped.store';
import { DISettingsStore } from 'stores/Settings/DISettings.store';
import { ISettingsStore } from 'stores/Settings/Settings.interface';
import { DITransportTypesService } from 'stores/TransportTypes/DITransportTypes.service';
import { DITransportTypesStore } from 'stores/TransportTypes/DITransportTypes.store';
import { ITransportTypesService, ITransportTypesStore } from 'stores/TransportTypes/TransportTypes.interface';
import { DITripService } from 'stores/Trip/DITrip.service';
import { DITripStore } from 'stores/Trip/DITrip.store';
import { ITripService, ITripStore } from 'stores/Trip/Trip.interface';

import { DICompensationService } from '../stores/Compensations/DICompensation.service';
import { StoreNames } from './ioc.storeNames';
import { TYPES } from './ioc.types';

export type IAppStore = {
  [StoreNames.geoStore]: DIGeoStore;
  [StoreNames.limitsStore]: DILimitsStore;
  [StoreNames.limitsRequestStore]: DILimitsRequestStore;
  [StoreNames.settingsStore]: DISettingsStore;
  [StoreNames.delegatesStore]: DIDelegatesStore;
  [StoreNames.transportTypesStore]: DITransportTypesStore;
  [StoreNames.corporateStore]: DICorporateStore;
  [StoreNames.addressStore]: DIAddressStore;
  [StoreNames.tripStore]: DITripStore;
  [StoreNames.mappedStore]: MappedStore;

  [StoreNames.cargoTariffStore]: DICargoTariffStore;
  [StoreNames.cargoTypeStore]: DICargoTypeStore;
  [StoreNames.cargoStore]: DICargoStore;
  [StoreNames.exchangeStore]: DIExchangeStore;
  [StoreNames.cargoMassStoreMultiple]: DICargoMassStoreMultiple;
  [StoreNames.cargosStore]: DICargosStore;
  [StoreNames.compensationStore]: DICompensationStore;
} & IRootStore;

export default function initAppStore(container: interfaces.Container): IAppStore {
  container.bind<IGeoService>(TYPES.IGeoService).to(DIGeoService);
  container.bind<IGeoStore>(TYPES.IGeoStore).to(DIGeoStore);

  container.bind<ILimitsService>(TYPES.ILimitsServiceNew).to(DILimitsService);
  container.bind<ILimitsStore>(TYPES.ILimitsStore).to(DILimitsStore);

  container.bind<ILimitsRequestService>(TYPES.ILimitsRequestService).to(DILimitsRequestService);
  container.bind<ILimitsRequestStore>(TYPES.ILimitsRequestStore).to(DILimitsRequestStore);

  container.bind<IFilesService>(TYPES.IFilesService).to(DIFilesService);
  container.bind<IFilesStore>(TYPES.IFilesStore).to(DIFilesStore);

  container.bind<ISettingsStore>(TYPES.ISettingsStore).to(DISettingsStore);

  container.bind<IDelegatesService>(TYPES.IDelegatesService).to(DIDelegatesService);
  container.bind<IDelegatesStore>(TYPES.IDelegatesStore).to(DIDelegatesStore);

  container.bind<ITransportTypesService>(TYPES.ITransportTypesService).to(DITransportTypesService);
  container.bind<ITransportTypesStore>(TYPES.ITransportTypesStore).to(DITransportTypesStore);

  container.bind<ICorporateService>(TYPES.ICorporateService).to(DICorporateService);
  container.bind<ICorporateStore>(TYPES.ICorporateStore).to(DICorporateStore);

  container.bind<ITripService>(TYPES.ITripService).to(DITripService);
  container.bind<ITripStore>(TYPES.ITripStore).to(DITripStore);

  container.bind<IAddressService>(TYPES.IAddressService).to(DIAddressService);
  container.bind<IAddressStore>(TYPES.IAddressStore).to(DIAddressStore);

  container.bind<MappedStore>(TYPES.MappedStore).to(MappedStore);

  container.bind<ICargoTariffService>(TYPES.ICargoTariffService).to(DICargoTariffService);
  container.bind<ICargoTariffStore>(TYPES.ICargoTariffStore).to(DICargoTariffStore);

  container.bind<ICargoTypeService>(TYPES.ICargoTypeService).to(DICargoTypeService);
  container.bind<ICargoTypeStore>(TYPES.ICargoTypeStore).to(DICargoTypeStore);

  container.bind<ICargoService>(TYPES.ICargoService).to(DICargoService);
  container.bind<ICargoStore>(TYPES.ICargoStore).to(DICargoStore);

  container.bind<IExchangeStore>(TYPES.IExchangeStore).to(DIExchangeStore);

  container.bind<ICargoMassServiceMultiple>(TYPES.ICargoMassServiceMultiple).to(DICargoMassServiceMultiple);
  container.bind<ICargoMassStoreMultiple>(TYPES.ICargoMassStoreMultiple).to(DICargoMassStoreMultiple);

  container.bind<ICargosService>(TYPES.ICargosService).to(DICargosService);
  container.bind<ICargosStore>(TYPES.ICargosStore).to(DICargosStore);

  // Компенсация
  container.bind<ICompensationService>(TYPES.ICompensationService).to(DICompensationService);
  container.bind<ICompensationStore>(TYPES.ICompensationStore).to(DICompensationStore);

  return {
    [StoreNames.geoStore]: container.get<IGeoStore>(TYPES.IGeoStore),
    [StoreNames.limitsStore]: container.get<ILimitsStore>(TYPES.ILimitsStore),
    [StoreNames.limitsRequestStore]: container.get<ILimitsRequestStore>(TYPES.ILimitsRequestStore),
    [StoreNames.settingsStore]: container.get<ISettingsStore>(TYPES.ISettingsStore),
    [StoreNames.delegatesStore]: container.get<IDelegatesStore>(TYPES.IDelegatesStore),
    [StoreNames.transportTypesStore]: container.get<ITransportTypesStore>(TYPES.ITransportTypesStore),
    [StoreNames.corporateStore]: container.get<ICorporateStore>(TYPES.ICorporateStore),
    [StoreNames.addressStore]: container.get<IAddressStore>(TYPES.IAddressStore),
    [StoreNames.tripStore]: container.get<ITripStore>(TYPES.ITripStore),
    [StoreNames.mappedStore]: container.get<MappedStore>(TYPES.MappedStore),

    [StoreNames.cargoTariffStore]: container.get<ICargoTariffStore>(TYPES.ICargoTariffStore),
    [StoreNames.cargoTypeStore]: container.get<ICargoTypeStore>(TYPES.ICargoTypeStore),
    [StoreNames.cargoStore]: container.get<ICargoStore>(TYPES.ICargoStore),
    [StoreNames.exchangeStore]: container.get<DIExchangeStore>(TYPES.IExchangeStore),
    [StoreNames.cargoMassStoreMultiple]: container.get<ICargoMassStoreMultiple>(TYPES.ICargoMassStoreMultiple),
    [StoreNames.cargosStore]: container.get<ICargosStore>(TYPES.ICargosStore),
    // Компенсация
    [StoreNames.compensationStore]: container.get<ICompensationStore>(TYPES.ICompensationStore),
  } as IAppStore;
}
