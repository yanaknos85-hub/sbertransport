/* eslint-disable @typescript-eslint/no-duplicate-enum-values */
import { Moment } from 'moment';

import { Ownership } from '../../../constants/vehicles.constants';

import { FieldsEnum } from '../constants/vehiclesForm.fields';

export interface VehicleFormValues {
  // Собственность
  [FieldsEnum.ownership]: Ownership;

  // Транспортное средство
  [FieldsEnum.vcRegistrationNumber]: string;
  [FieldsEnum.vcColor]: string;
  [FieldsEnum.vcPassengerSeatsCount]: number;

  // Свидетельство о браке
  [FieldsEnum.docs_mc_series]: string;
  [FieldsEnum.docs_mc_number]: string;
  [FieldsEnum.docs_mc_dateOfIssue]: Moment;

  // Водительское удостоверение
  [FieldsEnum.docs_dl_fio]: string;
  [FieldsEnum.docs_dl_dateOfIssue]: Moment;
  [FieldsEnum.docs_dl_validUntil]: Moment;
  [FieldsEnum.docs_dl_series]: string;
  [FieldsEnum.docs_dl_number]: string;
  [FieldsEnum.docs_dl_issuedBy]: string;
  [FieldsEnum.docs_dl_whereIssued]: string;
  [FieldsEnum.docs_dl_category]: string;
  [FieldsEnum.docs_dl_file]: [];

  // ПТС
  [FieldsEnum.docs_pts_vin]: string;
  [FieldsEnum.docs_pts_brandName]: string;
  [FieldsEnum.docs_pts_model]: string;
  [FieldsEnum.docs_pts_engineVolume]: number;
  [FieldsEnum.docs_pts_enginePower]: string;
  [FieldsEnum.docs_pts_file]: [];

  // ОСАГО
  [FieldsEnum.docs_osago_series]: string;
  [FieldsEnum.docs_osago_number]: string;
  [FieldsEnum.docs_osago_startTime]: Moment;
  [FieldsEnum.docs_osago_endTime]: Moment;
  [FieldsEnum.docs_osago_file]: [];

  // ПДН
  [FieldsEnum.docs_pdn_file]: [];

  // Подтверждение
  [FieldsEnum.confirmaDataAccuracy]: boolean;
  [FieldsEnum.agreementPersonalData]: boolean;
}

export enum UsedPersonalTransportFileFormatEnum {
  JPG = 'image/jpg',
  JPEG = 'image/jpeg',
  PNG = 'image/png',
  TIFF = 'image/tiff',
  PDF = 'application/pdf',
  pdf = 'application/pdf',
  jpg = 'image/jpg',
  jpeg = 'image/jpeg',
  png = 'image/png',
  tiff = 'image/tiff',
  heif = 'heif',
}
