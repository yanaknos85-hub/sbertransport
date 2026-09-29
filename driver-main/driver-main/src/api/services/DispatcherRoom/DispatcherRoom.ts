// TODO переделать в класс mobx, который сам будет доставать http

import dayjs from 'dayjs';
import {
  BlockPhoneConfirmation,
  ContractorSelf, DriverSelf, VehicleSelf
} from './DispatcherRoom.types';
import { SYSTEM_MESSAGES } from 'constants/auth.constants';
import { useAppStore } from 'stores/stores.context';
import { ignore } from 'utils';
import { logger } from 'utils/logger/logger';

const DISPATCHER_ROOM_SERVICE = '/dispatcher-room';
const USERS_AVATAR = '/users/avatar';
const CONFIRMATION = '/confirmation';

enum DispatcherRoomEndpoints {
  Self = `${DISPATCHER_ROOM_SERVICE}/self/driver/`,
  EditSelf = `${DISPATCHER_ROOM_SERVICE}/:contractorId/drivers/:driverId/`,
  MyVehicle = `${DISPATCHER_ROOM_SERVICE}/self/vehicle/`,
  Consent = `${DISPATCHER_ROOM_SERVICE}/self/driver/consent/`,
  Contractor = `${DISPATCHER_ROOM_SERVICE}/:contractorId/`,
}

export const useDispatcherRoomService = () => {
  const { http, process } = useAppStore();

  const getSelf = () => {
    return http
      .get<DriverSelf>(DispatcherRoomEndpoints.Self)
      .then(process.decodeResponseData(DriverSelf));
  };

  const editSelf = ({
    contractorId, id, ...rest
  }: DriverSelf) => {
    return http
      .put(DispatcherRoomEndpoints.EditSelf,
        rest,
        { urlParams: { contractorId, driverId: id } }
      )
      .then(() => {
        logger('success', SYSTEM_MESSAGES.selfEditSuccess);
      });
  };

  const getMyVehicle = () => {
    return http
      .get<VehicleSelf>(DispatcherRoomEndpoints.MyVehicle)
      .then(process.decodeResponseData(VehicleSelf));
  };

  const agreeWithPrivacyPolicy = () => {
    return http
      .patch(DispatcherRoomEndpoints.Consent, {})
      .then(ignore);
  };

  const getUserAvatar = (userId: string | undefined) => {
    return http
      .get<Blob>(`${USERS_AVATAR}/${userId}`, { responseType: 'arraybuffer' })
      .then(x => {
        const file = new Blob([x.data]);
        return URL.createObjectURL(file);
      })
      .catch(ignore);
  };

  const setUserAvatar = (fileData: File) => {
    const formData = new FormData();
    formData.append('avatar', fileData);

    return http
      .post(USERS_AVATAR, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      .then(() => {
        logger('success', SYSTEM_MESSAGES.avatarEditSuccess);
      })
      .catch(() => {
        logger('fail', SYSTEM_MESSAGES.generalSaveError);
      });
  };

  const getContractor = (contractorId: string) => {
    return http
      .get<ContractorSelf>(DispatcherRoomEndpoints.Contractor, { urlParams: { contractorId } })
      .then(process.decodeResponseData(ContractorSelf));
  };

  const checkPhoneConfirmBlock = (): Promise<void | BlockPhoneConfirmation> => {
    return http
      .get<BlockPhoneConfirmation>(`${CONFIRMATION}/block`)
      .then(process.decodeResponseData(BlockPhoneConfirmation))
      .then(data => {
        if (data.blocked) {
          const minutesLeft = dayjs(data.nextAllowTime).diff(dayjs(), 'minute');

          logger(
            'fail',
            `Вы превысили количество попыток на запрос кода подтверждения. Повторите действие через ${minutesLeft} минут`
          );
        }

        return data;
      })
      .catch(() => {
        logger('fail', SYSTEM_MESSAGES.blockPhoneConfirmationError);
      });
  };

  const getConfirmCode = (phoneNumber: string) => {
    return http
      .patch(DispatcherRoomEndpoints.Self, [{ field: 'phone', value: phoneNumber }])
      .then(ignore);
  };

  const confirmPhone = (code: string) => {
    return http
      .put(`${CONFIRMATION}/phone`, {}, { headers: { 'x-code': code }, hush: [409] })
      .then(ignore)
      .catch(error => {
        const message = error.response?.data?.message;

        if (message?.includes('Wrong code')) {
          throw message;
        } else {
          logger('fail', SYSTEM_MESSAGES.phoneConfirmationError);
        }
      });
  };

  return {
    getSelf,
    getMyVehicle,
    agreeWithPrivacyPolicy,
    getUserAvatar,
    setUserAvatar,
    editSelf,
    getContractor,
    checkPhoneConfirmBlock,
    getConfirmCode,
    confirmPhone,
  };
};
