import React, { FC, useEffect } from 'react';
import { Button, Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import ErrorBoundary from 'antd/lib/alert/ErrorBoundary';
import { useParams } from 'react-router-dom';
import { observer } from 'mobx-react';

import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { searchSymbol } from 'utils/searchSymbol';
import { useProfile } from 'api/profile';
import { usePositions } from 'api/positions';
import FormItem from '../../FormItem';
import Input from '../../Inputs/Input';
import Select from '../../Inputs/Select';
import { DatePickerRange } from '../../Inputs/DatePickerRange';

import { SelectDepartmentFilter } from '../../Inputs/SelectDepartmentFilter';
import { useStatuses } from './useStatuses';
import { FeedSearchQuery } from 'stores/Engineer/Models/Feed';
import {
  FIELD_LABELS,
  TAXI_CLASSES_OPTIONS,
  BUS_CLASSES_OPTIONS,
  COMMON_CLASSES_OPTIONS,
  TransportTypesFilter
} from './constans';
import { Value } from 'shared/components/DateInput/types';
import { CategoryItem } from 'modules/OrderExecution/interfaces/Orders.types';
import { Category } from 'modules/OrderExecution/constants/Tabs';
import { ValidationRules } from 'shared/fieldValidationRules';

import styles from '../search.module.scss';

interface ISearchProps {
  onClose: () => void;
}

interface Fields extends FeedSearchQuery {
  creationTime?: Value;
  desiredTime?: Value;
  creationTimeFrom?: string | null;
  creationTimeTo?: string | null;
  desiredTimeFrom?: string | null;
  desiredTimeTo?: string | null;
}

export const PassengerSearch: FC<ISearchProps> = observer(({ onClose }): JSX.Element => {
  const { passengerStore } = useAppStoreContext();
  const [form] = useForm<Fields | null>();
  const { category } = useParams<{ type: CategoryItem }>();
  const transportType = passengerStore.personQueryFilters.transportType;
  const pageSettings = { page: 0, pageSize: 0 };

  const isGroupTransfer = category === TransportTypesFilter.GROUP_TRANSFER;
  const isTaxi = category === TransportTypesFilter.TAXI;
  const isBus = category === Category.bus;

  const transportClassOptions = isTaxi
    ? TAXI_CLASSES_OPTIONS
    : isBus
      ? BUS_CLASSES_OPTIONS
      : COMMON_CLASSES_OPTIONS;

  const handleSubmit = (queryProps: Fields | null) => {
    const targetQueryProps = {
      ...queryProps,
      transportType,
      status: queryProps?.status && queryProps?.status,
    };

    if (queryProps?.creationTime) {
      targetQueryProps.creationTimeFrom = queryProps.creationTime.value[0]?.toISOString() || null;
      targetQueryProps.creationTimeTo = queryProps.creationTime.value[1]?.toISOString() || null;
      delete targetQueryProps.creationTime;
    }

    if (queryProps?.desiredTime) {
      targetQueryProps.desiredTimeFrom = queryProps.desiredTime.value[0]?.toISOString() || null;
      targetQueryProps.desiredTimeTo = queryProps.desiredTime.value[1]?.toISOString() || null;
      delete targetQueryProps.desiredTime;
    }

    for (const i in targetQueryProps) {
      if (targetQueryProps[i] === '') {
        targetQueryProps[i] = undefined;
      }
    }

    passengerStore.setPersonFilterQueryProps(targetQueryProps);
    passengerStore.setSavePersonFilterQueryProps({
      ...queryProps,
      transportType,
      status: queryProps?.status && queryProps?.status,
      ...pageSettings,
    });
    if (isOrganization) {
      passengerStore.getPersonOrderList();
    } else {
      passengerStore.getPersonOrderListExec();
    }
    onClose();
  };

  useEffect(() => {
    const transportTypeForForm = form.getFieldValue('transportType');

    if (category !== TransportTypesFilter[transportTypeForForm]) {
      form.setFieldsValue({});
    }
  }, [category]);

  const saveFilters = () => {
    passengerStore.setPersonFilterQueryProps({ ...form.getFieldsValue(), transportType });
    passengerStore.setSavePersonFilterQueryProps({
      ...form.getFieldsValue(), transportType, ...pageSettings,
    });
  };

  const {
    organizationId, isOrganization,
  } = useProfile().data;
  const { positions: positionOptions } = usePositions(organizationId).data;
  const preparedPositionOptions = positionOptions.map(({ id: value, positionName: label }) => ({ label, value }));

  const statuses = useStatuses(transportType);

  useEffect(() => {
    const status = form.getFieldValue('status');
    if (status && !statuses.some(x => x.value === status)) {
      form.setFieldsValue({ status: undefined });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statuses, form.getFieldValue, form.setFieldsValue]);

  useEffect(() => {
    form.setFieldsValue(passengerStore.savePersonQueryFilters);
  }, [passengerStore.savePersonQueryFilters]);

  const handleReset = () => {
    passengerStore.setPersonFilterQueryProps({ transportType });
    passengerStore.setSavePersonFilterQueryProps({
      transportType,
      ...pageSettings,
    });
    form.resetFields();
  };

  return (
    <ErrorBoundary>
      <Form
        form={form}
        onFinish={handleSubmit}
        className={styles.form}
      >
        <div className={styles.blockTitle}>Заявка</div>
        <div className={styles.blockRow}>
          <FormItem name="id" label={FIELD_LABELS.id}>
            <Input
              allowClear
              placeholder={FIELD_LABELS.id}
              name="id"
              minLength={3}
            />
          </FormItem>

          <FormItem name="status" label={FIELD_LABELS.status}>
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={FIELD_LABELS.status}
              options={statuses}
            />
          </FormItem>

          <FormItem name="creationTime" label={FIELD_LABELS.creationTime}>
            <DatePickerRange rangeTimeShow />
          </FormItem>

          <FormItem name="desiredTime" label={FIELD_LABELS.deadline}>
            <DatePickerRange rangeTimeShow />
          </FormItem>
        </div>

        <div className={styles.blockTitle}>Исполнитель</div>
        <div className={styles.blockRow}>
          <FormItem name="transportClass" label={FIELD_LABELS.carClass}>
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={FIELD_LABELS.carClass}
              options={transportClassOptions}
            />
          </FormItem>
        </div>

        <div className={styles.blockTitle}>Заявитель</div>
        <div className={styles.blockRow}>
          <FormItem name="passengerName" label={FIELD_LABELS.declarantFIO}>
            <Input allowClear placeholder={FIELD_LABELS.declarantFIO} />
          </FormItem>

          <FormItem name="passengerPhone" label={FIELD_LABELS.declarantPhone}>
            <Input allowClear placeholder={FIELD_LABELS.declarantPhone} />
          </FormItem>

          <FormItem name="passengerPosition" label={FIELD_LABELS.declarantPositionName}>
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={FIELD_LABELS.declarantPositionName}
              options={preparedPositionOptions}
            />
          </FormItem>

          {isGroupTransfer && (
            <>
              <FormItem
                name="addContactFIO"
                label={FIELD_LABELS.addContractFIO}
                rules={[ValidationRules.general.maxLength(50)]}
              >
                <Input allowClear placeholder={FIELD_LABELS.addContractFIO} />
              </FormItem>

              <FormItem
                name="addContactPhone"
                label={FIELD_LABELS.addContactPhone}
              >
                <Input allowClear placeholder={FIELD_LABELS.addContactPhone} />
              </FormItem>

            </>
          )}

          <FormItem name="departure" label={FIELD_LABELS.declarantAddressFrom}>
            <Input allowClear placeholder={FIELD_LABELS.declarantAddressFrom} />
          </FormItem>

          <FormItem name="destination" label={FIELD_LABELS.declarantAddressTo}>
            <Input allowClear placeholder={FIELD_LABELS.declarantAddressTo} />
          </FormItem>
        </div>

        <div className={styles.blockTitle} />
        <div className={styles.blockRow}>
          <FormItem name="passengerDepartment" label={FIELD_LABELS.department}>
            <SelectDepartmentFilter allowClear placeholder={FIELD_LABELS.department} />
          </FormItem>
        </div>

        <div className={styles.formFooter}>
          <div className={styles.saveFilter} onClick={saveFilters}>
            Сохранить фильтр
          </div>
          <div className={styles.actionButtons}>
            <div
              className={styles.resetFilter}
              onClick={handleReset}
            >
              Сбросить
            </div>
            <Button
              type="primary"
              className={styles.acceptFilter}
              onClick={form.submit}
            >
              Подтвердить
            </Button>
          </div>
        </div>
      </Form>
    </ErrorBoundary>
  );
});
