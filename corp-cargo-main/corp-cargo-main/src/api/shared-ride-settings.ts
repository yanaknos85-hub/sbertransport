import { IHttpService, ResponseService } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

import {
  APIQueryResult, updateQueryCache, useAPI, useAPIMutation
} from 'api';

import {
  CREATE_SHARED_RIDE_SETTINGS,
  GET_ALL_SHARED_RIDE_SETTINGS,
  GET_SHARED_RIDE_SETTING_TYPES,
  UPDATE_SHARED_RIDE_SETTINGS
} from 'constants/constants.api';

import {
  SharedRideSettings,
  SharedRideSettingsType,
  SharedRideSettingsQueryInput
} from 'stores/SharedRide/SharedRideSettings.interface';

const SettingsCacheValue = t.record(SharedRideSettings.props.transportType, t.union([SharedRideSettings, t.undefined]));

type SettingsCacheValue = t.TypeOf<typeof SettingsCacheValue>;

declare module 'api' {
  interface Cache {
    sharedRideSettings: {
      key: ['sharedRideSettings', tt.UUID];
      value: SettingsCacheValue;
    };

    sharedRideSettingTypes: {
      key: ['sharedRideSettingTypes'];
      value: SharedRideSettingsType[];
    };
  }
}

const getSharedRideSettingsReader = (orgId: string) => ({
  http,
  process,
}: {
  http: IHttpService;
  process: ResponseService;
}) => http
  .get<SharedRideSettings[]>(GET_ALL_SHARED_RIDE_SETTINGS, { urlParams: { orgId } })
  .then(process.decodeResponseData(t.array(SharedRideSettings)))
  .then((settings: SharedRideSettings[]) => settings.reduce((map: SettingsCacheValue, record: SharedRideSettings) => {
    map[record.transportType] = record;
    return map;
  }, {})
  );

export const useSharedRideSettings = (orgId: tt.UUID): APIQueryResult<SettingsCacheValue, unknown> => useAPI(['sharedRideSettings', orgId], getSharedRideSettingsReader(orgId));

export const useSharedRideSettingTypes = (): APIQueryResult<SharedRideSettingsType[], unknown> => useAPI(['sharedRideSettingTypes'], ({ http, process }) => http
  .get<SharedRideSettingsType[]>(GET_SHARED_RIDE_SETTING_TYPES)
  .then(process.decodeResponseData(t.array(t.string)))
);

// HACK: server side requires not only ids for positions/employees/attributes but full objects
const DUMMY_POSITION = { name: 'Test', positionName: 'Test' };
const DUMMY_ATTRIBUTE = { name: 'Test' };
const DUMMY_EMPLOYEE = {
  firstName: 'Test',
  lastName: 'Test',
  patronymic: 'Test',
  personnelNumber: 'Test',
  positionId: 'e9cde36f-d5c0-4ef1-b3cc-b34f574d89ea',
};

export const useSaveSharedRideSettings = () => useAPIMutation(
  ({ http }, settings: SharedRideSettingsQueryInput) => {
    const settingsForBackend = {
      ...settings,
      settings: Object.keys(settings.settings).reduce(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (map: Record<SharedRideSettingsType, any>, key: SharedRideSettingsType) => {
          map[key] = {
            id: settings.settings[key].id,
            positions: settings.settings[key].positions.map(position => ({ ...DUMMY_POSITION, ...position })),
            attributes: settings.settings[key].attributes.map(attribute => ({ ...DUMMY_ATTRIBUTE, ...attribute })),
            employees: settings.settings[key].employees.map(employee => ({ ...DUMMY_EMPLOYEE, ...employee })),
          };

          return map;
        },
        {}
      ),
    };

    if (settings.id) {
      return http.put(UPDATE_SHARED_RIDE_SETTINGS, settingsForBackend, {
        urlParams: { orgId: settings.organizationId },
      });
    }
    return http.post(CREATE_SHARED_RIDE_SETTINGS, settingsForBackend, {
      urlParams: { orgId: settings.organizationId },
    });
  },
  {
    onSuccess: ({
      cache, result, process, t,
    }) => {
      process.processStatus(200, t.SharedRides.SharedRideSettings.SaveSuccess);
      const savedSettings = result.data as SharedRideSettings;
      return updateQueryCache(
        cache,
        ['sharedRideSettings', savedSettings.organizationId],
        (cache: SettingsCacheValue) => ({ ...cache, [savedSettings.transportType]: savedSettings })
      );
    },
  }
);
