import { MutationResultPair } from 'react-query';
import { AxiosError } from 'axios';
import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { CONSENT, GET_SELF_EMPLOYEE } from 'constants/constants.api';
import { useOrganizationContext } from 'context/Organization.context';
import { SelfEmployee } from 'stores/Employee/Employee.interface';
import { UUID } from '../utils/io-ts';

declare module 'api' {
  interface Cache {
    profile: { key: ['profile']; value: SelfEmployee };
  }
}

export const useProfile = (disabled?: boolean): APIQueryResult<SelfEmployee> => {
  const {
    organizationId, isOrganization, executorGroupId,
  } = useOrganizationContext();

  const query = useAPI(
    ['profile'],
    ({ http, process }) => (
      http.get<SelfEmployee>(GET_SELF_EMPLOYEE).then(process.decodeResponseData(SelfEmployee))
    ),
    { enabled: !disabled }
  );

  if (disabled) {
    return {
      ...query,
      data: {
        ...query.data,
      },
    };
  }

  return {
    ...query,
    data: {
      ...query.data,
      organizationId: (organizationId as UUID) ?? query.data.organizationId,
      isOrganization: isOrganization,
      executorGroupId: executorGroupId,
    },
  };
};

/** Подписание согласия на обработку персональных данных внешним сотрудником */
export const useConsent = (): MutationResultPair<unknown, AxiosError, unknown, unknown> => (
  useAPIMutation(
    ({ http }) => (
      http.patch(CONSENT, {})
    ),
    {
      onSuccess: ({ cache }) => {
        cache.refetchQueries(['profile']);
      },
    }
  ));
