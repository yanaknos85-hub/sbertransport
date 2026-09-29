import { APIQueryResult, useAPI } from 'api';
import { GET_SELF_EMPLOYEE } from 'constants/constants.api';
import { useOrganizationContext } from 'context/Organization.context';
import { SelfEmployee } from 'stores/Employee/Employee.interface';
import { UUID } from '../utils/io-ts';

declare module 'api' {
  interface Cache {
    profile: { key: ['profile']; value: SelfEmployee };
  }
}

export const useProfile = (disabled?: boolean): APIQueryResult<SelfEmployee> => {
  const { organizationId } = useOrganizationContext();

  const query = useAPI(['profile'], ({ http, process }) => http.get<SelfEmployee>(GET_SELF_EMPLOYEE).then(process.decodeResponseData(SelfEmployee))
    , { enabled: !disabled });

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
    },
  };
};
