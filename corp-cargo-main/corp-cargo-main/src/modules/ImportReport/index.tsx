import React, {
  Dispatch,
  FC,
  SetStateAction,
  useEffect,
  useMemo,
  useState
} from 'react';
import { useRouteMatch } from 'react-router-dom';
import { Table, Button, Descriptions, Col, Statistic, Row, Card } from 'antd';
import { ToolbarElement, ToolbarProvider } from 'components/Toolbar';
import Panel from 'components/Panel';
import {
  ImportReportRow, ImportReport, useImportReport, ImportReportSinglePage
} from 'api/upload';
import styled from 'styled-components';
import { CloseCircleOutlined, WarningOutlined } from '@ant-design/icons';
import uuid from 'utils/uuid';
import { DefaultValues, integrationExceptionTitles, IntegrationExceptionTypes } from 'constants/constants.app';
import { importExportEndpointMap } from '../UploadButton';

const Exception = styled.span`
  font-family: monospace;
`;

const columns = [
  {
    title: 'Номер строки',
    dataIndex: 'rowNumber',
    width: '22%',
  },
  {
    title: 'Ошибки формата',
    dataIndex: 'violations',
    render: (errors: any[]) => errors.length
      ? errors.map(error => Object.keys(IntegrationExceptionTypes).some(e => e === error.type) ? (
        <Exception key={error}>
          {error.field && (
            <span>
              {error.field}
              :
              {' '}
            </span>
          )}
          {integrationExceptionTitles[error.type as IntegrationExceptionTypes]}
        </Exception>
      ) : (
        <pre key={error}>{JSON.stringify(error, null, 2)}</pre>
      )
      )
      : DefaultValues.emptyValueInTable,
    width: '39%',
  },
  {
    title: 'Ошибки обработки',
    dataIndex: 'exceptions',
    render: (value: string[]) => value.length ? value.map(err => <Exception key={err}>{err}</Exception>) : DefaultValues.emptyValueInTable,
    width: '39%',
  },
];

const Rows: FC<{ rows: ImportReportRow[] }> = ({ rows }) => {
  const tableRows = useMemo(
    () => rows.map(row => ({
      ...row,
      violations: row.violations.map(v => ({
        ...v,
        field: row.field,
      })),
    })),
    [rows]
  );

  return (
    <Table
      dataSource={tableRows}
      columns={columns}
      rowKey={row => row.rowNumber}
    />
  );
};

const Report: FC<{ report: ImportReport }> = ({ report }) => {
  const pages: ImportReportSinglePage[] = 'pages' in report ? report.pages : [report];

  return (
    <>
      {pages.map(page => (
        <Panel key={`${page.name}`}>
          <Descriptions>
            {page.name && <Descriptions.Item label="Страница">{page.name}</Descriptions.Item>}
            <Descriptions.Item label="Импорт завершен">{page.finished ? 'да' : 'нет'}</Descriptions.Item>
            <Descriptions.Item label="Ошибки">
              {page.exceptions.length ? page.exceptions.map(e => <Exception key={e}>{e}</Exception>) : 0}
            </Descriptions.Item>
          </Descriptions>
          <Rows rows={page.row} />
        </Panel>
      ))}
    </>
  );
};

export const Reports: FC<{ entity: keyof typeof importExportEndpointMap }> = ({ entity }) => {
  const { data: reports } = useImportReport(entity);

  return (
    <>
      {reports.map(report => (
        <Report key={uuid()} report={report} />
      ))}
    </>
  );
};

const ImportReportPage: FC = () => {
  const {
    params: { entity },
  } = useRouteMatch<{ entity: keyof typeof importExportEndpointMap }>();

  const { refetch } = useImportReport(entity);

  return (
    <ToolbarProvider>
      <div>
        <ToolbarElement>
          <Button onClick={refetch as () => void}>Обновить</Button>
        </ToolbarElement>

        <Reports entity={entity as keyof typeof importExportEndpointMap} />
      </div>
    </ToolbarProvider>
  );
};

interface IImportReportSummary {
  entity: keyof typeof importExportEndpointMap;
  setBusy?: Dispatch<SetStateAction<boolean>>;
}

export const ImportReportSummary: FC<IImportReportSummary> = ({ entity, setBusy }): JSX.Element => {
  const [refetchInterval, setRefetchInterval] = useState<number | false>(false);
  const [errorCounter, setErrorCounter] = useState(0);
  const [warningCounter] = useState(0); // на данный момент в импортах предупреждений нет, только ошибки
  const { data: reports } = useImportReport(entity, { refetchInterval, suspense: false });

  const REFETCH_INTERVAL = 1000;

  useEffect(() => {
    if (!reports) {
      return;
    }

    const isLoading = reports.some(report => report.finished === false);

    setRefetchInterval(isLoading ? REFETCH_INTERVAL : false);
    if (setBusy) {
      setBusy(isLoading);
    }

    let errors = 0;

    reports.forEach(report => {
      const pages: ImportReportSinglePage[] = 'pages' in report ? report.pages : [report];

      pages.forEach(page => {
        errors += page.exceptions.length;

        page.row.forEach(row => {
          errors += row.exceptions.length + row.violations.length;
        });
      });
    });

    setErrorCounter(errors);
  }, [reports, setBusy]);

  return (
    <>
      {' '}
      <Row gutter={16}>
        <Col span={12}>
          <Card key="errors-employee-import">
            <Statistic
              title="Ошибки"
              value={errorCounter}
              valueStyle={{ color: '#cf1322' }}
              prefix={<CloseCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card key="warnings-employee-import">
            <Statistic
              title="Предупреждения"
              value={warningCounter}
              valueStyle={{ color: '#08c' }}
              prefix={<WarningOutlined />}
            />
          </Card>
        </Col>
      </Row>
    </>
  );
};

export default ImportReportPage;
