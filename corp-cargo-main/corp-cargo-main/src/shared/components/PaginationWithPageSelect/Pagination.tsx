import { Pagination as AntdPagination, Select } from 'antd';
import React, { FC, useState } from 'react';
import classNames from 'classnames';
import styles from './Pagination.module.scss';

interface Props {
  pagination: { page: number; size: number };
  total: number;
  setPagination: (pagination: Props['pagination']) => void;
  wrapperStyles?: string;
}

const selectOptions = [10, 20, 50, 100].map(option => ({ value: option }));

export const Pagination: FC<Props> = ({
  pagination, total, setPagination, wrapperStyles,
}) => {
  const [isOpenSelect, setIsOpenSelect] = useState(false);

  const toggleSelect = () => setIsOpenSelect(is => !is);

  const handleChangePage = (page: number) => {
    setPagination({ ...pagination, page: page - 1 });
  };

  const handleChangeSize = (size: number) => {
    setPagination({ ...pagination, size });
  };

  return (
    <div className={classNames([styles.paginationContainer, wrapperStyles])}>
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
          defaultValue={pagination.size}
          onChange={handleChangeSize}
        />
      </div>
      <AntdPagination
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
