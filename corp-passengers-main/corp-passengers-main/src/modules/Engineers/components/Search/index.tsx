/* eslint-disable */
import { Button, Form, Input } from 'antd';
import ErrorBoundary from 'antd/lib/alert/ErrorBoundary';
import { useForm } from 'antd/lib/form/Form';
import { FeedSearchQuery } from 'stores/Engineer/Models/Feed';
import * as React from 'react';
import {FC, useEffect, useState} from 'react';
import { UUID } from 'utils/io-ts';
import * as R from 'ramda';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { SearchOutlined } from '@ant-design/icons';
import { useOtoFeed, useOtoFeedExecutor, useSearchParams } from 'api/engineer';
import { useTranslation } from 'i18n';
import SelectStatus from 'shared/components/SelectStatus';
import SelectTransportType from 'shared/components/SelectTransportType';
import SelectContractor from 'shared/components/SelectContractor';
import { ValidationRules } from 'shared/fieldValidationRules';
import SelectAddressType from 'shared/components/SelectAddressType';
import SelectTaxiType from 'shared/components/SelectTaxiType';
import CustomInput from 'shared/components/PhoneMask/inputMask';
import SelectDateTimeType from 'shared/components/SelectDateTimeType';
import { searchSymbol } from 'utils/searchSymbol';
import { DateInput } from 'shared/components/DateInput/DateInput';
import { Value } from 'shared/components/DateInput/types';
import { useGettingAllTravelStatuses } from 'api/travel-status';
import { ADDRESS_TYPE } from 'stores/Engineer/Models';
import { DATE_TYPES } from 'stores/DateTypes/DateTypes.interface';
import styles from './search.module.scss';
import { SelectPosition } from '../../../Employees/EditEmployee';
import { useProfile } from 'api/profile';
import { SelectDepartment } from 'shared/components/SelectDepartment';


const SearchResults: FC<{ query: Fields; children: (feedIds: UUID[]) => JSX.Element }> = ({
  query,
  children,
}) => {
  const { organizationId, isOrganization, executorGroupId } = useProfile().data;
  const queryParams = useSearchParams(query);
  const {
    data: { content: results },
    isLoading,
  // @ts-ignore
  } = useOtoFeedExecutor(organizationId, executorGroupId, queryParams);

  return (
    <ErrorBoundary>
      <div>{isLoading ? <SpinWrapped /> : children(results.map(R.prop('id')))}</div>
    </ErrorBoundary>
  );
};

export type Fields = FeedSearchQuery | null;

