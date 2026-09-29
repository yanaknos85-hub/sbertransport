import React, { FC, useState } from 'react';
import {
  DatePicker, Form, Select, TimePicker
} from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import locale from 'antd/es/date-picker/locale/ru_RU';

import { useProfile } from 'api/profile';
import { useSelectDepartmentsSearch } from 'api/departments';
import { SelectEndlessScroll } from 'components/SelectEndlessScroll';
import { preventDefault } from 'utils';
import {
  ConditionType,
  TripPurposesHandbookTexts,
  TripPurposesHandbookTextsCyrillic
} from '../constants/TripPurposes.constants';
import { getInitialOrganizationOptions } from '../utils/utils';
import { InnerTripPurposeFormFieldsProps } from '../types/types';

import styles from '../styles/TripPurposeForm.module.scss';

interface ConditionState {
  condition?: ConditionType;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  value?: any;
}

export const InnerTripPurposeFormFields: FC<InnerTripPurposeFormFieldsProps> = ({
  onDelete,
  index,
  conditionSelectOptions,
  weekdaysSelectOptions,
  attributeOptions,
  innerFormInitialValues,
}) => {
  const { organizationId } = useProfile().data;
  const [getDepartments] = useSelectDepartmentsSearch(organizationId);
  const [conditionValue, setConditionValue] = useState<ConditionState>({
    condition: innerFormInitialValues && innerFormInitialValues.conditionSelectOption,
  });

  const timeFormat = 'HH:mm';
  const { RangePicker } = TimePicker;
  const { RangePicker: DateRange } = DatePicker;

  const handleConditionChange = (condition: ConditionType) => setConditionValue(prev => ({ ...prev, condition }));
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleConditionValueChange = (value: any) => setConditionValue(prev => ({ ...prev, value }));

  return (
    <div className={styles.conditionSection}>
      {onDelete && <DeleteOutlined onClick={onDelete} className={styles.removeSectionButton} />}

      <Form.Item
        className={styles.formItem}
        label={TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.condition]}
        key={`condition-${index}`}
        rules={[{ required: true }]}
      >
        <Select
          allowClear
          className={styles.customSelect}
          defaultValue={innerFormInitialValues?.conditionSelectOption}
          onSelect={handleConditionChange}
          options={conditionSelectOptions}
          onInputKeyDown={preventDefault}
        />
      </Form.Item>

      {conditionValue.condition === ConditionType.attribute && (
        <Form.Item
          className={styles.formItem}
          label={TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.employeeAttribute]}
          name={`attribute-${index}`}
          rules={[
            {
              required: true,
              message: TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.attributeRequired],
            },
          ]}
        >
          <Select
            className={styles.customSelect}
            allowClear
            options={attributeOptions}
            onSelect={handleConditionValueChange}
            onInputKeyDown={preventDefault}
          />
        </Form.Item>
      )}

      {conditionValue.condition === ConditionType.daysOfWeek && (
        <Form.Item
          className={styles.formItem}
          label={TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.daysOfWeek]}
          name={`daysOfWeek-${index}`}
          rules={[
            {
              required: true,
              message: TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.daysOfWeekRequired],
            },
          ]}
        >
          <Select
            className={styles.customSelect}
            mode="multiple"
            allowClear
            options={weekdaysSelectOptions}
            onSelect={handleConditionValueChange}
            onInputKeyDown={preventDefault}
          />
        </Form.Item>
      )}

      {conditionValue.condition === ConditionType.departureDate && (
        <Form.Item
          className={styles.formItem}
          label={TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.departureDate]}
          name={`departureDate-${index}`}
          rules={[
            {
              required: true,
              message: TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.departureDateRequired],
            },
          ]}
        >
          <DateRange
            locale={locale}
            className={styles.datePicker}
            format="DD.MM.YYYY"
            onChange={handleConditionValueChange}
          />
        </Form.Item>
      )}

      {conditionValue.condition === ConditionType.departureTime && (
        <Form.Item
          className={styles.formItem}
          label={TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.departureTime]}
          name={`departureTime-${index}`}
          rules={[
            {
              required: true,
              message: TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.departureTimeRequired],
            },
          ]}
        >
          <RangePicker
            onChange={handleConditionValueChange}
            format={timeFormat}
            locale={locale}
            className={styles.timePicker}
            order={false}
          />
        </Form.Item>
      )}

      {conditionValue.condition === ConditionType.organization && (
        <Form.Item
          className={styles.formItem}
          name={`organization-${index}`}
          label={TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.organization]}
          rules={[
            {
              required: true,
              message: TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.organizationRequired],
            },
          ]}
        >
          <SelectEndlessScroll
            fetch={getDepartments}
            labelField="departmentName"
            valueField="id"
            className={styles.customSelect}
            allowClear
            initialOptions={getInitialOrganizationOptions(innerFormInitialValues)}
            onSelect={handleConditionValueChange}
            onInputKeyDown={preventDefault}
          />
        </Form.Item>
      )}
    </div>
  );
};
