import * as t from 'io-ts';
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
  routeId?: string | null;
  routeNumber?: string;
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

export type Fields = t.TypeOf<typeof Fields>;


