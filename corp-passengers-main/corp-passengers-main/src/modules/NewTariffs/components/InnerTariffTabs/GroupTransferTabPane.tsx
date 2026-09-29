import React, { FC, memo } from 'react';
import {
  Col, Form, InputNumber, Row, Select
} from 'antd';
import { preventDefault } from 'utils/Misc';
import { TariffStrings, defaultTimeDistanceUnit, selectTimeDistanceUnitOptions } from '../../constants/Tariffs.constants';
import { PersonalTariffTab } from '../../types/types';
import FormField from 'shared/form/FormField/FormField';
import { FieldType } from 'shared/form/Field/Field';
import moment from 'moment';

import styles from '../../styles/groupTransferTabPane.module.scss';

const SectionTitle: FC = ({ children }) => (
  <div className={styles.sectionTitle}>
    {children}
  </div>
);

export const GroupTransferTabPane: FC<PersonalTariffTab> = memo(({ isFormEditDisabled, initialValues }) => {
  return (
    <div className={styles.formWrapper}>
      <SectionTitle>{TariffStrings.tariffCommonData}</SectionTitle>
      <Row>
        <Col span={8} className={styles.dateRange}>
          <FormField
            type={FieldType.dateRange}
            name="tariffDateRange"
            params={{
              disabled: [isFormEditDisabled, false],
              defaultValue: [moment(initialValues?.tariffStartDate), moment(initialValues?.tariffEndDate)],
            }}
            label={TariffStrings.tariffDateRange}
            rules={[
              {
                required: true,
                message: TariffStrings.anyParameterRequired,
              },
            ]}
          />
        </Col>

        <Col span={8}>
          <Form.Item
            className={styles.tabItem}
            label={TariffStrings.rideCostPerKm}
            name="rideCostPerKm"
            rules={[
              {
                required: true,
                message: TariffStrings.anyParameterRequired,
              },
            ]}
          >
            <InputNumber
              disabled={isFormEditDisabled}
              min={0}
              step={0.01}
              max={99999}
            />
          </Form.Item>
        </Col>
        <Col span={8}>
          <Form.Item
            className={styles.tabItem}
            label={TariffStrings.rideCostPerMin}
            name="rideCostPerMin"
            rules={[
              {
                required: true,
                message: TariffStrings.anyParameterRequired,
              },
            ]}
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              step={0.01}
              max={99999}
              disabled={isFormEditDisabled}
            />
          </Form.Item>
        </Col>
      </Row>

      <SectionTitle>{TariffStrings.minAttributesTitle}</SectionTitle>
      <Row className={styles.rowWrapper}>
        <Col span={8}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.minRideValue}
            name="minRideValue"
            rules={[
              {
                required: true,
                message: TariffStrings.anyParameterRequired,
              },
            ]}
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              max={1000}
              disabled={isFormEditDisabled}
              addonAfter={(
                <Form.Item
                  noStyle
                  name="minRideValueUnits"
                  initialValue={defaultTimeDistanceUnit}
                >
                  <Select
                    defaultValue="km"
                    disabled={isFormEditDisabled}
                    options={selectTimeDistanceUnitOptions}
                  />
                </Form.Item>
              )}
            />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.minRideAmount}
            name="minRideAmount"
            rules={[
              {
                required: true,
                message: TariffStrings.anyParameterRequired,
              },
            ]}
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              max={99999}
              disabled={isFormEditDisabled}
            />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.transferFreeWaitingTime}
            name="freeWaitingTime"
            rules={[
              {
                required: false, // проверить требования, убрать rules если поле необяз
                message: TariffStrings.anyParameterRequired,
              },
            ]}
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              max={999}
              type="number"
              disabled={isFormEditDisabled}
            />
          </Form.Item>
        </Col>
      </Row>

      <SectionTitle>{TariffStrings.tariffExtendedData}</SectionTitle>
      <Row className={styles.rowWrapper}>
        <Col span={6}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.transferCostPerKmCity}
            name="transferCostPerKmCity"
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              step={0.01}
              max={99999}
              disabled={isFormEditDisabled}
            />
          </Form.Item>
        </Col>

        <Col span={6}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.transferCostPerMinCity}
            name="transferCostPerMinCity"
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              step={0.01}
              max={99999}
              disabled={isFormEditDisabled}
            />
          </Form.Item>
        </Col>

        <Col span={6}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.transferCostPerKmSuburb}
            name="transferCostPerKmSuburb"
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              step={0.01}
              max={99999}
              disabled={isFormEditDisabled}
            />
          </Form.Item>
        </Col>

        <Col span={6}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.transferCostPerMinSuburb}
            name="transferCostPerMinSuburb"
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              step={0.01}
              max={99999}
              disabled={isFormEditDisabled}
            />
          </Form.Item>
        </Col>

      </Row>
      <Row className={styles.rowWrapper}>
        <Form.Item
          className={styles.formItem}
          label={TariffStrings.transferWaitCostPerMin}
          name="transferWaitCostPerMin"
          rules={[
            {
              required: true,
              message: TariffStrings.anyParameterRequired,
            },
          ]}
        >
          <InputNumber
            onPressEnter={preventDefault}
            min={0}
            step={1}
            max={99999}
            disabled={isFormEditDisabled}
          />
        </Form.Item>
        {/* негабаритный, детские кресла, животные  - пока не делаем, уточнение БТ-макета */}
      </Row>

      <SectionTitle>{TariffStrings.conditionsTitle}</SectionTitle>
      <Row className={styles.rowWrapper}>
        <Col span={8}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.minOrderCreationTimeMin}
            name="minOrderCreationTimeMin"
            rules={[
              {
                required: true,
                message: TariffStrings.anyParameterRequired,
              },
            ]}
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              step={1}
              max={99999}
            />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.minOrderCancelationTimeMin}
            name="minOrderCancelationTimeMin"
            rules={[
              {
                required: true,
                message: TariffStrings.anyParameterRequired,
              },
            ]}
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              step={0.01}
              max={999}
            />
          </Form.Item>
        </Col>

        <Col span={8}>
          <Form.Item
            className={styles.formItem}
            label={TariffStrings.triggerTime}
            name="triggerTime"
            rules={[
              {
                required: true,
                message: TariffStrings.anyParameterRequired,
              },
            ]}
          >
            <InputNumber
              onPressEnter={preventDefault}
              min={0}
              step={1}
              max={250000}
            />
          </Form.Item>
        </Col>
      </Row>
    </div>
  );
});
