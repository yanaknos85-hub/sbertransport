/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useEffect } from 'react';
import { Select as SelectAntd } from 'antd';
import { useOrganizationProjection } from 'api/organizations/search';
import { UUID } from 'utils/io-ts';
import { Organization } from 'stores/Organizations/Organizations.interface';
import { Select as NewSelect } from '../Select';

const handleFilterOrganizations = (input: string, option?: any): boolean => (
  ((option!.children as unknown) as string).includes(input)
);

const handleSortOrganizations = (optionA: any, optionB: any) => ((optionA!.children as unknown) as string)
  .toLowerCase()
  .localeCompare(((optionB!.children as unknown) as string).toLowerCase());

interface ISelectOrganization extends React.ComponentProps<typeof SelectAntd> {
  groupId?: UUID;
  onListChange?: (list: Organization[]) => void;
  isNewDesign?: boolean;
}

export const SelectOrganization: React.FC<ISelectOrganization> = React.memo(({
  groupId, onListChange, isNewDesign = false, ...props
}) => {
  const organizations = useOrganizationProjection({ query: { groupId } }, { suspense: false }).data;

  const { Option } = SelectAntd;

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
      {...props}
    >
      {organizations?.map(entry => (
        <Option key={entry.id} value={entry.id}>
          {entry.officialName}
        </Option>
      ))}
    </Select>
  );
});
