import React, { FC } from 'react';
import { useParams } from 'react-router-dom';

import { Col, Divider, Row } from 'antd';
import { useTranslation } from 'i18n';

import { useWaybill } from 'api/waybill/waybill.api';
import { TelemechType } from 'modules/Waybill/Waybill.constants';
import Panel from 'components/Panel/Panel';

import DetailedHeader from './components/Header/Header';
import DriverInfo from './components/DriverInfo/DriverInfo';
import MedicInfo from './components/MedicInfo/MedicInfo';
import OrganizationInfo from './components/OrganizationInfo/OrganizationInfo';
import DetailedResponsible from './components/Responsible/Responsible';
import TelemechanicInfo from './components/TelemechanicInfo/TelemechanicInfo';
import TransportInfo from './components/TransportInfo/TransportInfo';

import styles from './WaybillDetailed.module.scss';

const WaybillDetailed: FC = () => {
  const { id } = useParams<{ id: string }>();
  const { detailed: i18 } = useTranslation().t.Waybill;

  const waybill = useWaybill(id).data;

  const {
    humanReadableId,
    creationDate,
    startDate,
    finishDate,
    ewbUuid,
    regionCode,
    msrn,
    tin,
    status,
    organizationName,
    telemechIn,
    telemechOut,
    medic,
    transport,
    author,
    drivingLicense,
    driver,
  } = waybill;
  const hasInspections = !!medic || !!telemechIn || !!telemechOut;

  return (
    <div className={styles.container}>
      <DetailedHeader
        waybill={waybill}
        humanReadableId={humanReadableId}
        creationDate={creationDate}
        startDate={startDate}
        finishDate={finishDate}
        ewbUuid={ewbUuid}
        status={status}
      />

      <Panel className={styles.panel}>
        <span className={styles.panel__header}>{i18.common.title}</span>

        <Divider className={styles.panel__divider} />

        <Row gutter={[19, 16]}>
          <Col
            xs={24}
            xl={12}
            xxl={8}
          >
            <OrganizationInfo
              organizationName={organizationName}
              regionCode={regionCode}
              msrn={msrn}
              tin={tin}
            />
          </Col>

          <Col
            xs={24}
            xl={12}
            xxl={8}
          >
            <TransportInfo
              brand={transport.brand}
              model={transport.model}
              stateNumber={transport.stateNumber}
              type={transport.transportType}
            />
          </Col>

          <Col
            xs={24}
            xl={12}
            xxl={8}
          >
            <DriverInfo
              firstName={driver.firstName}
              lastName={driver.lastName}
              patronymic={driver.patronymic}
              personnelNumber={driver.personnelNumber}
              mobilePhone={driver.mobilePhone}
              series={drivingLicense.series}
              number={drivingLicense.number}
              issueDate={drivingLicense.issueDate}
            />
          </Col>
        </Row>
      </Panel>

      <DetailedResponsible author={author} />

      {hasInspections && (
        <Panel className={styles.panel}>
          <span className={styles.panel__header}>{i18.inspections.title}</span>

          <Divider className={styles.panel__divider} />

          <Row gutter={[24, 16]}>
            <Col span={12}>
              {!!medic && (
                <MedicInfo
                  firstName={medic.firstName}
                  lastName={medic.lastName}
                  patronymic={medic.patronymic}
                  position={medic.position}
                  decisionTime={medic.decisionTime}
                  success={medic.medicSuccess}
                />
              )}
            </Col>

            <Col span={12}>
              {!!telemechOut && (
                <TelemechanicInfo
                  type={TelemechType.OUT}
                  firstName={telemechOut.firstName}
                  lastName={telemechOut.lastName}
                  patronymic={telemechOut.patronymic}
                  position={telemechOut.position}
                  decisionTime={telemechOut.decisionTime}
                  success={telemechOut.telemechSuccess}
                  mileage={telemechOut.mileage}
                />
              )}

            </Col>

            <Col span={12}>
              {!!telemechIn && (
                <TelemechanicInfo
                  type={TelemechType.IN}
                  firstName={telemechIn.firstName}
                  lastName={telemechIn.lastName}
                  patronymic={telemechIn.patronymic}
                  position={telemechIn.position}
                  decisionTime={telemechIn.decisionTime}
                  success={telemechIn.telemechSuccess}
                  mileage={telemechIn.mileage}
                />
              )}
            </Col>
          </Row>
        </Panel>
      )}
    </div>
  );
};

export default WaybillDetailed;
