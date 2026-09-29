import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Select } from 'antd';
import { UUID } from 'utils/io-ts';

import { useProfile } from 'api/profile';
import { usePositions } from 'api/positions';
import { LabeledValue } from 'utils/Types';
import { startsWithIgnoreCase } from 'utils';
import { EditableCellProps } from 'shared/components/EditableTable';
import { SettingsRecord } from '.';

import styles from './styles.module.scss';

export const SelectPositionsCell: FC<EditableCellProps<SettingsRecord, SettingsRecord['positions']>> = observer(
  ({
    cellValue, isEditingRecord, onChange,
  }) => {
    const profile = useProfile().data;
    // @ts-ignore
    const { positions } = usePositions(profile.organizationId).data;

    const selectedPositions = (cellValue || []).map(({ id }) => id);

    if (isEditingRecord) {
      const handleChange = (positionIds: UUID[]) => onChange({ positions: positionIds.map(id => ({ id })) });

      const options: LabeledValue[] = positions.map(({ id, positionName }) => ({
        value: id,
        label: positionName,
      }));

      return (
        <Select
          mode="multiple"
          className={styles.cellSelect}
          allowClear
          onChange={handleChange}
          value={selectedPositions}
          options={options}
          filterOption={filterOption}
        />
      );
    }

    return (
      <div className={styles.cellDisplay}>
        {positions
          .filter(position => selectedPositions.includes(position.id))
          .map(({ positionName }) => positionName)
          .join(', ')}
      </div>
    );
  }
);

const filterOption = (inputValue: string, o: any): boolean => {
  const label = (o as LabeledValue).label as string;
  const filterValue = inputValue.trim();
  return filterValue.length === 0 || startsWithIgnoreCase(label, filterValue);
};
