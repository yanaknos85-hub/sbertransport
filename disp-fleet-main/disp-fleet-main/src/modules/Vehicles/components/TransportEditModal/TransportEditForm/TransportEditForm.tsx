import React, { useState } from 'react';
import type { FC } from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import moment from 'moment';
import type { Moment } from 'moment';
import {
  Row, Col, Form, Input, Spin
} from 'antd';
import type { Store } from 'antd/lib/form/interface';
import classNames from 'classnames';
import { useTranslation } from 'i18n';

import { DATE_FORMAT } from 'constants/app.constants';
import AutoparkSelect from 'components/AutoparkSelect/AutoparkSelect';
import BranchSelect from 'components/BranchSelect/BranchSelect';
import { SelectVehicleType } from 'components/SelectVehicleType/SelectVehicleType';
import DatePicker from 'components/DatePicker/DatePicker';
import { useTelematicsSelectOptions } from 'modules/Vehicles/hooks/useTelematicsSelectOptions';
import { useUsingAccessiblePositionIdSelectOptions } from 'modules/Vehicles/hooks/useUsingAccessiblePositionIdSelectOptions';
import { useUsingTypeSelectOptions } from 'modules/Vehicles/hooks/useUsingTypeSelectOptions';
import { useUsingSubTypeSelectOptions } from 'modules/Vehicles/hooks/useUsingSubTypeSelectOptions';
import { useRoleMap } from 'hooks/useRoleMap';
import { FinalRegNumberMask, normalizeRegCarNumber } from 'utils/regCarNumber';
import { handleInputOnlyNumbers } from 'utils/inputOnlyNumbers';
import { UUID } from 'utils/io-ts';

import VINField from '../../VINField';
import styles from './TransportEditForm.module.scss';

export interface IFormData {
  contractorId: UUID;
  autoparkId: UUID;
  locationAddress: string;
  parkingAddress: string;
  comment: string;
  accessiblePositionId: string;
  exploitationStart: Moment;
  stateNumber: string;
  inventoryNumber?: string;
  assetNumber: string;
  vinCode: string;
  telematicsId?: string;
  currentMileage: number;
  bodyNumber?: string;
  chassisNumber?: string;
  certificateNumber: string;
  certificateIssuedDate: Moment;
  vehicleType: string;
  balanceUnitNumber: string;
  facility: string;
  equipmentUnitSystemNumber: string;
  type: string;
  subtype: string;
}

interface TransportEditFormProps {
  initialValues: IFormData;
  onFinish: (data: IFormData) => void;
}

export const FORM_ID = 'edit-transport';

