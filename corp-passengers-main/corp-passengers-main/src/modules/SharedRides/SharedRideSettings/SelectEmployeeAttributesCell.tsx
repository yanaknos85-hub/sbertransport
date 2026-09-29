import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Select } from 'antd';
import { UUID } from 'utils/io-ts';

import { useProfile } from 'api/profile';
import { useEmployeeAttributes } from 'api/employee-attributes';
import { LabeledValue } from 'utils/Types';
import { startsWithIgnoreCase } from 'utils';
import { EditableCellProps } from 'shared/components/EditableTable';
import { SettingsRecord } from '.';

import styles from './styles.module.scss';

export const SelectEmployeeAttributesCell: FC<
  EditableCellProps<SettingsRecord, SettingsRecord['attributes']>
> = observer(({
  cellValue, isEditingRecord, onChange,
}) => {
  const profile = useProfile().data;

  // @ts-ignore
  const attributes = useEmployeeAttributes(profile.organizationId).data;
  const selectedAttributes = (cellValue || []).map(({ id }) => id);

  if (isEditingRecord) {
    const handleChange = (attributeIds: UUID[]) => onChange({ attributes: attributeIds.map(id => ({ id })) });

    const options: LabeledValue[] = attributes.map(({ id, name }) => ({
      value: id,
      label: name,
    }));

    return (
      <Select
        mode="multiple"
        className={styles.cellSelect}
        allowClear
        onChange={handleChange}
        value={selectedAttributes}
        options={options}
        // eslint-disable-next-line no-use-before-define
        filterOption={filterOption}
      />
    );
  }

  return (
    <div className={styles.cellDisplay}>
      {attributes
        .filter(attribute => selectedAttributes.includes(attribute.id as UUID))
        .map(({ name }) => name)
        .join(', ')}
    </div>
  );
});

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const filterOption = (inputValue: string, o: any): boolean => {
  const label = (o as LabeledValue).label as string;
  const filterValue = inputValue.trim();
  return filterValue.length === 0 || startsWithIgnoreCase(label, filterValue);
};
