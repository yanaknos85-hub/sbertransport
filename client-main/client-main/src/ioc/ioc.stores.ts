import { IRootStore, DISelfStore } from '@sber-sbertransport/mf-core';
import { interfaces } from 'inversify';

import { DILimitsStore } from 'stores/Limits/DILimits.store';

import { ILimitsRequestService, ILimitsRequestStore } from 'stores/Limits/LimitsRequest.interface';
import { DILimitsRequestService } from 'stores/Limits/DILimitsRequest.service';
import { DILimitsRequestStore } from 'stores/Limits/DILimitsRequest.store';

import { IFilesService, IFilesStore } from 'stores/Files/Files.interface';
import { DIFilesService } from 'stores/Files/DIFiles.service';
import { DIFilesStore } from 'stores/Files/DIFiles.store';

import { ISettingsStore } from 'stores/Settings/Settings.interface';
import { DISettingsStore } from 'stores/Settings/DISettings.store';

import { IDelegatesService, IDelegatesStore } from 'stores/Delegates/Delegates.interface';
import { DIDelegatesService } from 'stores/Delegates/DIDelegates.service';
import { DIDelegatesStore } from 'stores/Delegates/DIDelegates.store';

import { ICargoTariffService, ICargoTariffStore } from 'stores/CargoTariff/CargoTariff.interface';
import { DICargoTariffService } from 'stores/CargoTariff/DICargoTariff.service';
import { DICargoTariffStore } from 'stores/CargoTariff/DICargoTariff.store';

import { ICargoTypeService, ICargoTypeStore } from 'stores/CargoType/CargoType.interface';
import { DICargoTypeService } from 'stores/CargoType/DICargoType.service';
import { DICargoTypeStore } from 'stores/CargoType/DICargoType.store';

import { ICargoListService, ICargoListStore } from 'stores/CargoList/CargoList.interface';
import { DICargoListService } from 'stores/CargoList/DICargoList.service';
import { DICargoListStore } from 'stores/CargoList/DICargoList.store';

import { DITripService } from 'stores/Trip/DITrip.service';
import { DITripStore } from 'stores/Trip/DITrip.store';
import { ITripService, ITripStore } from 'stores/Trip/Trip.interface';

import { ITransportTypesService, ITransportTypesStore } from 'stores/TransportTypes/TransportTypes.interface';
import { DITransportTypesService } from 'stores/TransportTypes/DITransportTypes.service';
import { DITransportTypesStore } from 'stores/TransportTypes/DITransportTypes.store';

import { ICorporateService, ICorporateStore } from 'stores/Corporate/Corporate.interface';
import { DICorporateService } from 'stores/Corporate/DICorporate.service';
import { DICorporateStore } from 'stores/Corporate/DICorporate.store';

import { IAddressService, IAddressStore } from 'stores/Address/Address.interface';
import { DIAddressService } from 'stores/Address/DIAddress.service';
import { DIAddressStore } from 'stores/Address/DIAddress.store';

import { MappedStore } from 'stores/Mapped/DIMapped.store';

import { DIGeoService } from 'stores/Geo/DIGeo.service';
import { DIGeoStore } from 'stores/Geo/DIGeo.store';
import { IGeoService, IGeoStore } from 'stores/Geo/Geo.interface';

import { DILimitsService } from 'stores/Limits/DILimits.service';
import { ILimitsService, ILimitsStore } from 'stores/Limits/Limit.interface';

import { StoreNames } from './ioc.storeNames';

import { TYPES } from './ioc.types';

export type IAppStore = {
  [StoreNames.geoStore]: DIGeoStore;
  [StoreNames.limitsStore]: DILimitsStore;
  [StoreNames.limitsRequestStore]: DILimitsRequestStore;
  [StoreNames.settingsStore]: DISettingsStore;
  [StoreNames.delegatesStore]: DIDelegatesStore;
  [StoreNames.cargoTariffStore]: DICargoTariffStore;
  [StoreNames.cargoTypeStore]: DICargoTypeStore;
  [StoreNames.cargoListStore]: DICargoListStore;
  [StoreNames.tripStore]: DITripStore;
  [StoreNames.transportTypesStore]: DITransportTypesStore;
  [StoreNames.corporateStore]: DICorporateStore;
  [StoreNames.addressStore]: DIAddressStore;
  [StoreNames.mappedStore]: MappedStore;

} & IRootStore
& { [StoreNames.selfStore]: DISelfStore };

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

  container.bind<ICargoTariffService>(TYPES.ICargoTariffService).to(DICargoTariffService);
  container.bind<ICargoTariffStore>(TYPES.ICargoTariffStore).to(DICargoTariffStore);

  container.bind<ICargoTypeService>(TYPES.ICargoTypeService).to(DICargoTypeService);
  container.bind<ICargoTypeStore>(TYPES.ICargoTypeStore).to(DICargoTypeStore);

  container.bind<ICargoListService>(TYPES.ICargoListService).to(DICargoListService);
  container.bind<ICargoListStore>(TYPES.ICargoListStore).to(DICargoListStore);

  container.bind<ITripService>(TYPES.ITripService).to(DITripService);
  container.bind<ITripStore>(TYPES.ITripStore).to(DITripStore);

  container.bind<ITransportTypesService>(TYPES.ITransportTypesService).to(DITransportTypesService);
  container.bind<ITransportTypesStore>(TYPES.ITransportTypesStore).to(DITransportTypesStore);

  container.bind<ICorporateService>(TYPES.ICorporateService).to(DICorporateService);
  container.bind<ICorporateStore>(TYPES.ICorporateStore).to(DICorporateStore);

  container.bind<IAddressService>(TYPES.IAddressService).to(DIAddressService);
  container.bind<IAddressStore>(TYPES.IAddressStore).to(DIAddressStore);

  container.bind<MappedStore>(TYPES.MappedStore).to(MappedStore);

  return {
    [StoreNames.geoStore]: container.get<IGeoStore>(TYPES.IGeoStore),
    [StoreNames.limitsStore]: container.get<ILimitsStore>(TYPES.ILimitsStore),
    [StoreNames.limitsRequestStore]: container.get<ILimitsRequestStore>(TYPES.ILimitsRequestStore),
    [StoreNames.settingsStore]: container.get<ISettingsStore>(TYPES.ISettingsStore),
    [StoreNames.delegatesStore]: container.get<IDelegatesStore>(TYPES.IDelegatesStore),
    [StoreNames.cargoTariffStore]: container.get<ICargoTariffStore>(TYPES.ICargoTariffStore),
    [StoreNames.cargoTypeStore]: container.get<ICargoTypeStore>(TYPES.ICargoTypeStore),
    [StoreNames.cargoListStore]: container.get<ICargoListStore>(TYPES.ICargoListStore),
    [StoreNames.tripStore]: container.get<ITripStore>(TYPES.ITripStore),
    [StoreNames.transportTypesStore]: container.get<ITransportTypesStore>(TYPES.ITransportTypesStore),
    [StoreNames.corporateStore]: container.get<ICorporateStore>(TYPES.ICorporateStore),
    [StoreNames.addressStore]: container.get<IAddressStore>(TYPES.IAddressStore),
    [StoreNames.mappedStore]: container.get<MappedStore>(TYPES.MappedStore),
  } as IAppStore;
}
