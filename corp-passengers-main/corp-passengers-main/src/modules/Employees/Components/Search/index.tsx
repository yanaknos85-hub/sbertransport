import ErrorBoundary from 'antd/lib/alert/ErrorBoundary';
import { useSearchEmployee, EmployeeSearchQuery } from 'api/employee/search';
import * as React from 'react';
import { UUID } from 'utils/io-ts';
import * as R from 'ramda';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useProfile } from 'api/profile';
import { Filters } from '../Filters';
import styles from './search.module.scss';

const SearchResults: React.FC<{ query: EmployeeSearchQuery; children: (employeeIds: UUID[]) => JSX.Element }> = ({
  query,
  children,
}) => {
  const { organizationId } = useProfile().data;
  const {
    data: { content: results },
    isLoading,
  // @ts-ignore
  } = useSearchEmployee(query, { page: query?.page || 0, size: 20 }, organizationId);
  return (
    <ErrorBoundary>
      <div>{isLoading ? <SpinWrapped /> : children(results.map(R.prop('id')))}</div>
    </ErrorBoundary>
  );
};

export type Fields = EmployeeSearchQuery;

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
    setSearchQuery(searchQuery ?? {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onApplyFilters = React.useCallback(
    query => {
      setSearchQuery({ ...query, ...(query.status ? { status: query.status } : { status: undefined }) });
    },
    [setSearchQuery]
  );

  return (
    <ErrorBoundary>
      <Filters onApplyFilters={onApplyFilters} />
      <div className={styles.searchResults}>
        <React.Suspense fallback={<SpinWrapped />}>
          {searchQuery ? <SearchResults query={searchQuery}>{children}</SearchResults> : null}
        </React.Suspense>
      </div>
    </ErrorBoundary>
  );
};
