import React, {
  FC, useState, useMemo, useCallback
} from 'react';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile/profile.api';
import { AutoParksQuery } from 'api/autopark/autopark.types';
import { useDeleteAutopark, useGetAutoParks } from 'api/autopark/autopark.api';
import { useSelfAutopark } from 'api/contractors/contractors.api';
import { List, Props as ListProps } from 'components/List/List';
import { Props as RowProps } from 'components/List/components/Row/Row';
import { defaultPagination } from 'hooks/useQuery';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

import { formJSX } from './formJSX/formJSX';
import { useModalForm } from '../../context/ModalForm';
import { useActiveAutopark } from '../../context/ActiveAutopark';

export const AutoparkList: FC = () => {
  const [query, setQuery] = useState<Record<string, unknown>>({ ...defaultPagination });

  const { t } = useTranslation();
  const { handleOpenAdd, handleOpenEdit } = useModalForm();

  const { contractorId } = useProfile().data;
  const { content: autoparks, totalElements } = useGetAutoParks({ contractorId, query: query as AutoParksQuery }).data;

  const { isInternal } = useSelfAutopark().data;

  const { activeAutopark, toggleActiveAutopark } = useActiveAutopark();

  const [deleteAutopark] = useDeleteAutopark({ contractorId });

  const handleEditClick = useCallback(
    (selectedAutopark: (typeof autoparks)[0]) => handleOpenEdit(selectedAutopark),
    [handleOpenEdit]
  );

  const handleDeleteClick = useCallback(
    (id: UUID) => deleteAutopark({ autoparkId: id }).catch(ignore),
    [deleteAutopark]
  );

  const rows: ListProps['rows'] = useMemo(
    () => autoparks.map(autopark => ({
      rowData: autopark,
      activeRow: activeAutopark,
      toggleActive: toggleActiveAutopark as RowProps['toggleActive'],
      statuses: [{ type: autopark.active ? 'active' : 'inactive' }],
      onEdit: isInternal ? undefined : handleEditClick as RowProps['onEdit'],
      onDelete: isInternal ? undefined : handleDeleteClick,
    })),
    [activeAutopark, autoparks, isInternal, handleDeleteClick, handleEditClick, toggleActiveAutopark]
  );

  return (
    <List
      autoSize
      onChangeQuery={setQuery}
      onAddClick={isInternal ? undefined : handleOpenAdd}
      formJSX={formJSX(t)}
      rows={rows}
      totalElements={totalElements}
      searchPanelProps={{
        placeholder: t.AutoparkTable.searchPlaceholder,
        searchValue: query.name as string,
        searchField: 'name',
      }}
    />
  );
};
