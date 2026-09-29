import * as t from 'io-ts';
import moment from 'moment';

import {
  CalculatedTariffMulti,
  CargoListItem as CargoListItemMulti,
  ContactMulti,
  PackageMulti,
  SegmentMulti,
  WaypointMulti
} from 'stores/Cargos/typesMulti';
import { TransportTypeEnum } from 'types/Cargo';
import { TARIFF_SYSTEM_TYPE_VALUE } from 'modules/CargoMassMultiple/constants';

export enum Periodicity {
  week = 'WEEK',
  month = 'MONTH',
  quarter = 'QUARTER',
}

export interface TariffsOption {
  label: string;
  value: TransportTypeEnum | typeof TARIFF_SYSTEM_TYPE_VALUE;
}

const RequestObjectMulti = t.intersection([
  t.type({
    desiredDate: t.string,
    author: ContactMulti,
    humanReadableId: t.string,
    organizationId: t.string,
    cargoDetails: t.array(CargoListItemMulti),
    calculatedTariff: CalculatedTariffMulti,
    waypoints: t.array(WaypointMulti),
    source: t.literal('WEB'),
  }),
  t.partial({
    packages: t.array(PackageMulti),
    deliveryTime: t.number,
    distance: t.number,
    segments: t.array(SegmentMulti),
    time: t.number,
    cost: t.number,
    loaders: t.number,
    comment: t.string,
    requestNumber: t.number,
    approver: t.string,
  }),
]);

export type RequestObjectMulti = t.TypeOf<typeof RequestObjectMulti>;

export interface FormValues {
  senderAddress: string;
  senderPhone: string;
  senderOrganization: string;
  sourceLoaders: string;
  recipientAddress: string;
  recipientName: string;
  recipientPhone: string;
  recipientOrganization: string;
  tariff: TransportTypeEnum;
  deliveryUrgency: string;
  destinationLoaders: string;
  desiredDate: moment.Moment;
}
