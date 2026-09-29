import React, { useCallback, useMemo } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { ColumnType } from 'antd/lib/table';

import { SortFields } from 'api/register-search';
import { useTranslation } from 'i18n';
import { CellLink } from 'modules/Registry/components/Cells/CellLink/CellLink';
import { CarSharingSearchQuery } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { CarSharingRegistryColumnProps, TableRecord } from '../types';

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
      ...genDefaultCol('mvzVisible', { width: 110 }),
      ...genDefaultCol('desiredDateVisible', { width: 255 }),
      ...genDefaultCol('approveDateVisible', { width: 211 }),
      ...genDefaultCol('employeeFioVisible', { width: 286 }),
      ...genDefaultCol('requestStatusVisible', { width: 232 }),
      ...genDefaultCol('totalCostVisible', { width: 258 }),
      ...genDefaultCol('drivingLengthVisible', { width: 238 }),
      ...genDefaultCol('organizationalUnitCode', { width: 166 }),
      ...genDefaultCol('carVisible', { width: 220 }),
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [genDefaultCol, getSortConfig, userSortSettings]
  );
};
