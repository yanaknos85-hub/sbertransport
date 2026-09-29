import React, {
  FC, memo, useEffect, useState
} from 'react';
import {
  Col, DatePicker, Select, Spin, TimePicker
} from 'antd';
import Form from 'antd/es/form';
import { useForm } from 'antd/es/form/Form';
import Modal from 'antd/lib/modal/Modal';
import { AxiosError } from 'axios';
import cn from 'classnames';
import moment, { Moment } from 'moment';

import { DefaultError, ErrorTypes } from 'api';
import { useProfile } from 'api/profile/profile.api';
import { useCreateShifts, useDeleteShifts, useUpdateShift } from 'api/schedule2.0/schedule.api';
import { ShiftsUpdate } from 'api/schedule2.0/schedule.types';

import { TripTypes } from 'constants/app.constants';

import { useModalState } from 'hooks/useModal';

import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { getFullName } from 'utils/getFullName';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

import { useTranslation } from 'i18n';

import { useAppStore } from 'ioc/ioc.context';

import Flex from 'components/Flex/Flex';
import { RowWithContainer } from 'components/Row/Row';
import { SelectDrivers } from 'components/SelectDrivers/SelectDrivers';
import { Switcher } from 'components/Switcher/Switcher';
import TabSwitcher, { Tab } from 'components/TabSwitcher/TabSwitcher';

import { SelectVehicle } from '../../components/SelectVehicle';
import { PeriodicityTypes, flexibleDays, periodicityTitles } from '../../constants/schedule.constants';
import { useModals } from '../../context/modal.context';
import { useSelectedShift } from '../../context/selectedShift.context';
import { DayOfWeekPicker } from './components/DayOfWeekPicker/DayOfWeekPicker';
import RepeatTooltip from './components/RepeatTooltip';
import { useConflictData } from './useConflictData';
import { buildShifts, disableTime, FormValues } from './utils';

import styles from './CreateModal.module.scss';

const { required } = ValidationRules.general;

const periodicityOptions = Object.entries(periodicityTitles).map(([value, label]) => ({ value, label }));

enum EditTypes {
  Full,
  Part,
}

const editTypes: Tab[] = [
  { key: EditTypes.Full, label: 'Изменить всю смену' },
  { key: EditTypes.Part, label: 'С выбранного времени' },
];

