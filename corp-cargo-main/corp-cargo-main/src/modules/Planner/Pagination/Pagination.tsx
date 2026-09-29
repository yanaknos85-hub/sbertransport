import { Pagination as AntdPagination, Select } from 'antd';
import React, { FC, useEffect, useRef, useState } from 'react';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';

import styles from './Pagination.module.scss';

interface Props {
  pagination: PaginationParams;
  total: number;
  setPagination: (pagination: Props['pagination']) => void;
}

const selectOptions = [10, 20, 50, 100].map(option => ({ value: option }));

export const Pagination: FC<Props> = ({
  pagination, total, setPagination,
}) => {
  const [isOpenSelect, setIsOpenSelect] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const toggleSelect = () => setIsOpenSelect(is => !is);

  useEffect(() => {
    if (!isOpenSelect) return undefined;

    const handleMouseDown = (event: MouseEvent) => {
      const { target } = event;
      if (!(target instanceof Element)) return;

      if (containerRef.current?.contains(target)) return;

      if (target.closest('.ant-select-dropdown')) return;

      setIsOpenSelect(false);
    };

    document.addEventListener('mousedown', handleMouseDown);

    return () => {
      document.removeEventListener('mousedown', handleMouseDown);
    };
  }, [isOpenSelect]);

  const handleChangePage = (page: number) => {
    setPagination({ ...pagination, page: page - 1 });
  };

  const handleChangeSize = (size: number) => {
    setPagination({ ...pagination, size });
  };

  return (
    <div className={styles.paginationContainer}>
      <div
        ref={containerRef}
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
        showLessItems
      />
    </div>
  );
};
