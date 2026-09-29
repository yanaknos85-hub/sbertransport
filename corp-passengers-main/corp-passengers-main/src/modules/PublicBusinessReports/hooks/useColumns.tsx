import { useRouteMatch } from 'react-router-dom';
import React, { useCallback, useMemo } from 'react';
import { ColumnProps, ColumnType } from 'antd/lib/table';
import { SorterResult } from 'antd/lib/table/interface';
import { useTranslation } from 'i18n';
import { SortSettings } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { CellLink } from 'modules/Registry/components/Cells/CellLink/CellLink';
import { JournalRecord } from '../types/types';
import { SortFields } from '../constants/PublicBusinessReports.constants';

export type TripRegistryColumnProps = ColumnProps<JournalRecord> &
  ({ sorting: true; sortProperty: SortFields } | { sorting?: false; sortProperty?: never }) & { checked?: boolean };

export interface TripRegistrySorterResult extends SorterResult<JournalRecord> {
  column?: TripRegistryColumnProps;
}

const getSortModifier = (isAscending: boolean) => (isAscending ? 'ascend' : 'descend');

const getSortConfig = (
  field: SortFields,
  userSortSettings: SortSettings | undefined
): { sorter: true; sorting: true; sortProperty: SortFields } & Partial<ColumnType<JournalRecord>> => {
  const currentSortingField = userSortSettings && userSortSettings.property === field;
  return {
    sorter: true,
    sorting: true,
    sortProperty: field,
    defaultSortOrder: currentSortingField ? getSortModifier(userSortSettings!.directionAsc) : undefined,
    sortOrder: currentSortingField ? getSortModifier(userSortSettings!.directionAsc) : null,
  };
};

export const useColumns = (
  isStatusChangeActive: boolean,
  userSortSettings?: SortSettings
): TripRegistryColumnProps[] => {
  const match = useRouteMatch();
  const { t } = useTranslation();

  const implementSortConfigs = useCallback(
    (field: SortFields) => (!isStatusChangeActive ? getSortConfig(field, userSortSettings) : {}),
    [isStatusChangeActive, userSortSettings]
  );

  return useMemo(
    () => [
      {
        dataIndex: 'requestIdVisible',
        title: t.Forms.informationAttributesOfRegistries.requestIdVisible,
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
        title: t.Forms.informationAttributesOfRegistries.mvzVisible,
        key: 'mvzVisible',
        width: 110,
      },
      {
        dataIndex: 'desiredDateVisible',
        title: t.Forms.informationAttributesOfRegistries.desiredDateVisible,
        key: 'desiredDateVisible',
        width: 255,
      },
      {
        dataIndex: 'controlPeriodOfPayment',
        title: t.Forms.informationAttributesOfRegistries.controlPeriodOfPayment,
        key: 'controlPeriodOfPayment',
        width: 223,
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
        key: 'passengerFioVisible',
        width: 286,
      },
      {
        dataIndex: 'requestStatusVisible',
        title: t.Forms.informationAttributesOfRegistries.requestStatusVisible,
        key: 'requestStatusVisible',
        width: 232,
      },
      {
        dataIndex: 'plannedPriceVisible',
        title: t.Forms.informationAttributesOfRegistries.plannedPriceVisible,
        key: 'plannedPriceVisible',
        width: 210,
      },
      {
        dataIndex: 'expectedDistanceVisible',
        title: t.Forms.informationAttributesOfRegistries.expectedDistanceVisible,
        key: 'expectedDistanceVisible',
        width: 208,
      },
      {
        dataIndex: 'paymentPeriodVisible',
        title: t.Forms.informationAttributesOfRegistries.paymentPeriodVisible,
        key: 'paymentPeriodVisible',
        width: 200,
      },
      {
        dataIndex: 'departmentCodeVisible',
        title: t.Forms.informationAttributesOfRegistries.organizationalUnitCode,
        key: 'departmentCodeVisible',
        width: 189,
      },
    ],
    [t, match, implementSortConfigs]
  );
};
