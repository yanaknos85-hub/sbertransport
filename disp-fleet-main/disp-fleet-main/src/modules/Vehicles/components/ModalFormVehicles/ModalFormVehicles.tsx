import React, { FC, useEffect, useState } from 'react';
import {
  Input, Modal, Form, InputNumber, Spin, Popconfirm
} from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { Store } from 'antd/lib/form/interface';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile/profile.api';
import { useCreateVehicle, useUpdateVehicle } from 'api/vehicles/vehicles.api';
import { Vehicle } from 'api/vehicles/vehicles.types';
import { useSelfAutopark } from 'api/contractors/contractors.api';

import { insuranceNumberMask, vehiclePassportMask, vinMask } from 'components/Masks/Masks';
import { StateNumberMask } from 'components/StateNumberMask/StateNumberMask';
import { SelectStateNumberMask } from 'components/SelectStateNumberMask/SelectStateNumberMask';
import { SelectVehicleType } from 'components/SelectVehicleType/SelectVehicleType';
import { Button } from 'components/Button';
import BranchSelect from 'components/BranchSelect/BranchSelect';

import { ignore } from 'utils/utils';
import { UUID } from 'utils/io-ts';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { StateNumberMasks, TripTypes } from 'constants/app.constants';
import { useRoleMap } from 'hooks/useRoleMap';

import { useModalForm } from '../../context/ModalForm';
import { useActiveVehicle } from '../../context/ActiveVehicle';
import { SelectEcoClass } from './SelectEcoClass';
import { SelectInExploitation } from './SelectInExploitation';
import styles from './ModalFormVehicles.module.scss';

const {
  required: requiredField,
  maxLength,
  checkStateNumber,
  checkSemitrailerNumber,
  checkVin,
  floatDecimal,
  checkInsuranceNumber,
  positiveNumbers,
  maxInt,
  validationFloatingNumbers,
  minLength,
  minManufactureYear,
  checkVehiclePassport,
  checkTrimmedField,
} = ValidationRules.general;

