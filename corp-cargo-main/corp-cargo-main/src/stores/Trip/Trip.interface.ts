/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-use-before-define */
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

import {
  TransportTypes
} from 'stores/TransportTypes/TransportTypes.interface';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { Route } from '../Geo/Geo.interface';

export enum TaxiClass {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  BUSINESS = 'BUSINESS',
  OFFICIAL = 'OFFICIAL',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',

  VIP_BUS = 'VIP_BUS',
  SMALL_BUS = 'SMALL_BUS',
  MIDDLE_BUS = 'MIDDLE_BUS',
  LARGE_BUS = 'LARGE_BUS',
}

export enum TaxiClassShort {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  BUSINESS = 'BUSINESS',

  VIP_BUS = 'VIP_BUS',
  SMALL_BUS = 'SMALL_BUS',
  MIDDLE_BUS = 'MIDDLE_BUS',
  LARGE_BUS = 'LARGE_BUS',
}

export const TaxiClassDescriptions: Record<TaxiClass, string> = {
  ECONOMY: 'Эконом',
  COMFORT: 'Комфорт',
  COMFORT_PLUS: 'Комфорт+',
  BUSINESS: 'Бизнес',
  OFFICIAL: 'Служебный',
  CARSHARING: 'Каршеринг',
  BICYCLE: 'Велосипед',
  WALK: 'Пешком',

  VIP_BUS: 'Автобус до 9 мест',
  SMALL_BUS: 'Автобус от 10 до 21 места',
  MIDDLE_BUS: 'Автобус от 22 до 41 мест',
  LARGE_BUS: 'Автобус от 42 до 55 мест',
};

export const TaxiClassShortDescriptions: Record<TaxiClassShort, string> = {
  ECONOMY: 'Эконом',
  COMFORT: 'Комфорт',
  COMFORT_PLUS: 'Комфорт+',
  BUSINESS: 'Бизнес',

  VIP_BUS: 'Автобус до 9 мест',
  SMALL_BUS: 'Автобус от 10 до 21 места',
  MIDDLE_BUS: 'Автобус от 22 до 41 мест',
  LARGE_BUS: 'Автобус от 42 до 55 мест',
};

export const Purpose = t.intersection([
  t.type({
    id: tt.uuid,
  }),
  t.partial({
    purpose: tt.nullable(t.string),
  }),
]);
export type Purpose = t.TypeOf<typeof Purpose>;

export const ApprovedBy = t.partial({
  id: tt.uuid,
  humanReadableId: t.string,
  userId: tt.uuid,
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  personnelNumber: t.string,
  itinerantType: t.string,
  mvz: t.string,
  delegatedById: tt.uuid,
  supervisorId: tt.uuid,
  positionId: tt.uuid,
  organizationId: tt.uuid,
  departmentId: tt.uuid,
});
export type ApprovedBy = t.TypeOf<typeof ApprovedBy>;

export const RequestRating = t.partial({
  advantages: tt.nullable(t.array(t.string)),
  drawbacks: tt.nullable(t.array(t.string)),
  ratingComment: tt.nullable(t.string),
  rating: tt.nullable(t.number),
});
export type RequestRating = t.TypeOf<typeof RequestRating>;

export const Passenger = t.intersection([
  t.type({
    humanReadableId: t.string,
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
  }),
  t.partial({
    organizationId: tt.nullable(tt.uuid),
    positionId: tt.nullable(tt.uuid),
    constCenter: tt.nullable(t.string),
    positionName: tt.nullable(t.string),
    mobilePhone: tt.nullable(t.string),
    phone: tt.nullable(t.string),
    userId: tt.nullable(tt.uuid),
    patronymic: tt.nullable(t.string),
    personnelNumber: tt.nullable(t.string),
    itinerantType: tt.nullable(t.string),
    mvz: tt.nullable(t.string),
    marriageCertificateNumber: t.union([tt.nullable(t.number), tt.nullable(t.string)]),
    delegatedById: tt.nullable(t.string),
    supervisorId: tt.nullable(t.string),
    departmentId: tt.nullable(tt.uuid),
    departmentName: tt.nullable(t.string),
  }),
]);
export type Passenger = t.TypeOf<typeof Passenger>;

export const TripRequest = t.intersection([
  t.type({
    id: t.string,
    humanReadableId: t.string,
    author: Passenger,
    passenger: Passenger,
    transportType: ioTypeFromEnum<TransportTypes>('TransportTypeEnum', TransportTypes),
    expected: Route,
    purpose: Purpose,
    passengerCount: t.number,
    status: t.string,
    creationTime: t.number,
    coopTrip: t.boolean,
    approvalState: t.string,
  }),
  t.partial({
    department: t.strict({ id: tt.uuid, departmentName: t.string }),
    position: t.intersection([t.type({ id: tt.uuid }), t.partial({ positionName: t.string })]),
    desiredDate: t.number,
    taxiClass: ioTypeFromEnum<TaxiClass>('TaxiClassEnum', TaxiClass),
    tariffId: t.string,
    approvedBy: ApprovedBy,
    requestRating: tt.nullable(RequestRating),
    passengers: t.array(Passenger),
    sharedRideId: t.string,
    humanReadableLimitId: t.string,
    commentForDriver: t.string,
  }),
]);

export type TripRequest = t.TypeOf<typeof TripRequest>;

export enum TaxiOptions {
  CHILD_SEAT = 'CHILD_SEAT',
  PET_TRANSPORTATION = 'PET_TRANSPORTATION',
  BICYCLE_SKI_TRANSPORTATION = 'BICYCLE_SKI_TRANSPORTATION',
  NON_SMOKING_DRIVER = 'NON_SMOKING_DRIVER',
  YELLOW_REG_PLATES = 'YELLOW_REG_PLATES',
  MATERIAL_ASSETS_TRANSPORTATION = 'MATERIAL_ASSETS_TRANSPORTATION',
  PERSONAL_USAGE = 'PERSONAL_USAGE',
}

export const TaxiOptionsDescriptions: Record<TaxiOptions, string> = {
  CHILD_SEAT: 'Детское кресло',
  PET_TRANSPORTATION: 'Перевозка животных',
  BICYCLE_SKI_TRANSPORTATION: 'Перевозка велосипедов и лыж',
  NON_SMOKING_DRIVER: 'Некурящий водитель',
  YELLOW_REG_PLATES: 'Желтые номера',
  MATERIAL_ASSETS_TRANSPORTATION: 'Материальная ценность',
  PERSONAL_USAGE: 'Личное пользование',
};

export enum UsedMIMEFileFormatEnum {
  JPG = 'image/jpg',
  JPEG = 'image/jpeg',
  PNG = 'image/png',
  TIFF = 'image/tiff',
  PDF = 'application/pdf',
  DOC = 'application/msword',
  DOCX = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  HEIC = 'heic',
}

export enum UsedFileFormatEnum {
  JPG = 'JPG',
  JPEG = 'JPEG',
  PNG = 'PNG',
  TIFF = 'TIFF',
  PDF = 'PDF',
  DOC = 'DOC',
  DOCX = 'DOCX',
  HEIC = 'HEIC',
}
