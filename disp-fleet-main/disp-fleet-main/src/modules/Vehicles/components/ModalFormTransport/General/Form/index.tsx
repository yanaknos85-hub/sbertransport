import React, { useState, FC, useEffect } from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import type { Moment } from 'moment';
import {
  Row, Col, Form, Input, Spin
} from 'antd';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile/profile.api';
import { DATE_FORMAT } from 'constants/app.constants';
import { Button } from 'components/Button';
import AutoparkSelect from 'components/AutoparkSelect/AutoparkSelect';
import BranchSelect from 'components/BranchSelect/BranchSelect';
import DatePicker from 'components/DatePicker/DatePicker';
import { useRoleMap } from 'hooks/useRoleMap';

import { DirectoriesTypes } from 'modules/Vehicles/constants/transport';
import { useTelematicsSelectOptions } from 'modules/Vehicles/hooks/useTelematicsSelectOptions';
import { useUsingTypeSelectOptions } from 'modules/Vehicles/hooks/useUsingTypeSelectOptions';
import { useUsingSubTypeSelectOptions } from 'modules/Vehicles/hooks/useUsingSubTypeSelectOptions';
import { useUsingAccessiblePositionIdSelectOptions } from 'modules/Vehicles/hooks/useUsingAccessiblePositionIdSelectOptions';
import { handleInputOnlyNumbers } from 'utils/inputOnlyNumbers';
import { UUID } from 'utils/io-ts';

import styles from './styles.module.scss';

export interface IFormData {
  contractorId: UUID[];
  autoparkId: UUID[];
  locationAddress: string;
  parkingAddress: string;
  exploitationStart: Moment;
  bodyColor: string;
  telematicsId?: string;
  typeId: string;
  subtypeId: string;
  comment: string;
  accessiblePositionId: string;
  balanceUnitNumber: string;
  facility: string;
  equipmentUnitSystemNumber: string;
}

interface IProps {
  loading: boolean;
  onFinish: (data: IFormData) => void;
}

