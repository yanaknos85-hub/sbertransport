import React, { FC } from 'react';
import { Descriptions, Table, Modal } from 'antd';
import { useTranslation } from 'i18n';

import { useImportReport } from 'api/upload/upload.api';
import { ImportReport, ImportReportRow, ImportReportSinglePage } from 'api/upload/upload.types';

import { Icon } from 'components/Icon/Icon';
import { Button } from 'components/Button';
import Panel from 'components/Panel/Panel';
import uuid from 'utils/uuid';
import { useModalForm } from 'modules/Vehicles/context/ModalForm';

import styles from './ResultModal.module.scss';

export enum IntegrationExceptionTypes {
  NotBlank = 'NotBlank',
}

export type IntegrationExcetionTitles = {
  [key in IntegrationExceptionTypes]: string;
};

export const integrationExceptionTitles: IntegrationExcetionTitles = {
  [IntegrationExceptionTypes.NotBlank]: 'Поле не должно быть пустым',
};

const columns = [
  {
    title: 'Номер строки',
    dataIndex: 'rowNumber',
    width: '22%',
  },
  {
    title: 'Ошибки формата',
    dataIndex: 'violations',
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    render: (errors: any[]) => errors.length
      ? errors.map(error => Object.keys(IntegrationExceptionTypes).some(e => e === error.type) ? (
        <span key={error}>
          {error.field && (
            <span>
              {error.field}
              :
              {' '}
            </span>
          )}
          {integrationExceptionTitles[error.type as IntegrationExceptionTypes]}
        </span>
      ) : (
        <pre key={error}>{JSON.stringify(error, null, 2)}</pre>
      )
      )
      : '-',
    width: '39%',
  },
  {
    title: 'Ошибки обработки',
    dataIndex: 'exceptions',
    render: (value: string[]) => (value.length ? value.map(err => <span key={err}>{err}</span>) : '-'),
    width: '39%',
  },
];

const Rows: React.FC<{ rows: ImportReportRow[] }> = ({ rows }) => {
  const tableRows = React.useMemo(
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

const Report: React.FC<{ report: ImportReport }> = ({ report }) => {
  const pages: ImportReportSinglePage[] = 'pages' in report ? report.pages : [report];

  return (
    <>
      {pages.map(page => (
        <Panel key={`${page.name}`}>
          <Descriptions>
            {page.name && <Descriptions.Item label="Страница">{page.name}</Descriptions.Item>}
            <Descriptions.Item label="Импорт завершен">{page.finished ? 'да' : 'нет'}</Descriptions.Item>
            <Descriptions.Item label="Ошибки">
              {page.exceptions.length ? page.exceptions.map(e => <span key={e}>{e}</span>) : 0}
            </Descriptions.Item>
          </Descriptions>
          <Rows rows={page.row} />
        </Panel>
      ))}
    </>
  );
};

export const ResultModal: FC = () => {
  const { t } = useTranslation();
  const { stateShowModal, handleClose } = useModalForm();

  const isVisible = stateShowModal.isOpen && stateShowModal.type === 'result';

  const { data: reports, refetch } = useImportReport('vehicles', {
    enabled: isVisible,
  });

  return (
    <Modal
      centered
      title={t.global.importResult}
      className={styles.modal}
      visible={isVisible}
      closeIcon={<Icon type="closeModal" className={styles.ModalIcon} />}
      onCancel={handleClose}
      footer={null}
    >
      <div className={styles.content}>
        <Button className={styles.refetchBtn} onClick={refetch as () => void}>Обновить</Button>

        {reports?.map(report => (
          <Report key={uuid()} report={report} />
        ))}
      </div>
    </Modal>
  );
};
