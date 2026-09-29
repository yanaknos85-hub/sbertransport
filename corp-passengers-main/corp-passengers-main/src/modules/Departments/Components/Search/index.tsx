import ErrorBoundary from 'antd/lib/alert/ErrorBoundary';
import * as React from 'react';
import { UUID } from 'utils/io-ts';
import * as R from 'ramda';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useProfile } from 'api/profile';
import { DepartmentSearchQuery, useSearchDepartment } from 'api/departments/search';
import { Filters } from '../Filters';
import styles from './search.module.scss';

const SearchResults: React.FC<{
  query: DepartmentSearchQuery;
  children: (depIds: UUID[]) => JSX.Element;
}> = ({ query, children }) => {
  const { organizationId } = useProfile().data;
  const {
    data: { content: results },
    isLoading,
  } = useSearchDepartment({
    query,
    pagination: { page: query?.page || 0, size: 20 },
    projection: 'FULL',
    // @ts-ignore
    orgId: organizationId,
  });
  return (
    <ErrorBoundary>
      <div>{isLoading ? <SpinWrapped /> : children(results?.map(R.prop('id')))}</div>
    </ErrorBoundary>
  );
};

export type Fields = DepartmentSearchQuery;

export const Search = ({
  searchQuery,
  setSearchQuery,
  children,
}: {
  searchQuery: Fields | null;
  setSearchQuery: (query: Fields) => void;
  children: (employeeIds: UUID[]) => JSX.Element;
}): JSX.Element => {
  React.useEffect(() => {
    setSearchQuery(searchQuery || {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <ErrorBoundary>
      <Filters acceptFilters={setSearchQuery} />
      <div className={styles.searchResults}>
        <React.Suspense fallback={<SpinWrapped />}>
          {searchQuery ? <SearchResults query={searchQuery}>{children}</SearchResults> : null}
        </React.Suspense>
      </div>
    </ErrorBoundary>
  );
};
