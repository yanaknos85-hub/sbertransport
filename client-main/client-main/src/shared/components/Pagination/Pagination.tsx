import React, { FC, useState } from 'react';
import { Pagination as AntdPagination, Select } from 'antd';
import cn from 'classnames';

import { ReactComponent as Icon } from 'shared/form/Select/images/selectArrow.svg';
import styles from './Pagination.module.scss';

interface Props {
  pagination: { page: number; size: number };
  total: number;
  setPagination: (pagination: Props['pagination']) => void;
  className?: string;
  showLessItems?: boolean;
}

const selectOptions = [10, 20, 50, 100].map(option => ({ value: option }));

export const Pagination: FC<Props> = ({
  pagination, total, setPagination, className, showLessItems,
}) => {
  const [isOpenSelect, setIsOpenSelect] = useState(false);

  const toggleSelect = () => setIsOpenSelect(isOpen => !isOpen);

  const handleChangePage = (page: number) => {
    setPagination({ ...pagination, page: page - 1 });
  };

  const handleChangeSize = (size: number) => {
    setPagination({ page: 0, size });
  };

  return (
    <div className={cn(styles.paginationContainer, className)}>
      <div
        className={styles.sizeSelector}
        tabIndex={-1}
        onKeyDown={toggleSelect}
        role="button"
        onClick={toggleSelect}
      >
        <span className={styles.selectDescription}>Показывать по</span>
        <Select
          open={isOpenSelect}
          className={styles.select}
          options={selectOptions}
          defaultValue={selectOptions[0].value}
          value={pagination.size}
          suffixIcon={<Icon />}
          onChange={handleChangeSize}
        />
      </div>
      <AntdPagination
        showLessItems={showLessItems}
        className={styles.pagination}
        pageSize={pagination.size}
        current={pagination.page + 1}
        total={total}
        showSizeChanger={false}
        size="small"
        onChange={handleChangePage}
      />
    </div>
  );
};
