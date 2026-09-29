import React, {
  FC, useEffect, useMemo, useState
} from 'react';
import { useParams } from 'react-router-dom';
import { Modal } from 'shared/components/Modal/Modal';
import { UUID } from 'utils/io-ts';
import moment from 'moment';
import cn from 'classnames';
import {
  Button,
  Col, Form, Row, Spin
} from 'antd';
import { SelectValue } from 'antd/lib/select';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile';
import { useContractors } from 'api/contractors';
import { useGetListRegions, useTariffTransport } from 'api/tariffs';
import { useOrganizationProjection } from 'api/organizations/search';
import { useGroupTransferTransportClasses } from 'api/passengers-transport-classes';
import { useGetAvailableTransportTypes, useTransportServiceTypes } from 'api/transport-types';

import { sTransport, TariffTypes } from 'constants/constants.app';
import { TariffStrings, taxiClassOptions } from 'modules/NewTariffs/constants/Tariffs.constants';
import { useContractsInput } from 'modules/NewTariffs/hooks/useContractsInput';
import { getFormRule } from 'modules/NewTariffs/utils/utils';
import { TaxiTariffTabPane } from 'modules/NewTariffs/components/InnerTariffTabs/TaxiTariffTabPane';
import { PersonalTariffTabPane } from 'modules/NewTariffs/components/InnerTariffTabs/PersonalTariffTabPane';
import { CarsharingTabPane } from 'modules/NewTariffs/components/InnerTariffTabs/CarsharingTabPane';
import { BicycleTabPane } from 'modules/NewTariffs/components/InnerTariffTabs/BicycleTabPane';
import { PublicTransportTabPane } from 'modules/NewTariffs/components/InnerTariffTabs/PublicTransportTabPane';
import { GroupTransferTabPane } from 'modules/NewTariffs/components/InnerTariffTabs/GroupTransferTabPane';

import { SelectOption } from 'modules/TripPurposes/types/types';
import { ServiceTypeEnum } from 'modules/Departments/DelegatesPage/constants/EmployeeApp.constants';

import { ValidationRules } from 'shared/fieldValidationRules';
import FormField from 'shared/form/FormField/FormField';
import { FieldType } from 'shared/form/Field/Field';
import { Option, Select } from 'shared/components/Select';
import { FormItem } from 'shared/components/FormItem';

import { preventDefault } from 'utils';
import { searchSymbol } from 'utils/searchSymbol';

import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { TariffTransportClasses } from 'stores/TransportClasses/TransportClasses.interface';
import { TariffTransportParams } from 'stores/Tariffs/Tariffs.interface';

import { useTariff } from '../../hooks/useTariffs';
import { useContract } from '../../hooks/useContract';
import { useModalForm } from './useModalForm';
import { useModal } from '../../context/modal.context';
import { DoubleTransportTariffForm } from '../DoubleTransportForm/DoubleTransportTariffForm';
import { formatTariffText } from '../../utils/formatTariffText';
import { createTransportLabel } from '../../utils/utils';

import styles from './style.module.scss';

const CARGO_TRANSPORT_TYPES = ['INTERREGIONAL', 'DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INDIVIDUAL'];

