import React, {
  type Dispatch,
  type SetStateAction,
  useEffect,
  useMemo,
  useState,
  type FC,
  type ReactElement
} from 'react';
import {
  Col,
  Divider,
  Form,
  Row,
  Select as AntdSelect,
  Tooltip,
  InputNumber
} from 'antd';
import { Select } from '../Select/Select';
import { type Moment } from 'moment';
import cn from 'classnames';
import { ReactComponent as InfoCircleIcon } from 'shared/icons/info-circle.svg';

import { FormInstance } from 'antd/es/form';
import { ValidationRules } from 'shared/fieldValidationRules';
import { defaultTimeDistanceUnit, selectTimeDistanceUnitOptions, TariffStrings } from 'modules/NewTariffs/constants/Tariffs.constants';
import { getFormRule } from 'modules/TripPurposes/utils/utils';
import { useGetAvailableTransportTypes, useTransportServiceTypes } from 'api/transport-types';
import { SelectOption } from 'modules/Employees/EditEmployee';
import { useGroupTransferTransportClasses } from 'api/passengers-transport-classes';
import FormField from 'shared/form/FormField/FormField';
import { FieldType } from 'shared/form/Field/Field';
import { useContractors } from 'api/contractors';
import { searchSymbol } from 'utils/searchSymbol';
import {
  TariffJson, TariffTransportParams
} from 'stores/Tariffs/Tariffs.interface';
import { useOrganizationProjection } from 'api/organizations/search';
import { useTariffForm } from '../../hooks/useTariffForm';
import { useGetListRegions } from 'api/tariffs';
import { useTariffTransport } from 'api/tariffs';
import { SelectValue } from 'antd/lib/select';
import { TariffTransportClasses } from 'stores/TransportClasses/TransportClasses.interface';
import moment from 'moment';
import { createTransportLabel } from '../../utils/utils';

import styles from './doubleTransportTariffForm.module.scss';

interface DoubleTransportTariffFormProps {
  formStep: number;
  form: FormInstance<unknown>;
  cargoTransportType: string[];
  initialValues: TariffJson;
  contractOptions: SelectOption[];
  transportType: string | undefined;
  specificServiceType: string;
  serviceType: string;
  onSelect: (field: string) => (value: string) => void;
  onChangeTransportServiceType: (value: SelectValue) => void;
  setTransferClass: Dispatch<SetStateAction<string>>;
}

