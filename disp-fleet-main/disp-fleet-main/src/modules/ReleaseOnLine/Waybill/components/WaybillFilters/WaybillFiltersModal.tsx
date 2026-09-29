import React, {
  FC, useCallback, useEffect, useState
} from 'react';
import { Form, FormInstance, Modal } from '@sber-sbertransport/ui-kit/src';
import { Input, Select } from '@sber-sbertransport/ui-kit/src';

import { DatePicker } from 'antd';
import moment, { Moment } from 'moment';

import { WaybillStatusesNames } from 'api/waybill/waybill.constants';
import { WaybillFilters } from 'api/waybill/waybill.types';

import { UUID } from 'utils/io-ts';
import { PaginationParams } from 'utils/io-ts/pagination';

import AutoparkSelect from 'components/AutoparkSelect/AutoparkSelect';
import BranchSelect from 'components/BranchSelect/BranchSelect';
import { FormItem } from 'components/FormItem';

import styles from './WaybillFiltersModal.module.scss';

const periodToRange = (period?: { start?: number; end?: number }): [Moment, Moment] | undefined => {
  if (!period?.start || !period?.end) return undefined;
  return [moment(period.start), moment(period.end)];
};

const { RangePicker } = DatePicker;

interface WaybillFiltersModalProps {
  visible: boolean;
  onClose: () => void;
  query: WaybillFilters;
  setQuery: (query: WaybillFilters) => void;
  form: FormInstance;
  initialValues: Omit<WaybillFilters, keyof PaginationParams>;
}

const statusOptions = Object.entries(WaybillStatusesNames).map(([value, label]) => ({
  value,
  label,
}));

const WaybillFiltersModal: FC<WaybillFiltersModalProps> = ({
  visible, onClose, query, setQuery, form, initialValues,
}) => {
  const [selectedContractorIds, setSelectedContractorIds] = useState<UUID[]>(query.contractorIds ?? []);

  useEffect(() => {
    if (visible) {
      form.setFieldsValue({
        ...query,
        creationTime: periodToRange(query.creationTime),
        finishTime: periodToRange(query.finishTime),
      });

      setSelectedContractorIds(query.contractorIds ?? []);
    }
  }, [form, query, visible]);

  const handleFinish = useCallback(
    (values: WaybillFilters) => {
      const { creationTime, finishTime } = values;

      setQuery({
        ...values,
        creationTime: creationTime
          ? { start: creationTime[0].valueOf(), end: creationTime[1].valueOf() }
          : query.creationTime,
        finishTime: finishTime
          ? { start: finishTime[0].valueOf(), end: finishTime[1].valueOf() }
          : query.finishTime,
      });

      onClose();
    },
    [query, setQuery, onClose]
  );

  const handleAutoparkChange = useCallback((value: string | string[]) => {
    const ids = Array.isArray(value) ? value as UUID[] : [value as UUID];
    setSelectedContractorIds(ids);
  }, []);

  return (
    <Modal
      open={visible}
      width={1224}
      title="Фильтры"
      onCancel={onClose}
      onOk={() => form.submit()}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleFinish}
        initialValues={initialValues}
      >
        <div className={styles.formColumns}>
          <FormItem name="contractorIds" label="Автопарк">
            <AutoparkSelect
              mode="multiple"
              onChange={handleAutoparkChange}
              placeholder="Выберите из списка"
              maxTagCount="responsive"
            />
          </FormItem>

          <FormItem name="autoparkIds" label="Филиал">
            <BranchSelect
              autoparkId={selectedContractorIds?.[0] ?? ''}
              mode="multiple"
              placeholder="Выберите из списка"
            />
          </FormItem>

          <FormItem name="requestStatusSet" label="Статус заявки">
            <Select
              mode="multiple"
              placeholder="Выберите статус"
              options={statusOptions}
              size="small"
            />
          </FormItem>

          <FormItem name="stateNumber" label="Гос. номер автомобиля">
            <Input placeholder="Введите номер" size="small" />
          </FormItem>

          <FormItem name="creationTime" label="Дата начала путевого листа">
            <RangePicker
              className={styles.rangePickerFullWidth}
              size="large"
              placeholder={['Выберите период или дату', '']}
            />
          </FormItem>

          <FormItem name="finishTime" label="Дата окончания путевого листа">
            <RangePicker
              className={styles.rangePickerFullWidth}
              size="large"
              placeholder={['Выберите период или дату', '']}
            />
          </FormItem>
        </div>
      </Form>
    </Modal>
  );
};

export default WaybillFiltersModal;
