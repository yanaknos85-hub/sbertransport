import React from 'react';
import { Pagination } from 'antd';

import { EmployeeResponse } from 'stores/Employee/Employee.interface';
import { Fields } from '../Search';
import styles from './pagination.module.scss';

const EmployeePagination: React.FC<{
  employeeResponse: EmployeeResponse;
  searchQuery: Fields | null;
  setSearchQuery: (searchQuery: Fields) => void;
}> = ({
  employeeResponse, searchQuery, setSearchQuery,
}) => {
  const activePage = searchQuery?.page || 0;

  const handleChange = (goToPage: number): void => {
    setSearchQuery({ ...searchQuery, page: goToPage - 1 });
  };

  return (
    <div className={styles.wrapper}>
      <Pagination
        className={styles.pagination}
        total={employeeResponse.totalElements}
        pageSize={employeeResponse.size}
        defaultPageSize={employeeResponse.size}
        current={activePage + 1}
        showQuickJumper={false}
        showSizeChanger={false}
        onChange={handleChange}
      />
    </div>
  );
};

export default EmployeePagination;
