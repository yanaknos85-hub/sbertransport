import './override.scss';

import { SaveOutlined } from '@ant-design/icons';
import {
  Button, DatePicker, Form, Select
} from 'antd';
import { observer } from 'mobx-react';
import moment, { Moment } from 'moment';
import React, { FC, useState } from 'react';

import { DATE_FORMAT } from 'constants/constants.app';

import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypesModel } from 'stores/TransportTypes/TransportTypes.interface';

import PageLayout from 'shared/components/PageLayout/PageLayout';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { DelegateCandidateAutoComplete } from 'shared/components/EmployeeDynamicAutoComplete';

import {
  Delegates, DelegatesCyrillic, DelegatesTexts, DelegatesTextsCyrillic
} from './Delegates.constants';
import { useDatesValidation } from './hooks/useDatesValidation';
import { useDelegates } from './hooks/useNewDelegateCard';

import style from './delegates.module.scss';

const { Option: OptionSelect } = Select;

const NewDelegateCard: FC = observer(() => {
  const {
    [StoreNames.delegatesStore]: delegatesStore,
    [StoreNames.transportTypesStore]: transportTypesStore,
    [StoreNames.selfStore]: selfEmployee,
  } = useAppStoreContext();

  const [isStartDateActive, setIsStartDateActive] = useState(false);

  const { availableTransportTypes, activeTransportType } = transportTypesStore;
  const {
    onFinish, goDelegatesList, form, onValuesChange,
  } = useDelegates(
    delegatesStore,
    transportTypesStore,
    selfEmployee
  );

  const {
    datesCorrectionObject,
    checkStartDateCorrection,
    checkEndDateCorrection,
    checkDelegatesEndDate,
    checkDelegatesStartDate,
  } = useDatesValidation(form);

  const onStartDateChange = (date: moment.Moment | null): void => {
    if (date !== null) {
      checkStartDateCorrection(date);
    }
    setIsStartDateActive(true);
  };

  const onEndDateChange = (date: moment.Moment | null): void => {
    if (date !== null) {
      checkEndDateCorrection(date);
    }
  };

  return (
    <PageLayout title={DelegatesTextsCyrillic[DelegatesTexts.pageHeader]} onBack={goDelegatesList}>
      <Form
        layout="vertical"
        form={form}
        name="create-request"
        size="middle"
        onFinish={onFinish}
        onValuesChange={onValuesChange}
      >
        <Form.Item
          name={Delegates.transportTypeId}
          label={DelegatesCyrillic[Delegates.transportTypeId]}
          rules={[ValidationRules.general.required]}
        >
          <Select placeholder={DelegatesCyrillic[Delegates.transportTypeId]}>
            {availableTransportTypes.map((x: TransportTypesModel) => (
              <OptionSelect
                key={x.id}
                value={x.name}
                title={x.rusName}
              >
                {x.rusName}
              </OptionSelect>
            ))}
          </Select>
        </Form.Item>
        {activeTransportType && (
          <>
            <Form.Item
              name={Delegates.startDate}
              label={DelegatesCyrillic[Delegates.startDate]}
              rules={[
                ValidationRules.general.required,
                // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
                () => ({
                  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
                  validator(_, value) {
                    // FIXME @typescript-eslint/explicit-function-return-type
                    return checkDelegatesStartDate(datesCorrectionObject.start, value);
                  },
                }),
              ]}
            >
              <DatePicker
                onChange={onStartDateChange}
                style={{ width: '100%' }}
                className="picker"
                format={DATE_FORMAT.BASE}
                placeholder="Выберите дату и время"
                dropdownClassName="no-now-btn"
              />
            </Form.Item>

            <Form.Item
              name={Delegates.endDate}
              label={DelegatesCyrillic[Delegates.endDate]}
              rules={[
                ValidationRules.general.required,
                // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
                () => ({
                  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
                  validator(_, value) {
                    // FIXME @typescript-eslint/explicit-function-return-type
                    return checkDelegatesEndDate(datesCorrectionObject.end, value);
                  },
                }),
              ]}
            >
              <DatePicker
                style={{ width: '100%' }}
                className="picker"
                format={DATE_FORMAT.BASE}
                placeholder="Выберите дату и время"
                dropdownClassName="no-now-btn"
                onChange={onEndDateChange}
              />
            </Form.Item>

            {isStartDateActive ? (
              <Form.Item
                name={Delegates.employee}
                label={DelegatesCyrillic[Delegates.employee]}
                rules={[ValidationRules.general.required]}
              >
                <DelegateCandidateAutoComplete
                  extraParams={{
                    transportType: form.getFieldValue(Delegates.transportTypeId),
                    date: (form.getFieldValue(Delegates.startDate) as Moment).format('YYYY-MM-DD'),
                  }}
                  placeholder={DelegatesCyrillic.employeePlaceholder}
                />
              </Form.Item>
            ) : null}
          </>
        )}

        <div>
          <Button
            icon={<SaveOutlined />}
            size="middle"
            htmlType="submit"
            className={style.save_button}
          >
            {DelegatesTextsCyrillic[DelegatesTexts.save]}
          </Button>
        </div>
      </Form>
    </PageLayout>
  );
});

export default NewDelegateCard;
