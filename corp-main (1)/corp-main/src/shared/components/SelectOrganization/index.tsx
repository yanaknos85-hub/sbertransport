import React, { useEffect } from 'react';
import { Select as SelectAntd } from 'antd';
import { useOrganizationProjection } from 'api/organizations/search';
import { UUID } from 'utils/io-ts';
import { Organization } from 'stores/Organizations/Organizations.interface';
import { Select as NewSelect } from '../Select';

import styles from './selectexecutor.module.scss';

const handleFilterOrganizations = (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  input: string, option?: any
): boolean => ((option!.children as unknown) as string).includes(input);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const handleSortOrganizations = (optionA: any, optionB: any) => ((optionA!.children as unknown) as string)
  .toLowerCase()
  .localeCompare(((optionB!.children as unknown) as string).toLowerCase());

interface IOption {
  id: string;
  officialName: string;
}

interface ISelectOrganization extends React.ComponentProps<typeof SelectAntd> {
  groupId?: UUID;
  onListChange?: (list: Organization[]) => void;
  isNewDesign?: boolean;
  isHome?: boolean;
  defaultOption?: IOption | undefined;
}

export const SelectOrganization: React.FC<ISelectOrganization> = React.memo(({
  groupId,
  onListChange,
  isNewDesign = false,
  isHome = false,
  defaultOption,
  ...props
}) => {
  const organizations = useOrganizationProjection({ query: { groupId } }, { suspense: false }).data;
  const { Option } = SelectAntd;

  const options = [
    defaultOption,
    ...(organizations ?? []).map(({ id, officialName }) => ({ id, officialName })),
  ].filter((current, index, array) => Boolean(current) && array.findIndex(elem => elem?.id === current?.id) === index
  ) as IOption[];

  useEffect(() => {
    if (!organizations) return;

    onListChange?.(organizations);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [organizations]);

  const Select = isNewDesign ? NewSelect : SelectAntd;

  return (
    <Select
      showSearch
      filterOption={handleFilterOrganizations}
      filterSort={handleSortOrganizations}
      mode={isHome ? 'multiple' : undefined}
      className={isHome ? undefined : styles.selectFieldExecutor}
      allowClear={isHome}
      showArrow
      maxTagCount={isHome ? 'responsive' : undefined}
      {...props}
    >
      {options.map(({ id, officialName }) => (
        <Option key={id} value={id}>
          {officialName}
        </Option>
      ))}
    </Select>
  );
});
