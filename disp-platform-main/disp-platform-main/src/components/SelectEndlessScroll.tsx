import React, {
  FC, useCallback, useEffect, useMemo, useRef, useState
} from 'react';
import { Select, Option } from '@sber-sbertransport/ui-kit/src';
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
  /** Функция из useApiMutation для получения списка данных */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  fetch: MutateFunction<AnyPaginatedObj, any, QueryType, any>;
  /** Функция из useApiMutation для получения текущего значения */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  fetchOne?: MutateFunction<Record<string, any>, any, any, any>;
  /** Фильтры */
  query?: Omit<QueryType, keyof PaginationParams>;
  /** Название поля, содержащего value для Option */
  valueField?: string;
  /** Название поля, содержащего label для Option */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  labelField?: string | ((data: any) => string);
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
  fetchOne,
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

  const [defaultLabelValue, setDefaultLabelValue] = useState(labelForDefaultValue);

  const isDefaultValue = useMemo(() => {
    const value = props.mode === 'multiple' ? props.value?.[0] : props.value;

    return value && options.length && !options.some(option => option.value === value);
  }, [options, props.mode, props.value]);

  const getNewData = useCallback(() => {
    if (!enabled) {
      setOptions([]);
      return;
    }

    if (searchString && searchString.length < 3) {
      return;
    }

    const saveNewOptions = (response?: AnyPaginatedObj) => {
      if (response) {
        const content = response?.content.map(data => ({
          value: data[valueField],
          label: typeof labelField === 'function' ? labelField(data) : data[labelField],
        }));
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
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fetch, query, valueField, JSON.stringify(labelField), pageSize, enabled, searchString, searchField]);

  const getDefaultValue = useCallback(() => {
    if (fetchOne && !defaultLabelValue) {
      const value = props.mode === 'multiple' ? props.value?.[0] : props.value;

      fetchOne(value)
        .then(data => {
          if (data) {
            setDefaultLabelValue(typeof labelField === 'function' ? labelField(data) : data[labelField]);
          }
        })
        .catch(ignore);
    }
  }, [defaultLabelValue, fetchOne, labelField, props.mode, props.value]);

  useEffect(() => {
    page.current = 0;
    getNewData();
  }, [getNewData]);

  useEffect(() => {
    if (page.current === 0 && isDefaultValue) {
      getDefaultValue();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isDefaultValue]);

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

  return (
    <Select
      onPopupScroll={handleScroll}
      onSearch={props.showSearch ? onSearch : undefined}
      filterOption={false}
      {...props}
    >
      {isDefaultValue && (
        <Option value={props.mode === 'multiple' ? props.value?.[0] : props.value}>
          {defaultLabelValue ?? props.value}
        </Option>
      )}

      {options.map(option => (
        <Option key={option.value} value={option.value}>
          {option.label}
        </Option>
      ))}

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
