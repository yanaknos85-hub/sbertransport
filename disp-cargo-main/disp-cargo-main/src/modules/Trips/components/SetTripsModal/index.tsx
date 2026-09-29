import { Form, Modal, Skeleton } from 'antd';
import React, { FC, useMemo } from 'react';
import { useTranslation } from 'i18n';
import { useForm } from 'antd/lib/form/Form';
import { Store } from 'antd/lib/form/interface';
import { UUID } from 'utils/io-ts';
import { ReactComponent as Flag } from 'assets/icons/russian-flag.svg';
import { StarFilled } from '@ant-design/icons';
import { getFullName } from 'utils/getFullName';
import styles from './index.module.scss';
import { useTripsModal } from '../../context/TripsModal';
import { SelectEndlessScroll } from 'components/SelectEndlessScroll';
import { useActiveTrip } from '../../context/ActiveTrip';
import { useProfile } from 'api/profile/profile.api';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { useCargoTripsMutation, useSetDriverToCargoRequest } from 'api/trips-cargo/trips-cargo.api';
import { useDriver } from 'api/drivers/drivers.api';
import { useShift } from 'api/schedule/schedule.api';

export const SetTripModal: FC = () => {
  const { t } = useTranslation();
  const {
    modalState, isOpened, closeModal,
  } = useTripsModal();
  const { activeTrip } = useActiveTrip();
  const [form] = useForm();
  const { contractorId, id } = useProfile().data;
  const [setDriver] = useSetDriverToCargoRequest(contractorId);

  const handleCancel = () => {
    closeModal();
  };

  const handleSuccess = () => {
    form.submit();
  };

  const { data: driver, isLoading: isLoadingDriver } = useDriver({
    contractorId,
    // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
    driverId: modalState.driverData?.driverId!,
  }, {
    enabled: !!modalState.driverData?.driverId,
    suspense: false,
  });

  const { data: shift, isLoading: isLoadingShift } = useShift({
    contractorId,
    // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
    shiftId: modalState.driverData?.shiftId!,
  }, {
    enabled: !!modalState.driverData?.shiftId,
    suspense: false,
  });

  const handleSetDriver = async (values: Store) => {
    await setDriver({ driverId: modalState.driverData?.driverId as UUID, tripId: values.tripId });
    closeModal();
  };

  const [fetchTrips] = useCargoTripsMutation(contractorId);

  const tripsQuery = useMemo(() => ({ dispatcherId: id }), [id]);

  return (
    <Modal
      title={t.Requests.Modal.SetRequest.title}
      className={styles.modal}
      visible={isOpened('setRequest')}
      onCancel={handleCancel}
      onOk={handleSuccess}
      okText={t.Requests.Modal.set}
      cancelText={t.global.cancel}
      cancelButtonProps={{ className: styles.cancelButton }}
      afterClose={form.resetFields}
      destroyOnClose
    >
      <div className={styles.carCard}>
        <Skeleton loading={isLoadingDriver && isLoadingShift}>
          <div>{driver && <b>{getFullName(driver)}</b>}</div>

          {shift?.vehicle.stateNumber && (
            <div>
              <b>{shift?.vehicle.stateNumber}</b>
              <Flag className={styles.flag} />
            </div>
          )}

          {driver && (
            <div>
              Рейтинг:
              {' '}
              <StarFilled />
              {' '}
              {(driver?.rating ?? 0) / 100}
            </div>
          )}
        </Skeleton>
      </div>

      <Form form={form} onFinish={handleSetDriver}>
        <div className={styles.subtitle}>{t.Requests.Modal.SetRequest.subtitle}</div>
        <Form.Item
          label={t.Requests.trip}
          name="tripId"
          rules={[ValidationRules.general.required]}
          initialValue={activeTrip?.id}
        >
          <SelectEndlessScroll
            query={tripsQuery}
            fetch={fetchTrips}
            labelField="humanReadableId"
            enabled={modalState.type === 'setRequest'}
            labelForDefaultValue={activeTrip?.humanReadableId ?? undefined}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
