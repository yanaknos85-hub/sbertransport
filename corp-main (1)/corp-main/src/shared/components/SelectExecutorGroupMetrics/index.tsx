/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useEffect } from 'react';
import { Select as SelectAntd } from 'antd';
import { UUID } from 'utils/io-ts';
import { Organization } from 'stores/Organizations/Organizations.interface';
import { Select as NewSelect } from '../Select';
import styles from './selectexecutor.module.scss';

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
  isHome?: boolean;
  executorGroup: any;
}

export const SelectExecutorGroupMetrics: React.FC<ISelectOrganization> = React.memo(({
  groupId, onListChange, isNewDesign = false, isHome = false, executorGroup, ...props
}) => {
  const { Option } = SelectAntd;

  useEffect(() => {
    if (!executorGroup) return;

    onListChange?.(executorGroup);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [executorGroup]);

  const Select = isNewDesign ? NewSelect : SelectAntd;

  return (
    <Select
      showSearch
      filterOption={handleFilterOrganizations}
      filterSort={handleSortOrganizations}
      mode="multiple"
      className={isHome ? undefined : styles.selectFieldExecutor}
      allowClear
      showArrow
      placeholder={isHome && 'Группы исполнителей'}
      maxTagCount={isHome ? 'responsive' : undefined}
      {...props}
    >
      {executorGroup?.filter(el => el.active).map(entry => (
        <Option key={entry.id} value={entry.id}>
          {entry.name}
        </Option>
      ))}
    </Select>
  );
});
