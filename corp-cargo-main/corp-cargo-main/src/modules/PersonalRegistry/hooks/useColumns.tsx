import { ColumnProps, ColumnType } from 'antd/lib/table';
import { SorterResult } from 'antd/lib/table/interface';
import { useTranslation } from 'i18n';
import React, { useCallback, useMemo } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { CellLink } from 'modules/Registry/components/Cells/CellLink/CellLink';
import { SortFields, SortSettings } from 'stores/PersonalSearch/PersonalSearch.interface';
import { TableRecord } from '../types/types';

export type PersonalRegistryColumnProps = ColumnProps<TableRecord> &
  ({ sorting: true; sortProperty: SortFields } | { sorting?: false; sortProperty?: never }) & { checked?: boolean };

export interface PersonalRegistrySorterResult extends SorterResult<TableRecord> {
  column?: PersonalRegistryColumnProps;
}

const getSortModifier = (isAscending: boolean) => (isAscending ? 'ascend' : 'descend');

const getSortConfig = (
  field: SortFields,
  userSortSettings: SortSettings | undefined
): { sorter: true; sorting: true; sortProperty: SortFields } & Partial<ColumnType<TableRecord>> => {
  const currentSortingField = userSortSettings && userSortSettings.property === field;
  return {
    sorter: true,
    sorting: true,
    sortProperty: field,
    defaultSortOrder: currentSortingField ? getSortModifier(userSortSettings!.directionAsc) : undefined,
    sortOrder: currentSortingField ? getSortModifier(userSortSettings!.directionAsc) : null,
  };
};

export type TripRegistryColumnProps = ColumnProps<TableRecord> &
  ({ sorting: true; sortProperty: SortFields } | { sorting?: false; sortProperty?: never }) & { checked?: boolean };

export interface TripRegistrySorterResult extends SorterResult<TableRecord> {
  column?: TripRegistryColumnProps;
}

export const useColumns = (
  isStatusChangeActive: boolean,
  userSortSettings?: SortSettings
): PersonalRegistryColumnProps[] => {
  const match = useRouteMatch();
  const { t } = useTranslation();

  const implementSortConfigs = useCallback(
    (field: SortFields) => !isStatusChangeActive && {
      ...getSortConfig(field, userSortSettings),
    },
    [isStatusChangeActive, userSortSettings]
  );

  return useMemo(
    () => [
      {
        dataIndex: 'requestIdVisible',
        title: t.Forms.registryPersonalSettings.requestIdVisible,
        checked: false,
        key: 'requestIdVisible',
        fixed: 'left',
        width: 156,
        render: (requestIdVisible: string, record: any): JSX.Element => (
          <CellLink to={`${match.path}/${record.id}`}>{requestIdVisible}</CellLink>
        ),
        ...implementSortConfigs(SortFields.REQUEST_HUMAN_ID),
      },
      {
        dataIndex: 'mvzVisible',
        title: t.Forms.registryPersonalSettings.mvzVisible,
        checked: false,
        key: 'mvzVisible',
        width: 110,
      },
      {
        dataIndex: 'desiredDateVisible',
        title: t.Forms.registryPersonalSettings.desiredDateVisible,
        checked: false,
        key: 'desiredDateVisible',
        width: 255,
      },
      {
        dataIndex: 'controlPeriodOfPayment',
        title: t.Forms.registryPersonalSettings.controlPeriodOfPayment,
        checked: false,
        key: 'controlPeriodOfPayment',
        width: 223,
      },
      {
        dataIndex: 'orderPaymentFormationStartDateVisible',
        title: t.Forms.registryPersonalSettings.orderPaymentFormationStartDateVisible,
        checked: false,
        key: 'orderPaymentFormationStartDateVisible',
        width: 209,
      },
      {
        dataIndex: 'passengerFioVisible',
        title: t.Forms.registryPersonalSettings.passengerFioVisible,
        checked: false,
        key: 'passengerFioVisible',
        width: 286,
      },
      {
        dataIndex: 'requestStatusVisible',
        title: t.Forms.registryPersonalSettings.requestStatusVisible,
        checked: false,
        key: 'requestStatusVisible',
        width: 232,
      },
      {
        dataIndex: 'plannedPriceVisible',
        title: t.Forms.registryPersonalSettings.plannedPriceVisible,
        checked: false,
        key: 'plannedPriceVisible',
        width: 210,
      },
      {
        dataIndex: 'tripFactPriceVisible',
        title: t.Forms.registryPersonalSettings.tripFactPriceVisible,
        checked: false,
        key: 'tripFactPriceVisible',
        width: 240,
      },
      {
        dataIndex: 'plannedRangeVisible',
        title: t.Forms.registryPersonalSettings.plannedRangeVisible,
        key: 'plannedRangeVisible',
        width: 208,
      },
      {
        dataIndex: 'paymentPeriodVisible',
        title: t.Forms.registryPersonalSettings.paymentPeriodVisible,
        checked: false,
        key: 'paymentPeriodVisible',
        width: 136,
      },
      {
        dataIndex: 'departmentCodeVisible',
        title: t.Forms.registryPersonalSettings.departmentCode,
        checked: false,
        key: 'departmentCodeVisible',
        width: 189,
      },
      {
        dataIndex: 'sharedRideOwnerVisible',
        title: t.Forms.registryPersonalSettings.sharedRideOwnerVisible,
        checked: false,
        key: 'sharedRideOwnerVisible',
        width: 232,
      },
      {
        dataIndex: 'carVisible',
        title: t.Forms.registryPersonalSettings.carVisible,
        checked: false,
        key: 'carVisible',
        width: 220,
      },
      {
        dataIndex: 'carEngineVolumeVisible',
        title: t.Forms.registryPersonalSettings.carEngineVolumeVisible,
        checked: false,
        key: 'carEngineVolumeVisible',
        width: 149,
      },
      {
        dataIndex: 'tripTypeVisible',
        title: t.Forms.registryPersonalSettings.tripTypeVisible,
        checked: false,
        key: 'tripTypeVisible',
        width: 143,
      },
    ] as PersonalRegistryColumnProps[],
    [t, match, implementSortConfigs]
  );
};
