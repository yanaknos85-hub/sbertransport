import { useAPI } from 'api';
import { ContractorDispatcherSelf } from './profile.types';
import { CONTRACTORS_SELF } from './profile.constants';
import { useRole } from 'hooks/useRole';
import { Roles } from 'constants/app.constants';
import { useDecodedToken } from 'hooks/useToken';
import { useAutoparkContext } from 'context/Autopark.context';
import { UUID } from 'utils/io-ts';

declare module 'api' {
  interface Cache {
    profile: { key: ['profile']; value: ContractorDispatcherSelf };
  }
}

export const useProfile = () => {
  const roles = useRole();
  const isRoomAdmin = roles.includes(Roles.ROLE_DISPATCHER_ROOM_ADMIN);
  const isAdmin = roles.includes(Roles.ROLE_DISPATCHER_ROOM_ADMIN)
    || roles.includes(Roles.ROLE_FEDERAL_DISPATCHER_CONTRACTOR);

  const profile = useAPI(['profile'], ({ http, process }) => (
    http
      .get<ContractorDispatcherSelf>(CONTRACTORS_SELF)
      .then(process.decodeResponseData(ContractorDispatcherSelf))
  ), {
    enabled: !isRoomAdmin,
  });

  const { jti } = useDecodedToken();
  const { autoparkId } = useAutoparkContext();

  const adminProfile: ContractorDispatcherSelf = {
    firstName: 'Автопарков',
    lastName: 'Администатор',
    id: jti,
    phone: '',
    email: '',
    contractorId: autoparkId! as UUID,
    humanReadableId: '',
    consent: true,
  };

  return isRoomAdmin ? { data: adminProfile } : {
    ...profile,
    data: {
      ...profile.data,
      contractorId: isAdmin ? autoparkId! as UUID : profile.data.contractorId,
    },
  };
};