export const DoubleTransportTariffForm: FC<DoubleTransportTariffFormProps>
= ({
  form,
  formStep,
  cargoTransportType,
  initialValues,
  specificServiceType,
  serviceType,
  transportType,
  onSelect,
  contractOptions,
  onChangeTransportServiceType,
  setTransferClass,
}) => {
  const {
    contractorTariffId,
    setContractorTariffId,
    regionId,
    setRegionId,
    setOrganizationId,
    organizationId,
    setContractId,
  } = useTariffForm(initialValues);

  const preventDefault = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
    }
  };

  const onGroupTransferClassChange = (value: string) => {
    setTransferClass(value);
    form.resetFields(['contractId']);
  };

  const clearContractId = (value: string) => {
    setContractorTariffId(value);
    form.resetFields(['contractId']);
  };

  const isCarChoiceAvailable = specificServiceType === TariffTransportClasses.TRANSFER_CAR_CHOICE;

  const contractorsOptions
  = useContractors({ config: { suspense: false } }).data?.contractors?.map(({ name: label, id: value }) => ({
    label,
    value,
  })) ?? [];

  const organizationsOptions
  = useOrganizationProjection({}, { suspense: false }).data?.map(({ officialName: label, id: value }) => ({
    label,
    value,
  })) ?? [];

  const groupTransferClassOptions
  = useGroupTransferTransportClasses()
    .data.map(({ rusName: label, value }) => ({
      label,
      value,
    }));

  const transportTypesOptions: SelectOption[]
  = useGetAvailableTransportTypes(organizationId, { suspense: false })
    .data?.filter(({ name }) => !cargoTransportType.includes(name))
    ?.map(({ rusName: label, name: value }) => ({
      label,
      value,
    })) ?? [];

  const regionsOptions
    = useGetListRegions({ suspense: false }).data?.map(({ name: label, id: value }) => ({ label, value })) ?? [];

  const transportServiceTypes
    = useTransportServiceTypes({ suspense: false })
      .data?.filter(({ name }) => name === 'EMPLOYEE_TRANSPORTATION')
      ?.map(({ rusName: label, name: value }) => ({
        label,
        value,
      })) ?? [];

  const { required } = ValidationRules.general;
  const isSecondFormAvailable = formStep === 2;

  const [dateValue, setDateValue] = useState<[Moment?, Moment?]>([]);
  const isDateFilled = dateValue?.length == 2;

  const tariffTransportParams = {
    tariffStartDate: dateValue?.[0]?.format('YYYY-MM-DD'),
    tariffEndDate: dateValue?.[1]?.format('YYYY-MM-DD'),
    contractorId: contractorTariffId,
    organizationId: organizationId,
    regionIds: regionId?.map(item => item.value).join(','),
    page: 0,
    size: 300,
  } as TariffTransportParams;

  const {
    data: tariffTransportResponse, refetch: tariffTransportRefetch,
  }
  = useTariffTransport(tariffTransportParams, { enabled: !!regionId?.length && !!dateValue?.length, suspense: false });

  const onTransportTypeChange = () => {
    form.resetFields(['groupTransferClass']);
    setTransferClass('');
  };

  useEffect(() => {
    if (!!regionId?.length && !!dateValue?.length && contractorTariffId && organizationId) {
      tariffTransportRefetch();
    }
  }, [dateValue, contractorTariffId, organizationId, regionId, tariffTransportRefetch, specificServiceType]);

  const tariffTransportOptions = useMemo(() => {
    return tariffTransportResponse?.content?.map(item => ({
      ...item, label: createTransportLabel(item.brand, item.model, item.stateNumber), value: item.id,
    }));
  }, [tariffTransportResponse]);

  const RideCostUnitsSelect = (
    <Form.Item
      noStyle
      name="minRideValueUnits"
      initialValue={defaultTimeDistanceUnit}
    >
      <AntdSelect
        defaultValue="km"
        options={selectTimeDistanceUnitOptions}
      />
    </Form.Item>
  );

  const renderOptionTariffTitle = (tariffId: string) => (
    <div className={styles.optionTitle}>
      На автомобиль уже заведен тариф&nbsp;
      <span className={styles.accentTariffLink}>{tariffId}</span>
    </div>
  );

  const renderTariffDropdown = (children: ReactElement) => {
    return (
      <div className={styles.tariffTransportSelect}>
        {children}
      </div>
    );
  };

  const disabledPassedDate = (d: Moment) => d.startOf('day').isBefore(moment().startOf('day'));

  const isOrganizationFilled = !!organizationId;

  return (
    <div className={styles.form}>
      <div className={cn({ [styles.hidden]: isSecondFormAvailable })}>
        <div className={styles.formBlock}>
          <Row gutter={16}>
            <Col span={8}>
              <FormField
                type={FieldType.dateRange}
                name="tariffDateRange"
                label={TariffStrings.tariffDateRange}
                params={{
                  allowClear: true,
                  value: dateValue,
                  onChange: setDateValue,
                  className: styles.dateRangePicker,
                  disabledDate: disabledPassedDate,
                }}
                rules={[
                  ValidationRules.general.dateRangeValidation(),
                ]}
              />
            </Col>
          </Row>
        </div>

        {isDateFilled && (
          <>
            <Divider />

            <div className={styles.formBlock}>
              <span className={styles.formBlockTitle}>{TariffStrings.tariffClientData}</span>
              <Row gutter={16}>
                <Col span={8}>
                  <Form.Item
                    label={TariffStrings.tariffContractorId}
                    className={styles.formItem}
                    name="organizationId"
                    initialValue={initialValues.organizationId}
                    shouldUpdate
                    rules={[required]}
                  >
                    <Select
                      onInputKeyDown={preventDefault}
                      options={organizationsOptions}
                      className={styles.selectItem}
                      onChange={setOrganizationId}
                      filterOption={searchSymbol}
                      value={organizationId}
                      onSelect={onSelect('organization')}
                      allowClear
                      showSearch
                    />
                  </Form.Item>
                </Col>
              </Row>
            </div>
            <Divider />
            {isOrganizationFilled && (
            <div className={styles.formBlock}>
              <span className={styles.formBlockTitle}>
                {TariffStrings.contractorData}
              </span>
              <Row gutter={16}>
                <Col span={16}>
                  <Form.Item
                    label={TariffStrings.contractorId}
                    rules={[required]}
                    name="contractorId"
                    className={styles.formItem}
                  >
                    <Select
                      className={styles.selectItem}
                      options={contractorsOptions}
                      allowClear
                      showSearch
                      filterOption={searchSymbol}
                      onSelect={onSelect('contractor')}
                      onChange={clearContractId}
                    />
                  </Form.Item>
                </Col>
                {!!contractorTariffId && (
                <Col span={8}>
                  <Form.Item
                    label={TariffStrings.tariffContractNumber}
                    name="contractId"
                    rules={getFormRule(true, TariffStrings.contractRequired)}
                    className={styles.formItem}
                  >
                    <Select
                      className={styles.selectItem}
                      options={contractOptions}
                      disabled={!contractOptions?.length}
                      onChange={setContractId}
                    />
                  </Form.Item>
                </Col>
                )}
              </Row>
              {contractorTariffId && (
              <Row gutter={16}>
                <Col span={24}>
                  <Form.Item
                    label={TariffStrings.tariffRegion}
                    rules={[required]}
                    name="regionIds"
                    className={styles.formItem}
                  >
                    <Select
                      className={styles.selectItem}
                      mode="multiple"
                      options={regionsOptions}
                      optionFilterProp="label"
                      onChange={(_, option) => setRegionId(option as SelectOption[])}
                      onSelect={onSelect('regionId')}
                    />
                  </Form.Item>
                </Col>
              </Row>
              )}
            </div>
            )}
            {!!regionId?.length && (
            <>
              <Divider />
              <div className={styles.formBlock}>
                <span className={styles.formBlockTitle}>{TariffStrings.tariffServiceData}</span>
                <Row gutter={16}>
                  <Col span={8}>
                    <Form.Item
                      className={styles.formItem}
                      label={TariffStrings.serviceType}
                      rules={[required]}
                      shouldUpdate
                      name="serviceType"
                    >
                      <Select
                        allowClear
                        value={serviceType}
                        className={styles.selectItem}
                        options={transportServiceTypes}
                        onChange={onChangeTransportServiceType}
                        onSelect={onSelect('serviceType')}
                        disabled
                      />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item
                      className={styles.formItem}
                      label="Вид услуги"
                      shouldUpdate
                      name="transportType"
                      rules={[required]}
                    >
                      <Select
                        value={transportType}
                        allowClear
                        className={styles.selectItem}
                        options={transportTypesOptions}
                        onChange={onTransportTypeChange}
                        onSelect={onSelect('transportType')}
                        disabled
                      />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item
                      label={TariffStrings.groupTransferClass}
                      name="groupTransferClass"
                      rules={getFormRule(true, TariffStrings.specificServiceType)}
                      className={styles.formItem}
                    >
                      <Select
                        allowClear
                        value={specificServiceType}
                        className={styles.selectItem}
                        onChange={onGroupTransferClassChange}
                        options={groupTransferClassOptions}
                        disabled
                      />
                    </Form.Item>
                  </Col>
                </Row>
              </div>
            </>
            )}
          </>
        )}
      </div>
      <div className={cn({ [styles.hidden]: !isSecondFormAvailable })}>
        {isCarChoiceAvailable && (
          <>
            <div className={styles.formBlock}>
              <span className={styles.formBlockTitle}>{TariffStrings.tariffTransportTitle}</span>
              <Row gutter={16}>
                <Col span={24}>
                  <Form.Item
                    name="transportIds"
                    label={TariffStrings.tariffChosenTransport}
                    className={styles.formItem}
                    rules={[
                      {
                        required: true,
                        message: TariffStrings.anyParameterRequired,
                      },
                    ]}
                  >
                    <AntdSelect
                      className={styles.selectItem}
                      mode="multiple"
                      allowClear
                      showSearch
                      optionFilterProp="label"
                      dropdownRender={renderTariffDropdown}
                      optionLabelProp="label"
                    >
                      {tariffTransportOptions?.map(transport => (
                        <AntdSelect.Option
                          label={transport.label}
                          value={transport.value}
                          key={transport.value}
                          disabled={!!transport.tariffId}
                          className={styles.transportOption}
                        >
                          <div className={styles.transportOptionItem}>
                            <span>{transport.label}</span>
                            {transport?.tariffHumanReadableId && (
                            <div>
                              <Tooltip
                                placement="topRight"
                                title={renderOptionTariffTitle(transport.tariffHumanReadableId)}
                                overlayClassName={styles.tariffTooltip}
                              >
                                <InfoCircleIcon />
                              </Tooltip>
                            </div>
                            )}
                          </div>
                        </AntdSelect.Option>
                      ))}
                    </AntdSelect>
                  </Form.Item>
                </Col>
              </Row>
            </div>

            <Divider />
          </>
        )}

        <Row gutter={16}>
          <Col flex={3}>
            <div className={styles.formBlock}>
              <span className={styles.formBlockTitle}>
                {TariffStrings.tariffCommonData}
              </span>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item
                    name="rideCostPerKm"
                    rules={[{
                      required: true,
                      message: TariffStrings.anyParameterRequired,
                    }]}
                    className={styles.formItem}
                    label="Стоимость 1 км, руб."
                  >
                    <InputNumber
                      min={0}
                      max={99999}
                      className={styles.input}
                    />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    className={styles.formItem}
                    label="Стоимость 1 минуты, руб."
                    name="rideCostPerMin"
                    rules={[{
                      required: true,
                      message: TariffStrings.anyParameterRequired,
                    }]}
                  >
                    <InputNumber
                      className={styles.input}
                      min={0}
                      max={99999}
                    />
                  </Form.Item>
                </Col>
              </Row>
            </div>
          </Col>
          <Col flex={5}>
            <div className={styles.formBlock}>
              <span className={styles.formBlockTitle}>{TariffStrings.tariffAttributes}</span>
              <Row gutter={16} align="bottom">
                <Col span={9}>
                  <Form.Item
                    label="Минимальное количество"
                    className={styles.formItem}
                    name="minRideValue"
                    rules={[
                      {
                        required: true,
                        message: TariffStrings.anyParameterRequired,
                      },
                    ]}
                  >
                    <InputNumber
                      className={cn(styles.input, styles.inputWithSelect)}
                      min={0}
                      max={1000}
                      addonAfter={RideCostUnitsSelect}
                    />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item
                    className={styles.formItem}
                    label="Стоимость, руб."
                    name="minRideAmount"
                    rules={[
                      {
                        required: true,
                        message: TariffStrings.anyParameterRequired,
                      },
                    ]}
                  >
                    <InputNumber
                      className={styles.input}
                      min={0}
                      max={99999}
                    />
                  </Form.Item>
                </Col>
                <Col span={9}>
                  <Form.Item
                    name="freeWaitingTime"
                    className={styles.formItem}
                    label="Бесплатное время ожидания, мин."
                    rules={[
                      {
                        required: true,
                        message: TariffStrings.anyParameterRequired,
                      },
                    ]}
                  >
                    <InputNumber
                      min={0}
                      step={1}
                      max={999}
                      className={styles.input}
                    />
                  </Form.Item>
                </Col>
              </Row>
            </div>
          </Col>
        </Row>

        <Divider />

        <div className={styles.formBlock}>
          <span className={styles.formBlockTitle}>
            {TariffStrings.tariffExtendedData}
          </span>
          <Row gutter={16}>
            <Col flex="auto">
              <Form.Item
                label="1 км в городе, руб."
                className={styles.formItem}
                name="transferCostPerKmCity"
              >
                <InputNumber
                  className={styles.input}
                  min={0}
                  max={99999}
                />
              </Form.Item>
            </Col>
            <Col flex="auto">
              <Form.Item
                label="1 мин. в городе, руб."
                className={styles.formItem}
                name="transferCostPerMinCity"
              >
                <InputNumber
                  className={styles.input}
                  min={0}
                  max={99999}
                />
              </Form.Item>
            </Col>
            <Col flex="auto">
              <Form.Item
                label="1 км. за городом, руб."
                className={styles.formItem}
                name="transferCostPerKmSuburb"
              >
                <InputNumber
                  className={styles.input}
                  min={0}
                  max={99999}
                />
              </Form.Item>
            </Col>
            <Col flex="auto">
              <Form.Item
                label="1 мин. за городом, руб."
                name="transferCostPerMinSuburb"
                className={styles.formItem}
              >
                <InputNumber
                  className={styles.input}
                  min={0}
                  max={99999}
                />
              </Form.Item>
            </Col>
            <Col flex="auto">
              <Form.Item
                label="1 минута ожидания, руб."
                className={styles.formItem}
                name="transferWaitCostPerMin"
                rules={[
                  {
                    required: true,
                    message: TariffStrings.anyParameterRequired,
                  },
                ]}
              >
                <InputNumber
                  className={styles.input}
                  min={0}
                  max={99999}
                />
              </Form.Item>
            </Col>
          </Row>
        </div>
        <Divider />
        <div className={styles.formBlock}>
          <span className={styles.formBlockTitle}>
            {TariffStrings.tariffConditions}
          </span>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                className={styles.formItem}
                label="Мин. время для формирования  заказа, мин."
                name="minOrderCreationTimeMin"
                rules={[
                  {
                    required: true,
                    message: TariffStrings.anyParameterRequired,
                  },
                ]}
              >
                <InputNumber
                  min={0}
                  max={99999}
                  className={styles.input}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                className={styles.formItem}
                label="Минимальное время отмены заявки, мин."
                name="minOrderCancelationTimeMin"
                rules={[
                  {
                    required: true,
                    message: TariffStrings.anyParameterRequired,
                  },
                ]}
              >
                <InputNumber
                  min={0}
                  max={999}
                  className={styles.input}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                className={styles.formItem}
                label="Триггерное время, мин."
                name="triggerTime"
                rules={[
                  {
                    required: true,
                    message: TariffStrings.anyParameterRequired,
                  },
                ]}
              >
                <InputNumber
                  min={0}
                  max={250000}
                  className={styles.input}
                />
              </Form.Item>
            </Col>
          </Row>

        </div>
      </div>
    </div>
  );
};
