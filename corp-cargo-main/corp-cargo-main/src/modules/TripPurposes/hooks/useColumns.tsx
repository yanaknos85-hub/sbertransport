import { useMemo } from 'react';
import { useTranslation } from 'i18n';

interface ColumnT {
  id: string;
  label: string;
  condition?: string;
}

export const useColumns = (deleteButton: any) => {
  const { t } = useTranslation();
  return useMemo(
    () => [
      {
        title: t.Forms.TripPurposes.label,
        dataIndex: 'label',
        key: 'label',
        sorter: {
          compare: (a: ColumnT, b: ColumnT) => a.label.localeCompare(b.label),
        },
        sorting: true,
        defaultSortOrder: undefined,
      },
      {
        title: t.Forms.TripPurposes.condition,
        dataIndex: 'condition',
        key: 'condition',
      },
      {
        ...deleteButton,
      },
    ],
    []
  );
};