export const CreateModal: FC = memo(
  () => {
    const { logger } = useAppStore();

    const [form] = useForm();
    const { t } = useTranslation();

    const { contractorId } = useProfile().data;

    const {
      selectedShift, setSelectedShift, conflictShift, setConflictShift,
    } = useSelectedShift();
    const { isCreateOpened, handleClose } = useModals();

    const [isRepeat, setIsRepeat] = useState(false);
    const [driverSpeciality, setDriverSpeciality] = useState(selectedShift?.vehicle.vehicleType);
    const [shiftsToDelete, setShiftsToDelete] = useState<UUID[]>([]);

    const [createShift, { isLoading }] = useCreateShifts(contractorId);
    const [editShift, { isLoading: isEditLoading }] = useUpdateShift(contractorId, selectedShift?.id as UUID);

    const isAdd = !selectedShift?.id;

    const handleCancel = () => {
      setSelectedShift(null);
      setConflictShift(null);
      handleClose();
    };

    const [isConfirmationOpened, { show: showConfirmation, hide: hideConfirmation }] = useModalState();

    const handleFinish = async (formValues: FormValues) => {
      if (isLoading || isEditLoading) {
        return;
      }

      if (formValues.duration?.format('HH:mm') === '00:00') {
        logger.toMessage('error', 'Введите длительность смены');
        return;
      }

      const shifts = buildShifts({
        formValues,
        isRepeat,
        routeId: conflictShift?.routeId,
      });

      if (isAdd) {
        return createShift(shifts)
          .then(handleCancel)
          .catch(ignore);
      }

      const updatingShift = shifts[0];

      const updatedValues: ShiftsUpdate = {
        ...updatingShift,
        endDate: formValues.changeDriverId && formValues.editType === EditTypes.Part
          ? formValues.newShiftStartDate.clone().utc().add(-1, 'm').endOf('minute').format()
          : updatingShift.endDate,
        id: selectedShift?.id as UUID,
        driverId: formValues.editType === EditTypes.Full ? formValues.changeDriverId : selectedShift?.driver?.id,
        vehicleId: selectedShift?.vehicle?.id,
      };

      editShift(updatedValues)
        .then(async () => {
          if (formValues.changeDriverId && formValues.editType === EditTypes.Part) {
            try {
              const response = await createShift([{
                ...updatingShift,
                driverId: formValues.changeDriverId,
                vehicleId: selectedShift?.vehicle?.id,
                startDate: formValues.newShiftStartDate.clone().utc().startOf('minute').format(),
                endDate: updatingShift.endDate,
              }]);

              if (!!response?.[0] && 'problems' in response[0]) {
                let conflictEntitiesIds: UUID[] = [];
                response[0].problems.forEach(problem => {
                  if ('conflictEntitiesIds' in problem) {
                    conflictEntitiesIds = [...conflictEntitiesIds, ...problem.conflictEntitiesIds];
                  }
                });
                if (conflictEntitiesIds.length) {
                  setShiftsToDelete(conflictEntitiesIds);
                  showConfirmation();
                } else {
                  handleCancel();
                }
              } else {
                handleCancel();
              }
            } catch (error) {
              // eslint-disable-next-line no-console
              console.error(error);
            }
          } else {
            handleCancel();
          }
        })
        .catch((error: AxiosError<DefaultError>) => {
          if (!error.isAxiosError) return;

          let conflictEntitiesIds: UUID[] = [];

          error.response?.data.problems.forEach(problem => {
            const driverShiftDateConflict = problem.constraints?.find(
              constraint => constraint.type === ErrorTypes.DRIVER_SHIFT_DATE_CONFLICT
            );
            if (driverShiftDateConflict) {
              conflictEntitiesIds = [...conflictEntitiesIds, ...driverShiftDateConflict.value];
            }
          });
          if (conflictEntitiesIds.length) {
            setShiftsToDelete(conflictEntitiesIds);
            showConfirmation();
          } else {
            handleCancel();
          }
        });
    };

    useEffect(() => {
      if (!isCreateOpened) return;

      const isCurrentShift = moment().isBetween(
        moment.utc(selectedShift?.startDate),
        moment.utc(selectedShift?.endDate)
      );

      form.setFieldsValue({
        driverId: selectedShift?.driver?.id,
        vehicleId: selectedShift?.vehicle?.id,
        startDate: moment.utc(selectedShift?.startDate).local(),
        startTime: moment.utc(selectedShift?.startDate).local(),
        shiftEndDate: selectedShift?.endDate ? moment.utc(selectedShift?.endDate).local() : undefined,
        editType: isCurrentShift ? EditTypes.Part : EditTypes.Full,
      });

      setIsRepeat(false);
      setDriverSpeciality(selectedShift?.vehicle?.vehicleType);
    }, [selectedShift, form, isCreateOpened]);

    useConflictData(form, isCreateOpened);

    useEffect(() => {
      if (isRepeat) {
        form.setFieldsValue({
          startTime: form.getFieldValue('startDate'),
          endDate: form.getFieldValue('shiftEndDate'),
        });
      }
    }, [isRepeat, form]);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const handleFormChange = (changedValues: Record<string, any>, values: Record<string, any>) => {
      // Триггер на смену машины
      if ('changeDriverId' in changedValues) {
        if (changedValues.changeDriverId) {
          if (moment().isBetween(
            values.startDate,
            values.shiftEndDate
          )) {
            form.setFieldsValue({
              newShiftStartDate: moment().add(1, 'm').startOf('minute'),
            });
          }
        }
      }
    };

    const disabledTime = (date: Moment | null) => disableTime(date, isRepeat, form);

    const [deleteShift, { isLoading: isDeletingShift }] = useDeleteShifts(contractorId);

    const confirmDeletion = () => {
      hideConfirmation();
      const deleteRequests = shiftsToDelete.map(shiftId => deleteShift(shiftId).catch(ignore));
      Promise.all(deleteRequests).then(() => {
        form.submit();
        setShiftsToDelete([]);
      });
    };

    return (
      <>
        <Modal
          width={880}
          visible={isCreateOpened}
          className={styles.modal}
          title={!isAdd || !!conflictShift ? t.Shifts.editShift : t.Shifts.createShift}
          onCancel={handleCancel}
          okText={t.global.save}
          onOk={form.submit}
          okButtonProps={{ loading: isEditLoading || isLoading }}
          cancelText={t.global.cancel}
          afterClose={form.resetFields}
          destroyOnClose
        >
          {!isAdd && selectedShift?.vehicle && (
            <div className={styles.driver}>
              <div className={styles.contentSide}>
                <p className={styles.vehicleName}>
                  {selectedShift.vehicle.model?.brand}
                  {' '}
                  {selectedShift.vehicle.model?.name}
                </p>
                <p className={styles.stateNumber}>{selectedShift?.vehicle?.stateNumber}</p>
                <p className={styles.driverName}>{getFullName(selectedShift?.driver ?? {})}</p>
              </div>
            </div>
          )}

          <Spin spinning={isEditLoading || isLoading}>
            <Form
              form={form}
              onFinish={handleFinish}
              onValuesChange={handleFormChange}
            >
              <div className={styles.form}>
                <RowWithContainer>
                  {isAdd && (
                    <>
                      <Col span={12}>
                        <Form.Item
                          className={styles.formItem}
                          name="vehicleId"
                          label={t.Shifts.vehicle}
                          rules={[required]}
                        >
                          <SelectVehicle
                            className={styles.select}
                            onChangeVal={(val, speciality) => setDriverSpeciality(speciality)}
                          />
                        </Form.Item>
                      </Col>

                      <Col span={12}>
                        <Form.Item
                          className={styles.formItem}
                          name="driverId"
                          label={t.Shifts.driver}
                          rules={[required]}
                        >
                          <SelectDrivers
                            allowAutoparkRestrict
                            className={styles.select}
                            type={driverSpeciality}
                            disabled={!driverSpeciality}
                          />
                        </Form.Item>
                      </Col>
                    </>
                  )}
                </RowWithContainer>

                {isAdd && !conflictShift && (
                  <Flex alignItems="center">
                    <Switcher
                      checked={isRepeat}
                      onChange={setIsRepeat}
                      title={t.Shifts.repeat}
                    />

                    <RepeatTooltip />
                  </Flex>
                )}

                <div className={styles.additionalForm}>
                  <RowWithContainer>
                    <Col span={isRepeat ? 8 : 12}>
                      <Form.Item
                        className={styles.formItem}
                        name="startDate"
                        label={t.Shifts.startDate}
                        rules={[required, ValidationRules.general.validatorStartDate(form, isRepeat ? 'endDate' : 'shiftEndDate')]}
                      >
                        <DatePicker
                          className={styles.input}
                          allowClear
                          showSecond={false}
                          format={isRepeat ? 'DD.MM.YYYY' : 'DD.MM.YYYY HH:mm'}
                          showToday
                          showTime={isRepeat ? false : { format: 'HH:mm' }}
                          dropdownClassName={styles.dropdownPickerPanel}
                        />
                      </Form.Item>
                    </Col>

                    {isRepeat && (
                      <Col span={8}>
                        <Form.Item
                          className={styles.formItem}
                          name="startTime"
                          label={t.Shifts.startTime}
                          rules={[required]}
                        >
                          <TimePicker
                            className={styles.input}
                            allowClear
                            showSecond={false}
                            format="HH:mm"
                            showNow={false}
                            popupClassName={styles.dropdownPickerPanel}
                          />
                        </Form.Item>
                      </Col>
                    )}

                    <Col span={isRepeat ? 8 : 12}>
                      {isRepeat ? (
                        <Form.Item
                          className={styles.formItem}
                          name="duration"
                          label={t.Shifts.duration}
                          rules={[required]}
                        >
                          <TimePicker
                            className={styles.input}
                            allowClear
                            showSecond={false}
                            format="HH:mm"
                            showNow={false}
                            placeholder={t.Shifts.hours}
                            popupClassName={styles.dropdownPickerPanel}
                          />
                        </Form.Item>
                      ) : (
                        <Form.Item
                          className={styles.formItem}
                          name="shiftEndDate"
                          label={t.Shifts.endDate}
                          rules={[required]}
                        >
                          <DatePicker
                            className={styles.input}
                            allowClear
                            showSecond={false}
                            format="DD.MM.YYYY HH:mm"
                            showToday
                            showTime={{ format: 'HH:mm' }}
                            dropdownClassName={styles.dropdownPickerPanel}
                          />
                        </Form.Item>
                      )}
                    </Col>

                    {!isAdd && (
                      <>
                        <Col span={12}>
                          <Form.Item
                            className={styles.formItem}
                            name="changeDriverId"
                            label={t.Shifts.changeDriver}
                          >
                            <SelectDrivers
                              allowAutoparkRestrict
                              className={styles.select}
                              type={driverSpeciality as TripTypes}
                              disabled={!driverSpeciality}
                            />
                          </Form.Item>
                        </Col>

                        <Col span={24}>
                          <Form.Item dependencies={['changeDriverId']} noStyle>
                            {({ getFieldValue }) => !!getFieldValue('changeDriverId') && (
                              <Form.Item
                                className={styles.formItem}
                                name="editType"
                                rules={[required]}
                              >
                                <TabSwitcher tabs={editTypes} />
                              </Form.Item>
                            )}
                          </Form.Item>
                        </Col>

                        <Col span={12}>
                          <Form.Item dependencies={['changeDriverId', 'startDate', 'startTime', 'duration', 'editType']} noStyle>
                            {({ getFieldValue }) => !!getFieldValue('changeDriverId') && getFieldValue('editType') === EditTypes.Part && (
                              <Form.Item
                                className={styles.formItem}
                                name="newShiftStartDate"
                                label={t.Shifts.changeDate}
                                rules={[required]}
                              >
                                <DatePicker
                                  className={styles.input}
                                  allowClear
                                  showTime={{ format: 'HH:mm' }}
                                  showSecond={false}
                                  format="DD.MM.YYYY HH:mm"
                                  showToday
                                  disabledDate={current => current && (
                                    current < (getFieldValue('startDate') as Moment).clone().startOf('day')
                                    || current > (isRepeat
                                      ? (getFieldValue('startDate') as Moment)
                                        .clone()
                                        .add(getFieldValue('duration')?.hours() ?? 0, 'h')
                                        .add(getFieldValue('duration')?.minutes() ?? 0, 'm')
                                      : getFieldValue('shiftEndDate'))
                                  )}
                                  disabledTime={disabledTime}
                                />
                              </Form.Item>
                            )}
                          </Form.Item>
                        </Col>
                      </>
                    )}
                  </RowWithContainer>
                </div>

                {isRepeat && (
                  <RowWithContainer>
                    <Col span={12}>
                      <Form.Item
                        className={styles.formItem}
                        name="periodicity"
                        label={t.Shifts.periodicity}
                        rules={[required]}
                      >
                        <Select
                          className={styles.select}
                          placeholder={t.Shifts.periodicity}
                          options={periodicityOptions}
                        />
                      </Form.Item>
                    </Col>

                    <Col span={12}>
                      <Form.Item
                        className={styles.formItem}
                        name="endDate"
                        label={t.Shifts.endDate}
                        rules={[required]}
                      >
                        <DatePicker
                          className={styles.input}
                          allowClear
                          showSecond={false}
                          format="DD.MM.YYYY"
                        />
                      </Form.Item>
                    </Col>

                    <Form.Item dependencies={['periodicity']} noStyle>
                      {({ getFieldValue }) => (
                        <>
                          {getFieldValue('periodicity') === PeriodicityTypes.Weekly && (
                            <Col span={12}>
                              <Form.Item
                                className={styles.formItem}
                                name="daysOfWeek"
                                label={t.Shifts.daysOfweek}
                                rules={[required]}
                              >
                                <DayOfWeekPicker />
                              </Form.Item>
                            </Col>
                          )}

                          {getFieldValue('periodicity') === PeriodicityTypes.Flexible && (
                            <Col span={12}>
                              <RowWithContainer>
                                <Col span={6}>
                                  <Form.Item
                                    className={styles.formItem}
                                    name="workDaysLength"
                                    label={t.Shifts.scheduler}
                                    rules={[required]}
                                  >
                                    <Select options={flexibleDays} className={styles.select} />
                                  </Form.Item>
                                </Col>

                                <Col span={4}>
                                  <Form.Item className={styles.formItem} label=" ">
                                    <div className={cn(styles.select, styles.betweenSelects)}>Через</div>
                                  </Form.Item>
                                </Col>

                                <Col span={6}>
                                  <div className={styles.flexibleSelect2}>
                                    <Form.Item
                                      className={styles.formItem}
                                      name="dayOffLength"
                                      label=" "
                                      rules={[required]}
                                    >
                                      <Select options={flexibleDays} className={styles.select} />
                                    </Form.Item>
                                  </div>
                                </Col>
                              </RowWithContainer>
                            </Col>
                          )}
                        </>
                      )}
                    </Form.Item>
                  </RowWithContainer>
                )}
              </div>
            </Form>
          </Spin>
        </Modal>
        <Modal
          width={500}
          visible={isConfirmationOpened}
          className={styles.modal}
          title={t.Shifts.conflict}
          onCancel={hideConfirmation}
          okText={t.global.yes}
          onOk={confirmDeletion}
          okButtonProps={{ loading: isEditLoading || isLoading || isDeletingShift }}
          cancelText={t.global.no}
          destroyOnClose
        >

        </Modal>
      </>
    );
  },
  () => false
);
