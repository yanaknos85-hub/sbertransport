import React, { FC } from 'react';
import { observer } from 'mobx-react';

import { EditableCellProps } from 'shared/components/EditableTable';
import { NumericInput } from 'shared/components/Inputs/NumericInput';
import { formatRubles } from 'utils';

export const NumericInputCell: FC<EditableCellProps<any>> = observer(
  ({
    cellValue, isEditingRecord, dataIndex, onChange,
  }) => {
    if (isEditingRecord) {
      return <NumericInput value={cellValue} onChange={e => onChange({ [dataIndex!]: e })} />;
    }

    return <span>{formatRubles(cellValue)}</span>;
  }
);
