import React from 'react';
import { Pagination } from 'antd';
import { OrganizationResponse } from 'stores/Organizations/Organizations.interface';
import { Fields } from '../Search';
import styles from './pagination.module.scss';

const OrganizationPagination: React.FC<{
  organizationResponse: OrganizationResponse;
  setSearchQuery: React.Dispatch<React.SetStateAction<Fields>>;
  isFetching: boolean;
}> = ({
  organizationResponse, setSearchQuery, isFetching,
}) => {
  const handleChange = (goToPage: number): void => {
    setSearchQuery((query: Fields) => ({ ...query, page: goToPage - 1 }));
  };

  return (
    <div className={styles.wrapper}>
      <Pagination
        className={styles.pagination}
        total={organizationResponse.totalElements}
        pageSize={organizationResponse.size}
        defaultPageSize={organizationResponse.size}
        showQuickJumper={false}
        showSizeChanger={false}
        disabled={isFetching}
        onChange={e => handleChange(e)}
      />
    </div>
  );
};

export default OrganizationPagination;