export const AddEditModal: FC = () => {
  const { t } = useTranslation();

  const { modalState, closeModal } = useModal();
  const [transportServiceType, setTransportServiceType] = useState('');

  const id = modalState.type === 'edit' ? modalState.id : undefined;
  const transportType = modalState.type === 'edit' ? modalState.transportType : undefined;
  const { data: tariff, isLoading } = useTariff(id as UUID, transportType?.toLowerCase() as string, false);
  const { data: regions } = useGetListRegions({ suspense: false });
  const { data: contract } = useContract(tariff?.contractId as UUID, false);

  const { tariffType } = useParams<{ tariffType: TariffTypes }>();

  const {
    form, // форма
    saveForm, // функция для сохранения формы (создание, редактирование)
    initialValues, // дефолтные значения
    transportValue,
    setTransportValue,
    selectedOrganizationId,
    setSelectedOrganizationId,
    groupTransferClass,
    setGroupTransferClass,
    transportTypeDisabled,
    setTransportTypeDisabled,
  } = useModalForm(tariff);

  const { required } = ValidationRules.general;
  const [transferClass, setTransferClass] = useState<string>('');
  const [tariffCarStep, setTariffCarStep] = useState(1);
  const groupTransferClassOptions
    = useGroupTransferTransportClasses()
      .data.map(({ rusName: label, value }) => ({
        label,
        value,
      }));

  // ВСЯ ЛОГИКА НИЖЕ ВЗЯТА СО СТАРОГО ДИЗАЙНА, ОТРЕФАКТОРИТЬ

  /**
   * После выбора услуги подтягиваем данные в поле Вид транспорта, обнуляем текущее выбранное значение и разблокируем его
   * @param e событие смены Услуги
   */

  const handleStepBack = () => {
    setTariffCarStep(1);
  };

  const handleOnSave = (data: unknown) => {
    saveForm(data);
  };

  const onChangeTransportServiceType = (value: SelectValue) => {
    setTransportServiceType(value as string);
    setTransportValue('');
    setTransportTypeDisabled(false);
    form.resetFields(['transportType', 'groupTransferClass']);
  };

  const transportServiceTypes
    = useTransportServiceTypes({ suspense: false })
      .data?.filter(({ name }) => name === 'EMPLOYEE_TRANSPORTATION')
      ?.map(({ rusName: label, name: value }) => ({
        label,
        value,
      })) ?? [];

  const [regionIds, setRegionIds] = useState<string[]>(form.getFieldValue('regionId') ?? []);

  const {
    contractOptions, onSelect, contractsData, isContractLoading,
  } = useContractsInput(modalState.type === 'edit', {
    contractor: initialValues?.contractorId,
    serviceType: initialValues?.serviceType,
    organization: tariffType === TariffTypes.INCOME ? initialValues?.organizationId : undefined,
    transportType: initialValues?.transportType,
    regionId: initialValues.regionId?.length ? initialValues.regionId[0] : null,
  });

  const organizationsOptions
    = useOrganizationProjection({}, { suspense: false }).data?.map(({ officialName: label, id: value }) => ({
      label,
      value,
    })) ?? [];

  const initialRegionIds = useMemo(() => [...initialValues?.regionId || []], [initialValues?.regionId]);

  const handleOnChangeOrganization = (e: string) => {
    setSelectedOrganizationId(e);
  };

  const { organizationId } = useProfile().data;

  const transportTypesOptions: SelectOption[]
    = useGetAvailableTransportTypes(organizationId, { suspense: false })
      .data?.filter(({ name }) => !CARGO_TRANSPORT_TYPES.includes(name))
      .filter(({ name }) => tariffType ? name === TransportTypes.TAXI : true)
      ?.map(({ rusName: label, name: value }) => ({
        label,
        value,
      })) ?? [];

  const contractorsOptions
    = useContractors({ config: { suspense: false } }).data?.contractors?.map(({ name: label, id: value }) => ({
      label,
      value,
    })) ?? [];

  const clear = () => form.resetFields(['contractId']);

  const regionsOptions = useMemo(
    () => regions
      ?.filter(region => modalState.type === 'edit'
        ? contract?.regionIds?.includes(region.id) || tariff?.regionId?.includes(region.id)
        : true
      )
      .map(({ name: label, id: value }) => ({ label, value })) ?? [],
    [regions, contract, tariff, modalState.type]
  );

  const regionOptionsWithFixed = useMemo(() => {
    return regionsOptions?.length
      ? regionsOptions.map(item => ({ ...item, disabled: initialRegionIds.includes(item.value) }))
      : [];
  }, [regionsOptions, initialRegionIds]);

  useEffect(() => {
    if (modalState.type === null) {
      setTransportValue('');
      setGroupTransferClass('');
      setSelectedOrganizationId('');
      setRegionIds([]);
      setTransportTypeDisabled(true);
      setTariffCarStep(1);
      setTransferClass('');
    }
  }, [
    modalState.type,
    setGroupTransferClass,
    setSelectedOrganizationId,
    setTransportTypeDisabled,
    setTransportValue,
    setTariffCarStep,
    setTransferClass,
  ]);

  const resetFormOnStepBack = () => {
    setTransportValue('');
    setGroupTransferClass('');
    setSelectedOrganizationId('');
    setRegionIds([]);
    setTransportTypeDisabled(true);
    setTariffCarStep(1);
    setTransferClass('');

    form.resetFields();
  };

  useEffect(() => {
    if (transportValue !== TransportTypes.DEDICATED && transportValue !== TransportTypes.INDIVIDUAL) {
      return;
    }
    if (organizationId !== selectedOrganizationId) {
      setSelectedOrganizationId(organizationId);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [transportValue, organizationId]);

  const onContractIdChange = () => {
    const contractId = form.getFieldValue('contractId');
    const contract = contractsData.find(item => item.id === contractId);
    if (contract) {
      form.setFieldsValue({
        tariffDateRange: [
          moment(contract.startDate),
          moment(contract.endDate),
        ],
      });
    }
  };

  const isCarTariffServiceType = transportServiceType === ServiceTypeEnum.EMPLOYEE_TRANSPORTATION;
  const isCarTariffTransportType = transportValue === TransportTypes.GROUP_TRANSFER;

  const isTariffWithCarsModal = Boolean(isCarTariffServiceType && isCarTariffTransportType && transferClass && modalState.type === 'add');

  const isSecondStepForm = isTariffWithCarsModal && tariffCarStep === 2;
  const cancelButtonTitle = isSecondStepForm ? 'Назад' : 'Отменить';

  const handleMoveNextStep = () => {
    form.validateFields(['contractId', 'contractorId', 'groupTransferClass', 'organizationId', 'regionIds', 'serviceType', 'tariffDateRange', 'transportType'])
      .then(() => setTariffCarStep(prev => prev + 1));
  };

  const isTransferCarChoiceEditForm = useMemo(() => {
    return initialValues.serviceType === ServiceTypeEnum.EMPLOYEE_TRANSPORTATION
      && transportType === TransportTypes.GROUP_TRANSFER
      && groupTransferClass === TariffTransportClasses.TRANSFER_CAR_CHOICE && modalState.type === 'edit';
  },
  [initialValues.serviceType, transportType, groupTransferClass, modalState]);

  const isFormEditDisabled = useMemo(() => isTransferCarChoiceEditForm
    || (transferClass === TariffTransportClasses.TRANSFER_CAR_CHOICE && modalState.type === 'edit'),
  [
    modalState,
    transferClass,
    isTransferCarChoiceEditForm,
  ]);

  const tariffTransportParams = {
    tariffStartDate: String(initialValues.tariffStartDate),
    tariffEndDate: String(initialValues.tariffEndDate),
    contractorId: initialValues.contractorId,
    organizationId: organizationId,
    regionIds: initialValues.regionId?.join(','),
    page: 0,
    size: 300,
  } as TariffTransportParams;

  const {
    data: tariffTransportResponse, isLoading: isTariffTransportLoading,
  }
  = useTariffTransport(tariffTransportParams, { enabled: isFormEditDisabled, suspense: false });

  const dependantRegionOptions = isFormEditDisabled ? regionOptionsWithFixed : regionsOptions;

  const chosenCarsOptions = useMemo(() => {
    return tariffTransportResponse?.content?.length ? tariffTransportResponse.content
      .filter(item => item?.tariffId === tariff.id)
      .map(item => ({ label: createTransportLabel(item.brand, item.model, item.stateNumber), value: item.id })) : [];
  }, [tariffTransportResponse, tariff]);

  const createModalTitle = () => {
    if (initialValues.groupTransferClass && modalState.type === 'edit') {
      return `Редактирование тарифа${tariff.humanReadableId ? ' ' + tariff.humanReadableId : ''}`;
    }

    return modalState.type === 'add' ? 'Создание тарифа' : 'Редактирование тарифа';
  };

  const initialContractIdOptions
= (contractOptions.length === 0 || isFormEditDisabled) && initialValues?.contractNumber
  ? [{ value: initialValues.contractId, label: initialValues!.contractNumber }] as SelectOption[]
  : contractOptions;

  const ModalFooter = () => {
    return (
      <footer>
        <div className={styles.footerContainer}>
          {isTariffWithCarsModal && (
            <div className={styles.stepContainer}>
              Шаг
              {' '}
              {tariffCarStep}
              {' '}
              / 2
            </div>
          )}
          <div className={styles.buttonBlock}>
            {isTariffWithCarsModal && tariffCarStep === 1 ? (
              <>
                <Button
                  className={cn(styles.actionButton, styles.cancelButton)}
                  onClick={resetFormOnStepBack}
                >
                  Назад
                </Button>
                <Button
                  onClick={handleMoveNextStep}
                  className={styles.actionButton}
                  type="primary"
                >
                  Далее
                </Button>
              </>
            ) : (
              <>
                <Button
                  onClick={isSecondStepForm ? handleStepBack : closeModal}
                  className={cn({
                    [styles.actionButton]: isTariffWithCarsModal,
                    [styles.cancelButton]: isTariffWithCarsModal,
                  })}
                >
                  {cancelButtonTitle}
                </Button>
                <Button
                  className={cn({ [styles.actionButton]: isTariffWithCarsModal })}
                  type="primary"
                  onClick={form.submit}
                >
                  {modalState.type === 'add' ? t.global.create : t.global.save}
                </Button>
              </>
            )}
          </div>
        </div>
      </footer>
    );
  };

  return (
    <Modal
      visible={modalState.type === 'add' || modalState.type === 'edit'}
      onCancel={closeModal}
      onOk={form.submit}
      width="90vw"
      okText={modalState.type === 'add' ? t.global.create : t.global.save}
      title={createModalTitle()}
      className={cn({ [styles.newTariffForm]: isTariffWithCarsModal })}
      footer={<ModalFooter />}
    >
      <Form
        form={form}
        initialValues={initialValues}
        onFinish={handleOnSave}
      >
        <Spin spinning={isLoading}>
          { isTariffWithCarsModal ? (
            <DoubleTransportTariffForm
              form={form}
              formStep={tariffCarStep}
              cargoTransportType={CARGO_TRANSPORT_TYPES}
              initialValues={initialValues}
              transportType={transportType}
              serviceType={transportServiceType}
              specificServiceType={transferClass}
              contractOptions={contractOptions}
              onSelect={onSelect}
              onChangeTransportServiceType={onChangeTransportServiceType}
              setTransferClass={setTransferClass}
            />
          ) : (
            <>
              <Row>
                <Col span={8}>
                  <Form.Item
                    label={TariffStrings.serviceType}
                    name="serviceType"
                    rules={[required]}
                    className={styles.formItem}
                  >
                    <Select
                      onInputKeyDown={preventDefault}
                      disabled={modalState.type === 'edit'}
                      allowClear
                      value={transportServiceType}
                      onChange={onChangeTransportServiceType}
                      options={transportServiceTypes}
                      // eslint-disable-next-line @typescript-eslint/no-explicit-any
                      onSelect={onSelect('serviceType') as any}
                    />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    label={TariffStrings.transportType}
                    name="transportType"
                    rules={[required]}
                    className={styles.formItem}
                  >
                    <Select
                      onInputKeyDown={preventDefault}
                      disabled={modalState.type === 'edit' || transportTypeDisabled}
                      allowClear
                      value={transportValue}
                      onChange={e => setTransportValue(String(e))}
                      options={transportTypesOptions}
                      // eslint-disable-next-line @typescript-eslint/no-explicit-any
                      onSelect={onSelect('transportType') as any}
                    />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  {transportValue === TransportTypes.TAXI && (
                  <Form.Item
                    label={TariffStrings.taxiClass}
                    name="taxiClass"
                    className={styles.formItem}
                    rules={getFormRule(true, TariffStrings.taxiClassRequired)}
                  >
                    <Select
                      onInputKeyDown={preventDefault}
                      disabled={modalState.type === 'edit'}
                      allowClear
                      options={taxiClassOptions}
                    />
                  </Form.Item>
                  )}

                  {transportValue === TransportTypes.GROUP_TRANSFER && (
                  <div className={styles.groupTransferWrapper}>
                    <Form.Item
                      label={TariffStrings.specificServiceType}
                      name="groupTransferClass"
                      rules={getFormRule(true, TariffStrings.specificServiceType)}
                      className={styles.formItem}
                    >
                      <Select
                        onInputKeyDown={preventDefault}
                        allowClear
                        options={groupTransferClassOptions}
                        onChange={clear}
                        disabled={isFormEditDisabled}
                        // eslint-disable-next-line @typescript-eslint/no-explicit-any
                        onSelect={(value: any) => { setTransferClass(value); }}
                      />
                    </Form.Item>

                    {transferClass && transferClass !== 'TRANSFER' && (
                    <FormField
                      className={styles.vipCheckbox}
                      type={FieldType.checkbox}
                      name="isVip"
                      initialValue={false}
                      label="VIP"
                    />
                    )}
                  </div>
                  )}
                </Col>
              </Row>

              {isTransferCarChoiceEditForm && (
                <Row>
                  <Col span={24}>
                    <Form.Item
                      name="transportIds"
                      label={TariffStrings.tariffChosenTransport}
                      className={styles.formItem}
                    >
                      <Select
                        disabled
                        loading={isTariffTransportLoading}
                        mode="multiple"
                        options={chosenCarsOptions}
                      />
                    </Form.Item>
                  </Col>
                </Row>
              )}

              <Row>
                <Col span={24}>
                  {(transportValue === TransportTypes.TAXI
                  || transportValue === TransportTypes.PERSONAL
                  || transportValue === TransportTypes.PUBLIC
                  || transportValue === TransportTypes.CARSHARING
                  || transportValue === TransportTypes.WALK
                  || transportValue === TransportTypes.BICYCLE
                  || transportValue === TransportTypes.SCOOTER
                  || transportValue === TransportTypes.GROUP_TRANSFER) && (
                  <Form.Item
                    label={formatTariffText(tariffType)`${{ INCOME: 'Организация-клиент', OUTCOME: 'Агрегатор' }}`}
                    name="organizationId"
                    rules={tariffType === TariffTypes.INCOME ? [required] : []}
                    className={styles.formItem}
                  >
                    {tariffType === TariffTypes.OUTCOME ? sTransport : (
                      <Select
                        onInputKeyDown={preventDefault}
                        disabled={modalState.type === 'edit'}
                        allowClear
                        showSearch
                        filterOption={searchSymbol}
                        options={organizationsOptions}
                        // eslint-disable-next-line @typescript-eslint/no-explicit-any
                        onChange={handleOnChangeOrganization as any}
                        // eslint-disable-next-line @typescript-eslint/no-explicit-any
                        onSelect={onSelect('organization') as any}
                      />
                    )}
                  </Form.Item>
                  )}
                </Col>
              </Row>

              {(transportValue === TransportTypes.DEDICATED
              || transportValue === TransportTypes.INDIVIDUAL
              || transportValue === TransportTypes.TAXI
              || transportValue === TransportTypes.CARSHARING
              || transportValue === TransportTypes.GROUP_TRANSFER) && (
              <Row>
                <Col span={12}>
                  <Form.Item
                    label={formatTariffText(tariffType)`${{ INCOME: 'Агрегатор', OUTCOME: 'Исполнитель' }}`}
                    name="contractorId"
                    rules={
                      tariffType === TariffTypes.OUTCOME ? getFormRule(true, TariffStrings.contractorRequired) : []
                    }
                    className={styles.formItem}
                  >
                    {tariffType === TariffTypes.INCOME ? 'ООО Транспортные решения' : (
                      <Select
                        onInputKeyDown={preventDefault}
                        disabled={modalState.type === 'edit'}
                        allowClear
                        showSearch
                        filterOption={searchSymbol}
                        options={contractorsOptions}
                        // eslint-disable-next-line @typescript-eslint/no-explicit-any
                        onSelect={onSelect('contractor') as any}
                        onChange={clear}
                      />
                    )}
                  </Form.Item>
                </Col>
              </Row>
              )}

              {(transportValue === TransportTypes.TAXI
              || transportValue === TransportTypes.PERSONAL
              || transportValue === TransportTypes.PUBLIC
              || transportValue === TransportTypes.CARSHARING
              || transportValue === TransportTypes.WALK
              || transportValue === TransportTypes.BICYCLE
              || transportValue === TransportTypes.SCOOTER
              || transportValue === TransportTypes.DEDICATED
              || transportValue === TransportTypes.INDIVIDUAL
              || transportValue === TransportTypes.GROUP_TRANSFER) && (
              <FormItem
                label={TariffStrings.region}
                rules={[required]}
                name="regionId"
                className={styles.formItem}
              >
                <Select
                  onInputKeyDown={preventDefault}
                  allowClear
                  showSearch
                  value={regionIds.length ? regionIds[0] : undefined}
                  optionFilterProp={isFormEditDisabled ? 'children' : 'label'}
                  filterOption={searchSymbol}
                  options={dependantRegionOptions}
                  // eslint-disable-next-line @typescript-eslint/no-explicit-any
                  onSelect={onSelect('regionId') as any}
                  // eslint-disable-next-line @typescript-eslint/no-explicit-any
                  onChange={(value: any) => {
                    setRegionIds(value ? [value] : []);
                    clear();
                  }}
                >
                  {isFormEditDisabled && regionOptionsWithFixed?.map(item => (
                    <Option
                      key={item.value}
                      value={item.value}
                      disabled={item.disabled}
                    >
                      {item.label}
                    </Option>
                  ))}
                </Select>
              </FormItem>
              )}

              {(transportValue === TransportTypes.DEDICATED
              || transportValue === TransportTypes.INDIVIDUAL
              || transportValue === TransportTypes.TAXI
              || transportValue === TransportTypes.CARSHARING
              || transportValue === TransportTypes.GROUP_TRANSFER) && (
                <Form.Item
                  label={t.CustomersTariffs.contractId}
                  name="contractId"
                  rules={getFormRule(true, TariffStrings.contractRequired)}
                  className={styles.formItem}
                >
                  <Select
                    onInputKeyDown={preventDefault}
                    disabled={contractOptions.length === 0 || isFormEditDisabled || modalState.type === 'edit'}
                    loading={isContractLoading}
                    optionLabelProp="label"
                    allowClear
                    options={initialContractIdOptions}
                    onChange={onContractIdChange}
                  />
                </Form.Item>
              )}

              {transportValue === TransportTypes.TAXI && (
              <Row>
                <Col span={12}>
                  <FormField
                    className={styles.nightTariffItem}
                    type={FieldType.checkbox}
                    name="isNightTariff"
                    initialValue={false}
                    label={TariffStrings.isNightTariff}
                  />
                  <p className={styles.nightTariffItem__subTitle}>{TariffStrings.nightTariffSubTitle}</p>
                </Col>
              </Row>
              )}

              {transportValue === TransportTypes.TAXI && (
              <TaxiTariffTabPane
                rideCostPerKmInit={initialValues?.rideCostPerKm}
                rideCostPerMinInit={initialValues?.rideCostPerMin}
                distanceIncludedInit={initialValues?.distanceIncluded}
                timeIncludedInit={initialValues?.timeIncluded}
                minRideDistanceCostInit={initialValues?.minRideDistanceCost}
                minRideTimeCostInit={initialValues?.minRideTimeCost}
              />
              )}

              {transportValue === TransportTypes.PERSONAL && (
              <PersonalTariffTabPane
                rideCostPerKmInit={initialValues?.rideCostPerKm}
                rideCostPerMinInit={initialValues?.rideCostPerMin}
                distanceIncludedInit={initialValues?.distanceIncluded}
                timeIncludedInit={initialValues?.timeIncluded}
                minRideDistanceCostInit={initialValues?.minRideDistanceCost}
                minRideTimeCostInit={initialValues?.minRideTimeCost}
              />
              )}

              {transportValue === TransportTypes.CARSHARING && (
              <CarsharingTabPane
                rideCostPerKmInit={initialValues?.rideCostPerKm}
                rideCostPerMinInit={initialValues?.rideCostPerMin}
              />
              )}

              {(transportValue === TransportTypes.BICYCLE || transportValue === TransportTypes.SCOOTER) && (
              <BicycleTabPane />
              )}

              {transportValue === TransportTypes.PUBLIC && <PublicTransportTabPane />}

              {transportValue === TransportTypes.GROUP_TRANSFER && (
              <GroupTransferTabPane
                initialValues={initialValues}
                isFormEditDisabled={isFormEditDisabled}
              />
              )}
            </>
          )}
          {/* ВСЯ ФОРМА ВЗЯТА СО СТАРОГО ДИЗАЙНА, ОТРЕФАКТОРИТЬ - частично порефакторено май-2024 */}
        </Spin>
      </Form>
    </Modal>
  );
};
