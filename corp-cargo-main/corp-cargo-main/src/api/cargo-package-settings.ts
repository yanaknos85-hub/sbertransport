import { MutationResultPair } from 'react-query';
import { AxiosResponse } from 'axios';

import {
  CREATE_CARGO_PACKAGE,
  DELETE_CARGO_PACKAGE,
  GET_ALL_CARGO_PACKAGE,
  UPDATE_CARGO_PACKAGE
} from 'constants/constants.api';

import {
  CargoPackageSettingsArray,
  CargoPackageSettingsType,
  SaveCargoPackageSettingsType
} from 'stores/CargoPackage/CargoPackageSettings.interface';
import { APIQueryResult, useAPI, useAPIMutation } from './index';
import { getErrorMessage } from '../utils';
import { UUID } from '../utils/io-ts';

/**
 * Определяем параметры кэша для настроек упаковок
 */
declare module 'api' {
  interface Cache {
    cargoPackageSettings: {
      key: ['cargoPackageSettings', string];
      value: CargoPackageSettingsType[];
    };
  }
}

/**
 * Получаем упаковки выбранного контрагента
 * @param contractorId uuid контрагента
 */
export const useCargoPackageSettings = (
  contractorId: string
): APIQueryResult<CargoPackageSettingsType[], CargoPackageSettingsType[]> => useAPI(['cargoPackageSettings', contractorId], ({ http, process }) => {
  if (contractorId === '') {
    return [];
  }
  return http
    .get<CargoPackageSettingsType[]>(GET_ALL_CARGO_PACKAGE, { urlParams: { contractorId } })
    .then(process.decodeResponseData(CargoPackageSettingsArray))
    .catch(e => {
      if (e.response.status === 404 || e.response.status === 405) {
        process.processStatus(
          e.response.status,
          'Невозмножно установить по умолчанию данные, которые ни разу не создавались'
        );
        return [];
      }
      throw e;
    });
});

/**
 * Сохраняем новую упаковку выбранного контрагента
 * @param contractorId uuid контрагента
 */
export const useCreateCargoPackageSettings = (
  contractorId: string
// eslint-disable-next-line @stylistic/max-len, @typescript-eslint/no-explicit-any
): MutationResultPair<AxiosResponse<unknown>, any, { settings: SaveCargoPackageSettingsType }, unknown> => useAPIMutation(
  ({ http }, { settings }: { settings: SaveCargoPackageSettingsType }) => http.post(CREATE_CARGO_PACKAGE, settings, {
    urlParams: { contractorId },
  }),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.CargoPackageSettings.SaveSuccess);
      cache.refetchQueries(['cargoPackageSettings', contractorId]);
    },
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error));
    },
  }
);

/**
 * Обновляем указанную упаковку выбранного контрагента
 * @param contractorId uuid контрагента
 */
export const useUpdateCargoPackageSettings = (
  contractorId: string
): MutationResultPair<
  AxiosResponse<unknown>,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  any,
  { settings: SaveCargoPackageSettingsType; packageId: UUID },
  unknown
> => useAPIMutation(
  // eslint-disable-next-line @stylistic/max-len
  ({ http }, { settings, packageId }: { settings: SaveCargoPackageSettingsType; packageId: UUID }) => http.put(UPDATE_CARGO_PACKAGE, settings, {
    urlParams: { contractorId, packageId },
  }),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.CargoPackageSettings.SaveSuccess);
      cache.refetchQueries(['cargoPackageSettings', contractorId]);
    },
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error));
    },
  }
);

/**
 * Удаляем указанную упаковку выбранного контрагента
 * @param contractorId uuid контрагента
 */
export const useDeleteCargoPackageSettings = (
  contractorId: string
): MutationResultPair<AxiosResponse<number>, unknown, { packageId: UUID }, unknown> => useAPIMutation(
  // eslint-disable-next-line @stylistic/max-len
  ({ http }, { packageId }: { packageId: UUID }) => http.delete<number>(DELETE_CARGO_PACKAGE, { urlParams: { contractorId, packageId } }),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.CargoPackageSettings.SaveSuccess);
      cache.refetchQueries(['cargoPackageSettings', contractorId]);
    },
  }
);
