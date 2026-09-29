import React, { FC, useCallback, useEffect, useState } from 'react';
import { Modal } from 'shared/components/Modal/Modal';
import { UUID } from 'utils/io-ts';
import moment from 'moment';
import { Col, Form, Row, Select, Spin } from 'antd';
import { TariffStrings } from 'modules/NewTariffs/constants/Tariffs.constants';
import { ValidationRules } from 'shared/fieldValidationRules';
import { preventDefault } from 'utils';
import { useGetAvailableTransportTypes, useTransportServiceTypesCargo } from 'api/transport-types';
import { useContractsInput } from 'modules/NewTariffs/hooks/useContractsInput';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import AccessControl from 'shared/components/AccessControl';
import { useRole } from 'utils/useRole';
import { Roles } from 'constants/constants.app';
import { searchSymbol } from 'utils/searchSymbol';
import { SelectOption } from 'modules/TripPurposes/types/types';
import { useProfile } from 'api/profile';
import { getFormRule } from 'modules/NewTariffs/utils/utils';
import { useContractors } from 'api/contractors';
import { useGetListRegions } from 'api/tariffs';
import { CargoDedicatedTabPane } from 'modules/NewTariffs/components/InnerTariffTabs/CargoDedicatedTabPane';
import { CargoImportExportTabPane } from 'modules/NewTariffs/components/InnerTariffTabs/CargoImportExportTabPane';
import { importExportEndpointMap } from 'modules/UploadButton';
import { useOrganizationProjection } from 'api/organizations/search';
import { useContractsInput as useContractsInputCargo } from 'modules/NewTariffs/hooks/useContractsInput';
import { useTranslation } from 'i18n';
import { useDebounce, useDebounceEffect } from 'shared/hooks/useDebounce';
import { useTariff } from '../../hooks/useTariffs';
import { useModalForm } from './useModalForm';
import { useModal } from '../../context/modal.context';
import { ReactComponent as Icon } from 'shared/form/SelectMultiple/images/selectArrow.svg';

import styles from './style.module.scss';

const CARGO_TRANSPORT_TYPES = ['INTERREGIONAL', 'DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INDIVIDUAL'];

