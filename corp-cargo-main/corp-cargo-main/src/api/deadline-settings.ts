import { DeadlineSettings, DeadlineSettingsType } from '../stores/DeadlineSettings/DeadlineSettings.interface';
import {
  TypeAtKey, updateQueryCache, useAPI, useAPIMutation
} from './index';
import { DEADLINE_SETTINGS, DEADLINE_SETTINGS_DEFAULT, DEADLINE_SETTINGS_UPDATE } from '../constants/constants.api';
import { UUID } from '../utils/io-ts';

declare module 'api' {
  interface Cache {
    deadlineSettings: {
      key: ['deadlineSettings', UUID];
      value: {
        deadlineSettings: DeadlineSettingsType;
      };
    };
  }
}

type CacheItem = TypeAtKey<['deadlineSettings', UUID]>;

const raw2cache = (deadlineSettings: DeadlineSettingsType): CacheItem => ({ deadlineSettings });

export const useDeadlineSettings = (organizationId: UUID) => useAPI(['deadlineSettings', organizationId], ({ http, process }) => http
  .get<DeadlineSettingsType>(DEADLINE_SETTINGS, { urlParams: { organizationId } })
  .then(process.decodeResponseData(DeadlineSettings))
  .then(raw2cache)
);

export const useCreateDeadlineSettings = (organizationId: UUID) => useAPIMutation(
  (
    { http, process },
    {
      deadlineSettings,
      organizationId,
    }: { deadlineSettings: Omit<DeadlineSettingsType, 'id'>; organizationId: UUID }
  ) => http
    .post(DEADLINE_SETTINGS, deadlineSettings, { urlParams: { organizationId } })
  // @ts-ignore
    .then<DeadlineSettingsType>(process.getResponseData),
  {
    onSuccess: ({
      cache, result: deadlineSetting, process, t,
    }) => {
      process.processStatus(200, t.DeadlineSettings.messages.saveSuccess);
      updateQueryCache(cache, ['deadlineSettings', organizationId], () => raw2cache(deadlineSetting));
    },
    onError: ({ logger, t }) => {
      logger.toMessage('error', t.DeadlineSettings.messages.error);
    },
  }
);

export const useUpdateDeadlineSettings = () => useAPIMutation(
  (
    { http },
    {
      deadlineSettings,
      organizationId,
      settingId,
    }: { deadlineSettings: DeadlineSettingsType; organizationId: UUID; settingId: UUID }
  ) => http.put(DEADLINE_SETTINGS_UPDATE, deadlineSettings, { urlParams: { organizationId, settingId } }),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.DeadlineSettings.messages.updateSuccess);
      cache.refetchQueries(['deadlineSettings']);
    },
  }
);

export const useSetDefaultDeadlineSettings = () => useAPIMutation(
  // eslint-disable-next-line @stylistic/max-len
  ({ http }, { organizationId, settingId }: { organizationId: UUID; settingId: UUID }) => http.put(DEADLINE_SETTINGS_DEFAULT, {}, { urlParams: { organizationId, settingId } }),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.DeadlineSettings.messages.defaultSuccess);
      cache.refetchQueries(['deadlineSettings']);
    },
  }
);
