import { Input } from 'antd';
import { InputProps } from 'antd/lib/input';
import React, {
  ChangeEvent, FC, useEffect, useRef, useState
} from 'react';
import cn from 'classnames';
import { ReactComponent as SearchIcon } from 'shared/assets/svg/search.svg';
import styles from './SearchPanel.module.scss';

type Props = {
  onSearch?: (value: string) => void;
  isShort?: boolean;
} & InputProps;

export const SearchPanel: FC<Props> = ({
  onSearch, isShort, ...props
}) => {
  const [value, setValue] = useState<string>('');

  const onChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;

    setValue(value);
  };

  const isInitialValue = useRef(true);

  useEffect(() => {
    if (isInitialValue.current) {
      isInitialValue.current = false;
      return;
    }

    const timer = setTimeout(() => onSearch?.(value), 500);

    return () => {
      clearTimeout(timer);
    };
  }, [value, onSearch]);

  return (
    <div className={cn(styles.searchInput, { [styles.short]: isShort })}>
      <Input
        value={value}
        className={styles.input}
        onChange={onChange}
        {...props}
      />
      <div className={styles.divider} />
      <SearchIcon />
    </div>
  );
};
