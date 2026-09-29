import React, { useState, useEffect, FC } from 'react';
import { Form, Select as SelectAntd, Tooltip } from 'antd';
import { QuestionCircleFilled } from '@ant-design/icons';
import { observer } from 'mobx-react';
import moment from 'moment';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import DatePicker from 'shared/form/DatePicker/DatePicker';
import Select from 'shared/form/Select/Select';
import { ValidationRules } from 'shared/fieldValidationRules';
import { DelegateCandidateAutoComplete } from 'shared/components/EmployeeDynamicAutoComplete';

import { TransportType, TransportTypesModel } from 'stores/TransportTypes/TransportTypes.interface';
import { DelegateModel } from 'stores/Delegates/Delegates.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { DATE_FORMAT } from 'constants/constants.app';

import { useDatesValidation } from '../../hooks/useDatesValidation';
import { IUseDelegate } from '../../hooks/useDelegateForm';
import { Delegates, DelegatesCyrillic } from '../../Delegates.constants';
import styles from './DelegateAddEditForm.module.scss';

const { Option: OptionSelect } = SelectAntd;

export interface DelegateFormValues {
  transportTypeId: string;
  delegateEmployee: DelegateModel['delegateEmployee'];
  startDate: moment.Moment | null;
  endDate: moment.Moment | null;
}

type DelegateAddEditFormProps = Pick<IUseDelegate, 'onFinish' | 'form' | 'onValuesChange'> & {
  delegate?: DelegateModel;
};

const DelegateAddEditForm: FC<DelegateAddEditFormProps> = observer(({
  delegate, onFinish, form, onValuesChange,
}) => {
  const { [StoreNames.transportTypesStore]: transportTypesStore } = useAppStoreContext();
  const [startDate, setStartDate] = useState<moment.Moment | null>();
  const {
    availableTransportTypes, activeTransportType, setActiveTransportType,
  } = transportTypesStore;

  useEffect(() => {
    if (delegate) {
      form.setFieldsValue({
        [Delegates.transportTypeId]: delegate.transportType,
        [Delegates.delegateEmployee]: delegate.delegateEmployee,
        [Delegates.startDate]: moment(delegate.startDate),
        [Delegates.endDate]: moment(delegate.endDate),
      });
      setActiveTransportType(delegate.transportType as TransportType);
      setStartDate(moment(delegate.startDate));
    }
  }, [delegate]);

  const {
    datesCorrectionObject,
    checkStartDateCorrection,
    checkEndDateCorrection,
    checkDelegatesEndDate,
    checkDelegatesStartDate,
  } = useDatesValidation();

  const onStartDateChange = (date: moment.Moment | null): void => {
    if (date !== null) {
      checkStartDateCorrection(date, form.getFieldValue(Delegates.endDate));
      !datesCorrectionObject.end && form.validateFields([Delegates.endDate]);
    }
    setStartDate(date);
  };

  const onEndDateChange = (date: moment.Moment | null): void => {
    if (date !== null) {
      checkEndDateCorrection(date, form.getFieldValue(Delegates.startDate));
      !datesCorrectionObject.start && form.validateFields([Delegates.startDate]);
    }
  };

  const handleFinish = formValues => {
    onFinish({ ...formValues, ...(!!delegate && { id: delegate.id }) });
  };

  const disabledDate = (date: moment.Moment) => date.isBefore(moment(), 'day');

  return (
    <Form
      form={form}
      name="create-request"
      className={styles.delegateForm}
      onFinish={handleFinish}
      onValuesChange={onValuesChange}
    >
      <Form.Item
        name={Delegates.transportTypeId}
        label={DelegatesCyrillic[Delegates.transportTypeId]}
        rules={[ValidationRules.general.required]}
      >
        <Select placeholder="Выберите вид">
          {availableTransportTypes.map(({
            id, name, rusName,
          }: TransportTypesModel) => (
            <OptionSelect
              key={id}
              value={name}
              title={rusName}
            >
              {rusName}
            </OptionSelect>
          ))}
        </Select>
      </Form.Item>
      <Form.Item
        name={Delegates.delegateEmployee}
        label={(
          <div>
            {DelegatesCyrillic[Delegates.delegateEmployee]}
            {!delegate && (
              <Tooltip title={DelegatesCyrillic[Delegates.employeeDep]} placement="bottomLeft">
                <QuestionCircleFilled className={styles.tooltipIcon} />
              </Tooltip>
            )}
          </div>
        )}
        rules={[ValidationRules.general.required]}
      >
        <DelegateCandidateAutoComplete
          disabled={!activeTransportType || !startDate || !!delegate?.id}
          placeholder="Выберите сотрудника"
          extraParams={{
            transportType: form.getFieldValue(Delegates.transportTypeId),
            date: startDate ? startDate.format(DATE_FORMAT.BASE) : '',
          }}
        />
      </Form.Item>
      <Form.Item
        name={Delegates.startDate}
        label={DelegatesCyrillic[Delegates.startDate]}
        rules={[
          ValidationRules.general.required,
          () => ({
            validator(_, value) {
              return checkDelegatesStartDate(datesCorrectionObject.start, value);
            },
          }),
        ]}
      >
        <DatePicker
          format={DATE_FORMAT.BASE_REVERTED_DOTS}
          placeholder="Выберите дату"
          disabledDate={disabledDate}
          onChange={onStartDateChange}
        />
      </Form.Item>
      <Form.Item
        name={Delegates.endDate}
        label={DelegatesCyrillic[Delegates.endDate]}
        rules={[
          ValidationRules.general.required,
          () => ({
            validator(_, value) {
              return checkDelegatesEndDate(datesCorrectionObject.end, value);
            },
          }),
        ]}
      >
        <DatePicker
          format={DATE_FORMAT.BASE_REVERTED_DOTS}
          placeholder="Выберите дату"
          disabledDate={disabledDate}
          onChange={onEndDateChange}
        />
      </Form.Item>
    </Form>
  );
});

export default DelegateAddEditForm;
