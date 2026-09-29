import * as t from 'io-ts';
import { TransportTypeEnum } from '../TransportTypesDelegates/TransportTypes.interface';
import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
import * as tt from '../../utils/io-ts';
import { EmployeesAttribute } from '../EmployeesAttribute/EmployeesAttribute.interface';
import { EmployeeStatus, OrgStructureType } from '../../constants/constants.app';

const TransportTypeEnumCol = ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum);

export const DelegateEmployee = t.intersection([
  t.type({
    humanReadableId: t.string,
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
    attributes: t.array(EmployeesAttribute),
  }),
  t.partial({
    status: t.keyof(EmployeeStatus),
    organizationId: tt.uuid,
    departmentId: tt.uuid,
    positionId: tt.uuid,
    organizationName: t.string,
    departmentName: t.string,
    availableTransportTypes: t.UnknownArray,
    userId: t.string,
    personnelNumber: t.string,
    roles: t.array(t.string),
    patronymic: t.string,
    mobilePhone: t.string,
    email: t.string,
    supervisorId: t.string,
    delegatedById: t.string,
    personalCars: t.UnknownArray,
    positionName: t.string,
    gender: t.string,
    orgStructureType: ioTypeFromEnum<OrgStructureType>('OrgStructureType', OrgStructureType),
  }),
]);

export const Delegate = t.strict({
  id: t.string,
  supervisorId: t.string,
  delegateId: t.string,
  startDate: t.string,
  endDate: t.string,
  transportType: TransportTypeEnumCol,
  delegateEmployee: DelegateEmployee,
});

export type Delegate = t.TypeOf<typeof Delegate>;

export interface UseDelegateListInterface {
  namesWithInitials: Record<string, string>;

  goToNewDelegate(): void;

  deleteHandler(id?: string): void;

  path: string;
}

export enum StoreNames {
  configStore = 'configStore',
  selfStore = 'selfStore',
  rootStore = 'rootStore',
  authStore = 'authStore',
  settingsStore = 'settingsStore',
  geoStore = 'geoStore',
  cargoStore = 'cargoStore',
  cargosStore = 'cargosStore',
  cargoMassStore = 'cargoMassStore',
  cargoTariffStore = 'cargoTariffStore',
  cargoTypeStore = 'cargoTypeStore',
  employeeStore = 'employeeStore',
  limitsStore = 'limitsStore',
  limitsRequestStore = 'limitsRequestStore',
  corporateStore = 'corporateStore',
  tripStore = 'tripStore',
  addressStore = 'addressStore',
  transportTypesStore = 'transportTypesStore',
  filesStore = 'filesStore',
  delegatesStore = 'delegatesStore',
  mappedStore = 'mappedStore',
  corporateFiltersStore = 'corporateFiltersStore',
}
