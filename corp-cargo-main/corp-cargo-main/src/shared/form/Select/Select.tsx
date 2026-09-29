import React from 'react';
import { SelectProps, SelectValue } from 'antd/lib/select';
import { Select as SelectAnt } from 'antd';
import cn from 'classnames';
import styles from './Select.module.scss';

import { ReactComponent as Icon } from './images/selectArrow.svg';

type SelectType = SelectProps<SelectValue> & { className?: string };

const Select = (props: SelectType) => {
  const {
    className, suffixIcon, ...restProps
  } = props;

  // Если передана иконка через suffixIcon, объединяем её со стрелкой
  const renderSuffixIcon = () => {
    if (suffixIcon) {
      return (
        <>
          {suffixIcon}
          <Icon />
        </>
      );
    }
    return <Icon />;
  };

  return (
    <SelectAnt
      className={cn(styles.select, className)}
      suffixIcon={renderSuffixIcon()}
      {...restProps}
    />
  );
};

export default Select;
