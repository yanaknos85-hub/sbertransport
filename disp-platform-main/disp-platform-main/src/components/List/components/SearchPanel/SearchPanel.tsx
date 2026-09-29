import React, { FC, useEffect, useState } from 'react';
import { Input } from 'antd';
import { InputProps } from 'antd/lib/input';
import cn from 'classnames';
import { useTranslation } from 'i18n';

import { defaultHandleSearch, onChange as onChangeHandler } from '../../lib/handlers';
import { Query } from '../../types';
import { ReactComponent as SearchOutlined } from './images/SearchOutlined.svg';
import styles from './SearchPanel.module.scss';

export type SearchPanelProps = {
  setQuery: (query: Query) => void;
  onSearch?: (value: string) => void;
  placeholder?: string;
  /** Название поля для поиска */
  searchField?: string;
  /** Значение поля поиска */
  searchValue?: string;
} & InputProps;

export const SearchPanel: FC<SearchPanelProps> = ({
  setQuery,
  onSearch,
  className,
  onKeyDown,
  onChange,
  placeholder,
  searchField,
  searchValue,
  ...inputProps
}) => {
  const [value, setValue] = useState(searchValue ?? '');

  useEffect(() => {
    setValue(searchValue ?? '');
  }, [searchValue]);

  const { t } = useTranslation();

  const onSearchByField = (field: string) => (val: string) => setQuery({ [field]: val || undefined });

  const handleSearch = onSearch ?? (searchField ? onSearchByField(searchField) : defaultHandleSearch(setQuery));

  const handleSearchTrigger = () => handleSearch(value);

  const handlePress: React.KeyboardEventHandler<HTMLInputElement> = e => {
    onKeyDown?.(e);

    if (e.key !== 'Enter') {
      return;
    }

    handleSearchTrigger();
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChangeHandler(setValue)(e);
    onChange?.(e);
  };

  return (
    <div className={styles.searchInput}>
      <Input
        value={value}
        className={cn(styles.input, className)}
        onChange={handleChange}
        placeholder={placeholder || t.Requests.List.searchPanel}
        onKeyDown={handlePress}
        {...inputProps}
      />
      <SearchOutlined onClick={handleSearchTrigger} />
    </div>
  );
};
