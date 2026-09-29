import { Select } from 'antd';
import { useRoles } from 'api/roles';
import * as React from 'react';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const SelectRole = ({ defaultValue, ...selectProps }: React.ComponentProps<any>): JSX.Element => {
  const { data: roles } = useRoles();

  return (
    <Select
      optionFilterProp="label"
      {...selectProps}
      defaultValue={defaultValue}
    >
      {roles.map(({ code, name }) => (
        <Select.Option
          key={code}
          value={code}
          label={name}
        >
          {name}
        </Select.Option>
      ))}
    </Select>
  );
};

SelectRole.FormItem = SelectRole as FormItem<typeof SelectRole>;
