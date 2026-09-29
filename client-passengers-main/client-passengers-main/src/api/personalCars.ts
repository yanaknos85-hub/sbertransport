import {
  ILogger,
  ResponseService,
  Employee,
  IOPersonalCar,
  IOsagoUploadResponseModel,
  OsagoBodyResponce,
  OsagoBodyResponceErrors,
  OsagoResponce,
  PersonalCar,
  OsagoUploadResponseModel
} from '@sber-sbertransport/mf-core';

import * as t from 'io-ts';
import { MutationResultPair, QueryConfig } from 'react-query';

import {
  APIQueryCache, APIQueryResult, useAPI, useAPIMutation
} from 'api';

import { CLEAR_QUERY_CONFIG, SYSTEM_MESSAGES } from 'constants/constants.app';
import {
  MOCKED_API_PREFIX,
  PERSONAL_CARS_EDIT_OR_DELETE_PARAMS,
  PERSONAL_CARS_GET_OR_ADD_PARAMS,
  UPLOAD_OSAGO_CAR_FILE
} from 'constants/constants.env';

declare module 'api' {
  interface Cache {
    personalCars: {
      key: ['employeePersonalCars'];
      value: PersonalCar[];
    };
    personalCar: {
      key: ['employeePersonalCar', string];
      value: PersonalCar;
    };
  }
}

export const defaultOptions = {
  enabled: true,
  ...CLEAR_QUERY_CONFIG,
};

export const useEmployeePersonalCars = (
  {
    organizationId: orgId, departmentId: depId, id: empId,
  }: Employee,
  options: QueryConfig<PersonalCar[], unknown>
): APIQueryResult<PersonalCar[]> => useAPI(
  ['employeePersonalCars'],
  ({ http, process }) => http
    .get<PersonalCar[]>(PERSONAL_CARS_GET_OR_ADD_PARAMS, {
      urlParams: {
        orgId, depId, empId,
      },
    })
    .then(process.decodeResponseData(t.array(IOPersonalCar)))
    .catch(err => {
      process.processStatus(404, err);
      return [];
    }),
  options
);

export const useEmployeePersonalCar = (
  {
    organizationId: orgId, departmentId: depId, id: empId,
  }: Employee,
  carId: string,
  options: QueryConfig<PersonalCar, unknown>
): APIQueryResult<PersonalCar, unknown> => useAPI(
  ['employeePersonalCar', carId],
  ({ http, process }) => http
    .get<PersonalCar>(PERSONAL_CARS_EDIT_OR_DELETE_PARAMS, {
      urlParams: {
        orgId, depId, empId, autoId: carId,
      },
    })
    .then(process.decodeResponseData(IOPersonalCar)),
  options
);

export const useAddPersonalCar = ({
  organizationId: orgId,
  departmentId: depId,
  id: empId,
}: Employee): MutationResultPair<unknown, unknown, PersonalCar, unknown> => useAPIMutation(
  ({ http, process }, { ...data }) => http
    .post<PersonalCar>(PERSONAL_CARS_GET_OR_ADD_PARAMS, { ...data }, {
      urlParams: {
        orgId, depId, empId,
      },
    })
    .then(process.getResponseData),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, SYSTEM_MESSAGES.personalCarRequestAddSuccess);
      cache.refetchQueries(['employeePersonalCars']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.personalCarRequestAddFailed);
    },
  }
);

export const useEditPersonalCar = ({
  organizationId: orgId,
  departmentId: depId,
  id: empId,
}: Employee): MutationResultPair<unknown, unknown, PersonalCar, unknown> => useAPIMutation(
  ({ http, process }, { ...data }) => http
    .put<PersonalCar>(
      PERSONAL_CARS_EDIT_OR_DELETE_PARAMS,
      { ...data },
      {
        urlParams: {
          orgId, depId, empId, autoId: data.id,
        },
      }
    )
    .then(process.getResponseData),
  {
    onSuccess: ({ cache, process }) => {
      process.processStatus(200, SYSTEM_MESSAGES.personalCarRequestEditSuccess);
      cache.refetchQueries(['employeePersonalCars']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.personalCarRequestEditFailed);
    },
  }
);

export const useDeletePersonalCar = ({
  organizationId: orgId,
  departmentId: depId,
  id: empId,
}: Employee): MutationResultPair<unknown, unknown, { autoId: string }, unknown> => useAPIMutation(
  ({ http, process }, { autoId }) => http
    .delete(PERSONAL_CARS_EDIT_OR_DELETE_PARAMS, {
      urlParams: {
        orgId, depId, empId, autoId,
      },
    })
    .then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, SYSTEM_MESSAGES.personalCarRequestDeleteSuccess);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.personalCarRequestDeleteFailed);
    },
  }
);

export const useUploadOsagoCarFile = (): MutationResultPair<
  IOsagoUploadResponseModel,
  unknown,
  { body: FormData },
  unknown
> => useAPIMutation(
  ({ http, process }, { body }) => http
    .postFormData<OsagoResponce>(`${MOCKED_API_PREFIX}${UPLOAD_OSAGO_CAR_FILE}`, body)
    .then(process.getResponseData)
    .then<OsagoBodyResponce>((res: OsagoResponce): OsagoBodyResponce => {
      if (!res.body.status && res.body.errors) {
        throw res.body.errors.map((error: OsagoBodyResponceErrors): string => error.text);
      }

      return res.body;
    })
    .then<IOsagoUploadResponseModel>((res: OsagoBodyResponce): IOsagoUploadResponseModel => {
      const osago = new OsagoUploadResponseModel(res);

      if (new Date() > new Date(osago.endDate as unknown as string)) {
        throw SYSTEM_MESSAGES.osagoIsExpired;
      }

      return osago;
    })
    .catch(e => {
      throw e || SYSTEM_MESSAGES.uploadOsagoFileError;
    }),
  {
    onSuccess: ({ cache, process }: { cache: APIQueryCache; process: ResponseService }): void => {
      process.processStatus(200, SYSTEM_MESSAGES.uploadOsagoFileSuccess);
      cache.refetchQueries(['employeePersonalCars']);
    },
    onError: ({ logger, error }: { logger: ILogger; error?: string }): void => {
      logger.toMessage('error', error || SYSTEM_MESSAGES.uploadOsagoFileErrorServer);
    },
  }
);
