import React, { FC, useEffect, useState } from 'react';
import { Input } from 'antd';
import { InputProps } from 'antd/lib/input';
import cn from 'classnames';
import { useTranslation } from 'i18n';

import { defaultHandleSearch, onChange as onChangeHandler } from '../List/lib/handlers';
import { ReactComponent as SearchOutlined } from './images/SearchOutlined.svg';
import styles from './SearchPanel.module.scss';

export type SearchPanelProps = {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  setQuery?: (query: any) => void;
  onSearch?: (value: string) => void;
  placeholder?: string;
  /** Название поля для поиска */
  searchField?: string;
  /** Значение поля поиска */
  searchValue?: string;
  /** Минимальная ширина */
  minWidth?: number;
  /** Класс для контейнера */
  containerClassName?: string;
} & InputProps;

export const SearchPanel: FC<SearchPanelProps> = ({
  setQuery,
  onSearch,
  className,
  containerClassName,
  onKeyDown,
  onChange,
  placeholder,
  searchField,
  searchValue,
  minWidth = 340,
  ...inputProps
}) => {
  const [value, setValue] = useState(searchValue ?? '');

  useEffect(() => {
    setValue(searchValue ?? '');
  }, [searchValue]);

  const { t } = useTranslation();

  const onSearchByField = (field: string) => (val: string) => setQuery?.({ [field]: val || undefined });

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

  const searchIcon = (
    <SearchOutlined className={styles.searchIcon} />
  );

  return (
    <div className={cn(styles.searchInput, containerClassName)} style={{ minWidth }}>
      <Input.Search
        value={value}
        className={cn(className)}
        onChange={handleChange}
        onSearch={handleSearchTrigger}
        placeholder={placeholder || t.Requests.List.searchPanel}
        onPressEnter={handlePress}
        allowClear
        enterButton={searchIcon}
        {...inputProps}
      />
    </div>
  );
};
