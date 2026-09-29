import React, {
  ChangeEvent, FC, useEffect, useState
} from 'react';
import { Input } from 'antd';
import { InputProps } from 'antd/lib/input';
import cn from 'classnames';

import { useDebounce } from 'hooks/useDebounce';
import { ReactComponent as SearchOutlined } from './images/SearchOutlined.svg';
import styles from './SearchPanel.module.scss';

interface Props extends InputProps {
  onSearch?: (value: string) => void;
  isShort?: boolean;
}

export const SearchPanel: FC<Props> = ({
  onSearch, isShort, className, ...props
}) => {
  const [value, setValue] = useState<string>(props.value as string);

  useEffect(() => {
    setValue(props.value as string);
  }, [props.value]);

  const handleSearch = useDebounce(searchValue => onSearch?.(searchValue.trim()), 500);

  const onChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;

    setValue(value);
    handleSearch(value);
  };

  return (
    <div className={cn(styles.searchInput, { [styles.short]: isShort }, className)}>
      <Input
        className={styles.input}
        onChange={onChange}
        {...props}
        value={value}
      />
      <SearchOutlined onClick={() => handleSearch(value)} />
    </div>
  );
};
