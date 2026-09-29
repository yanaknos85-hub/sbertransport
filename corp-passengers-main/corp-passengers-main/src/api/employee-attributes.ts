import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import * as t from 'io-ts';
import { UUID } from 'utils/io-ts';
import { EmployeesAttribute } from 'stores/EmployeesAttribute/EmployeesAttribute.interface';
import { MutationResultPair } from 'react-query';
import { AxiosResponse } from 'axios';

import {
  DELETE_EMPLOYEES_ATTRIBUTE,
  EMPLOYEES_ATTRIBUTE_ADD_PARAMS,
  EMPLOYEES_ATTRIBUTE_PARAMS,
  GET_ALL_EMPLOYEES_ATTRIBUTES,
  GET_ACTIVE_EMPLOYEES_ATTRIBUTES
} from '../constants/constants.api';
import { mkUseUploadEntity } from './upload';

declare module 'api' {
  interface Cache {
    employeeAttributes: { key: ['employeeAttributes', UUID]; value: EmployeesAttribute[] };
    employeeActiveAttributes: { key: ['employeeActiveAttributes', UUID]; value: EmployeesAttribute[] };
  }
}

export const useEmployeeAttributes = (orgId: UUID): APIQueryResult<EmployeesAttribute[], Error> => useAPI(['employeeAttributes', orgId], ({ http, process }) => http
  .get<EmployeesAttribute[]>(GET_ALL_EMPLOYEES_ATTRIBUTES, { urlParams: { orgId } })
  .then(process.decodeResponseData(t.array(EmployeesAttribute)))
);

export const useEmployeeActiveAttributes = (orgId: UUID): APIQueryResult<EmployeesAttribute[], Error> => useAPI(['employeeActiveAttributes', orgId], ({ http, process }) => http
  .get<EmployeesAttribute[]>(GET_ACTIVE_EMPLOYEES_ATTRIBUTES, { urlParams: { orgId } })
  .then(process.decodeResponseData(t.array(EmployeesAttribute)))
);

export const useDeleteEmployeeAttribute = (): MutationResultPair<
  AxiosResponse<number>,
  unknown,
  { eAttrId: UUID },
  unknown
> => useAPIMutation(
  ({ http }, { eAttrId }: { eAttrId: UUID }) => (
    http.delete<number>(DELETE_EMPLOYEES_ATTRIBUTE, { urlParams: { eAttrId } })
  ),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.EmployeeAttributes.employeeAttributeDeleteSuccess);
      cache.refetchQueries(['employeeAttributes']);
      cache.refetchQueries(['activeEmployeeAttributes']);
    },
  }
);

export const useCreateEmployeeAttribute = () => useAPIMutation(
  ({ http, process }, { attribute }: { attribute: Omit<EmployeesAttribute, 'id' | 'status'> }) => (
    // @ts-ignore
    http.post(EMPLOYEES_ATTRIBUTE_ADD_PARAMS, attribute).then<EmployeesAttribute>(process.getResponseData)
  ), {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.EmployeeAttributes.employeeAttributeAddSuccess);
      cache.refetchQueries(['employeeAttributes']);
      cache.refetchQueries(['activeEmployeeAttributes']);
    },
    onError: ({
      error, logger, t,
    }) => {
      if (error?.request?.status === 409) {
        logger.toMessage('error', t.EmployeeAttributes.duplicateAttributeError);
      }
    },
  }
);

export const useUpdateEmployeeAttribute = () => useAPIMutation(
  ({ http }, { attribute, eAttrId }: { attribute: Omit<EmployeesAttribute, 'status'>; eAttrId: UUID }) => http.put(EMPLOYEES_ATTRIBUTE_PARAMS, attribute, { urlParams: { eAttrId } }),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.EmployeeAttributes.employeeAttributeEditSuccess);
      cache.refetchQueries(['employeeAttributes']);
      cache.refetchQueries(['activeEmployeeAttributes']);
    },
    onError: ({
      error, logger, t,
    }) => {
      if (error?.request?.status === 409) {
        logger.toMessage('error', t.EmployeeAttributes.duplicateAttributeError);
      } else {
        logger.toMessage('error', t.EmployeeAttributes.anyError);
      }
    },
  }
);

export const useUploadEmployeeAttributes = mkUseUploadEntity('employeeAttributes', {
  onSuccess: ({ cache }) => {
    cache.invalidateQueries(['employeeAttributes']);
    cache.refetchQueries(['activeEmployeeAttributes']);
  },
});
