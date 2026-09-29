import { QueryConfig } from 'react-query';
import { APIQueryResult, useAPI } from 'api';
import { UUID } from 'utils/io-ts';
import { Shift } from './schedule.types';
import { SHIFT_ONE } from './schedule.constants';

declare module 'api' {
  interface Cache {
    shift: { key: ['shift', UUID, UUID]; value: Shift };
  }
}

export const useShift = (
  { contractorId, shiftId }: { contractorId: UUID; shiftId: UUID },
  config?: QueryConfig<Shift, Error>
): APIQueryResult<Shift, Error> => (
  useAPI(
    ['shift', contractorId, shiftId],
    ({ http, process }) => (
      http.get<Shift>(SHIFT_ONE, { urlParams: { contractorId, shiftId } })
        .then(process.decodeResponseData(Shift))
    ),
    config
  )
);
