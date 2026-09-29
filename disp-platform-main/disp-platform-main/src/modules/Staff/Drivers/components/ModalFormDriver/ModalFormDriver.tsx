import React, { FC, useEffect, useState } from 'react';
import {
  Input, Modal, Form, Select, InputNumber, Spin, Button, Popconfirm
} from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { Store } from 'antd/lib/form/interface';
import { useTranslation } from 'i18n';

import { useCreateDriver, useGetDriverLicenses, useUpdateDriver } from 'api/drivers/drivers.api';
import { Driver } from 'api/drivers/drivers.types';
import { useProfile } from 'api/profile/profile.api';
import { useResetUserPass } from 'api/user/user.api';

import {
  DriverSpecialityTypes,
  DRIVER_ACTIVE_DESCRIPTIONS,
  DRIVER_EXPERIENCE_DESCRIPTIONS
} from 'constants/driver.constants';

import { UUID } from 'utils/io-ts';
import { ignore, inputCleaner, preventDefault } from 'utils/utils';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { preparePhoneForBackend } from 'utils/preparePhoneForBackend';
import { driverLicenseMask, driverPassportMask, phoneMask } from 'components/Masks/Masks';
import { SelectDriverSpeciality } from 'components/SelectDriverSpeciality';
import { useModalForm } from '../../context/ModalForm';
import { getDriverLicenseOptions } from '../../utils';

import { useActiveDriver } from '../../context/ActiveDriver';
import styles from './ModalFormDriver.module.scss';

const {
  required,
  nameRule,
  patronymicRule,
  minMaxLength,
  passportNumberRule,
  driverLicenseRule,
  checkPhoneNumber,
  maxLength,
  floatTwoSymbols,
  max,
  email,
  checkTrimmedField,
} = ValidationRules.general;

export const activeOptions = Object.entries(DRIVER_ACTIVE_DESCRIPTIONS).map(([value, label]) => ({
  value,
  label,
}));

export const experienceOptions = Object.entries(DRIVER_EXPERIENCE_DESCRIPTIONS).map(([value, label]) => ({
  value,
  label,
}));

