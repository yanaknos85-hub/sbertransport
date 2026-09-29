/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { ComponentProps, FC, memo } from 'react';
import { Select as SelectAntd } from 'antd';
import { UUID } from 'utils/io-ts';
import { Organization } from 'stores/Organizations/Organizations.interface';
import { Select as NewSelect } from '../Select';

interface ISelectFlag extends ComponentProps<typeof SelectAntd> {
  options: any[];
  groupId?: UUID;
  onListChange?: (list: Organization[]) => void;
  isNewDesign?: boolean;
}

export const SelectFlag: FC<ISelectFlag> = memo(({
  groupId, onListChange, isNewDesign = false, options, ...props
}) => {
  const { Option } = SelectAntd;

  const Select = isNewDesign ? NewSelect : SelectAntd;

  return (
    <Select
      showSearch
      {...props}
    >
      {options?.map((entry, i) => (
        <Option key={i} value={entry}>
          {entry}
        </Option>
      ))}
    </Select>
  );
});
