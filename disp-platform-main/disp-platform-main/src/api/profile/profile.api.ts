import { Employee } from '@sber-sbertransport/mf-core';
import { useAPI } from 'api';
import { ContractorDispatcherSelf } from './profile.types';
import { CONTRACTORS_SELF, GET_SELF_EMPLOYEE } from './profile.constants';
import { useDecodedToken } from 'hooks/useToken';
import { useRole } from 'hooks/useRole';
import { useRoleMap } from 'hooks/useRoleMap';
import { AutoparkContextOptions, useAutoparkContext } from 'context/Autopark.context';
import { useBranchContext } from 'context/Branch.context';
import { UUID } from 'utils/io-ts';
import { Roles } from 'constants/app.constants';

declare module 'api' {
  interface Cache {
    profile: { key: ['profile']; value: ContractorDispatcherSelf };
    employee: { key: ['employee']; value: Employee };
  }
}

export const useProfile = (autoparktOptions: AutoparkContextOptions = { out: false }) => {
  const { isAdmin, isManager } = useRoleMap();
  const roles = useRole();
  const isRoomAdmin = roles.includes(Roles.ROLE_DISPATCHER_ROOM_ADMIN);

  const profile = useAPI(['profile'], ({ http, process }) => (
    http
      .get<ContractorDispatcherSelf>(CONTRACTORS_SELF)
      .then(process.decodeResponseData(ContractorDispatcherSelf))
  ), {
    enabled: !isRoomAdmin,
  });

  const { data: employee } = useAPI(['employee'], ({ http, process }) => (
    http
      .get<Employee>(GET_SELF_EMPLOYEE)
      .then(process.decodeResponseData(Employee))
  ), {
    enabled: isRoomAdmin || profile?.data.oauthId,
  });

  const { jti } = useDecodedToken();
  const { autoparkId } = useAutoparkContext(autoparktOptions);
  const { branchId } = useBranchContext();

  const adminProfile: ContractorDispatcherSelf = {
    firstName: 'Автопарков',
    lastName: 'Администатор',
    id: jti,
    phone: '',
    email: '',
    contractorId: autoparkId! as UUID,
    autoparkId: branchId === 'null' ? undefined : branchId as UUID,
    organizationId: employee?.organizationId,
    departmentId: employee?.departmentId,
    humanReadableId: '',
    consent: true,
  };

  return isRoomAdmin
    ? { data: adminProfile }
    : {
      data: {
        ...profile.data,
        contractorId: isAdmin ? autoparkId! as UUID : profile.data.contractorId,
        originAutoparkId: profile.data.autoparkId,
        autoparkId: isAdmin
          ? branchId === 'null' ? undefined : branchId as UUID
          : isManager
            ? branchId && branchId !== 'null' ? branchId : undefined
            : profile.data.autoparkId,
        organizationId: employee?.organizationId,
        departmentId: employee?.departmentId,
      },
    };
};
