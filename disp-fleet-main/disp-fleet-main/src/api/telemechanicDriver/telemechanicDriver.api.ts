import { MutationResultPair } from 'react-query';
import { TelemechanicDrivers, TelemechanicDriversFilters } from './telemechanicDriver.types';
import { AxiosError } from 'axios';
import { useAPIMutation } from 'api';
import { TELEMECHANIC_DRIVER } from './telemechanicDriver.constants';
import { ignore } from 'utils/utils';

export const useTelemechanicDriversMutation = (): MutationResultPair<
  TelemechanicDrivers,
  AxiosError<Error>,
  TelemechanicDriversFilters,
  unknown
> => (
  useAPIMutation(({ http, process }, params) => (
    http
      .get<TelemechanicDrivers>(TELEMECHANIC_DRIVER, { params })
      .then(process.getResponseData)
  ), {
    onSuccess: ignore,
  })
);
