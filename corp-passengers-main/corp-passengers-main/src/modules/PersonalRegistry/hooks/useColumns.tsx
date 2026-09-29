import { ColumnProps, ColumnType } from 'antd/lib/table';
import { SorterResult } from 'antd/lib/table/interface';
import { useTranslation } from 'i18n';
import React, { useCallback, useMemo } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { CellLink } from 'modules/Registry/components/Cells/CellLink/CellLink';
import { SortFields, SortSettings } from 'stores/PersonalSearch/PersonalSearch.interface';
import { TableRecord } from '../types/types';

import { RegistriesColumn } from 'shared/styles/styles';

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
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="110px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'desiredDateVisible',
        title: t.Forms.registryPersonalSettings.desiredDateVisible,
        checked: false,
        key: 'desiredDateVisible',
        width: 255,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="255px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'controlPeriodOfPayment',
        title: t.Forms.registryPersonalSettings.controlPeriodOfPayment,
        checked: false,
        key: 'controlPeriodOfPayment',
        width: 223,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="223px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'orderPaymentFormationStartDateVisible',
        title: t.Forms.registryPersonalSettings.orderPaymentFormationStartDateVisible,
        checked: false,
        key: 'orderPaymentFormationStartDateVisible',
        width: 209,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="209px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'passengerFioVisible',
        title: t.Forms.registryPersonalSettings.passengerFioVisible,
        checked: false,
        key: 'passengerFioVisible',
        width: 286,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="286px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'requestStatusVisible',
        title: t.Forms.registryPersonalSettings.requestStatusVisible,
        checked: false,
        key: 'requestStatusVisible',
        width: 232,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="232px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'plannedPriceVisible',
        title: t.Forms.registryPersonalSettings.plannedPriceVisible,
        checked: false,
        key: 'plannedPriceVisible',
        width: 210,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="210px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'tripFactPriceVisible',
        title: t.Forms.registryPersonalSettings.tripFactPriceVisible,
        checked: false,
        key: 'tripFactPriceVisible',
        width: 240,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="240px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'plannedRangeVisible',
        title: t.Forms.registryPersonalSettings.plannedRangeVisible,
        key: 'plannedRangeVisible',
        width: 208,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="208px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'paymentPeriodVisible',
        title: t.Forms.registryPersonalSettings.paymentPeriodVisible,
        checked: false,
        key: 'paymentPeriodVisible',
        width: 136,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="136px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'departmentCodeVisible',
        title: t.Forms.registryPersonalSettings.departmentCode,
        checked: false,
        key: 'departmentCodeVisible',
        width: 189,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="189px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'sharedRideOwnerVisible',
        title: t.Forms.registryPersonalSettings.sharedRideOwnerVisible,
        checked: false,
        key: 'sharedRideOwnerVisible',
        width: 232,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="232px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'carVisible',
        title: t.Forms.registryPersonalSettings.carVisible,
        checked: false,
        key: 'carVisible',
        width: 220,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="220px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'carEngineVolumeVisible',
        title: t.Forms.registryPersonalSettings.carEngineVolumeVisible,
        checked: false,
        key: 'carEngineVolumeVisible',
        width: 149,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="149px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'tripTypeVisible',
        title: t.Forms.registryPersonalSettings.tripTypeVisible,
        checked: false,
        key: 'tripTypeVisible',
        width: 143,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="143px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'requestRating',
        title: t.Forms.informationAttributesOfRegistries.requestRating,
        key: 'requestRating',
        width: 120,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="120px">{column}</RegistriesColumn>
        ),
      },
    ] as PersonalRegistryColumnProps[],
    [t, match, implementSortConfigs]
  );
};
