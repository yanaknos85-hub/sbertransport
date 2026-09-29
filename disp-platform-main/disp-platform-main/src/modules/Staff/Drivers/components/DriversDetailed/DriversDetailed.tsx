import React, { FC } from 'react';
import { useTranslation } from 'i18n';

import { useSelfAutopark } from 'api/contractors/contractors.api';
import { driverSpecialityTitles } from 'constants/driver.constants';
import { ListDetailed, Props as ListDetailedProps } from 'components/ListDetailed/ListDetailed';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';

import { useActiveDriver } from '../../context/ActiveDriver';
import { useEditDelete } from '../../context/EditDeleteContext';

export const DriversDetailed: FC = () => {
  const { t } = useTranslation();

  const { isInternal } = useSelfAutopark().data;

  const { activeDriver } = useActiveDriver();

  const { handleEditClick } = useEditDelete();

  const marks = [
    {
      label: `${t.global.id}:`,
      value: activeDriver?.humanReadableId ?? '',
    },

    {
      label: `${t.Registry.Labels.shortPhone}:`,
      value: formatPhoneNumber(activeDriver?.contactPhone),
    },
    {
      label: `${t.Registry.Labels.speciality}`,
      value: activeDriver?.driverSpeciality ? driverSpecialityTitles[activeDriver.driverSpeciality] : '',
    },
  ];

  const items: ListDetailedProps['items'] = [
    {
      label: t.Registry.Labels.emailShort,
      value: activeDriver?.email ?? '',
    },
    {
      type: activeDriver?.active ? 'active' : 'inactive',
    },
    {
      label: t.Registry.Labels.organization,
      value: '',
    },
    {
      label: t.Registry.Labels.department,
      value: '',
    },
    {
      label: t.Registry.Labels.jobTitle,
      value: '',
    },
    {
      label: t.Registry.Labels.signs,
      value: '',
    },
  ];

  return (
    <ListDetailed
      activeRow={activeDriver}
      marks={marks}
      items={items}
      emptyProps={{
        title: 'Сотрудник не выбран',
        description: 'Выберите сотрудника для получения подробной информации',
      }}
      onEdit={isInternal ? undefined : handleEditClick as ListDetailedProps['onEdit']}
    />
  );
};
