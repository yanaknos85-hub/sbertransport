import React, { useMemo } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { ColumnProps } from 'antd/lib/table';
import { SorterResult } from 'antd/lib/table/interface';

import { useTranslation } from 'i18n';
import type { SortSetting } from 'api/yandexTaxiRegistry/yandex-taxi-registry.types';
import { CellLink } from 'modules/Registry/components/Cells/CellLink/CellLink';

import { TableRecord } from '../types/types';
import { SortFields } from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';
import { RegistriesColumn } from 'shared/styles/styles';

export type YandexTaxiRegistryColumnProps = ColumnProps<TableRecord> &
  ({ sorting: true; sortProperty: SortFields } | { sorting?: false; sortProperty?: never }) & { checked?: boolean };

export interface TaxiRegistrySorterResult extends SorterResult<TableRecord> {
  column?: YandexTaxiRegistryColumnProps;
}

export const useColumns = (userSortSettings: SortSetting): YandexTaxiRegistryColumnProps[] => {
  const match = useRouteMatch();
  const { t } = useTranslation();

  return useMemo<YandexTaxiRegistryColumnProps[]>(
    () => [
      {
        dataIndex: 'humanReadableId',
        title: t.Forms.informationAttributesOfRegistries.humanReadableId,
        checked: false,
        key: 'humanReadableId',
        fixed: 'left',
        width: 156,
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        render: (humanReadableId: string, record: any): JSX.Element => (
          <CellLink to={`${match.path}/${record.id}`}>{humanReadableId}</CellLink>
        ),
      },
      {
        dataIndex: 'costCenter',
        title: t.Forms.informationAttributesOfRegistries.costCenter,
        checked: false,
        key: 'costCenter',
        fixed: 'left',
        width: 110,
        render: (costCenter: string): JSX.Element => (
          <RegistriesColumn minWidth="110px">{costCenter}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'passengerFullName',
        title: t.Forms.informationAttributesOfRegistries.authorFIO,
        checked: false,
        key: 'passengerFullName',
        width: 200,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="200px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'desiredDate',
        title: t.Forms.informationAttributesOfRegistries.tripDate,
        checked: false,
        key: 'desiredDate',
        width: 140,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="140px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'tariff',
        title: t.Forms.informationAttributesOfRegistries.taxiClass,
        checked: false,
        key: 'tariff',
        width: 80,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="80px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'requestStatus',
        title: t.Forms.informationAttributesOfRegistries.status,
        checked: false,
        key: 'requestStatus',
        width: 120,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="120px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'approverFullName',
        title: t.Forms.informationAttributesOfRegistries.approvedByFioVisible,
        key: 'approverFullName',
        width: 200,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="200px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'plannedRoute',
        title: t.Forms.informationAttributesOfRegistries.route,
        checked: false,
        key: 'plannedRoute',
        width: 300,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="300px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'plannedCost',
        title: t.Forms.informationAttributesOfRegistries.expectedCost,
        checked: false,
        key: 'plannedCost',
        width: 160,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="160px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'actualCost',
        title: t.Forms.informationAttributesOfRegistries.tripFactPrice,
        checked: false,
        key: 'actualCost',
        width: 160,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="160px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'receiptLink',
        title: t.Forms.informationAttributesOfRegistries.receiptLink,
        checked: false,
        key: 'receiptLink',
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="60px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'comment',
        title: t.Forms.informationAttributesOfRegistries.authorComment,
        key: 'comment',
        width: 180,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="180px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'reason',
        title: t.Forms.informationAttributesOfRegistries.denialReason,
        checked: false,
        key: 'reason',
        width: 250,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="140px">{column}</RegistriesColumn>
        ),
      },
    ],
    [t, match, userSortSettings]
  );
};
