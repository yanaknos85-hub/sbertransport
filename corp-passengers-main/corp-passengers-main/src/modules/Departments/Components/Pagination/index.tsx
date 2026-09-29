import React from 'react';
import { Pagination } from 'antd';
import { DepartmentsResponse } from 'stores/Department/Department.interface';
import { Fields } from '../Search';
import styles from './pagination.module.scss';

const DepartmentPagination: React.FC<{
  departmentsResponse: DepartmentsResponse;
  searchQuery: Fields | null;
  isFetching: boolean;
  setSearchQuery: (query: Fields) => void;
}> = ({
  departmentsResponse, searchQuery, isFetching, setSearchQuery,
}) => {
  const activePage = searchQuery?.page || 0;

  const handleChange = (goToPage: number): void => {
    setSearchQuery({ ...searchQuery, page: goToPage - 1 });
  };

  return (
    <div className={styles.wrapper}>
      <Pagination
        className={styles.pagination}
        total={departmentsResponse.totalElements}
        pageSize={departmentsResponse.size}
        defaultPageSize={departmentsResponse.size}
        showQuickJumper={false}
        showSizeChanger={false}
        disabled={isFetching}
        onChange={e => handleChange(e)}
        current={activePage + 1}
      />
    </div>
  );
};

export default DepartmentPagination;
