import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { UUID } from 'utils/io-ts';

export interface ChangedFieldsProps {
  id: UUID;
  label: string;
  description?: string | number | null;
  name?: string;
  transportType?: string;
  status?: string;
  departureAddressCoordinates: { latitude: number; longitude: number };
  source?: string;
  withAvailableFutureStatus?: boolean;
}

export const Fields = t.intersection([
  t.type({
    status: t.string,
  }),
  t.partial({
    vehicleInfo: t.string,
    cancelCode: t.string,
  }),
]);

export const StartPersonalTripRequest = t.intersection([
  t.type({
    requestId: tt.uuid,
    latitude: t.number,
    longitude: t.number,
  }),
  t.partial({
    absenceReason: t.string,
  }),
]);

export type Fields = t.TypeOf<typeof Fields>;

export type DescriptionsField = [string | JSX.Element, string | number | null | undefined, string?, string?];

export type UseDescriptions = DescriptionsField[];

export type StartPersonalTripRequest = t.TypeOf<typeof StartPersonalTripRequest>;
