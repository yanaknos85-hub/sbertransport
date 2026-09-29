import { Select as AntdSelect } from 'antd';
import { SelectProps } from 'antd/lib/select';
import React, { FC } from 'react';
import { useContractors } from '../../../api/contractors';
import { Select } from '../Select';

type Props<T> = Omit<SelectProps<T>, 'options' | 'children'> & { restrict?: T[]; isNewDesign?: boolean };

// any поставил специально, т.к. конфликтуют типы onSelect. Тут передавалась строка, а <Select /> считает, что там должно
// быть SelectValue | RawValue или еще что-то. Не придумал, как сделать красиво
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const SelectContractor: FC<Props<any>> = ({
  restrict, isNewDesign = false, ...selectProps
}) => {
  const contractors = useContractors({ config: { suspense: false } }).data?.contractors;
  const availableContractorsTypes = contractors?.filter(({ name }) => !restrict || restrict.includes(name));
  const options = availableContractorsTypes?.map(({ id: value, name: label }) => ({ value, label })) ?? [];

  // @ts-ignore
  if (isNewDesign) return (
    // @ts-ignore
    <Select
      {...selectProps}
      options={options}
      getPopupContainer={trigger => trigger.parentNode}
    />
  );

  return (
    <AntdSelect
      {...selectProps}
      options={options}
      getPopupContainer={trigger => trigger.parentNode}
    />
  );
};

export default SelectContractor;
