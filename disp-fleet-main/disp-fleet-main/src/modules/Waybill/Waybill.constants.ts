export enum Transportation {
  OwnNeeds = 'СН',
}

export const TransportationInfo: Record<Transportation, string> = {
  [Transportation.OwnNeeds]: 'СН — перевозки для собственных нужд',
} as const;

export enum Communication {
  Urban = 'Г',
}

export const CommunicationInfo: Record<Communication, string> = {
  [Communication.Urban]: 'Г — городское',
} as const;

export enum TelemechType {
  IN = 'IN',
  OUT = 'OUT',
}

export enum TitleType {
  FIRST = 'FIRST',
  SECOND = 'SECOND',
  THIRD = 'THIRD',
  FOURTH = 'FOURTH',
  FIFTH = 'FIFTH',
}

export enum DispatcherOrganizationField {
  name = 'organizationName',
  regionCode = 'organizationRegionCode',
  phone = 'organizationPhone',
  ogrn = 'organizationMsrn',
  tin = 'organizationTin',
}

export enum TransportField {
  transportId = 'transportId',
  stateNumber = 'stateNumber',
  brand = 'brand',
  model = 'model',
  transportType = 'transportType',
}

export enum DriverField {
  driverId = 'driverId',
  personnelNumber = 'driverPersonnelNumber',
  fullName = 'driverFullName',
  organizationName = 'driverOrganizationName',
  departmentName = 'driverDepartmentName',
  departmentId = 'driverDepartmentId',
  tin = 'driverTin',
  drivingLicenseSeries = 'drivingLicenseSeries',
  drivingLicenseNumber = 'drivingLicenseNumber',
  drivingLicenseIssueDate = 'drivingLicenseIssueDate',
}

export enum CreateFieldName {
  ewbUuid = 'ewbUuid',
  starDate = 'starDate',
  finishDate = 'finishDate',
  transportationType = 'transportationType',
  communicationType = 'communicationType',

  ewbHumanReadableId = 'ewbHumanReadableId',
}

export const selectableFields = [
  'startDate', 'finishDate', 'stateNumber', 'driverFullName',
];