export const AddEditModal: FC = () => {
  const { t } = useTranslation();

  const { modalState, closeModal } = useModal();

  const id = modalState.type === 'edit' ? modalState.id : undefined;
  const transportType = modalState.type === 'edit' ? modalState.transportType : undefined;
  const { data: tariff, isLoading } = useTariff(id as UUID, transportType?.toLowerCase() as string, false);
  const [isRequestInProgress, setRequestInProgress] = useState(false);

  const {
    form, // форма
    saveForm, // функция для сохранения формы (создание, редактирование)
    initialValues, // дефолтные значения
    transportValue,
    setTransportValue,
    selectedOrganizationId,
    setSelectedOrganizationId,
    setDepartmentsSearchString,
    transportTypeDisabled,
    setTransportTypeDisabled,
    contractorIdDisabled,
    setContractorIdDisabled,
    departmentsByNameSubstringOptions,
  } = useModalForm(tariff);

  const { required } = ValidationRules.general;

  // ВСЯ ЛОГИКА НИЖЕ ВЗЯТА СО СТАРОГО ДИЗАЙНА, ОТРЕФАКТОРИТЬ

  /**
   * После выбора услуги подтягиваем данные в поле Вид транспорта, обнуляем текущее выбранное значение и разблокируем его
   */
  const onChangeTransportServiceType = () => {
    // setTransportServiceTypeId(e); // нигде не используется, хз, зачем надо было
    setTransportValue('');
    setTransportTypeDisabled(false);
    form.resetFields(['transportType']);
  };

  const transportServiceTypes
    = useTransportServiceTypesCargo({ suspense: false })
      .data?.filter(({ name }) => name === 'CARGO_TRANSPORTATION')
      ?.map(({ rusName: label, name: value }) => ({
        label,
        value,
      })) ?? [];

  const {
    contractsData,
   } = useContractsInput(modalState.type === 'edit', {
    contractor: initialValues?.contractorId,
    serviceType: initialValues?.serviceType,
    organization: initialValues?.organizationId,
    transportType: initialValues?.transportType,
    regionId: null,
  });

  const {
    contractOptions: contractOptionsCargo, onSelect: onSelectCargo,
  } = useContractsInputCargo(modalState.type === 'edit', {
      contractor: initialValues?.contractorId,
      serviceType: initialValues?.serviceType,
      organization: initialValues?.organizationId,
      transportType: initialValues?.transportType,
      regionId: initialValues?.regionId && initialValues?.regionId[0],
    }
  );

  const organizationsOptions
    = useOrganizationProjection({}, { suspense: false }).data?.map(({ officialName: label, id: value }) => ({
      label,
      value,
    })) ?? [];

  const handleOnChangeOrganization = (e: string) => {
    setSelectedOrganizationId(e);
  };

  const [depSearchVal, setDepSearchVal] = useState('');

  const handleSearchDepartment = (val: string): void => {
    setDepSearchVal(val);
  };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  const searchDep = useCallback(() => setDepartmentsSearchString(depSearchVal), [depSearchVal]);

  useDebounceEffect(searchDep, 300);

  const roles = useRole();
  const isCurrentUserHasEditAccess = roles.some(currentUserRole => currentUserRole === Roles.ADMIN_DATA_MASTER);
  const isDepartmentFieldDisabledForEdit = modalState.type === 'edit' && !isCurrentUserHasEditAccess;

  const { organizationId } = useProfile().data;

  const transportTypesOptions: SelectOption[]
    = useGetAvailableTransportTypes(organizationId, { suspense: false })
      .data?.filter(({ name }) => CARGO_TRANSPORT_TYPES.includes(name))
      ?.map(({ rusName: label, name: value }) => ({
        label,
        value,
      })) ?? [];

  const contractorsOptions =
    useContractors({ config: { suspense: false } }).data?.contractors?.map(({ name: label, id: value }) => ({
      label,
      value,
    })) ?? [];

  const clear = () => form.resetFields(['contractId']);

  const regionsOptions
    = useGetListRegions({ suspense: false }).data?.map(({ name: label, id: value }) => ({ label, value })) ?? [];

    const cargoIdUrl = modalState.type === 'edit' ? modalState.id : 'empty';

  useEffect(() => {
    if (modalState.type === null) {
      setTransportValue('');
      setSelectedOrganizationId('');
      setDepSearchVal('');
      setTransportTypeDisabled(true);
      setContractorIdDisabled(true);
    }
  }, [
    modalState.type,
    setContractorIdDisabled,
    setDepSearchVal,
    setSelectedOrganizationId,
    setTransportTypeDisabled,
    setTransportValue,
  ]);

  useEffect(() => {
    if (transportValue !== TransportTypes.DEDICATED && transportValue !== TransportTypes.INDIVIDUAL) {
      return;
    }
    if (organizationId !== selectedOrganizationId) {
      setSelectedOrganizationId(organizationId);
      onSelectCargo('organization')(organizationId);
    }
      // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [transportValue, organizationId]);

  useEffect(() => {
    if (contractOptionsCargo.length !== 1) {
      return;
    }
    form.setFieldsValue({ contractId: contractOptionsCargo[0]?.value });
  }, [contractOptionsCargo]);

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

  const resetRequestInProgress = useDebounce(() => {
    setRequestInProgress(false);
  }, 1500);

  const handleOk = () => {
    if (isRequestInProgress) return;
    setRequestInProgress(true);
    form.submit();
    resetRequestInProgress()
  };

  return (
    <Modal
      visible={modalState.type === 'add' || modalState.type === 'edit'}
      onCancel={() => {
        setRequestInProgress(false);
        closeModal();
      }}
      onOk={handleOk}
      width="90vw"
      okText={modalState.type === 'add' ? t.global.create : t.global.save}
      title={modalState.type === 'add' ? 'Создание тарифа' : 'Редактирование тарифа'}
      okButtonProps={{
        style: {
          display: transportValue === TransportTypes.COURIER || transportValue === TransportTypes.INTERREGIONAL || transportValue === TransportTypes.DOMESTIC_COURIER
            ? 'none'
            : '',
        },
      }}
    >
      <Form
        form={form}
        initialValues={initialValues}
        onFinish={saveForm}
        >
        <Spin spinning={isLoading}>
          {/* ВСЯ ФОРМА ВЗЯТА СО СТАРОГО ДИЗАЙНА, ОТРЕФАКТОРИТЬ - частично порефакторено март-2024 */}
          <Row>
            <Col span={12}>
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
                  onChange={onChangeTransportServiceType}
                  options={transportServiceTypes}
                  onSelect={onSelectCargo('serviceType')}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
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
                  onChange={(e) => setTransportValue(e)}
                  options={transportTypesOptions}
                  onSelect={onSelectCargo('transportType')}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row>
            <Col span={24}>
              {(transportValue === TransportTypes.TAXI ||
                transportValue === TransportTypes.PERSONAL ||
                transportValue === TransportTypes.PUBLIC ||
                transportValue === TransportTypes.CARSHARING ||
                transportValue === TransportTypes.WALK ||
                transportValue === TransportTypes.BICYCLE ||
                transportValue === TransportTypes.SCOOTER) && (
                <Form.Item
                  label={TariffStrings.organizationId}
                  name="organizationId"
                  rules={[required]}
                  className={styles.formItem}
                >
                  <Select
                    onInputKeyDown={preventDefault}
                    disabled={modalState.type === 'edit'}
                    allowClear
                    options={organizationsOptions}
                    onChange={handleOnChangeOrganization}
                    onSelect={onSelectCargo('organization')}
                  />
                </Form.Item>
              )}
            </Col>
          </Row>

          <AccessControl
            userPermissions={roles}
            allowedPermissions={[Roles.ADMIN_CORP_CLIENT, Roles.ADMIN_DATA_MASTER]}
          >
            <Row>
              <Col span={24}>
                {(transportValue === TransportTypes.TAXI) &&
                  (selectedOrganizationId || initialValues?.organizationId) && (
                    <Form.Item label={TariffStrings.department} name="departmentId" className={styles.formItem}>
                      <Select
                        onInputKeyDown={preventDefault}
                        allowClear
                        onSearch={handleSearchDepartment}
                        showSearch
                        optionFilterProp="label"
                        filterOption={searchSymbol}
                        disabled={isDepartmentFieldDisabledForEdit}
                        options={departmentsByNameSubstringOptions}
                        onSelect={onSelectCargo('departmentId')}
                      />
                    </Form.Item>
                  )}
              </Col>
            </Row>
          </AccessControl>

          {(transportValue === TransportTypes.DEDICATED ||
            transportValue === TransportTypes.INDIVIDUAL ||
            transportValue === TransportTypes.TAXI ||
            transportValue === TransportTypes.CARSHARING) && (
            <Row>
              <Col span={12}>
                <Form.Item
                  label={TariffStrings.contractorId}
                  name="contractorId"
                  rules={getFormRule(true, TariffStrings.contractorRequired)}
                  className={styles.formItem}
                >
                  <Select
                    showSearch
                    suffixIcon={<Icon />}
                    optionFilterProp={'label'}
                    filterOption={searchSymbol}
                    onInputKeyDown={preventDefault}
                    disabled={modalState.type === 'edit'}
                    allowClear
                    options={contractorsOptions}
                    onSelect={onSelectCargo('contractor')}
                    onChange={clear}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label={TariffStrings.contractNumber}
                  name="contractId"
                  rules={getFormRule(true, TariffStrings.contractRequired)}
                  className={styles.formItem}
                >
                  <Select
                    onInputKeyDown={preventDefault}
                    disabled={contractOptionsCargo.length === 0 || contractOptionsCargo.length === 1}
                    allowClear
                    options={contractOptionsCargo}
                    onChange={onContractIdChange}
                  />
                </Form.Item>
              </Col>
            </Row>
          )}

          {(transportValue === TransportTypes.TAXI ||
            transportValue === TransportTypes.PERSONAL ||
            transportValue === TransportTypes.PUBLIC ||
            transportValue === TransportTypes.CARSHARING ||
            transportValue === TransportTypes.WALK ||
            transportValue === TransportTypes.BICYCLE ||
            transportValue === TransportTypes.SCOOTER ||
            transportValue === TransportTypes.DEDICATED ||
            transportValue === TransportTypes.INDIVIDUAL) && (
            <Form.Item label={TariffStrings.region} rules={[required]} name="regionId" className={styles.formItem}>
              <Select
                mode="multiple"
                onInputKeyDown={preventDefault}
                allowClear
                showSearch
                optionFilterProp="label"
                filterOption={searchSymbol}
                options={regionsOptions}
                onSelect={onSelectCargo('regionId')}
                onChange={clear}
              />
            </Form.Item>
          )}

          {(transportValue === TransportTypes.DEDICATED || transportValue === TransportTypes.INDIVIDUAL) && (
            <CargoDedicatedTabPane
              calculationType={initialValues.calculationType}
              autoPlanning={initialValues?.autoPlanning}
              driverLoader={initialValues?.driverLoader}
              contractId={initialValues?.contractId}
              contractorId={initialValues?.contractorId}
              disabledFields={transportValue === TransportTypes.INDIVIDUAL ? ['express', 'autoPlanning'] : ['express']}
              transportType={transportValue}
            />
          )}

          {transportValue === TransportTypes.COURIER && (
            <CargoImportExportTabPane
              entity={TransportTypes.COURIER.toLowerCase()}
              downloadUrl={`${
                importExportEndpointMap.courier
              }/files/${TransportTypes.COURIER.toLowerCase()}/?tariffId=${cargoIdUrl}`}
              useUploadEntity="courier"
              isEditable={modalState.type === 'edit'} 
            />
          )}

          {transportValue === TransportTypes.INTERREGIONAL && (
            <CargoImportExportTabPane
              entity={TransportTypes.INTERREGIONAL.toLowerCase()}
              downloadUrl={`${
                importExportEndpointMap.interregional
              }/files/${TransportTypes.INTERREGIONAL.toLowerCase()}/?tariffId=${cargoIdUrl}`}
              useUploadEntity="interregional"
              isEditable={modalState.type === 'edit'} 
            />
          )}

          {transportValue === TransportTypes.DOMESTIC_COURIER && (
            <CargoImportExportTabPane
              entity="domestic_courier"
              downloadUrl={`${
                importExportEndpointMap.domestic_courier
              }/files/${TransportTypes.DOMESTIC_COURIER.toLowerCase()}/?tariffId=${cargoIdUrl}`}
              useUploadEntity="domestic_courier"
              isEditable={modalState.type === 'edit'} 
            />
          )}
        </Spin>
      </Form>
    </Modal>
  );
};
