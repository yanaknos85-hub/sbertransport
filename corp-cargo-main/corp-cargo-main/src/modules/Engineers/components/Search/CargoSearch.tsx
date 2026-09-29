import { Button, Form, Input } from 'antd';
import ErrorBoundary from 'antd/lib/alert/ErrorBoundary';
import { useForm } from 'antd/lib/form/Form';
import * as React from 'react';
import { useState } from 'react';
import { UUID } from 'utils/io-ts';
import * as R from 'ramda';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { SearchOutlined } from '@ant-design/icons';
import { useCargoSearchParams, useCargoStatuses, useOtoFeedCargo } from 'api/engineer';
import { useTranslation } from 'i18n';
import SelectStatus from 'shared/components/SelectStatus';
import { ValidationRules } from 'shared/fieldValidationRules';
import SelectAddressType from 'shared/components/SelectAddressType';
import SelectDateTimeType from 'shared/components/SelectDateTimeType';
import { searchSymbol } from 'utils/searchSymbol';
import { DateInput } from 'shared/components/DateInput/DateInput';
import { Value } from 'shared/components/DateInput/types';
import { FeedCargoSearchQuery } from 'stores/Engineer/Models/FeedCargo';
import { ADDRESS_TYPE } from 'stores/Engineer/Models';
import { DATE_TYPES } from 'stores/DateTypes/DateTypes.interface';
import styles from './search.module.scss';
import { useProfile } from 'api/profile';

const SearchResults: React.FC<{ query: FieldsCargo; children: (feedIds: UUID[]) => JSX.Element }> = ({
  query,
  children,
}) => {
  const { organizationId } = useProfile().data;
  const queryParams = useCargoSearchParams(query);
  const {
    data: { content: results },
    isLoading,
  // @ts-ignore
  } = useOtoFeedCargo(organizationId, queryParams);

  return (
    <ErrorBoundary>
      <div>{isLoading ? <SpinWrapped /> : children(results.map(R.prop('id')))}</div>
    </ErrorBoundary>
  );
};

export type FieldsCargo = FeedCargoSearchQuery | null;

export const CargoSearch = ({
  children,
  searchCargoQuery,
  setSearchCargoQuery,
  setCurrentCargoPage,
}: {
  children: (feedIds: UUID[]) => JSX.Element;
  searchCargoQuery: FieldsCargo;
  setSearchCargoQuery: (q: FieldsCargo) => void;
  setCurrentCargoPage: (currentPage: number) => void;
}): JSX.Element => {
  const [form] = useForm<FieldsCargo>();
  const { t } = useTranslation();
  const [isDisabled, setIsDisable] = useState(true);
  const [dateTimeTypeRequired, setDateTimeTypeRequired] = useState(false);
  const [addressTypeRequired, setAddressTypeRequired] = useState(false);

  React.useEffect(() => {
    form.setFieldsValue(searchCargoQuery || {});
    form.submit();
  }, []);

  const handleSubmit = (query: FieldsCargo) => {
    setCurrentCargoPage(1);
    setSearchCargoQuery(query);
  };

  const handleReset = () => {
    form.resetFields();
    setCurrentCargoPage(1);
    setSearchCargoQuery(null);
  };

  const addressRule = { ...ValidationRules.general.required, required: addressTypeRequired };

  const dateTimeRule = { ...ValidationRules.general.required, required: dateTimeTypeRequired };

  const handleSetAddressTypeRequired: React.ChangeEventHandler<HTMLInputElement> = evt => {
    setAddressTypeRequired(!!evt.currentTarget.value);
  };

  const handleSetDateTimeTypeRequired = (value: Value | undefined) => {
    setDateTimeTypeRequired(!!value && !!(value.value[0] || value.value[1]));
  };

  const handlerSetIsDisabledFutureDate = (value: string) => value === 'DESIRED_DATE' ? setIsDisable(false) : setIsDisable(true);

  const handleOnChangeInput = (event: React.ChangeEvent<HTMLInputElement>) => {
    const {
      value, minLength, name,
    } = event.target;
    const correctValue = value.replace(/[^a-zA-Z0-9а-яА-ЯЁё@. +,-]/gi, '').replace(/\s+/g, ' ');
    form.setFieldsValue({ [name]: correctValue.length === minLength ? correctValue.trim() : correctValue });
  };

  return (
    <ErrorBoundary>
      <div className={styles.searchPanel}>
        <Form form={form} onFinish={handleSubmit}>
          <Form.Item
            name="id"
            label={`${t.Monitor.id}`}
            rules={[ValidationRules.general.minMaxLength(3, 20)]}
          >
            <Input
              allowClear
              type="text"
              placeholder={`${t.Monitor.id}`}
              onChange={handleOnChangeInput}
              name="id"
              minLength={3}
            />
          </Form.Item>
          <Form.Item name="status" label={`${t.Monitor.status}`}>
            <SelectStatus
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectStatus}`}
              statusHook={useCargoStatuses()}
            />
          </Form.Item>
          <Form.Item
            name="addressType"
            label={`${t.Monitor.addressType}`}
            rules={[addressRule]}
          >
            <SelectAddressType
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectAddressType}`}
              restrict={[ADDRESS_TYPE.DESTINATION_ADDRESS, ADDRESS_TYPE.WAYPOINT_ADDRESS]}
            />
          </Form.Item>
          <Form.Item
            name="addressValue"
            label={`${t.Monitor.addressValue}`}
            rules={[ValidationRules.general.minMaxLength(3, 50)]}
          >
            <Input
              allowClear
              placeholder={`${t.Monitor.addressValue}`}
              onChange={handleSetAddressTypeRequired}
            />
          </Form.Item>
          <Form.Item
            name="senderName"
            label={`${t.Monitor.sender}`}
            rules={[ValidationRules.general.minMaxLength(3, 50)]}
          >
            <Input
              allowClear
              placeholder={`${t.Monitor.sender}`}
              onChange={handleOnChangeInput}
              name="senderName"
              minLength={3}
            />
          </Form.Item>
          <Form.Item
            name="recipientName"
            label={`${t.Monitor.recipient}`}
            rules={[ValidationRules.general.minMaxLength(3, 50)]}
          >
            <Input
              allowClear
              placeholder={`${t.Monitor.recipient}`}
              onChange={handleOnChangeInput}
              name="recipientName"
              minLength={3}
            />
          </Form.Item>
          <Form.Item
            name="dateTimeType"
            label={`${t.Monitor.dateTime}`}
            rules={[dateTimeRule]}
          >
            <SelectDateTimeType
              allowClear
              showSearch
              optionFilterProp="label"
              filterOption={searchSymbol}
              placeholder={`${t.Monitor.selectDateTimeType}`}
              onChange={handlerSetIsDisabledFutureDate}
              restrict={[DATE_TYPES.START_TRIP_DATE, DATE_TYPES.END_TRIP_DATE]}
            />
          </Form.Item>
          <Form.Item name="date">
            <DateInput
              isDisabledFutureDate={isDisabled}
              onChange={handleSetDateTimeTypeRequired}
              rangeTimeShow
            />
          </Form.Item>
          <div className={styles.buttonBar}>
            <Button
              onClick={form.submit}
              type="primary"
              icon={<SearchOutlined />}
            >
              Поиск
            </Button>
            <Button onClick={handleReset}>Сбросить фильтры</Button>
          </div>
        </Form>
      </div>
      <React.Suspense fallback={<SpinWrapped />}>
        <div className={styles.searchResults}>
          <SearchResults query={searchCargoQuery}>{children}</SearchResults>
        </div>
      </React.Suspense>
    </ErrorBoundary>
  );
};
