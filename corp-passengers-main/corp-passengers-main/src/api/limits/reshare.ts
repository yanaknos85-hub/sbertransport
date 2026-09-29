import { AxiosError } from 'axios';
import { useAPIMutation } from 'api';
import { MutationResultPair } from 'react-query';
import { ignore } from 'utils';
import { uuid } from 'utils/io-ts';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { throwAxiosErrorMessage } from 'utils/throwAxiosErrorMessage';
import { refetchLimits } from '.';

const RedistributeDepartments = t.intersection([
  t.strict({
    parentLimitId: tt.uuid,
    sourceDepartmentId: tt.uuid,
    targetDepartmentId: tt.uuid,
    sourceTransportType: t.string,
    targetTransportType: t.string,
    sum: tt.money,
    year: t.number,
    reason: t.string,
  }),
  t.partial({
    fromPeriod: tt.optional(t.number),
    toPeriod: tt.optional(t.number),
  }),
]);

type RedistributeDepartments = t.TypeOf<typeof RedistributeDepartments>;

export const useRedistributeDepartments = (): MutationResultPair<void, unknown, RedistributeDepartments, unknown> => (
  useAPIMutation(
    ({ http }, variables) => (
      http.post('/limits/deplimits/reshare_departments', RedistributeDepartments.encode(variables)).then(ignore)
    ),
    { onSuccess: refetchLimits }
  )
);

const RedistributeTransportTypes = t.intersection([
  t.strict({
    limitId: uuid,
    sourceTransportType: t.string,
    targetTransportType: t.string,
    sum: tt.money,
  }),
  t.partial({
    fromPeriod: tt.optional(t.number),
    toPeriod: tt.optional(t.number),
  }),
]);

type RedistributeTransportTypes = t.TypeOf<typeof RedistributeTransportTypes>;

export const useRedistributeTransportTypes = (): MutationResultPair<
  void,
  unknown,
  RedistributeTransportTypes,
  unknown
> => useAPIMutation(
  ({ http }, variables) => http.post('/limits/deplimits/reshare_transporttypes', RedistributeTransportTypes.encode(variables)).then(ignore),
  {
    onSuccess: refetchLimits,
    onError: ({ error }) => throwAxiosErrorMessage(error as AxiosError),
  }
);