export const ModalFormDriver: FC = () => {
  const { t } = useTranslation();
  const { Labels } = useTranslation().t.Drivers;
  const { stateShowModal, handleClose } = useModalForm();
  const { setActiveDriver } = useActiveDriver();
  const [form] = useForm();
  const { contractorId, autoparkId } = useProfile().data;
  const [createDriver, { isLoading }] = useCreateDriver(contractorId);
  const [updateDriver, { isLoading: isUpdateLoading }] = useUpdateDriver(contractorId);

  const driverLicenses = useGetDriverLicenses();
  const driverLicenseOptions = getDriverLicenseOptions(driverLicenses);

  const [isLicenseAvailable] = useState(true);

  const [driverSpeciality, setDriverSpeciality] = useState(stateShowModal.driver?.driverSpeciality);

  const isAdd = stateShowModal.type === 'add';

  const [resetUserPass] = useResetUserPass();

  const handleResetPass = async () => {
    const id = stateShowModal.driver?.id as UUID;
    await resetUserPass({ userId: id });
  };

  const handleSuccess = () => {
    form.submit();
  };

  const handleCancel = () => {
    handleClose();
  };

  const handleSave = (values: Store) => {
    const formattedValues: Driver = {
      ...values,
      contactPhone: preparePhoneForBackend(values.contactPhone as string, 'withBrackets'),
      attributes: [] as Driver['attributes'],
      rating: values.rating * 100,
      active: values.active === 'ACTIVE',
      autoparkId: isAdd ? autoparkId : stateShowModal.driver.autoparkId,
    } as Driver;

    Object.entries(formattedValues).forEach(([key, val]) => {
      if (val === '') {
        delete formattedValues[key as keyof Driver];
      }
    });

    if (isAdd) {
      createDriver(formattedValues)
        .then(newDriver => {
          setActiveDriver(newDriver as Driver);
          handleClose();
        })
        .catch(ignore);

      return;
    }

    const id = stateShowModal.driver?.id as UUID;

    updateDriver({ driverId: id, data: { ...formattedValues } })
      .then(() => {
        setActiveDriver({ ...formattedValues, id });
        handleClose();
      })
      .catch(ignore);
  };

  const handleOnChangeInput = (event: React.ChangeEvent<HTMLInputElement>) => inputCleaner(event, form);

  useEffect(() => {
    form.setFieldsValue(
      (isAdd
        ? {}
        : {
          ...stateShowModal.driver,
          active: stateShowModal.driver?.active ? 'ACTIVE' : 'NOT_ACTIVE',
          rating: (stateShowModal.driver?.rating || 0) / 100,
        }) as Store
    );

    setDriverSpeciality(stateShowModal.driver?.driverSpeciality);
  }, [stateShowModal, isAdd, form]);

  return (
    <Modal
      title={t.Registry.Modal[stateShowModal.type]}
      className={styles.modal}
      visible={stateShowModal.isOpen}
      onCancel={handleCancel}
      onOk={handleSuccess}
      okText={isAdd ? t.global.add : t.global.save}
      cancelText={t.global.cancel}
      cancelButtonProps={{ className: styles.cancelButton }}
      afterClose={form.resetFields}
      footer={[
        !isAdd && (
          <Popconfirm
            placement="top"
            title={t.Drivers.resetDriverPass}
            onConfirm={handleResetPass}
            okText={t.global.confirm}
            cancelText={t.global.cancel}
            key="resetPass"
          >
            <Button size="middle">{t.global.resetPass}</Button>
          </Popconfirm>
        ),
        <Button
          key="back"
          onClick={handleCancel}
          disabled={isLoading || isUpdateLoading}
        >
          {t.global.cancel}
        </Button>,
        <Button
          key="submit"
          type="primary"
          onClick={handleSuccess}
          disabled={isLoading || isUpdateLoading}
        >
          {isAdd ? t.global.add : t.global.save}
        </Button>,
      ]}
    >
      <Spin spinning={isLoading || isUpdateLoading}>
        <Form
          className={styles.filtersForm}
          form={form}
          onFinish={handleSave}
        >
          <Form.Item
            className={styles.formItem}
            name="driverSpeciality"
            label={Labels.speciality}
            rules={[required]}
          >
            <SelectDriverSpeciality
              className={styles.select}
              placeholder={Labels.speciality}
              value={driverSpeciality}
              onChange={val => setDriverSpeciality(val as DriverSpecialityTypes)}
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.lastName}
            name="lastName"
            rules={[required, nameRule, minMaxLength(1, 50)]}
          >
            <Input
              className={styles.input}
              allowClear
              onPressEnter={preventDefault}
              onChange={handleOnChangeInput}
              minLength={2}
              placeholder={Labels.enterLastName}
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.firstName}
            name="firstName"
            rules={[required, nameRule, minMaxLength(1, 50)]}
          >
            <Input
              className={styles.input}
              allowClear
              onChange={handleOnChangeInput}
              minLength={2}
              onPressEnter={preventDefault}
              placeholder={Labels.enterFirstName}
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.patronymic}
            name="patronymic"
            rules={[patronymicRule, minMaxLength(1, 50)]}
          >
            <Input
              className={styles.input}
              onChange={handleOnChangeInput}
              minLength={2}
              allowClear
              onPressEnter={preventDefault}
              placeholder={Labels.enterPatronymic}
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.phoneNumber}
            name="contactPhone"
            rules={[required, checkPhoneNumber()]}
          >
            {phoneMask({ className: styles.input, name: 'contactPhone' })}
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.email}
            name="email"
            rules={[required, email, maxLength(254)]}
          >
            <Input
              className={styles.input}
              name="email"
              allowClear
              onPressEnter={preventDefault}
              placeholder={Labels.enterEmail}
            />
          </Form.Item>

          {(driverSpeciality === DriverSpecialityTypes.Passenger
          || driverSpeciality === DriverSpecialityTypes.Both) && (
            <Form.Item
              className={styles.formItem}
              label={Labels.serviceLicenseNumber}
              name="serviceLicenseNumber"
              rules={[minMaxLength(8, 12), maxLength(12), checkTrimmedField()]}
            >
              <Input
                className={styles.input}
                name="serviceLicenseNumber"
                onChange={handleOnChangeInput}
                allowClear
                maxLength={12}
                onPressEnter={preventDefault}
                placeholder={Labels.enterServiceLicenseNumber}
                disabled={!isLicenseAvailable}
              />
            </Form.Item>
          )}

          {(driverSpeciality === DriverSpecialityTypes.Cargo || driverSpeciality === DriverSpecialityTypes.Both) && (
            <Form.Item
              className={styles.formItem}
              label={Labels.serviceLicenseNumber}
              name="cargoLicenceNumber"
              rules={[minMaxLength(8, 12), maxLength(12), checkTrimmedField()]}
            >
              <Input
                className={styles.input}
                name="cargoLicenceNumber"
                onChange={handleOnChangeInput}
                allowClear
                maxLength={12}
                onPressEnter={preventDefault}
                placeholder={Labels.cargoLicenceNumber}
                disabled={!isLicenseAvailable}
              />
            </Form.Item>
          )}

          <Form.Item
            className={styles.formItem}
            label={Labels.passport}
            name="passport"
            rules={[passportNumberRule()]}
          >
            {driverPassportMask(styles.input)}
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.licenseClasses}
            name="driverLicenses"
          >
            <Select
              className={styles.select}
              options={driverLicenseOptions}
              allowClear
              placeholder={Labels.selectLicenseClasses}
              onInputKeyDown={preventDefault}
              mode="multiple"
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.driverLicenseNumber}
            name="driverLicenseNumber"
            rules={[driverLicenseRule()]}
          >
            {driverLicenseMask(styles.input)}
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.experience}
            name="experience"
          >
            <Select
              className={styles.select}
              options={experienceOptions}
              allowClear
              showSearch
              optionFilterProp="label"
              placeholder={Labels.selectExperience}
              onInputKeyDown={preventDefault}
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.active}
            name="active"
            rules={[required]}
            initialValue="ACTIVE"
          >
            <Select
              className={styles.select}
              options={activeOptions}
              allowClear
              placeholder={Labels.selectActive}
              onInputKeyDown={preventDefault}
            />
          </Form.Item>

          <Form.Item
            className={styles.formItem}
            label={Labels.rating}
            name="rating"
            rules={[floatTwoSymbols, max(5)]}
          >
            <InputNumber
              className={styles.input}
              onPressEnter={preventDefault}
              placeholder={Labels.enterRating}
              step={0.01}
            />
          </Form.Item>
        </Form>
      </Spin>
    </Modal>
  );
};
