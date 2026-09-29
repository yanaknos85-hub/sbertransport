import React, {
  FC, memo, useEffect, useState
} from 'react';
import { Col, DatePicker, Spin } from 'antd';
import Form, { RuleObject } from 'antd/es/form';
import { useForm } from 'antd/es/form/Form';
import { StoreValue } from 'antd/lib/form/interface';
import Modal from 'antd/lib/modal/Modal';
import moment, { Moment } from 'moment';

import { useProfile } from 'api/profile/profile.api';
import { useCreateShifts } from 'api/schedule2.0/schedule.api';
import { useSetDriverToRequest } from 'api/trips/trips.api';

import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

import { useTranslation } from 'i18n';

import { useModals } from 'modules/Schedule2.0/context/modal.context';
import { useSelectedTrip } from 'modules/Schedule2.0/tabs/Shifts/context/selectedTrip.context';

import { Icon } from 'components/Icon/Icon';
import { RowWithContainer } from 'components/Row/Row';
import { SelectDrivers } from 'components/SelectDrivers/SelectDrivers';

import { SelectVehicle } from '../../../../components/SelectVehicle';
import { disableEndShiftOrderTime, disableStartShiftOrderTime } from './utils';

import styles from './OrderModal.module.scss';

interface FormValues {
  vehicleId: UUID;
  driverId: UUID;
  startTime: Moment;
  endTime: Moment;
}

const { required } = ValidationRules.general;

export const OrderModal: FC = memo(
  () => {
    const [form] = useForm();
    const { t } = useTranslation();

    const { contractorId } = useProfile().data;
    const [createShift, { isLoading }] = useCreateShifts(contractorId);
    const [setDriver, { isLoading: isEditLoading }] = useSetDriverToRequest(contractorId);

    const { selectedTrip, setSelectedTrip } = useSelectedTrip();
    const { isOrderOpened, handleClose } = useModals();

    const [driverSpeciality, setDriverSpeciality] = useState(selectedTrip?.vehicle.vehicleType);

    useEffect(() => {
      if (!isOrderOpened) return;

      form.setFieldsValue({
        vehicleId: selectedTrip?.vehicle.id,
        startTime: moment.utc(selectedTrip?.expectedStartTime).local(),
        endTime: selectedTrip?.expectedEndTime ? moment.utc(selectedTrip.expectedEndTime).local() : undefined,
      });

      setDriverSpeciality(selectedTrip?.vehicle?.vehicleType);
    }, [selectedTrip, form, isOrderOpened]);

    const handleCancel = () => {
      setSelectedTrip(null);
      handleClose();
    };

    const handleFinish = async (formValues: FormValues) => {
      if (isLoading || isEditLoading) {
        return;
      }

      createShift([{
        driverId: formValues.driverId,
        vehicleId: formValues.vehicleId,
        index: 0,
        startDate: formValues.startTime.clone().utc().startOf('minute').format(),
        endDate: formValues.endTime.clone().utc().endOf('minute').format(),
      }])
        .then(async shifts => {
          try {
            if (shifts && ('id' in shifts[0])) {
              await setDriver({
                driverId: shifts[0]?.driverId,
                planningShiftId: shifts[0].id,
                tripId: selectedTrip!.id,
              });

              handleCancel();
            }
          } catch (error) {
          // eslint-disable-next-line no-console
            console.error(error);
          }
        })
        .catch(ignore);
    };

    return (
      <Modal
        width={880}
        visible={isOrderOpened}
        className={styles.modal}
        title={t.Orders.createOrderShift}
        onCancel={handleCancel}
        okText={t.global.save}
        onOk={form.submit}
        closeIcon={<Icon type="closeModal" className={styles.ModalIcon} />}
        okButtonProps={{ loading: isEditLoading || isLoading }}
        cancelText={t.global.cancel}
        afterClose={form.resetFields}
        destroyOnClose
      >
        <Spin spinning={isEditLoading || isLoading}>
          <Form form={form} onFinish={handleFinish}>
            <div className={styles.form}>
              <RowWithContainer>
                <Col span={12}>
                  <Form.Item
                    className={styles.formItem}
                    name="vehicleId"
                    label={t.Shifts.vehicle}
                    rules={[required]}
                  >
                    <SelectVehicle
                      disabled
                      className={styles.select}
                      searchValue={selectedTrip?.vehicle.stateNumber}
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
                    />
                  </Form.Item>
                </Col>
              </RowWithContainer>
              <RowWithContainer>
                <Col span={12}>
                  <Form.Item
                    className={styles.formItem}
                    name="startTime"
                    label={t.Shifts.startDate}
                    rules={[
                      required,
                      {
                        validator: (_: RuleObject, value: StoreValue): Promise<void> => (
                          value <= moment(selectedTrip?.expectedStartTime)
                            ? Promise.resolve()
                            : Promise.reject(t.Orders.startOrderError)
                        ),
                      },
                    ]}
                  >
                    <DatePicker
                      className={styles.input}
                      allowClear
                      showToday
                      showSecond={false}
                      format="DD.MM.YYYY HH:mm"
                      showTime={{ format: 'HH:mm' }}
                      disabledDate={d => d.isAfter(moment(selectedTrip?.expectedStartTime).endOf('day'))}
                      disabledTime={d => disableStartShiftOrderTime(d, selectedTrip?.expectedStartTime)}
                      dropdownClassName={styles.dropdownPickerPanel}
                    />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    className={styles.formItem}
                    name="endTime"
                    label={t.Shifts.endDate}
                    rules={[
                      required,
                      {
                        validator: (_: RuleObject, value: StoreValue): Promise<void> => (
                          value >= moment(selectedTrip?.expectedEndTime)
                            ? Promise.resolve()
                            : Promise.reject(t.Orders.endOrderError)
                        ),
                      },
                    ]}
                  >
                    <DatePicker
                      className={styles.input}
                      allowClear
                      showSecond={false}
                      format="DD.MM.YYYY HH:mm"
                      showToday
                      showTime={{ format: 'HH:mm' }}
                      disabledDate={d => d.isBefore(moment(selectedTrip?.expectedEndTime).startOf('day'))}
                      disabledTime={d => disableEndShiftOrderTime(d, selectedTrip?.expectedEndTime)}
                      dropdownClassName={styles.dropdownPickerPanel}
                    />
                  </Form.Item>
                </Col>
              </RowWithContainer>
            </div>
          </Form>
        </Spin>
      </Modal>
    );
  },
  () => false
);
