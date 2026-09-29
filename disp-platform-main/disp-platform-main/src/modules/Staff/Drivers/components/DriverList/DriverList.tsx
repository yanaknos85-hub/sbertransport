import React, { FC, useState } from 'react';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile/profile.api';
import { useSearchDrivers } from 'api/drivers/drivers.api';
import { UseSearchDriversProps } from 'api/drivers/drivers.types';
import { useSelfAutopark } from 'api/contractors/contractors.api';

import { driverSpecialityTitles } from 'constants/driver.constants';
import { Props as RowProps } from 'components/List/components/Row/Row';
import { List, Props as ListProps } from 'components/List/List';
import { defaultPagination } from 'hooks/useQuery';

import { formJSX } from './formJSX/formJSX';
import { useModalForm } from '../../context/ModalForm';
import { useActiveDriver } from '../../context/ActiveDriver';
import { useEditDelete } from '../../context/EditDeleteContext';

const defaultDriversQuery = { ...defaultPagination, isActive: true };

export const DriverList: FC = () => {
  const [query, setQuery] = useState<Record<string, unknown>>(defaultDriversQuery);
  const { t } = useTranslation();
  const { handleOpenAdd } = useModalForm();

  const { contractorId, autoparkId } = useProfile().data;

  const { content: drivers, totalElements } = useSearchDrivers({
    contractorId,
    autoparkId,
    data: query as UseSearchDriversProps['data'],
  }).data;

  const { activeDriver, toggleActiveDriver } = useActiveDriver();

  const { handleEditClick } = useEditDelete();

  const { isInternal } = useSelfAutopark().data;

  const rows: ListProps['rows']
    = drivers?.map(driver => ({
      rowData: driver,
      activeRow: activeDriver,
      statuses: [
        { type: (driver.id === activeDriver?.id ? activeDriver?.active : driver.active) ? 'active' : 'inactive' },
        { title: t.global.id, description: driver.humanReadableId },
        { description: driverSpecialityTitles[driver.driverSpeciality] },
      ] as RowProps['statuses'],
      toggleActive: toggleActiveDriver as RowProps['toggleActive'],
      onEdit: isInternal ? undefined : handleEditClick as RowProps['onEdit'],
    })) ?? [];

  const searchPanelProps = {
    searchField: 'driverFullName',
    searchValue: query.driverFullName as string,
  };

  return (
    <List
      autoSize
      onChangeQuery={setQuery}
      onAddClick={isInternal ? undefined : handleOpenAdd}
      formJSX={formJSX(t)}
      rows={rows}
      totalElements={totalElements as number}
      searchPanelProps={searchPanelProps}
      defaultQuery={defaultDriversQuery}
    />
  );
};
