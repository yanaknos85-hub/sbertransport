import { Button, Form, Input } from 'antd';
import ErrorBoundary from 'antd/lib/alert/ErrorBoundary';
import { useForm } from 'antd/lib/form/Form';
import * as React from 'react';
import { UUID } from 'utils/io-ts';
import * as R from 'ramda';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { SearchOutlined } from '@ant-design/icons';
import { OrganizationSearchQueryWithPagination } from 'api/organizations/search';
import { OrganizationResponse } from 'stores/Organizations/Organizations.interface';

import styles from './search.module.scss';

const SearchResults: React.FC<{
  children: (orgIds: UUID[]) => JSX.Element;
  isFetching: boolean;
  organizations: OrganizationResponse['content'];
}> = ({
  children, isFetching, organizations,
}) => (
  <ErrorBoundary>
    <div>{isFetching ? <SpinWrapped /> : children(organizations.map(R.prop('id')))}</div>
  </ErrorBoundary>
);

export type Fields = OrganizationSearchQueryWithPagination;
const Field: React.ComponentType<React.ComponentProps<typeof Form.Item> & { name: keyof Fields }> = Form.Item;

export const Search = ({
  searchQuery,
  setSearchQuery,
  children,
  isFetching,
  organizations,
}: {
  searchQuery: Fields;
  setSearchQuery: React.Dispatch<React.SetStateAction<Fields>>;
  children: (employeeIds: UUID[]) => JSX.Element;
  isFetching: boolean;
  organizations: OrganizationResponse['content'];
}): JSX.Element => {
  const [form] = useForm<Fields>();
  const handleFinish = (values: Fields) => setSearchQuery(query => ({ ...query, ...values }));

  const handleReset = () => {
    setSearchQuery(({ page, size }) => ({ page, size }));
    form.resetFields();
  };

  return (
    <ErrorBoundary>
      <div className={styles.searchPanel}>
        <Form form={form} onFinish={handleFinish}>
          <Field name="officialName" label="Наименование организации">
            <Input allowClear placeholder="Например, Ситимобил" />
          </Field>

          <Field name="address" label="Адрес">
            <Input allowClear placeholder="Уникальное значение" />
          </Field>

          <Field name="tid" label="ИНН">
            <Input allowClear placeholder="Уникальное значение" />
          </Field>

          <Field name="msrn" label="ОГРН">
            <Input allowClear placeholder="Уникальное значение" />
          </Field>

          <div className={styles.buttonBar}>
            <Button
              onClick={form.submit}
              type="primary"
              icon={<SearchOutlined />}
            >
              Поиск
            </Button>
            <Button onClick={handleReset}>Сбросить фильтры</Button>
          </div>
        </Form>
      </div>
      <div className={styles.searchResults}>
        <React.Suspense fallback={<SpinWrapped />}>
          {searchQuery ? (
            <SearchResults organizations={organizations} isFetching={isFetching}>
              {children}
            </SearchResults>
          ) : null}
        </React.Suspense>
      </div>
    </ErrorBoundary>
  );
};
