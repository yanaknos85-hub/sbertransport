import { useAPIMutation } from 'api';
import { MutationResultPair } from 'react-query';
import { GET_STATUS_CODE } from 'constants/constants.api';
import { ignore } from 'utils';

interface Status {
  statusCode: number;
  description: string;
}

interface StatusCancellationCodes {
  statusCodes: Status[];
}

export const useAllStatusCancellationCodes = (
  transportType: string
): MutationResultPair<StatusCancellationCodes, Error, unknown, unknown> => useAPIMutation(
  async ({ http, process }) => http
    .post<StatusCancellationCodes>(GET_STATUS_CODE, {}, {
      urlParams: { transportType },
    })
    .then(process.getResponseData),
  {
    onSuccess: ignore,
  }
);
