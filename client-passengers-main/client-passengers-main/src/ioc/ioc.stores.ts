import { IRootStore } from '@sber-sbertransport/mf-core';
import { interfaces } from 'inversify';

import { DIEmpoloyeeExtService } from 'stores/EmployeeExt/DIEmployeeExt.service';
import { DIEmployeeExtStore } from 'stores/EmployeeExt/DiEmployeeExt.store';
import { IEmpoloyeeExtService, IEmpoloyeeExtStore } from 'stores/EmployeeExt/EmployeeExt.interface';

import { DILimitsStore } from 'stores/Limits/DILimits.store';

import { DILimitsRequestService } from 'stores/Limits/DILimitsRequest.service';
import { DILimitsRequestStore } from 'stores/Limits/DILimitsRequest.store';
import { ILimitsRequestService, ILimitsRequestStore } from 'stores/Limits/LimitsRequest.interface';

import { DIFilesService } from 'stores/Files/DIFiles.service';
import { DIFilesStore } from 'stores/Files/DIFiles.store';
import { IFilesService, IFilesStore } from 'stores/Files/Files.interface';

import { DISettingsStore } from 'stores/Settings/DISettings.store';
import { ISettingsStore } from 'stores/Settings/Settings.interface';

import { IDelegatesService, IDelegatesStore } from 'stores/Delegates/Delegates.interface';
import { DIDelegatesService } from 'stores/Delegates/DIDelegates.service';
import { DIDelegatesStore } from 'stores/Delegates/DIDelegates.store';

import { DITransportTypesService } from 'stores/TransportTypes/DITransportTypes.service';
import { DITransportTypesStore } from 'stores/TransportTypes/DITransportTypes.store';
import { ITransportTypesService, ITransportTypesStore } from 'stores/TransportTypes/TransportTypes.interface';

import { ICorporateService, ICorporateStore } from 'stores/Corporate/Corporate.interface';
import { DICorporateService } from 'stores/Corporate/DICorporate.service';
import { DICorporateStore } from 'stores/Corporate/DICorporate.store';

import { DITripService } from 'stores/Trip/DITrip.service';
import { DITripStore } from 'stores/Trip/DITrip.store';
import { ITripService, ITripStore } from 'stores/Trip/Trip.interface';

import { IAddressService, IAddressStore } from 'stores/Address/Address.interface';
import { DIAddressService } from 'stores/Address/DIAddress.service';
import { DIAddressStore } from 'stores/Address/DIAddress.store';

import { DISRMMassServiceMultiple } from 'stores/SRMMassMultiple/DISRMMassMultiple.service';
import { DISRMMassStoreMultiple } from 'stores/SRMMassMultiple/DISRMMassMultiple.store';
import { ISRMMassServiceMultiple, ISRMMassStoreMultiple } from 'stores/SRMMassMultiple/SRMMassMultiple.interface';

import { MappedStore } from 'stores/Mapped/DIMapped.store';

import { DIGeoService } from 'stores/Geo/DIGeo.service';
import { DIGeoStore } from 'stores/Geo/DIGeo.store';
import { IGeoService, IGeoStore } from 'stores/Geo/Geo.interface';
import { DILimitsService } from 'stores/Limits/DILimits.service';
import { ILimitsService, ILimitsStore } from 'stores/Limits/Limit.interface';
import { DIPurpose } from 'stores/Purpose/DIPurpose.store';
import { IPurposeStore } from 'stores/Purpose/purpose.interface';
import { StoreNames } from './ioc.storeNames';
import { TYPES } from './ioc.types';

import { IApprovalsStore } from 'stores/Approvals/Approvals.interface';
import { DIApprovalsStore } from 'stores/Approvals/DIApprovals.store';
import { DIFraudStore } from 'stores/Fraud/DIFraud.store';
import { IFraudStore } from 'stores/Fraud/Fraud.interface';

export type IAppStore = {
  [StoreNames.employeeExtStore]: DIEmployeeExtStore;
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
  [StoreNames.purposeStore]: DIPurpose;
  [StoreNames.approvalsStore]: DIApprovalsStore;
  [StoreNames.srmMassStoreMultiple]: DISRMMassStoreMultiple;
  [StoreNames.fraudStore]: DIFraudStore;
} & IRootStore;

export default function initAppStore(container: interfaces.Container): IAppStore {
  container.bind<IEmpoloyeeExtService>(TYPES.IEmployeeExtService).to(DIEmpoloyeeExtService);
  container.bind<IEmpoloyeeExtStore>(TYPES.IEmployeeExtStore).to(DIEmployeeExtStore);

  container.bind<IGeoService>(TYPES.IGeoService).to(DIGeoService);
  container.bind<IGeoStore>(TYPES.IGeoStore).to(DIGeoStore);

  container.bind<IPurposeStore>(TYPES.IPurposeStore).to(DIPurpose);

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

  container.bind<ISRMMassServiceMultiple>(TYPES.ISRMMassServiceMultiple).to(DISRMMassServiceMultiple);
  container.bind<ISRMMassStoreMultiple>(TYPES.ISRMMassStoreMultiple).to(DISRMMassStoreMultiple);

  container.bind<MappedStore>(TYPES.MappedStore).to(MappedStore);

  container.bind<IApprovalsStore>(TYPES.IApprovalsStore).to(DIApprovalsStore);

  container.bind<IFraudStore>(TYPES.IFraudStore).to(DIFraudStore);

  return {
    [StoreNames.employeeExtStore]: container.get<IEmpoloyeeExtStore>(TYPES.IEmployeeExtStore),
    [StoreNames.geoStore]: container.get<IGeoStore>(TYPES.IGeoStore),
    [StoreNames.purposeStore]: container.get<IPurposeStore>(TYPES.IPurposeStore),
    [StoreNames.limitsStore]: container.get<ILimitsStore>(TYPES.ILimitsStore),
    [StoreNames.limitsRequestStore]: container.get<ILimitsRequestStore>(TYPES.ILimitsRequestStore),
    [StoreNames.settingsStore]: container.get<ISettingsStore>(TYPES.ISettingsStore),
    [StoreNames.delegatesStore]: container.get<IDelegatesStore>(TYPES.IDelegatesStore),
    [StoreNames.transportTypesStore]: container.get<ITransportTypesStore>(TYPES.ITransportTypesStore),
    [StoreNames.corporateStore]: container.get<ICorporateStore>(TYPES.ICorporateStore),
    [StoreNames.addressStore]: container.get<IAddressStore>(TYPES.IAddressStore),
    [StoreNames.tripStore]: container.get<ITripStore>(TYPES.ITripStore),
    [StoreNames.mappedStore]: container.get<MappedStore>(TYPES.MappedStore),
    [StoreNames.approvalsStore]: container.get<IApprovalsStore>(TYPES.IApprovalsStore),
    [StoreNames.srmMassStoreMultiple]: container.get<ISRMMassStoreMultiple>(TYPES.ISRMMassStoreMultiple),
    [StoreNames.fraudStore]: container.get<IFraudStore>(TYPES.IFraudStore),
  } as IAppStore;
}
