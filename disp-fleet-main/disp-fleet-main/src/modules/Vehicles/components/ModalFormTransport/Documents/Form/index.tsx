import React from 'react';
import type { FC } from 'react';
import type { Moment } from 'moment';
import {
  Row, Col, Form, Input
} from 'antd';
import type { Store } from 'antd/lib/form/interface';
import { useTranslation } from 'i18n';

import { Button } from 'components/Button';
import { DATE_FORMAT } from 'constants/app.constants';
import { SelectVehicleType } from 'components/SelectVehicleType/SelectVehicleType';
import DatePicker from 'components/DatePicker/DatePicker';
import VINField from 'modules/Vehicles/components/VINField';
import { FinalRegNumberMask, normalizeRegCarNumber } from 'utils/regCarNumber';

import styles from './styles.module.scss';

export interface IFormData {
  stateNumber: string;
  inventoryNumber?: string;
  assetNumber: string;
  vinCode: string;
  currentMileage: number;
  bodyNumber?: string;
  chassisNumber?: string;
  passportNumber: string;
  passportIssuedDate: Moment;
  brandByPassport: string;
  modelByPassport: string;
  certificateNumber: string;
  certificateIssuedDate: Moment;
  vehicleType: string;
}

interface IProps {
  initialValues: IFormData | undefined;
  onFinish: (data: IFormData) => void;
}

const DocumentsForm: FC<IProps> = ({ initialValues, onFinish }) => {
  const t = useTranslation().t.Transport;
  const [form] = Form.useForm();

  return (
    <Form
      form={form}
      className={styles.form}
      initialValues={initialValues as Store}
      onFinish={onFinish}
    >
      <div className={styles.header}>
        {t.directory.titles.general}
      </div>

      <Row gutter={16}>
        <Col>
          <Form.Item
            name="stateNumber"
            label={t.directory.commonFields.stateNumber}
            validateTrigger={['onChange', 'onBlur']}
            rules={[
              { required: true },
              {
                pattern: FinalRegNumberMask,
                message: t.directory.rules.stateNumber,
                validateTrigger: ['onBlur'],
              },
            ]}
            normalize={normalizeRegCarNumber}
            className={styles.formItem}
          >
            <Input
              allowClear
              placeholder={t.directory.commonFields.numberPlaceholder}
              className={styles.input}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="inventoryNumber"
            label={t.directory.commonFields.inventoryNumber}
            rules={[
              { required: true },
              {
                max: 50,
                message: t.directory.rules.inventoryNumber,
              },
            ]}
            normalize={value => value.toUpperCase()}
            className={styles.formItem}
          >
            <Input
              allowClear
              placeholder={t.directory.commonFields.numberPlaceholder}
              className={styles.input}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="assetNumber"
            label={t.directory.commonFields.assetNumber}
            rules={[
              { required: true },
              {
                max: 50,
                message: t.directory.rules.assetNumber,
              },
            ]}
            normalize={value => value.toUpperCase()}
            className={styles.formItem}
          >
            <Input
              allowClear
              placeholder={t.directory.commonFields.numberPlaceholder}
              className={styles.input}
            />
          </Form.Item>
        </Col>

        <Col>
          <VINField
            form={form}
            className={styles.formItem}
            inputProps={{
              className: styles.input,
              placeholder: t.directory.commonFields.numberPlaceholder,
            }}
          />
        </Col>

        <Col>
          <Form.Item
            name="currentMileage"
            label={t.directory.commonFields.currentMileage}
            rules={[
              { required: true },
              {
                type: 'number',
                min: 1,
                message: t.directory.rules.currentMileageRequired,
              },
            ]}
            normalize={(value, prevValue) => value === '' ? value : /^\d*$/.test(value) ? Number(value) : prevValue ?? ''}
            className={styles.formItem}
          >
            <Input
              placeholder="Укажите пробег"
              maxLength={6}
              className={styles.input}
            />
          </Form.Item>
        </Col>
      </Row>

      <div className={styles.header}>
        {t.directory.titles.bodywork}
      </div>

      <Row gutter={16}>
        <Col>
          <Form.Item
            name="bodyNumber"
            label={t.directory.commonFields.bodyNumber}
            className={styles.formItem}
            rules={[
              {
                max: 17,
                message: t.directory.rules.bodyNumber,
              },
            ]}
          >
            <Input
              allowClear
              placeholder={t.directory.commonFields.numberPlaceholder}
              className={styles.input}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="chassisNumber"
            label={t.directory.commonFields.chassisNumber}
            className={styles.formItem}
            rules={[
              {
                max: 17,
                message: t.directory.rules.chassisNumber,
              },
            ]}
          >
            <Input
              allowClear
              placeholder={t.directory.commonFields.numberPlaceholder}
              className={styles.input}
            />
          </Form.Item>
        </Col>
      </Row>

      <div className={styles.header}>
        {t.directory.titles.vehiclePassport}
      </div>

      <Row gutter={16}>
        <Col>
          <Form.Item
            name="passportNumber"
            label={t.directory.commonFields.passportNumber}
            className={styles.formItem}
            rules={[
              { required: true },
              {
                max: 15,
                message: t.directory.rules.passportNumber,
              },
            ]}
          >
            <Input
              allowClear
              placeholder={t.directory.commonFields.numberPlaceholder}
              className={styles.input}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="passportIssuedDate"
            label={t.directory.commonFields.passportIssuedDate}
            rules={[{ required: true }]}
            className={styles.formItem}
          >
            <DatePicker format={DATE_FORMAT.BASE_REVERTED_DOTS} style={{ width: '100%' }} />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="brandByPassport"
            label={t.directory.commonFields.brandByPassport}
            className={styles.formItem}
            rules={[
              { required: true },
              {
                max: 150,
                message: t.directory.rules.brandByPassport,
              },
            ]}
          >
            <Input
              allowClear
              placeholder="Укажите марку"
              className={styles.input}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="modelByPassport"
            label={t.directory.commonFields.modelByPassport}
            className={styles.formItem}
            rules={[
              { required: true },
              {
                max: 150,
                message: t.directory.rules.modelByPassport,
              },
            ]}
          >
            <Input
              allowClear
              placeholder="Укажите модель"
              className={styles.input}
            />
          </Form.Item>
        </Col>
      </Row>

      <div className={styles.header}>
        {t.directory.titles.registryCertificate}
      </div>

      <Row gutter={16} className={styles.certificateRow}>
        <Col>
          <Form.Item
            name="certificateNumber"
            label={t.directory.commonFields.certificateNumber}
            className={styles.formItem}
            rules={[
              { required: true },
              {
                max: 15,
                message: t.directory.rules.certificateNumber,
              },
            ]}
          >
            <Input
              allowClear
              placeholder="Укажите серию и номер"
              className={styles.input}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="certificateIssuedDate"
            label={t.directory.commonFields.certificateIssuedDate}
            className={styles.formItem}
            rules={[{ required: true }]}
          >
            <DatePicker format={DATE_FORMAT.BASE_REVERTED_DOTS} style={{ width: '100%' }} />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="vehicleType"
            label={t.directory.commonFields.vehicleType}
            className={styles.formItem}
            rules={[{ required: true }]}
          >
            <SelectVehicleType
              showDivider
              className={styles.select}
              placeholder={t.directory.commonFields.vehicleType}
            />
          </Form.Item>
        </Col>
      </Row>

      <Button
        type="primary"
        htmlType="submit"
        className={styles.button}
      >
        Далее
      </Button>
    </Form>
  );
};

export default DocumentsForm;