export const ModalFormVehicles: FC = () => {
  const { t } = useTranslation();
  const { stateShowModal, handleClose } = useModalForm();
  const { setActiveVehicle } = useActiveVehicle();
  const id = stateShowModal.vehicle?.id as UUID;
  const [form] = useForm();

  const { contractorId, autoparkId } = useProfile().data;

  const { isInternal } = useSelfAutopark().data;

  const [createVehicle, { isLoading: isLoadingCreate }] = useCreateVehicle(contractorId);
  const [updateVehicle, { isLoading: isLoadingUpdate }] = useUpdateVehicle(contractorId, id);

  const { isAdmin, isManager } = useRoleMap();

  const [vehicleType, setVehicleType] = useState(stateShowModal?.vehicle?.vehicleType);

  const isAdd = stateShowModal.type === 'add';
  const isEdit = stateShowModal.type === 'edit';

  const handleSuccess = () => {
    form.submit();
  };

  const handleCancel = () => {
    handleClose();
  };

  const handleSave = (values: Store) => {
    if (isLoadingCreate || isLoadingUpdate) {
      return;
    }

    const hasAdditionalData
      = values.semitrailerNumber || values.volume || values.length || values.width || values.height;

    const newData: Vehicle = {
      ...values,
      inExploitation: !!values.inExploitation,
      autopark: { id: values.autoparkId },
      autoparkId: isAdd ? undefined : stateShowModal.vehicle?.autopark.id,
      model: {
        name: values.name,
        brand: values.brand,
        year: values.manufactureYear,
      },
      stateNumber: values.stateNumber?.replace(/[\s_]/g, ''),
      insuranceNumber: values.insuranceNumber?.replace(/\s/g, ''),
      vin: values.vin?.replace(/-/g, ''),
      vehicleAdditional: hasAdditionalData
        ? {
          semitrailerNumber: values.semitrailerNumber || undefined,
          volume: values.volume || undefined,
          length: values.length || undefined,
          width: values.width || undefined,
          height: values.height || undefined,
        }
        : undefined,
    } as Vehicle;

    if (isAdd) {
      createVehicle(newData)
        .then(newVehicle => {
          setActiveVehicle(newVehicle as Vehicle);
          handleClose();
        })
        .catch(ignore);
      return;
    }

    updateVehicle(newData as Vehicle)
      .then(() => {
        setActiveVehicle({
          ...values,
          ...newData,
          id,
          active: newData.inExploitation,
        } as Vehicle);
        handleClose();
      })
      .catch(ignore);
  };

  useEffect(() => {
    if (!stateShowModal.isOpen) return;

    form.resetFields();
    form.setFieldsValue(
      (isAdd
        ? { autoparkId }
        : {
          ...stateShowModal.vehicle,
          inExploitation: Number(stateShowModal.vehicle?.inExploitation) ?? 0,
          autoparkId: stateShowModal.vehicle?.autopark.id,
          name: stateShowModal.vehicle?.model.name,
          brand: stateShowModal.vehicle?.model.brand,
          manufactureYear: stateShowModal.vehicle?.model.year,
          semitrailerNumber: stateShowModal.vehicle?.vehicleAdditional?.semitrailerNumber,
          volume: stateShowModal.vehicle?.vehicleAdditional?.volume,
          length: stateShowModal.vehicle?.vehicleAdditional?.length,
          width: stateShowModal.vehicle?.vehicleAdditional?.width,
          height: stateShowModal.vehicle?.vehicleAdditional?.height,
        }) as Store
    );

    setVehicleType(isAdd ? undefined : stateShowModal.vehicle?.vehicleType);
  }, [stateShowModal, isAdd, form, autoparkId]);

  return (
    <Modal
      title={t.Registry.Modal[stateShowModal.type]}
      className={styles.modal}
      visible={stateShowModal.isOpen && (isAdd || isEdit)}
      onCancel={handleCancel}
      onOk={handleSuccess}
      afterClose={form.resetFields}
      footer={null}
    >
      <Spin spinning={isLoadingCreate || isLoadingUpdate}>
        <Form
          className={styles.filtersForm}
          form={form}
          onFinish={handleSave}
        >
          <Form.Item
            className={styles.formItem}
            label={t.Vehicles.type}
            rules={[requiredField]}
            name="vehicleType"
          >
            <SelectVehicleType
              className={styles.select}
              placeholder={t.Vehicles.type}
              value={vehicleType}
              onChange={val => setVehicleType(val as TripTypes)}
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            name="autoparkId"
            label={t.Vehicles.autoparkName}
            rules={[requiredField, maxLength(128)]}
          >
            <BranchSelect
              autoparkId={contractorId}
              className={styles.select}
              showDivider={false}
              placeholder={t.Vehicles.selectAutoPark}
              disabled={!(isAdmin || isManager) && isInternal}
              allowClear
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            name="stateNumber"
            label={t.Vehicles.stateNumber}
            rules={[requiredField, checkStateNumber]}
          >
            <SelectStateNumberMask className={styles.input} />
          </Form.Item>
          {vehicleType === TripTypes.Cargo && (
            <Form.Item
              className={styles.formItem}
              name="semitrailerNumber"
              label={t.Vehicles.semitrailerNumber}
              rules={[checkSemitrailerNumber]}
            >
              <StateNumberMask
                className={styles.input}
                placeholder="АА12345 123 RUS"
                mask={StateNumberMasks.Trailer}
              />
            </Form.Item>
          )}

          <Form.Item
            className={styles.formItem}
            name="vin"
            label={t.Vehicles.vin}
            rules={[checkVin()]}
          >
            {vinMask(styles.input, 'vin')}
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            name="brand"
            label={t.Vehicles.brand}
            rules={[requiredField, maxLength(128), checkTrimmedField()]}
          >
            <Input
              allowClear
              className={styles.input}
              placeholder={t.Vehicles.enterBrand}
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            name="name"
            label={t.Vehicles.model}
            rules={[maxLength(128), checkTrimmedField()]}
          >
            <Input
              allowClear
              className={styles.input}
              placeholder={t.Vehicles.enterModel}
            />
          </Form.Item>

          {vehicleType === TripTypes.Passenger && (
            <>
              <Form.Item
                className={styles.formItem}
                name="insuranceNumber"
                label={t.Vehicles.insuranceNumber}
                rules={[checkInsuranceNumber()]}
              >
                {insuranceNumberMask(styles.input)}
              </Form.Item>

              <Form.Item
                className={styles.formItem}
                name="ecoClass"
                label={t.Vehicles.ecoClass}
                rules={[maxLength(128)]}
              >
                <SelectEcoClass className={styles.select} placeholder={t.Vehicles.selectEcoClass} />
              </Form.Item>

              <Form.Item
                name="fuelConsumption"
                className={styles.formItem}
                label={t.Vehicles.fuelConsumption}
                rules={[floatDecimal(), maxLength(10)]}
              >
                <InputNumber
                  className={styles.input}
                  step={0.1}
                  placeholder={t.Vehicles.enterFuelConsumption}
                />
              </Form.Item>

              <Form.Item
                className={styles.formItem}
                name="packageClass"
                label={t.Vehicles.packageClass}
                rules={[maxLength(128), checkTrimmedField()]}
              >
                <Input
                  className={styles.input}
                  allowClear
                  placeholder={t.Vehicles.enterPackageClass}
                />
              </Form.Item>

              <Form.Item
                name="mileage"
                className={styles.formItem}
                label={t.Vehicles.mileage}
                rules={[positiveNumbers(), maxLength(10), maxInt()]}
              >
                <InputNumber
                  className={styles.input}
                  step={1}
                  placeholder={t.Vehicles.enterMileage}
                />
              </Form.Item>
            </>
          )}

          <Form.Item
            className={styles.formItem}
            name="color"
            label={t.Vehicles.color}
            rules={[maxLength(128), checkTrimmedField()]}
          >
            <Input
              allowClear
              className={styles.input}
              placeholder={t.Vehicles.enterColor}
            />
          </Form.Item>

          {vehicleType === TripTypes.Passenger && (
            <Form.Item
              name="manufactureYear"
              className={styles.formItem}
              label={t.Vehicles.manufactureYear}
              rules={[validationFloatingNumbers(), minLength(4), minManufactureYear(1900), maxInt('manufactureYear')]}
            >
              <InputNumber
                className={styles.input}
                min={1900}
                name="manufactureYear"
                maxLength={4}
                step={1}
                placeholder={t.Vehicles.enterManufactureYear}
              />
            </Form.Item>
          )}

          <Form.Item
            name="maxAllowedWeight"
            className={styles.formItem}
            label={vehicleType === TripTypes.Passenger ? t.Vehicles.maxAllowedWeight : t.Vehicles.maxAllowedWeightT}
            rules={[positiveNumbers(), maxInt()]}
          >
            <InputNumber
              className={styles.input}
              min={1}
              step={1}
              placeholder={t.Vehicles.enterMaxAllowedWeight}
            />
          </Form.Item>

          {vehicleType === TripTypes.Cargo && (
            <>
              <Form.Item
                name="volume"
                className={styles.formItem}
                label={t.Vehicles.volume}
                rules={[positiveNumbers(), maxInt()]}
              >
                <InputNumber
                  className={styles.input}
                  min={1}
                  step={1}
                  placeholder={t.Vehicles.volume}
                />
              </Form.Item>

              <Form.Item
                name="length"
                className={styles.formItem}
                label={t.Vehicles.length}
                rules={[maxInt()]}
              >
                <InputNumber
                  className={styles.input}
                  min={0}
                  step={0.1}
                  decimalSeparator=","
                  placeholder={t.Vehicles.length}
                />
              </Form.Item>

              <Form.Item
                name="width"
                className={styles.formItem}
                label={t.Vehicles.width}
                rules={[maxInt()]}
              >
                <InputNumber
                  className={styles.input}
                  min={0}
                  step={0.1}
                  decimalSeparator=","
                  placeholder={t.Vehicles.width}
                />
              </Form.Item>

              <Form.Item
                name="height"
                className={styles.formItem}
                label={t.Vehicles.height}
                rules={[maxInt()]}
              >
                <InputNumber
                  className={styles.input}
                  min={0}
                  step={0.1}
                  decimalSeparator=","
                  placeholder={t.Vehicles.height}
                />
              </Form.Item>
            </>
          )}

          {vehicleType === TripTypes.Passenger && (
            <>
              <Form.Item
                className={styles.formItem}
                name="chassisType"
                label={t.Vehicles.chassisType}
                rules={[maxLength(128), checkTrimmedField()]}
              >
                <Input
                  allowClear
                  className={styles.input}
                  placeholder={t.Vehicles.enterChassisType}
                />
              </Form.Item>

              <Form.Item
                className={styles.formItem}
                name="engineType"
                label={t.Vehicles.engineType}
                rules={[maxLength(128), checkTrimmedField()]}
              >
                <Input
                  allowClear
                  className={styles.input}
                  placeholder={t.Vehicles.enterEngineType}
                />
              </Form.Item>

              <Form.Item
                className={styles.formItem}
                name="transmissionType"
                label={t.Vehicles.transmissionType}
                rules={[maxLength(128), checkTrimmedField()]}
              >
                <Input
                  allowClear
                  className={styles.input}
                  placeholder={t.Vehicles.enterTransmissionType}
                />
              </Form.Item>

              <Form.Item
                className={styles.formItem}
                name="bodyType"
                label={t.Vehicles.bodyType}
                rules={[maxLength(128), checkTrimmedField()]}
              >
                <Input
                  allowClear
                  className={styles.input}
                  placeholder={t.Vehicles.enterBodyType}
                />
              </Form.Item>

              <Form.Item
                className={styles.formItem}
                name="passport"
                label={t.Vehicles.passport}
                rules={[checkVehiclePassport()]}
              >
                {vehiclePassportMask(styles.input)}
              </Form.Item>
            </>
          )}

          <Form.Item
            className={styles.formItem}
            name="inExploitation"
            label={t.Vehicles.inExploitation}
            rules={[requiredField]}
            initialValue={1}
          >
            <SelectInExploitation
              allowClear
              className={styles.select}
              placeholder={t.Vehicles.selectInExploitation}
            />
          </Form.Item>

          <div className={styles.formActions}>
            <Popconfirm
              placement="top"
              title={t.EditableTable.cancelEditingQuery}
              onConfirm={handleCancel}
              okText={t.global.closingConfirmation}
              cancelText={t.EditableTable.cancelEditingReject}
            >
              <Button
                htmlType="submit"
                type="text"
                className={styles.cancelButton}
              >
                {t.global.cancel}
              </Button>
            </Popconfirm>
            <Button
              htmlType="submit"
              type="primary"
              className={styles.reloadButton}
            >
              {isAdd ? t.global.add : t.global.save}
            </Button>
          </div>
        </Form>
      </Spin>
    </Modal>
  );
};
