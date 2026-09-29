import { PersonalRegistryFilters, PersonalUIVisibilityDTO } from 'stores/PersonalSearch/PersonalSearch.interface';

export interface SortSetting {
  property: string;
  directionAsc: boolean;
}

export interface TableRecord {
  id: string;
  requestIdVisible: string | null;
  mvzVisible: string;
  desiredDateVisible: string;
  controlPeriodOfPayment: string;
  orderPaymentFormationStartDateVisible: string;
  passengerFioVisible: string | string[];
  requestStatusVisible: string | null;
  plannedPriceVisible: string | number;
  tripFactPriceVisible: string | number;
  plannedRangeVisible: string | number;
  paymentPeriodVisible: string | number;
  departmentCodeVisible: string;
  sharedRideOwnerVisible: string | null | undefined;
  carVisible: string;
  carEngineVolumeVisible: string | number;
  tripTypeVisible: string;
}

export interface RequestBodyPersonalParams {
  withFilters: boolean;
  withView?: boolean;
  filters: string;
  organizationId?: string;
}

export interface reportParams {
  reportType: 'REGISTRY' | 'COMPENSATIONS';
  withFilters: boolean;
  withView: boolean;
  filters: PersonalRegistryFilters;
  columnsVisibility?: PersonalUIVisibilityDTO;
}

export enum ApprovalLabel {
  'Дата утверждения поездки',
}

export enum RegistrationCertificateLabel {
  'Номер свидетельства о браке',
}

export enum PassengerInfo {
  contractorName = 'contractorName',
  department = 'department',
  organizationId = 'organizationId',
  costCenter = 'costCenter',
  personnelNumber = 'personnelNumber',
  fullName = 'fullName',
  travelingBehaviorVaries = 'travelingBehaviorVaries',
  purpose = 'purpose',
}

export interface CoopTripPassengerInfo {
  fullName: string;
  personnelNumber: string;
  contractorName: string;
  department: string;
  organizationId: string;
  purpose: string;
  travelingBehaviorVaries: string;
  costCenter: string;
  creationTime: number | string;
}

export interface DepartmentLevels {
  department1: string[];
  department2: string[];
  department3: string[];
  department4: string[];
  department5: string[];
  department6: string[];
  departmentLevel: number;
}
