import React, {
  FC, useCallback, useEffect, useRef, useState
} from 'react';
import { Option, Select } from './Select';
import * as t from 'io-ts';
import { MutateFunction } from 'react-query';
import { Spin } from 'antd';
import { useDebounce } from 'hooks/useDebounce';
import { PaginationParams, createPagination } from 'utils/io-ts/pagination';
import { LabeledValue } from 'types/Types';
import { ignore } from 'utils/utils';

const OFFSET_FOR_FETCHING = 200;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const AnyPaginatedObj = createPagination<any>(t.unknown);
type AnyPaginatedObj = t.TypeOf<typeof AnyPaginatedObj>;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type QueryType = Record<string, any> & PaginationParams;

export interface SelectEndlessScrollProps extends React.ComponentProps<typeof Select> {
  /** Функция из useApiMutation для получения данных */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  fetch: MutateFunction<AnyPaginatedObj, any, QueryType, any>;
  /** Фильтры */
  query?: Omit<QueryType, keyof PaginationParams>;
  /** Название поля, содержащего value для Option */
  valueField?: string;
  /** Название поля, содержащего label для Option */
  labelField?: string;
  /** Название поля для поиска, обязательно при включении showSearch */
  searchField?: string;
  /** Размер страницы */
  pageSize?: number;
  /** Запросы будут отправляться, только если enabled = true, по умолчанию true */
  enabled?: boolean;
  /** Label для выбранного элемента, если его нет на загруженных страницах. Например, если по умолчанию выбран элемент со второй страницы */
  labelForDefaultValue?: string;
}

export const SelectEndlessScroll: FC<SelectEndlessScrollProps> = ({
  fetch,
  query,
  valueField = 'id',
  labelField = 'name',
  pageSize = 20,
  enabled = true,
  searchField,
  labelForDefaultValue,
  ...props
}) => {
  const page = useRef(0);

  const [options, setOptions] = useState<LabeledValue[]>([]);
  const [totalPages, setTotalPages] = useState(Infinity);
  const [isFetching, setIsFetching] = useState(false);
  const [searchString, setSearchString] = useState('');

  const getNewData = useCallback(() => {
    if (!enabled) {
      setOptions([]);
      return;
    }

    const saveNewOptions = (response?: AnyPaginatedObj) => {
      if (response) {
        const content = response?.content.map(data => ({ value: data[valueField], label: data[labelField] }));
        setOptions(page.current === 0 ? content : prev => [...prev, ...content]);
        setTotalPages(response.totalPages);
      }

      return response;
    };

    fetch({
      ...query,
      page: page.current,
      size: pageSize,
      ...(searchField && { [searchField]: searchString || undefined }),
    })
      .then(saveNewOptions)
      .catch(ignore)
      .finally(() => setIsFetching(false));
  }, [fetch, query, valueField, labelField, pageSize, enabled, searchString, searchField]);

  useEffect(() => {
    page.current = 0;
    getNewData();
  }, [getNewData]);

  const handleScroll: React.UIEventHandler<HTMLDivElement> = useCallback(
    e => {
      const target = e.target as HTMLDivElement;

      if (isFetching || page.current + 1 >= totalPages) {
        return;
      }

      if (target.scrollTop + target.offsetHeight + OFFSET_FOR_FETCHING < target.scrollHeight) {
        return;
      }

      page.current++;
      setIsFetching(true);
      getNewData();
    },
    [totalPages, isFetching, getNewData]
  );

  const onSearch = useDebounce(setSearchString, 300);

  const isDefaultValue = props.value && !options.some(option => option.value === props.value);

  return (
    <Select
      onPopupScroll={handleScroll}
      onSearch={props.showSearch ? onSearch : undefined}
      filterOption={false}
      {...props}
    >
      {isDefaultValue && <Option value={props.value as string}>{labelForDefaultValue ?? props.value}</Option>}

      {options.map(option => <Option key={option.value} value={option.value}>{option.label}</Option>)}
      {isFetching && (
      <Option
        key="loading"
        value="loading"
        disabled
      >
        <Spin size="small" />
      </Option>
      )}
    </Select>
  );
};
