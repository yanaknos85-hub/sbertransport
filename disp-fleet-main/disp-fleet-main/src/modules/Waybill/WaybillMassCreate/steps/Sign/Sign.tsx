import React, { FC, useState } from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import {
  Row,
  Col,
  Divider,
  PageHeader,
  Table
} from 'antd';

import moment from 'moment';
import { useTranslation } from 'i18n';

import { Button } from 'components/Button';
import { TMassCreateFirstTitleItem } from 'api/shifts/shifts.types';
import { Shift } from 'api/shifts/shifts.types';
import { useSendFirstTitle } from 'api/shifts/shifts.api';
import { ignore } from 'utils/utils';
import { RELEASE_ON_LINE_LINK } from 'constants/routes.constants';
import { StepsEnum } from '../steps.enum';

import CertificateModal from './modals/CertificateModal/CertificateModal';
import Results from './modals/Results/Results';
import { useColumns } from './hooks/useColumns';
import { useDataSource } from './hooks/useDataSource';
import styles from './Sign.module.scss';

interface SignResult {
  signature: string;
  item: TMassCreateFirstTitleItem;
}

export interface SignProps {
  setCurrentStep: React.Dispatch<React.SetStateAction<StepsEnum>>;
  firstTitleResult: TMassCreateFirstTitleItem[] | undefined;
  selectedShifts: Shift[];
}

const Sign: FC<SignProps> = ({
  setCurrentStep,
  firstTitleResult,
  selectedShifts,
}) => {
  const { createMass: i18 } = useTranslation().t.Waybill;
  const { columns } = useColumns();
  const { dataSource } = useDataSource({ firstTitleResult, selectedShifts });

  const history = useHistory();

  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isResultsVisible, setIsResultsVisible] = useState(false);
  const [sendFirstTitle, { isLoading: signing }] = useSendFirstTitle();

  const handleBack = () => {
    setCurrentStep(StepsEnum.ShiftsStep);
  };

  const handleSignClick = () => {
    setIsModalVisible(true);
  };

  const handleCloseModal = () => {
    setIsModalVisible(false);
  };

  const handleSign = (signatures: SignResult[]) => {
    if (firstTitleResult && firstTitleResult.length > 0) {
      sendFirstTitle(signatures.map(({ signature, item }) => ({
        id: item.shiftId,
        content: item.content,
        fileName: item.fileName,
        ewbUuid: item.ewbId,
        signature,
        creationTime: item.creationTime,
        humanReadableId: item.humanReadableId,
        timeZone: moment().format('Z'),
      }))).then(() => {
        setIsResultsVisible(true);
      }).catch(ignore);
    }
    setIsModalVisible(false);
  };

  const handleCloseResults = () => {
    setIsResultsVisible(false);
    history.push(RELEASE_ON_LINE_LINK);
  };

  return (
    <>
      <PageHeader
        onBack={handleBack}
        title={i18.signTitle}
        className={styles.pageHeader}
      />
      <Divider className={styles.divider} />

      <div className={styles.helperText}>
        {i18.signHelperText}
      </div>

      <Table
        columns={columns}
        dataSource={dataSource}
        rowKey="key"
        pagination={false}
        scroll={{ x: 2500 }}
        className={styles.table}
      />

      <Row
        justify="end"
        gutter={[12, 0]}
        className={styles.actions}
      >
        <Col>
          <Button
            type="text"
            onClick={handleBack}
            className={styles.backButton}
          >
            {i18.backButton}
          </Button>
        </Col>
        <Col>
          <Button
            type="primary"
            onClick={handleSignClick}
          >
            {i18.signButton}
          </Button>
        </Col>
      </Row>

      <CertificateModal
        visible={isModalVisible}
        firstTitleResult={firstTitleResult}
        onClose={handleCloseModal}
        onSign={handleSign}
        loading={signing}
      />

      {isResultsVisible && (
        <Results
          visible={isResultsVisible}
          firstTitleResult={firstTitleResult}
          selectedShifts={selectedShifts}
          onClose={handleCloseResults}
        />
      )}
    </>
  );
};

export default Sign;
