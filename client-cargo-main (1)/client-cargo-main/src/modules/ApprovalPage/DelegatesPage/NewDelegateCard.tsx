import './override.scss';

import React, { FC } from 'react';
import { SaveOutlined } from '@ant-design/icons';
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import {
  Button, DatePicker, Form, Select
} from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import PageLayout from 'shared/components/PageLayout/PageLayout';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypesModel } from 'stores/TransportTypes/TransportTypes.interface';
import { DATE_FORMAT } from 'constants/constants.app';

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

  const { availableTransportTypes, activeTransportType } = transportTypesStore;
  const {
    onFinish,
    goDelegatesList,
    form,
    onValuesChange,
    transportType,
    isStartDateActive,
    getDelegates,
    selfCandidatesToDelegates,
  } = useDelegates(delegatesStore, transportTypesStore, selfEmployee);

  const {
    datesCorrectionObject,
    checkStartDateCorrection,
    checkEndDateCorrection,
    checkDelegatesEndDate,
    checkDelegatesStartDate,
  } = useDatesValidation(form);

  const onStartDateChange = (date: moment.Moment | null, dateString: string): Promise<void> => {
    if (date !== null) {
      checkStartDateCorrection(date);
    }
    return getDelegates(dateString);
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
                name={Delegates.delegateId}
                label={DelegatesCyrillic[Delegates.delegateId]}
                rules={[ValidationRules.general.required]}
              >
                <Select placeholder={DelegatesCyrillic[Delegates.delegateId]}>
                  {selfCandidatesToDelegates
                  && transportType
                  && selfCandidatesToDelegates.map((x: EmployeeModel) => (
                    <OptionSelect
                      key={x.id}
                      value={x.id}
                      title={x.fullName}
                    >
                      {x.fullName}
                    </OptionSelect>
                  ))}
                </Select>
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
