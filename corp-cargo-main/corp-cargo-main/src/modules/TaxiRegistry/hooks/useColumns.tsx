import React, { useMemo } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { ColumnProps, ColumnType } from 'antd/lib/table';
import { SorterResult } from 'antd/lib/table/interface';

import { useTranslation } from 'i18n';
import { RegisterSearchQuery, SortFields } from 'api/register-search';
import { CellLink } from 'modules/Registry/components/Cells/CellLink/CellLink';

import { TableRecord } from '../types/types';

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
      },
      {
        dataIndex: 'desiredDateVisible',
        title: t.Forms.informationAttributesOfRegistries.desiredDateVisible,
        checked: false,
        key: 'desiredDateVisible',
        width: 255,
      },
      {
        dataIndex: 'deadlineVisible',
        title: t.Forms.informationAttributesOfRegistries.deadlineVisible,
        checked: false,
        key: 'deadlineVisible',
        width: 320,
      },
      {
        dataIndex: 'driverArrivedDatetimeVisible',
        title: t.Forms.informationAttributesOfRegistries.driverArrivedDatetimeVisible,
        checked: false,
        key: 'driverArrivedDatetimeVisible',
        width: 307,
      },
      {
        dataIndex: 'counterpartyNameVisible',
        title: t.Forms.informationAttributesOfRegistries.counterpartyNameVisible,
        checked: false,
        key: 'counterpartyNameVisible',
        width: 300,
      },
      {
        dataIndex: 'approveDateVisible',
        title: t.Forms.informationAttributesOfRegistries.approveDateVisible,
        key: 'approveDateVisible',
        width: 211,
      },
      {
        dataIndex: 'passengerFioVisible',
        title: t.Forms.informationAttributesOfRegistries.passengerFioVisible,
        checked: false,
        key: 'passengerFioVisible',
        width: 286,
      },
      {
        dataIndex: 'requestStatusVisible',
        title: t.Forms.informationAttributesOfRegistries.requestStatusVisible,
        checked: false,
        key: 'requestStatusVisible',
        width: 232,
      },
      {
        dataIndex: 'plannedPriceVisible',
        title: t.Forms.informationAttributesOfRegistries.plannedPriceVisible,
        checked: false,
        key: 'plannedPriceVisible',
        width: 206,
      },
      {
        dataIndex: 'tripFactPriceVisible',
        title: t.Forms.informationAttributesOfRegistries.tripFactPriceVisible,
        checked: false,
        key: 'tripFactPriceVisible',
        width: 229,
      },
      {
        dataIndex: 'expectedDistanceVisible',
        title: t.Forms.informationAttributesOfRegistries.expectedDistanceVisible,
        key: 'expectedDistanceVisible',
        width: 208,
      },
      {
        dataIndex: 'organizationalUnitCode',
        title: t.Forms.informationAttributesOfRegistries.organizationalUnitCode,
        checked: false,
        key: 'organizationalUnitCode',
        width: 166,
      },
      {
        dataIndex: 'carVisible',
        title: t.Forms.registryPersonalSettings.carVisible,
        checked: false,
        key: 'carVisible',
        width: 220,
      },
      {
        dataIndex: 'tripTypeVisible',
        title: t.Forms.informationAttributesOfRegistries.tripTypeVisible,
        checked: false,
        key: 'tripTypeVisible',
        width: 143,
      },
    ],
    [t, match, userSortSettings]
  );
};
