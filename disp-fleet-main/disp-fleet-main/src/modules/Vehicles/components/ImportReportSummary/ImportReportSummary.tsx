import React, {
  Dispatch, FC, SetStateAction, useEffect, useState
} from 'react';
import {
  Col, Statistic, Row, Card
} from 'antd';
import { CloseCircleOutlined, WarningOutlined } from '@ant-design/icons';

import { useImportReport } from 'api/upload/upload.api';
import { ImportReportSinglePage } from 'api/upload/upload.types';
import { useAPIQueryCache } from 'api';

interface IImportReportSummary {
  setBusy?: Dispatch<SetStateAction<boolean>>;
}

const REFETCH_INTERVAL = 1000;

export const ImportReportSummary: FC<IImportReportSummary> = ({ setBusy }): JSX.Element => {
  const [refetchInterval, setRefetchInterval] = useState<number | false>(false);
  const [errorCounter, setErrorCounter] = useState(0);
  const [warningCounter] = useState(0);

  const { data: reports, isFetching } = useImportReport('vehicles', { refetchInterval, suspense: false });

  const isLoading = reports ? reports.some(report => report.finished === false) : false;

  const cache = useAPIQueryCache();

  useEffect(() => {
    if (reports && reports.some(report => report.finished)) {
      cache.refetchQueries(['allVehiclesSearch']);
    }
  }, [cache, reports]);

  useEffect(() => {
    if (!reports) return;

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

  useEffect(() => {
    if (setBusy && !isLoading) {
      setBusy(isFetching);
    }
  }, [isFetching, setBusy]);

  return (
    <>
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