const TransportEditForm: FC<TransportEditFormProps> = ({ initialValues, onFinish }) => {
  const t = useTranslation().t.Transport;
  const [form] = Form.useForm();

  const { isAdmin, isManager } = useRoleMap();

  const {
    options: telematicsOptions,
    isLoading: isTelematicsLoading,
    onSearch: onTelematicsSearch,
  } = useTelematicsSelectOptions();

  const { options: usingAccessiblePositionIdOptions, isLoading: isUsingAccessiblePositionIdLoading }
    = useUsingAccessiblePositionIdSelectOptions();

  const { options: usingTypeOptions, isLoading: isUsingTypeLoading } = useUsingTypeSelectOptions();
  const [typeId, setTypeId] = useState<string>();
  const { options: usingSubTypeOptions, isLoading: isUsingSubTypeLoading } = useUsingSubTypeSelectOptions(typeId);

  const handleSelectTypeId = value => {
    setTypeId(value);
    form.setFieldsValue({ subtype: undefined });
  };

  const handleClearTypeId = () => {
    setTypeId(undefined);
    form.resetFields(['subtype']);
  };

  const onValuesChange = (changedValues: IFormData) => {
    if ('contractorId' in changedValues) {
      form.setFieldsValue({
        autoparkId: undefined,
        inventoryNumber: undefined,
        assetNumber: undefined,
        exploitationStart: moment(),
      });
    }
  };

  return (
    <Form
      id={FORM_ID}
      form={form}
      className={styles.form}
      initialValues={initialValues as Store}
      onValuesChange={onValuesChange}
      onFinish={onFinish}
    >
      <div className={styles.block}>
        <div className={classNames(styles.header, styles.header_big)}>
          {t.directory.titles.general}
        </div>

        <div>
          <div className={styles.header}>
            {t.directory.titles.location}
          </div>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="contractorId"
                label={t.directory.commonFields.contractorId}
                className={styles.formItem}
                rules={[{ required: true }]}
              >
                <AutoparkSelect
                  showSearch
                  allowClear
                  size="middle"
                  disabled={!isAdmin}
                />
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item dependencies={['contractorId']} noStyle>
                {({ getFieldValue }) => {
                  const contractorId = getFieldValue('contractorId');

                  return (
                    <Form.Item
                      name="autoparkId"
                      label={t.directory.commonFields.autoparkId}
                      className={styles.formItem}
                      rules={[{ required: true }]}
                    >
                      <BranchSelect
                        autoparkId={contractorId}
                        disabled={!contractorId || !(isAdmin || isManager)}
                        placeholder={`Выберите ${contractorId ? 'филиал' : 'автопарк'}`}
                        size="middle"
                        showSearch
                        allowClear
                      />
                    </Form.Item>
                  );
                }}
              </Form.Item>
            </Col>

            <Col span={8}>
              <Form.Item
                name="balanceUnitNumber"
                label={t.directory.commonFields.balanceUnitNumber}
                className={styles.formItem}
                rules={[{ required: true, max: 4 }]}
              >
                <Input
                  maxLength={4}
                  className={styles.input}
                  placeholder={t.directory.commonFields.numberPlaceholder}
                  onInput={handleInputOnlyNumbers}
                />
              </Form.Item>
            </Col>

            <Col span={8}>
              <Form.Item
                name="facility"
                label={t.directory.commonFields.facility}
                className={styles.formItem}
                rules={[{ required: true, max: 4 }]}
              >
                <Input
                  maxLength={4}
                  className={styles.input}
                  placeholder={t.directory.commonFields.numberPlaceholder}
                  onInput={handleInputOnlyNumbers}
                />
              </Form.Item>
            </Col>

            <Col span={8}>
              <Form.Item
                name="equipmentUnitSystemNumber"
                label={t.directory.commonFields.equipmentUnitSystemNumber}
                className={styles.formItem}
                rules={[{ required: true, max: 30 }]}
              >
                <Input
                  maxLength={30}
                  className={styles.input}
                  placeholder={t.directory.commonFields.numberPlaceholder}
                  onInput={handleInputOnlyNumbers}
                />
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item
                name="locationAddress"
                label={t.directory.commonFields.locationAddress}
                className={styles.formItem}
                rules={[{ required: true, max: 255 }]}
              >
                <Input
                  allowClear
                  maxLength={255}
                  placeholder="Введите адрес"
                  className={styles.input}
                />
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item
                name="parkingAddress"
                label={t.directory.commonFields.parkingAddress}
                className={styles.formItem}
                rules={[{ required: true, max: 255 }]}
              >
                <Input
                  allowClear
                  maxLength={255}
                  placeholder="Введите адрес"
                  className={styles.input}
                />
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item
                name="accessiblePositionId"
                label={t.directory.commonFields.accessiblePositionId}
                className={styles.formItem}
              >
                <Select
                  allowClear
                  showDivider
                  placeholder="Выберите должность"
                  options={usingAccessiblePositionIdOptions}
                  disabled={isUsingAccessiblePositionIdLoading}
                />
              </Form.Item>
            </Col>
          </Row>
        </div>

        <div>
          <div className={styles.header}>
            {t.directory.titles.exploitation}
          </div>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="exploitationStart"
                label={t.directory.commonFields.exploitationStart}
                className={styles.formItem}
                rules={[{ required: true }]}
              >
                <DatePicker
                  className={styles.datepicker}
                  format={DATE_FORMAT.BASE_REVERTED_DOTS}
                  disabledDate={d => d.isBefore(moment().startOf('day'))}
                />
              </Form.Item>
            </Col>
          </Row>
        </div>

        <div>
          <div className={styles.header}>
            {t.directory.titles.category}
          </div>
          <Row gutter={12}>
            <Col span={12}>
              <Form.Item
                name="type"
                label={t.directory.commonFields.type}
                className={styles.formItem}
                rules={[{ required: true }]}
              >
                <Select
                  allowClear
                  showDivider
                  placeholder="Выберите вид"
                  options={usingTypeOptions}
                  disabled={isUsingTypeLoading}
                  onSelect={handleSelectTypeId}
                  onClear={handleClearTypeId}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="subtype"
                label={t.directory.commonFields.subtype}
                className={styles.formItem}
                rules={[{ required: true }]}
              >
                <Select
                  allowClear
                  showDivider
                  placeholder="Выберите подвид"
                  options={usingSubTypeOptions}
                  disabled={!typeId || isUsingSubTypeLoading}
                />
              </Form.Item>
            </Col>
          </Row>
        </div>
      </div>

      <div className={styles.block}>
        <div className={classNames(styles.header, styles.header_big)}>
          {t.directory.titles.documents}
        </div>

        <div>
          <div className={styles.header}>
            {t.directory.titles.general}
          </div>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="stateNumber"
                label={t.directory.commonFields.stateNumber}
                validateTrigger={['onChange', 'onBlur']}
                className={styles.formItem}
                rules={[
                  { required: true },
                  {
                    pattern: FinalRegNumberMask,
                    message: t.directory.rules.stateNumber,
                    validateTrigger: ['onBlur'],
                  },
                ]}
                normalize={normalizeRegCarNumber}
              >
                <Input
                  allowClear
                  placeholder="A777AA777"
                  className={styles.input}
                />
              </Form.Item>
            </Col>

            <Col span={8}>
              <Form.Item
                name="inventoryNumber"
                label={t.directory.commonFields.inventoryNumber}
                className={styles.formItem}
                rules={[
                  { required: true },
                  {
                    max: 50,
                    message: t.directory.rules.inventoryNumber,
                  },
                ]}
                normalize={value => value.toUpperCase()}
              >
                <Input
                  allowClear
                  placeholder={t.directory.commonFields.inventoryNumber}
                  className={styles.input}
                />
              </Form.Item>
            </Col>

            <Col span={8}>
              <Form.Item
                name="assetNumber"
                label={t.directory.commonFields.assetNumber}
                className={styles.formItem}
                rules={[
                  { required: true },
                  {
                    max: 50,
                    message: t.directory.rules.assetNumber,
                  },
                ]}
                normalize={value => value.toUpperCase()}
              >
                <Input
                  allowClear
                  placeholder={t.directory.commonFields.assetNumber}
                  className={styles.input}
                />
              </Form.Item>
            </Col>

            <Col span={8}>
              <VINField
                form={form}
                className={styles.formItem}
                inputProps={{ className: styles.input }}
              />
            </Col>

            <Col span={8}>
              <Form.Item
                name="telematicsId"
                label={t.directory.commonFields.telematics}
                className={styles.formItem}
              >
                <Select
                  allowClear
                  showSearch
                  showDivider
                  filterOption={false}
                  placeholder="Выберите телематику"
                  options={!isTelematicsLoading ? telematicsOptions : []}
                  notFoundContent={isTelematicsLoading ? <Spin size="small" /> : null}
                  onSearch={onTelematicsSearch}
                />
              </Form.Item>
            </Col>

            <Col span={8}>
              <Form.Item
                name="currentMileage"
                label={t.directory.commonFields.currentMileage}
                className={styles.formItem}
                rules={[
                  { required: true },
                  {
                    type: 'number',
                    min: 1,
                    message: t.directory.rules.currentMileageRequired,
                  },
                  {
                    validator: (_, value) => value >= initialValues.currentMileage
                      ? Promise.resolve()
                      : Promise.reject(t.directory.rules.currentMileageError),
                  },
                ]}
                normalize={(value, prevValue) => value === '' ? value : /^\d*$/.test(value) ? Number(value) : prevValue ?? ''}
              >
                <Input
                  placeholder="15000"
                  maxLength={6}
                  className={styles.input}
                />
              </Form.Item>
            </Col>
          </Row>
        </div>

        <div>
          <div className={styles.header}>{t.directory.titles.bodywork}</div>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="bodyNumber"
                label={t.directory.commonFields.bodyNumber}
                className={styles.formItem}
                rules={[{ max: 17 }]}
              >
                <Input
                  allowClear
                  placeholder="W09ХХХХХХХХYYYХХХ"
                  className={styles.input}
                />
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item
                name="chassisNumber"
                label={t.directory.commonFields.chassisNumber}
                className={styles.formItem}
                rules={[{ max: 17 }]}
              >
                <Input
                  allowClear
                  placeholder="W09ХХХХХХХХYYYХХХ"
                  className={styles.input}
                />
              </Form.Item>
            </Col>
          </Row>
        </div>
      </div>

      <div className={styles.block}>
        <div className={styles.header}>
          {t.directory.titles.registryCertificate}
        </div>

        <Row gutter={16}>
          <Col span={8}>
            <Form.Item
              name="certificateNumber"
              label={t.directory.commonFields.certificateNumber}
              className={styles.formItem}
              rules={[{ required: true, max: 15 }]}
            >
              <Input
                allowClear
                placeholder="77АА757744"
                className={styles.input}
              />
            </Form.Item>
          </Col>

          <Col span={8}>
            <Form.Item
              name="certificateIssuedDate"
              label={t.directory.commonFields.certificateIssuedDate}
              className={styles.formItem}
              rules={[{ required: true }]}
            >
              <DatePicker className={styles.datepicker} format={DATE_FORMAT.BASE_REVERTED_DOTS} />
            </Form.Item>
          </Col>

          <Col span={8}>
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
      </div>

      <div className={styles.block}>
        <div className={classNames(styles.header, styles.header_big)}>
          {t.directory.titles.comment}
        </div>

        <Row gutter={16}>
          <Col span={24}>
            <Form.Item
              name="comment"
              rules={[{ max: 255 }]}
              className={styles.formItem}
            >
              <Input
                allowClear
                maxLength={255}
                placeholder="Введите комментарий"
                className={styles.input}
              />
            </Form.Item>
          </Col>
        </Row>
      </div>
    </Form>
  );
};

export default TransportEditForm;
