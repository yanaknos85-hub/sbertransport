import * as React from 'react';
import { LimitsUploadReport } from 'api/limits';
import { Table, Input } from 'antd';
import styled from 'styled-components';

const columns = [
  { dataIndex: 'title', render: (value: string) => value },
  { dataIndex: 'value', render: (value: number) => value.toString() },
];

const Report = styled.div`
  min-width: 400px;

  table {
    .ant-table-thead {
      display: none;
    }
  }
`;

const Header = styled.h1`
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;

  &:not(first-child) {
    margin-top: 16px;
  }
`;

const Errors: React.FC<{ errors: string[] }> = ({ errors }) => (
  <>
    <Header>Ошибки импорта:</Header>
    <Input.TextArea autoSize={{ minRows: 6, maxRows: 10 }} value={errors.join('\n')} />
  </>
);

export const UploadReport: React.FC<{ uploadResult: LimitsUploadReport }> = ({
  uploadResult: {
    deleted, errorDescrs, errors, inserted, resultStatus, total, unchanged, updated,
  },
}) => {
  const result = [
    { title: 'Обработано записей', value: total },
    { title: 'Добавлено записей', value: inserted },
    { title: 'Удалено записей', value: deleted },
    { title: 'Обновлено записей', value: updated },
    { title: 'Записей без изменений', value: unchanged },
    { title: 'Записей с ошибками', value: errors },
  ];

  return (
    <Report>
      <Header>{resultStatus === 'SUCCESS' ? 'Импорт завершен успешно' : 'Импорт завершен с ошибками'}</Header>
      <Table
        dataSource={result}
        columns={columns}
        pagination={false}
      />
      {errorDescrs.length ? <Errors errors={errorDescrs} /> : null}
    </Report>
  );
};
