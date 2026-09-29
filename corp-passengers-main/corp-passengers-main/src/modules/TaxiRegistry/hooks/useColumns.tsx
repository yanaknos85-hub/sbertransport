import React, { useMemo } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { ColumnProps, ColumnType } from 'antd/lib/table';
import { SorterResult } from 'antd/lib/table/interface';

import { useTranslation } from 'i18n';
import { RegisterSearchQuery, SortFields } from 'api/register-search';
import { CellLink } from 'modules/Registry/components/Cells/CellLink/CellLink';

import { TableRecord } from '../types/types';
import { RegistriesColumn } from 'shared/styles/styles';

export type TaxiRegistryColumnProps = ColumnProps<TableRecord> &
  ({ sorting: true; sortProperty: SortFields } | { sorting?: false; sortProperty?: never }) & { checked?: boolean };

export interface TaxiRegistrySorterResult extends SorterResult<TableRecord> {
  column?: TaxiRegistryColumnProps;
}

const getSortModifier = (isAscending: boolean) => (isAscending ? 'ascend' : 'descend');

const getSortConfig = (
  sortProperty: SortFields,
  sortSettings: RegisterSearchQuery['sortSetting'],
  sortDefaultDescend = false
): { sorter: true; sorting: true; sortProperty: SortFields } & Partial<ColumnType<TableRecord>> => {
  // @ts-ignore
  const currentSortingField = sortSettings && sortSettings.property === sortProperty;

  return {
    sorter: true,
    sorting: true,
    sortProperty,
    sortDirections: sortDefaultDescend ? ['descend', 'ascend'] : ['ascend', 'descend'],
    // @ts-ignore
    defaultSortOrder: currentSortingField ? getSortModifier(sortSettings!.directionAsc) : undefined,
    // @ts-ignore
    sortOrder: currentSortingField ? getSortModifier(sortSettings!.directionAsc) : null,
  };
};

export const useColumns = (userSortSettings: RegisterSearchQuery['sortSetting']): TaxiRegistryColumnProps[] => {
  const match = useRouteMatch();
  const { t } = useTranslation();

  return useMemo<TaxiRegistryColumnProps[]>(
    () => [
      {
        dataIndex: 'requestIdVisible',
        title: t.Forms.informationAttributesOfRegistries.requestIdVisible,
        checked: false,
        key: 'requestIdVisible',
        fixed: 'left',
        width: 156,
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        render: (requestIdVisible: string, record: any): JSX.Element => (
          <CellLink to={`${match.path}/${record.id}`}>{requestIdVisible}</CellLink>
        ),
        ...getSortConfig(SortFields.REQUEST_HUMAN_ID, userSortSettings, true),
      },
      {
        dataIndex: 'mvzVisible',
        title: t.Forms.informationAttributesOfRegistries.mvzVisible,
        checked: false,
        key: 'mvzVisible',
        width: 110,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="110px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'desiredDateVisible',
        title: t.Forms.informationAttributesOfRegistries.desiredDateVisible,
        checked: false,
        key: 'desiredDateVisible',
        width: 255,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="255px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'deadlineVisible',
        title: t.Forms.informationAttributesOfRegistries.deadlineVisible,
        checked: false,
        key: 'deadlineVisible',
        width: 320,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="320px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'driverArrivedDatetimeVisible',
        title: t.Forms.informationAttributesOfRegistries.driverArrivedDatetimeVisible,
        checked: false,
        key: 'driverArrivedDatetimeVisible',
        width: 307,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="307px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'counterpartyNameVisible',
        title: t.Forms.informationAttributesOfRegistries.counterpartyNameVisible,
        checked: false,
        key: 'counterpartyNameVisible',
        width: 300,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="300px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'approveDateVisible',
        title: t.Forms.informationAttributesOfRegistries.approveDateVisible,
        key: 'approveDateVisible',
        width: 211,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="211px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'passengerFioVisible',
        title: t.Forms.informationAttributesOfRegistries.passengerFioVisible,
        checked: false,
        key: 'passengerFioVisible',
        width: 286,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="286px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'requestStatusVisible',
        title: t.Forms.informationAttributesOfRegistries.requestStatusVisible,
        checked: false,
        key: 'requestStatusVisible',
        width: 232,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="232px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'plannedPriceVisible',
        title: t.Forms.informationAttributesOfRegistries.plannedPriceVisible,
        checked: false,
        key: 'plannedPriceVisible',
        width: 206,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="206px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'tripFactPriceVisible',
        title: t.Forms.informationAttributesOfRegistries.tripFactPriceVisible,
        checked: false,
        key: 'tripFactPriceVisible',
        width: 229,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="229px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'expectedDistanceVisible',
        title: t.Forms.informationAttributesOfRegistries.expectedDistanceVisible,
        key: 'expectedDistanceVisible',
        width: 208,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="208px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'organizationalUnitCode',
        title: t.Forms.informationAttributesOfRegistries.organizationalUnitCode,
        checked: false,
        key: 'organizationalUnitCode',
        width: 166,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="166px">{column}</RegistriesColumn>
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
        dataIndex: 'tripTypeVisible',
        title: t.Forms.informationAttributesOfRegistries.tripTypeVisible,
        checked: false,
        key: 'tripTypeVisible',
        width: 143,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="143px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'registryFactPayment',
        title: t.Forms.informationAttributesOfRegistries.registryFactPayment,
        key: 'registryFactPayment',
        width: 143,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="143px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'registryFactCost',
        title: t.Forms.informationAttributesOfRegistries.registryFactCost,
        key: 'registryFactCost',
        width: 250,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="250px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'registryFactDistance',
        title: t.Forms.informationAttributesOfRegistries.registryFactDistance,
        key: 'registryFactDistance',
        width: 250,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="250px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'registryFactWaitingTime',
        title: t.Forms.informationAttributesOfRegistries.registryFactWaitingTime,
        key: 'registryFactWaitingTime',
        width: 300,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="300px">{column}</RegistriesColumn>
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
    ],
    [t, match, userSortSettings]
  );
};
