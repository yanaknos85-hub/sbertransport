import React, { FC } from 'react';
import { useTranslation } from 'i18n';

import { useSelfAutopark } from 'api/contractors/contractors.api';
import { ListDetailed, Props as ListDetailedProps } from 'components/ListDetailed/ListDetailed';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { UUID } from 'utils/io-ts';

import { useActiveDispatcher } from '../../context/ActiveDispatcher';
import { useEditDelete } from '../../context/EditDeleteContext';

export const DispatcherDetailed: FC = () => {
  const { t } = useTranslation();

  const { activeDispatcher, setActiveDispatcher } = useActiveDispatcher();

  const { handleEditClick, handleDeleteClick } = useEditDelete();

  const { isInternal } = useSelfAutopark().data;

  const marks = [
    {
      label: `${t.global.id}:`,
      value: activeDispatcher?.humanReadableId ?? '',
    },

    {
      label: `${t.Registry.Labels.shortPhone}:`,
      value: formatPhoneNumber(activeDispatcher?.phone),
    },
  ];

  const items: ListDetailedProps['items'] = [
    {
      label: t.Registry.Labels.emailShort,
      value: activeDispatcher?.email ?? '',
    },
    {
      type: 'active',
    },
  ];

  const handleDelete = (id: UUID) => {
    handleDeleteClick(id)
      .then(() => {
        if (activeDispatcher?.id === id) {
          setActiveDispatcher(null);
        }
      });
  };

  return (
    <ListDetailed
      activeRow={activeDispatcher}
      marks={marks}
      items={items}
      emptyProps={{
        title: 'Диспетчер не выбран',
        description: 'Выберите диспетчера для получения подробной информации',
      }}
      onEdit={isInternal ? undefined : handleEditClick as ListDetailedProps['onEdit']}
      onDelete={isInternal ? undefined : handleDelete}
    />
  );
};