const GeneralForm: FC<IProps> = ({ loading, onFinish }) => {
  const [form] = Form.useForm();
  const {
    t: {
      Transport: {
        directory: {
          types, titles, commonFields, rules,
        },
      },
    },
  } = useTranslation();
  const usingSubType = types[DirectoriesTypes.UsingSubType];

  const { contractorId, autoparkId } = useProfile().data;

  const { isAdmin, isManager } = useRoleMap();

  const {
    options: telematicsOptions,
    isLoading: isTelematicsLoading,
    onSearch: onTelematicsSearch,
  } = useTelematicsSelectOptions();

  const { options: usingTypeOptions, isLoading: isUsingTypeLoading } = useUsingTypeSelectOptions();

  const [typeId, setTypeId] = useState<string>();

  const { options: usingSubTypeOptions, isLoading: isUsingSubTypeLoading } = useUsingSubTypeSelectOptions(typeId);

  const { options: usingAccessiblePositionIdOptions, isLoading: isUsingAccessiblePositionIdLoading }
    = useUsingAccessiblePositionIdSelectOptions();

  useEffect(() => {
    form.setFieldsValue({ contractorId, autoparkId });
  }, [form, contractorId, autoparkId]);

  const handleSelectTypeId = value => {
    setTypeId(value);
    form.setFieldsValue({ subtypeId: undefined });
  };

  const handleClearTypeId = () => {
    setTypeId(undefined);
    form.resetFields(['subtypeId']);
  };

  const onValuesChange = (changedValues: IFormData) => {
    if ('contractorId' in changedValues) {
      form.setFieldsValue({ autoparkId: undefined });
    }
  };

  return (
    <Form
      form={form}
      className={styles.form}
      onFinish={onFinish}
      onValuesChange={onValuesChange}
    >
      <div className={styles.header}>
        {titles.location}
      </div>

      <Row gutter={16}>
        <Col span={12}>
          <Form.Item
            name="contractorId"
            label={commonFields.contractorId}
            className={styles.formItem}
            rules={[{ required: true }]}
          >
            <AutoparkSelect
              showSearch
              allowClear
              showDivider
              disabled={!isAdmin}
              size="middle"
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
                  label={commonFields.autoparkId}
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
                    showDivider
                  />
                </Form.Item>
              );
            }}
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            name="balanceUnitNumber"
            label={commonFields.balanceUnitNumber}
            className={styles.formItem}
            rules={[{ required: true, max: 4 }]}
          >
            <Input
              maxLength={4}
              className={styles.input}
              placeholder={commonFields.numberPlaceholder}
              onInput={handleInputOnlyNumbers}
            />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            name="facility"
            label={commonFields.facility}
            className={styles.formItem}
            rules={[{ required: true, max: 4 }]}
          >
            <Input
              maxLength={4}
              className={styles.input}
              placeholder={commonFields.numberPlaceholder}
              onInput={handleInputOnlyNumbers}
            />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            name="equipmentUnitSystemNumber"
            label={commonFields.equipmentUnitSystemNumber}
            className={styles.formItem}
            rules={[{ required: true, max: 30 }]}
          >
            <Input
              maxLength={30}
              className={styles.input}
              placeholder={commonFields.numberPlaceholder}
              onInput={handleInputOnlyNumbers}
            />
          </Form.Item>
        </Col>

        <Col span={12}>
          <Form.Item
            name="locationAddress"
            label={commonFields.locationAddress}
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
            label={commonFields.parkingAddress}
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
      </Row>

      <div className={styles.header}>
        {titles.exploitation}
      </div>

      <Row gutter={16}>
        <Col span={8}>
          <Form.Item
            name="exploitationStart"
            label={commonFields.exploitationStart}
            className={styles.formItem}
            rules={[{ required: true }]}
          >
            <DatePicker format={DATE_FORMAT.BASE_REVERTED_DOTS} style={{ width: '100%' }} />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            name="bodyColor"
            label={commonFields.bodyColor}
            className={styles.formItem}
            rules={[
              { required: true },
              {
                max: 50,
                message: rules.bodyColor,
              },
            ]}
          >
            <Input
              allowClear
              placeholder="Укажите цвет"
              className={styles.input}
            />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            name="telematicsId"
            label={commonFields.telematics}
            className={styles.formItem}
          >
            <Select
              allowClear
              showSearch
              showDivider
              size="middle"
              filterOption={false}
              placeholder="Выберите телематику"
              options={!isTelematicsLoading ? telematicsOptions : []}
              notFoundContent={isTelematicsLoading ? <Spin size="small" /> : null}
              onSearch={onTelematicsSearch}
            />
          </Form.Item>
        </Col>
      </Row>

      <div className={styles.header}>
        {titles.category}
      </div>

      <Row gutter={16}>
        <Col span={8}>
          <Form.Item
            name="typeId"
            label={usingSubType.fields.type}
            className={styles.formItem}
            rules={[{ required: true }]}
          >
            <Select
              allowClear
              showDivider
              size="middle"
              placeholder="Выберите вид"
              options={usingTypeOptions}
              disabled={isUsingTypeLoading}
              onSelect={handleSelectTypeId}
              onClear={handleClearTypeId}
            />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            name="subtypeId"
            label={usingSubType.fields.subType}
            className={styles.formItem}
            rules={[{ required: true }]}
          >
            <Select
              allowClear
              showDivider
              size="middle"
              placeholder="Выберите подвид"
              options={usingSubTypeOptions}
              disabled={!typeId || isUsingSubTypeLoading}
            />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            name="accessiblePositionId"
            label={usingSubType.fields.accessiblePositionId}
            className={styles.formItem}
          >
            <Select
              allowClear
              showDivider
              size="middle"
              placeholder="Выберите должность"
              options={usingAccessiblePositionIdOptions}
              disabled={isUsingAccessiblePositionIdLoading}
            />
          </Form.Item>
        </Col>
      </Row>

      <div className={styles.header}>
        {titles.comment}
      </div>

      <Row gutter={16} className={styles.commentRow}>
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

      <Button
        type="primary"
        htmlType="submit"
        loading={loading}
        className={styles.button}
      >
        Добавить
      </Button>
    </Form>
  );
};

export default GeneralForm;
