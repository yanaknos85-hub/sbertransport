/* eslint-disable @typescript-eslint/no-explicit-any */
import React, {
  FC, memo, useEffect, useCallback, useState
} from 'react';
import { Select as SelectAntd, SelectProps } from 'antd';
import { ExecutorGroups, useGetExecutorGroups } from 'api/executor-group';
import { UUID } from 'utils/io-ts';
import { Select as NewSelect } from '../Select';
import { useCargoRoute } from 'shared/hooks/useCargoRoute';
import {
  EXECUTOR_GROUP_ALL_ID,
  EXECUTOR_GROUP_EMPTY_ID
} from 'constants/constants.app';

import styles from './selectexecutor.module.scss';

const SPECIAL_OPTIONS_LABELS: Record<string, string> = {
  [EXECUTOR_GROUP_ALL_ID]: 'Все группы',
  [EXECUTOR_GROUP_EMPTY_ID]: 'Без групп',
};

const handleFilterOrganizations = (input: string, option?: any): boolean => (
  ((option!.children as unknown) as string).includes(input)
);

const handleSortOrganizations = (optionA: any, optionB: any) => ((optionA!.children as unknown) as string)
  .toLowerCase()
  .localeCompare(((optionB!.children as unknown) as string).toLowerCase());

const SPECIAL_IDS = [EXECUTOR_GROUP_ALL_ID, EXECUTOR_GROUP_EMPTY_ID];

type Special = 'all' | 'empty' | null;

interface ISelectExecutorGroupProps extends Omit<SelectProps<unknown>, 'onChange'> {
  groupId?: UUID;
  onListChange?: (list: ExecutorGroups[]) => void;
  isNewDesign?: boolean;
  isHome?: boolean;
  defaultValue?: string[];
  onChange?: (groupIds: string[]) => void;
  onEmptyChange?: (isEmpty: boolean) => void;
  emptyActive?: boolean;
  onAllChange?: (isAll: boolean) => void;
  allActive?: boolean;
  value?: string[];
  placeholder?: string;
}

export const SelectExecutorGroup: FC<ISelectExecutorGroupProps> = memo(({
  groupId,
  onListChange,
  isNewDesign = false,
  isHome = false,
  defaultValue = [],
  onChange,
  onEmptyChange,
  emptyActive = false,
  onAllChange,
  allActive = false,
  value,
  ...props
}) => {
  const { isCargoAny: isCargo } = useCargoRoute();

  const { data, isLoading } = useGetExecutorGroups({ page: 0, size: 200 }, isCargo);
  const executorGroup = data?.content;
  const { Option } = SelectAntd;

  useEffect(() => {
    if (!executorGroup) return;
    onListChange?.(executorGroup);
  }, [executorGroup, onListChange]);

  // Локальное состояние спец-опции: различаем 'all' (Все группы) и 'empty' (Без групп),
  // потому что обе дают value=[] в контексте. На бэк уходит всегда пустой массив.
  // Если приходит emptyActive=true из родителя (например, после восстановления из LS),
  // синхронизируем начальное значение.
  const [special, setSpecial] = useState<Special>(
    emptyActive ? 'empty' : (allActive ? 'all' : null)
  );

  const selectedValue: string[] = special === 'all'
    ? [EXECUTOR_GROUP_ALL_ID]
    : special === 'empty'
      ? [EXECUTOR_GROUP_EMPTY_ID]
      : (value ?? defaultValue);

  const Select = isNewDesign ? NewSelect : SelectAntd;

  const handleChange = (changedValue: unknown) => {
    const selectedIds = Array.isArray(changedValue) ? changedValue : [];

    const prev = selectedValue;
    const prevSet = new Set(prev);
    const newSet = new Set(selectedIds);

    const added = selectedIds.filter(id => !prevSet.has(id));
    const removed = prev.filter(id => !newSet.has(id));

    if (added.length === 0 && removed.length === 0) {
      return;
    }

    const addedEmpty = added.includes(EXECUTOR_GROUP_EMPTY_ID);
    const addedAll = added.includes(EXECUTOR_GROUP_ALL_ID);

    if (addedEmpty) {
      setSpecial('empty');
      onChange?.([]);
      onEmptyChange?.(true);
      onAllChange?.(false);
      return;
    }

    if (addedAll) {
      setSpecial('all');
      onChange?.([]);
      onEmptyChange?.(false);
      onAllChange?.(true);
      return;
    }

    // Реальные группы: фильтруем спец-id на всякий случай
    const realGroups = selectedIds.filter(id => !SPECIAL_IDS.includes(id));
    setSpecial(null);
    onChange?.(realGroups);
    onEmptyChange?.(false);
    onAllChange?.(false);
  };

  const handleClear = useCallback(() => {
    setSpecial(null);
    onChange?.([]);
    onEmptyChange?.(false);
    onAllChange?.(false);
  }, [onChange, onEmptyChange, onAllChange]);

  return (
    <Select
      showSearch
      filterOption={handleFilterOrganizations}
      filterSort={handleSortOrganizations}
      mode="multiple"
      className={isHome ? undefined : styles.selectFieldExecutor}
      allowClear
      showArrow
      placeholder={isHome ? 'Группы исполнителей' : props.placeholder}
      maxTagCount={isHome ? 'responsive' : undefined}
      loading={isLoading}
      value={selectedValue}
      onChange={handleChange}
      onClear={handleClear}
      {...props}
    >
      {isCargo && (
        <Option key={EXECUTOR_GROUP_ALL_ID} value={EXECUTOR_GROUP_ALL_ID}>
          {SPECIAL_OPTIONS_LABELS[EXECUTOR_GROUP_ALL_ID]}
        </Option>
      )}
      {isCargo && (
        <Option key={EXECUTOR_GROUP_EMPTY_ID} value={EXECUTOR_GROUP_EMPTY_ID}>
          {SPECIAL_OPTIONS_LABELS[EXECUTOR_GROUP_EMPTY_ID]}
        </Option>
      )}
      {executorGroup?.filter(el => el.active).map(entry => (
        <Option key={entry.id} value={entry.id}>
          {entry.name}
        </Option>
      ))}
    </Select>
  );
});
