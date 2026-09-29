import React, { FC, useCallback, useState } from 'react';
import { useTranslation } from 'i18n';

import { useAllDispatchers } from 'api/dispatchers/dispatchers.api';
import { useProfile } from 'api/profile/profile.api';
import { useSelfAutopark } from 'api/contractors/contractors.api';
import { List, Props as ListProps } from 'components/List/List';
import { Props as RowProps } from 'components/List/components/Row/Row';
import { defaultPagination } from 'hooks/useQuery';
import { getFullName } from 'utils/getFullName';
import { UUID } from 'utils/io-ts';

import { formJSX } from './formJSX/formJSX';
import { useModalForm } from '../../context/ModalForm';
import { useActiveDispatcher } from '../../context/ActiveDispatcher';
import { useEditDelete } from '../../context/EditDeleteContext';

export const DispatcherList: FC = () => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [query, setQuery] = useState<Record<string, any>>(defaultPagination);
  const { t } = useTranslation();
  const { handleOpenAdd } = useModalForm();

  const { contractorId, autoparkId } = useProfile().data;

  const { content: dispatchers, totalElements } = useAllDispatchers({
    contractorId, autoparkId, query,
  }).data;

  const { isInternal } = useSelfAutopark().data;

  const {
    activeDispatcher, setActiveDispatcher, toggleActiveDispatcher,
  } = useActiveDispatcher();

  const { handleEditClick, handleDeleteClick } = useEditDelete();

  const handleDelete = (id: UUID) => {
    handleDeleteClick(id)
      .then(() => {
        if (activeDispatcher?.id === id) {
          setActiveDispatcher(null);
        }
      });
  };

  const rows: ListProps['rows'] = dispatchers.map(dispatcher => ({
    rowData: dispatcher,
    activeRow: activeDispatcher,
    statuses: [{ type: 'active' }, { title: t.global.id, description: dispatcher.humanReadableId }],
    toggleActive: toggleActiveDispatcher as RowProps['toggleActive'],
    onEdit: isInternal ? undefined : handleEditClick as RowProps['onEdit'],
    onDelete: isInternal ? undefined : handleDelete,
  }));

  const setFilters = useCallback(filters => {
    setQuery({
      ...filters,
      firstName: filters.firstName || undefined,
      lastName: filters.lastName || undefined,
      patronymic: filters.patronymic || undefined,
    });
  }, []);

  return (
    <List
      autoSize
      onChangeQuery={setFilters}
      onAddClick={isInternal ? undefined : handleOpenAdd}
      formJSX={formJSX(t)}
      rows={rows}
      totalElements={totalElements}
      searchPanelProps={{ searchValue: getFullName(query) }}
    />
  );
};