export const Search = ({
  children,
  searchQuery,
  setSearchQuery,
  setCurrentPage,
}: {
  children: (feedIds: UUID[]) => JSX.Element;
  searchQuery: Fields;
  setSearchQuery: (q: Fields) => void;
  setCurrentPage: (currentPage: number) => void;
}): JSX.Element => {
  const [form] = useForm<Fields>();
  const { t } = useTranslation();
  const [isDisabled, setIsDisable] = useState(true);
  const [dateTimeTypeRequired, setDateTimeTypeRequired] = useState(false);
  const [addressTypeRequired, setAddressTypeRequired] = useState(false);

  useEffect(() => {
    form.setFieldsValue(searchQuery || {});
    form.submit();
  }, [] /* Не удалять массив и не добавлять туда ничего!
  Изначально все неправильно написано, менять = рефакторить много всего */ );

  const handleSubmit = (query: Fields) => {
    setCurrentPage(1);
    setSearchQuery(query);
  };

  const handleReset = () => {
    form.resetFields();
    setCurrentPage(1);
    setSearchQuery(null);
  };

  const addressRule = { ...ValidationRules.general.required, required: addressTypeRequired };

  const dateTimeRule = { ...ValidationRules.general.required, required: dateTimeTypeRequired };

  const handleSetAddressTypeRequired: React.ChangeEventHandler<HTMLInputElement> = evt => {
    setAddressTypeRequired(!!evt.currentTarget.value);
  };

  const handleSetDateTimeTypeRequired = (value: Value | undefined) => {
    setDateTimeTypeRequired(!!value && !!(value.value[0] || value.value[1]));
  };

  const handlerSetIsDisabledFutureDate = (value: string) =>
    value === 'DESIRED_DATE' ? setIsDisable(false) : setIsDisable(true);

  const handleOnChangeInput = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { value, minLength, name } = event.target;
    const correctValue = value.replace(/[^a-zA-Z0-9а-яА-ЯЁё@. +,-]/gi, '').replace(/\s+/g, ' ');
    form.setFieldsValue({ [name]: correctValue.length === minLength ? correctValue.trim() : correctValue });
  };

  return (
    <ErrorBoundary>
      <div className={styles.searchPanel}>
        <Form form={form} onFinish={handleSubmit}>
          <Form.Item name="passengerDepartment" label={`${t.Monitor.department}`}>
            <SelectDepartment
              allowClear
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectDepartment}`}
            />
          </Form.Item>
          <Form.Item name="status" label={`${t.Monitor.status}`}>
            <SelectStatus
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectStatus}`}
              statusHook={useGettingAllTravelStatuses()}
            />
          </Form.Item>
          <Form.Item name="id" label={`${t.Monitor.id}`} rules={[ValidationRules.general.minMaxLength(3, 20)]}>
            <Input
              allowClear
              type="text"
              placeholder={`${t.Monitor.id}`}
              onChange={handleOnChangeInput}
              name="id"
              minLength={3}
            />
          </Form.Item>
          <Form.Item name="contractor" label={`${t.Monitor.executor}`}>
            <SelectContractor
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectExecutor}`}
            />
          </Form.Item>
          <Form.Item name="transportType" label={`${t.Monitor.transportType}`}>
            <SelectTransportType
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectTransportType}`}
            />
          </Form.Item>
          <Form.Item name="transportClass" label={`${t.Monitor.taxiClass}`}>
            <SelectTaxiType
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectTransportClass}`}
            />
          </Form.Item>
          <Form.Item name="passengerPosition" label={`${t.Monitor.positionName}`}>
            <SelectPosition
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectPosition}`}
            />
          </Form.Item>
          <Form.Item
            name="passengerName"
            label={`${t.Monitor.passenger}`}
            rules={[ValidationRules.general.minMaxLength(3, 50)]}
          >
            <Input
              allowClear
              placeholder={`${t.Monitor.passenger}`}
              onChange={handleOnChangeInput}
              name="passengerName"
              minLength={3}
            />
          </Form.Item>
          <Form.Item
            name="passengerPhone"
            label={`${t.Monitor.passengerPhoneNumber}`}
            rules={[ValidationRules.general.checkPhoneMask()]}
          >
            <CustomInput allowClear mask="+7 (999) 999-99-99" placeholder="+7 (___) ___-__-__" />
          </Form.Item>
          <Form.Item name="dateTimeType" label={`${t.Monitor.dateTime}`} rules={[dateTimeRule]}>
            <SelectDateTimeType
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectDateTimeType}`}
              onChange={handlerSetIsDisabledFutureDate}
              restrict={[DATE_TYPES.APPROVAL_DATE, DATE_TYPES.SHIPMENT_DATE, DATE_TYPES.TRANSFER_DATE]}
            />
          </Form.Item>
          <Form.Item name="date" label={`${t.Monitor.dateTime}`}>
            <DateInput isDisabledFutureDate={isDisabled} onChange={handleSetDateTimeTypeRequired} rangeTimeShow />
          </Form.Item>
          <Form.Item name="deadline" label={`${t.Monitor.deadline}`}>
            <DateInput />
          </Form.Item>
          <Form.Item name="addressType" label={`${t.Monitor.addressType}`} rules={[addressRule]}>
            <SelectAddressType
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectAddressType}`}
              restrict={[ADDRESS_TYPE.RECIPIENT_ADDRESS]}
            />
          </Form.Item>
          <Form.Item
            name="addressValue"
            label={`${t.Monitor.addressValue}`}
            rules={[ValidationRules.general.minMaxLength(3, 50)]}
          >
            <Input allowClear placeholder={`${t.Monitor.addressValue}`} onChange={handleSetAddressTypeRequired} />
          </Form.Item>
          <div className={styles.buttonBar}>
            <Button onClick={form.submit} type="primary" icon={<SearchOutlined />}>
              Поиск
            </Button>
            <Button onClick={handleReset}>Сбросить фильтры</Button>
          </div>
        </Form>
      </div>
      <React.Suspense fallback={<SpinWrapped />}>
        <div className={styles.searchResults}>
          <SearchResults query={searchQuery}>{children}</SearchResults>
        </div>
      </React.Suspense>
    </ErrorBoundary>
  );
};
