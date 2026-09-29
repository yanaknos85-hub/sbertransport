import React, { useCallback, useMemo } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { ColumnProps, ColumnType } from 'antd/lib/table';

import { SortFields } from 'api/register-search';
import { useTranslation } from 'i18n';
import { CellLink } from 'modules/Registry/components/Cells/CellLink/CellLink';
import { CarSharingSearchQuery } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { CarSharingRegistryColumnProps, TableRecord } from '../types';

import { RegistriesColumn } from 'shared/styles/styles';

export type TripRegistryColumnProps = ColumnProps<TableRecord> &
  ({ sorting: true; sortProperty: SortFields } | { sorting?: false; sortProperty?: never }) & { checked?: boolean };

const getSortModifier = (isAscending: boolean) => (isAscending ? 'ascend' : 'descend');

const getSortConfig = (
  sortProperty: SortFields,
  sortSettings: CarSharingSearchQuery['sortSetting'],
  sortDefaultDescend = false
): { sorter: true; sorting: true; sortProperty: SortFields } & Partial<ColumnType<TableRecord>> => {
  const currentSortingField = sortSettings && sortSettings.property === sortProperty;

  return {
    sorter: true,
    sorting: true,
    sortProperty,
    sortDirections: sortDefaultDescend ? ['descend', 'ascend'] : ['ascend', 'descend'],
    defaultSortOrder: currentSortingField ? getSortModifier(sortSettings!.directionAsc) : undefined,
    sortOrder: currentSortingField ? getSortModifier(sortSettings!.directionAsc) : null,
  };
};

export const useColumns = (userSortSettings: CarSharingSearchQuery['sortSetting']): CarSharingRegistryColumnProps[] => {
  const match = useRouteMatch();
  const { t } = useTranslation();

  type ColName = keyof typeof t.Forms.informationAttributesOfRegistries;

  const genDefaultCol = useCallback(
    (name: ColName, options?: CarSharingRegistryColumnProps): [CarSharingRegistryColumnProps] => [
      {
        dataIndex: name,
        title: t.Forms.informationAttributesOfRegistries[name],
        key: name,
        ...options,
      },
    ],
    [t.Forms.informationAttributesOfRegistries]
  );

  return useMemo(
    () => [
      ...genDefaultCol('requestIdVisible', {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        render: (requestIdVisible: string, record: any): JSX.Element => (
          <CellLink to={`${match.path}/${record.id}`}>{requestIdVisible}</CellLink>
        ),
        ...getSortConfig(SortFields.REQUEST_HUMAN_ID, userSortSettings),
        width: 156,
      }),
      ...genDefaultCol('mvzVisible', {
        width: 110,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="110px">{column}</RegistriesColumn>
        ),
      }),
      ...genDefaultCol('desiredDateVisible', {
        width: 255,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="255px">{column}</RegistriesColumn>
        ),
      }),
      ...genDefaultCol('approveDateVisible', {
        width: 211,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="211px">{column}</RegistriesColumn>
        ),
      }),
      ...genDefaultCol('employeeFioVisible', {
        width: 286,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="286px">{column}</RegistriesColumn>
        ),
      }),
      ...genDefaultCol('requestStatusVisible', {
        width: 232,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="232px">{column}</RegistriesColumn>
        ),
      }),
      ...genDefaultCol('totalCostVisible', {
        width: 258,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="258px">{column}</RegistriesColumn>
        ),
      }),
      ...genDefaultCol('drivingLengthVisible', {
        width: 238,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="238px">{column}</RegistriesColumn>
        ),
      }),
      ...genDefaultCol('organizationalUnitCode', {
        width: 166,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="166px">{column}</RegistriesColumn>
        ),
      }),
      ...genDefaultCol('carVisible', {
        width: 220,
        render: (column: string): JSX.Element => (
          <RegistriesColumn minWidth="220px">{column}</RegistriesColumn>
        ),
      }),
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [genDefaultCol, getSortConfig, userSortSettings]
  );
};
