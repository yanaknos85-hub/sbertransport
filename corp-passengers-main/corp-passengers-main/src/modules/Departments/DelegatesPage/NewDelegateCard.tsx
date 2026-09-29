import React, { FC, useState } from 'react';
import { useHistory } from 'react-router-dom';
import { observer } from 'mobx-react';

import moment from 'moment';
import { SaveOutlined } from '@ant-design/icons';
import {
  Button, DatePicker, Form, Select
} from 'antd';

import PageLayout from 'shared/components/PageLayout/PageLayout';
import { ValidationRules } from 'shared/fieldValidationRules';
import { DATE_FORMAT } from 'constants/constants.app';
import { useGetAvailableTransportTypes } from 'api/transport-types';
import { useProfile } from 'api/profile';
import { usePostDelegates } from 'api/delegates';
import { ignore } from 'utils';
import { SelectEmployeeFilter } from './Autoselect/SelectEmployeeFilter';
import {
  Delegates, DelegatesCyrillic, DelegatesTexts, DelegatesTextsCyrillic
} from './constants/Delegates.constants';
import { useDatesValidation } from './hooks/useDatesValidation';
import { TransportTypeEnum } from 'stores/TransportTypesDelegates/TransportTypes.interface';
import useUrl from 'shared/hooks/useUrl';

import style from './delegates.module.scss';
import './override.scss';

const { Option: OptionSelect } = Select;

interface FormData {
  delegateId: string;
  endDate: string;
  startDate: string;
  supervisorId: string;
  transportType: TransportTypeEnum;
}

const NewDelegateCard: FC = observer(() => {
  const [form] = Form.useForm();
  const { data: profile } = useProfile();
  const history = useHistory();
  const { superviserId, departmentId } = useUrl();
  const [addDelegate] = usePostDelegates(profile.organizationId, departmentId as string);

  const [formData, setFormData] = useState<FormData>({
    delegateId: '',
    endDate: '',
    startDate: '',
    supervisorId: superviserId as string,
    transportType: '' as TransportTypeEnum,
  });
  const { data: transport } = useGetAvailableTransportTypes(profile.organizationId);

  const {
    datesCorrectionObject, checkDelegatesStartDate, checkDelegatesEndDate,
  } = useDatesValidation(form);

  const onValuesChange = (changedValues: { transportTypeId: TransportTypeEnum }): void => {
    if (Object.keys(changedValues)[0] === 'transportTypeId') {
      setFormData({ ...formData, transportType: changedValues.transportTypeId });
    }
  };

  const onSubmit = () => {
    addDelegate(formData).catch(ignore);
    history.push(`/directories/departments/delegates?superviserId=${superviserId}&departmentId=${departmentId}`);
  };

  return (
    <PageLayout
      title={DelegatesTextsCyrillic[DelegatesTexts.pageHeader]}
      onBack={() => {
        history.push(`/directories/departments/delegates?superviserId=${superviserId}&departmentId=${departmentId}`);
      }}
    >
      <Form
        form={form}
        layout="vertical"
        name="create-request"
        size="middle"
        onValuesChange={onValuesChange}
        onFinish={onSubmit}
      >
        <Form.Item
          name={Delegates.transportTypeId}
          label={DelegatesCyrillic[Delegates.transportTypeId]}
          rules={[ValidationRules.general.required]}
        >
          <Select placeholder={DelegatesCyrillic[Delegates.transportTypeId]}>
            {transport.map(x => (
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

        <Form.Item
          name={Delegates.startDate}
          label={DelegatesCyrillic[Delegates.startDate]}
          rules={[
            ValidationRules.general.required,
            () => ({
              validator(_, value) {
                setFormData({ ...formData, startDate: moment(value).utc().format('YYYY-MM-DD') });
                return checkDelegatesStartDate(datesCorrectionObject.start, value);
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
            disabledDate={current => {
              const customDate = moment().format('YYYY-MM-DD');
              return (
                current
                && (current < moment(customDate, 'YYYY-MM-DD')
                || current > moment(formData.endDate, 'YYYY-MM-DD').add(1, 'days'))
              );
            }}
          />
        </Form.Item>

        <Form.Item
          name={Delegates.endDate}
          label={DelegatesCyrillic[Delegates.endDate]}
          rules={[
            ValidationRules.general.required,
            () => ({
              validator(_, value) {
                setFormData({ ...formData, endDate: moment(value).utc().format('YYYY-MM-DD') });
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
            disabledDate={current => {
              const customDate = moment().format('YYYY-MM-DD');
              return (
                current
                && (current < moment(customDate, 'YYYY-MM-DD') || current < moment(formData.startDate, 'YYYY-MM-DD'))
              );
            }}
          />
        </Form.Item>

        <Form.Item
          name={Delegates.employee}
          label={DelegatesCyrillic[Delegates.employee]}
          rules={[ValidationRules.general.required]}
        >
          <SelectEmployeeFilter
            onChange={delegateId => {
              setFormData({ ...formData, delegateId: delegateId as string });
            }}
            value={formData.delegateId}
          />
        </Form.Item>

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
