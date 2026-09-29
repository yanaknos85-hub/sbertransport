/* eslint-disable no-use-before-define */
import React, {
  FC, useEffect, useMemo, Suspense
} from 'react';
import { useTranslation } from 'i18n';

import { EditableColumnType, EditableTable, EditableTableStore } from 'shared/components/EditableTable';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useSaveSharedRideSettings, useSharedRideSettings, useSharedRideSettingTypes } from 'api/shared-ride-settings';
import { useProfile } from 'api/profile';

import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import {
  SharedRideSettingsByType,
  SharedRideSettingsQueryInput,
  SharedRideSettingsType
} from 'stores/SharedRide/SharedRideSettings.interface';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SelectEmployeesCell } from './SelectEmployeesCell';
import { SelectEmployeeAttributesCell } from './SelectEmployeeAttributesCell';
import { SelectPositionsCell } from './SelectPositionsCell';

import styles from './styles.module.scss';

const rowKeyFn = (record: SettingsRecord) => record.settingType;

export interface SettingsRecord extends PartialBy<SharedRideSettingsByType, 'id'> {
  settingType: SharedRideSettingsType;
}

export const SharedRideSettingsByTansportType: FC<{ transportType: TransportTypes }> = ({ transportType }) => (
  <ErrorBoundary>
    <Suspense fallback={<SpinWrapped />}>
      <SharedRideSettingsInner transportType={transportType} />
    </Suspense>
  </ErrorBoundary>
);

const SharedRideSettingsInner: FC<{ transportType: TransportTypes }> = ({ transportType }) => {
  const { organizationId } = useProfile().data;

  const { data: sharedSettingTypes = [], isLoading: isLoadingTypes } = useSharedRideSettingTypes();
  // @ts-ignore
  const { data: sharedSettings = {}, isLoading: isLoadingSettings } = useSharedRideSettings(organizationId);
  const [saveSharedRideSettings, { isLoading: isSavingSettings }] = useSaveSharedRideSettings();

  const store = useMemo(
    () => new EditableTableStore<SettingsRecord>({
      rowKeyFn, disableDeleteAction: true, disableMultiRowEdit: true,
    }),
    []
  );
  const columns = useColumns();

  useEffect(() => {
    store.handleSave = async ({ settingType, ...other }: SettingsRecord) => {
      const settings: SharedRideSettingsQueryInput = {
        // @ts-ignore
        organizationId,
        transportType,
        economyIndicationYellowRangeLowerBorder: 30,
        economyIndicationYellowRangeUpperBorder: 60,
        ...sharedSettings[transportType],
        settings: {
          ...sharedSettings[transportType]?.settings,
          [settingType]: other,
        },
      };

      return (await saveSharedRideSettings(settings))?.status === 200;
    };
  }, [store, transportType, sharedSettings, saveSharedRideSettings, organizationId]);

  useEffect(() => {
    const settingsByType = (setting: SharedRideSettingsType) => sharedSettings[transportType]?.settings[setting];

    store.data = sharedSettingTypes.map(settingType => {
      const settings = settingsByType(settingType);
      return {
        settingType,
        id: settings?.id,
        positions: settings?.positions || [],
        attributes: settings?.attributes || [],
        employees: settings?.employees || [],
      };
    });
  }, [store, sharedSettings, sharedSettingTypes, transportType]);

  return (
    <div className={styles.sharedRideSettings}>
      <EditableTable
        store={store}
        columns={columns}
        pagination={false}
        style={{ overflow: 'scroll' }}
        scroll={{ x: 1200 }}
        className={styles.sharedRideSettingsTable}
      />
      {(isLoadingTypes || isLoadingSettings || isSavingSettings) && <SpinWrapped mask />}
    </div>
  );
};

const useColumns = (): EditableColumnType<SettingsRecord>[] => {
  const { t } = useTranslation();
  return useMemo(
    (): EditableColumnType<SettingsRecord>[] => [
      {
        title: t.SharedRides.SharedRideSettings.Columns.setting,
        dataIndex: 'settingType',
        key: 'settingType',
        width: 260,
        render: ({ cellValue }) => t.SharedRides.SharedRideSettings.Settings[
          cellValue as keyof typeof t.SharedRides.SharedRideSettings.Settings
        ],
      },
      {
        title: t.SharedRides.SharedRideSettings.Columns.positions,
        dataIndex: 'positions',
        key: 'positions',
        width: 300,
        render: props => <SelectPositionsCell {...props} />,
      },
      {
        title: t.SharedRides.SharedRideSettings.Columns.attributes,
        dataIndex: 'attributes',
        key: 'attributes',
        width: 250,
        render: props => <SelectEmployeeAttributesCell {...props} />,
      },
      {
        title: t.SharedRides.SharedRideSettings.Columns.employees,
        dataIndex: 'employees',
        key: 'employees',
        width: 250,
        render: props => <SelectEmployeesCell {...props} />,
      },
    ],
    [t]
  );
};
