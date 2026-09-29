import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Input } from 'antd';

import { EditableCellProps } from 'shared/components/EditableTable';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const TextCell: FC<EditableCellProps<any> & { toUpperCase?: boolean }> = observer(
  ({
    cellValue, isEditingRecord, dataIndex, onChange, toUpperCase,
  }) => {
    if (isEditingRecord) {
      return (
        <Input
          value={cellValue}
          onChange={e => onChange({ [dataIndex!]: toUpperCase ? e.target.value.toUpperCase() : e.target.value })}
        />
      );
    }

    return <span>{cellValue}</span>;
  }
);
