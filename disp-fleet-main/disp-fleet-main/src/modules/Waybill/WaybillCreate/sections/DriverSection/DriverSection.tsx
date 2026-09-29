import React, { useEffect, useState } from 'react';
import type { FC } from 'react';
import {
  Col, DatePicker, Divider, Row
} from 'antd';
import moment from 'moment';
import { FormInstance, Input } from '@sber-sbertransport/ui-kit/src';

import { useTranslation } from 'i18n';
import type { UUID } from 'utils/io-ts';
import { TDriverContent } from 'api/telemechanicDriver/telemechanicDriver.types';
import { DATE_FORMAT } from 'constants/app.constants';
import Panel from 'components/Panel/Panel';
import { FormItem } from 'components/FormItem';
import { DriverField } from '../../../Waybill.constants';
import DriversSelect from './DriverSelect';
import styles from './DriverSection.module.scss';

interface Props {
  disabled: boolean;
  transportId: UUID | null;
  form: FormInstance;
}

const DriverSection: FC<Props> = ({
  disabled,
  transportId,
  form,
}) => {
  const { driver: i18 } = useTranslation().t.Waybill.create;
  const [driverInfo, setDriverInfo] = useState<TDriverContent | null>(null);

  useEffect(() => {
    if (driverInfo) {
      form.setFieldsValue({
        [DriverField.driverId]: driverInfo.driver.id,
        [DriverField.personnelNumber]: driverInfo.driver.personnelNumber,
        [DriverField.tin]: driverInfo.driver.tin,
        [DriverField.organizationName]: driverInfo.driver.organizationName,
        [DriverField.departmentName]: driverInfo.driver.departmentName,
        [DriverField.departmentId]: driverInfo.driver.departmentId,
        [DriverField.drivingLicenseSeries]: driverInfo.drivingLicense.series,
        [DriverField.drivingLicenseNumber]: driverInfo.drivingLicense.number,
        [DriverField.drivingLicenseIssueDate]: moment(driverInfo.drivingLicense.issueDate),
      });
    } else {
      form.resetFields([
        [DriverField.driverId],
        [DriverField.personnelNumber],
        [DriverField.tin],
        [DriverField.organizationName],
        [DriverField.departmentName],
        [DriverField.departmentId],
        [DriverField.drivingLicenseSeries],
        [DriverField.drivingLicenseNumber],
        [DriverField.drivingLicenseIssueDate],
      ]);
    }
  }, [driverInfo, form]);

  return (
    <Panel className={styles.container}>
      <span className={styles.container__title}>{i18.title}</span>

      <Divider className={styles.container__divider} />

      <Row gutter={[32, 16]}>
        <Col span={12}>
          <FormItem name={DriverField.fullName} label={i18.fullName}>
            <DriversSelect
              disabled={disabled}
              transportId={transportId ?? undefined}
              onChange={(value, option) => setDriverInfo(option as TDriverContent)}
            />
          </FormItem>
        </Col>

        <Col span={6}>
          <FormItem name={DriverField.personnelNumber} label={i18.personnelNumber}>
            <Input disabled className={styles.field} />
          </FormItem>
        </Col>

        <Col span={6}>
          <FormItem name={DriverField.tin} label={i18.tin}>
            <Input disabled className={styles.field} />
          </FormItem>
        </Col>

        <Col span={6}>
          <FormItem name={DriverField.organizationName} label={i18.organizationName}>
            <Input disabled className={styles.field} />
          </FormItem>
        </Col>

        <Col span={6}>
          <FormItem name={DriverField.departmentName} label={i18.departmentName}>
            <Input disabled className={styles.field} />
          </FormItem>
        </Col>

        <Col span={3}>
          <FormItem name={DriverField.drivingLicenseSeries} label={i18.series}>
            <Input disabled className={styles.field} />
          </FormItem>
        </Col>

        <Col span={3}>
          <FormItem name={DriverField.drivingLicenseNumber} label={i18.number}>
            <Input disabled className={styles.field} />
          </FormItem>
        </Col>

        <Col span={6}>
          <FormItem name={DriverField.drivingLicenseIssueDate} label={i18.issueDate}>
            <DatePicker
              disabled
              className={styles.field}
              format={DATE_FORMAT.BASE_REVERTED_DOTS}
              size="large"
              placeholder="" // Убираем дефолтный плейсхолдер
            />
          </FormItem>
        </Col>

        <FormItem name={DriverField.driverId} noStyle />
        <FormItem name={DriverField.departmentId} noStyle />
      </Row>
    </Panel>
  );
};

export default DriverSection;
