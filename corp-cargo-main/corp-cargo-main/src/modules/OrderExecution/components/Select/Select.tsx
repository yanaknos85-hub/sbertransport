import React from 'react';
import { SelectProps, SelectValue } from 'antd/lib/select';
import { Select as SelectAnt } from 'antd';
import cn from 'classnames';
import { ReactComponent as Icon } from 'shared/assets/svg/selectArrow.svg';

import styles from './styles.module.scss';

type SelectType = SelectProps<SelectValue> & {
  className?: string;
};

const Select = (props: SelectType) => {
  const { className, suffixIcon, ...restProps } = props;

  const renderSuffixIcon = () => {
    if (suffixIcon) {
      return (
        <>
          {suffixIcon}
          <Icon className={styles.arrowIcon} />
        </>
      );
    }

    return <Icon className={styles.arrowIcon} />;
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