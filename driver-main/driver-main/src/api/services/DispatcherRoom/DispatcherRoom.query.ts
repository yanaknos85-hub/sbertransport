import {
  useSuspenseQuery, useMutation, useQueryClient, useQuery
} from '@tanstack/react-query';

import { BaseQueryOptions, BaseSuspenseQueryOptions } from 'api/query';
import { useDispatcherRoomService } from './DispatcherRoom';
import { ContractorSelf, DriverSelf, VehicleSelf } from './DispatcherRoom.types';

export enum DispatcherRoomKeys {
  Profile = 'profile',
  MyVehicle = 'my-vehicle',
  Avatar = 'avatar',
  Contractor = 'contractor',
}

declare module 'api' {
  interface Cache {
    profile: {
      key: [typeof DispatcherRoomKeys.Profile];
      value: DriverSelf;
    };
    myVehicle: {
      key: [typeof DispatcherRoomKeys.MyVehicle];
      value: VehicleSelf;
    };
    avatar: {
      key: [typeof DispatcherRoomKeys.Avatar];
      value: string;
    };
    contractor: {
      key: [typeof DispatcherRoomKeys.Contractor];
      value: ContractorSelf;
    };
  }
}

/** Получение данных о себе как водителе */
export const useProfile = (config?: BaseSuspenseQueryOptions<DriverSelf>) => {
  const { getSelf } = useDispatcherRoomService();

  return useSuspenseQuery({
    queryKey: [DispatcherRoomKeys.Profile],
    queryFn: getSelf,
    cacheTime: Infinity,
    staleTime: Infinity,
    ...config,
  });
};

/** Редактирование данных о себе как водителе */
export const useEditProfile = () => {
  const { editSelf } = useDispatcherRoomService();
  const client = useQueryClient();

  return useMutation({
    mutationFn: editSelf,
    onSuccess: () => {
      client.invalidateQueries({
        queryKey: [DispatcherRoomKeys.Profile],
      });
    },
  });
};

/** Получение данных о своем авто */
export const useVehicle = (config?: BaseQueryOptions<VehicleSelf>) => {
  const { getMyVehicle } = useDispatcherRoomService();

  return useQuery({
    queryKey: [DispatcherRoomKeys.MyVehicle],
    queryFn: () => getMyVehicle(),
    ...config,
    useErrorBoundary: false,
  });
};

/** Согласие на обработку персональных данных */
export const usePrivacyPolicy = () => {
  const { agreeWithPrivacyPolicy } = useDispatcherRoomService();

  return useMutation<void, Error, void>({
    mutationFn: agreeWithPrivacyPolicy,
  });
};

/** Получение аватарки пользователя */
export const useUserAvatar = (userId: string | undefined) => {
  const { getUserAvatar } = useDispatcherRoomService();

  return useQuery({
    queryKey: [DispatcherRoomKeys.Avatar],
    queryFn: () => getUserAvatar(userId),
  });
};

/** Обновление аватарки пользователя */
export const useUpdateAvatar = () => {
  const { setUserAvatar } = useDispatcherRoomService();
  const client = useQueryClient();

  return useMutation({
    mutationFn: setUserAvatar,
    onSuccess: () => {
      client.invalidateQueries({
        queryKey: [DispatcherRoomKeys.Avatar],
      });
    },
  });
};

/** Получение данных о контрагенте */
export const useContractor = (contractorId: string, config?: BaseQueryOptions<ContractorSelf>) => {
  const { getContractor } = useDispatcherRoomService();

  return useQuery({
    queryKey: [DispatcherRoomKeys.Contractor],
    queryFn: () => getContractor(contractorId),
    cacheTime: Infinity,
    staleTime: Infinity,
    ...config,
  });
};

/** Проверяет блокировку на подтверждение телефона */
export const useCheckPhoneConfirmBlock = () => {
  const { checkPhoneConfirmBlock } = useDispatcherRoomService();

  return useMutation({
    mutationFn: checkPhoneConfirmBlock,
  });
};

/** Получение кода для подтверждения телефона */
export const useConfirmCode = () => {
  const { getConfirmCode } = useDispatcherRoomService();

  return useMutation({
    mutationFn: getConfirmCode,
  });
};

/** Подтверждение номера телефона */
export const usePhoneConfirmation = () => {
  const { confirmPhone } = useDispatcherRoomService();
  const client = useQueryClient();

  return useMutation({
    mutationFn: confirmPhone,
    onSuccess: () => {
      client.invalidateQueries({ queryKey: [DispatcherRoomKeys.Profile] });
    },
  });
};
