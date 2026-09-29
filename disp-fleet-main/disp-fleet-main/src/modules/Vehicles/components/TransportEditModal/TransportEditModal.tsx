import React, { useMemo, FC } from 'react';
import { Modal } from 'antd';
import moment from 'moment';
import { useTranslation } from 'i18n';

import { useEditTransport } from 'api/transport/transport.api';
import { EditTransportRequest } from 'api/transport/transport.types';
import { Icon } from 'components/Icon/Icon';
import { useModalForm } from 'modules/Vehicles/context/ModalForm';
import { useActiveTransport } from 'modules/Vehicles/context/ActiveTransport';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

import TransportEditForm, { IFormData, FORM_ID } from './TransportEditForm/TransportEditForm';
import styles from './TransportEditModal.module.scss';

const TransportEditModal: FC = () => {
  const { t } = useTranslation();
  const { stateShowModal, handleClose } = useModalForm();
  const { activeTransport, update } = useActiveTransport();

  const [editTransport, { isLoading }] = useEditTransport(activeTransport?.id as UUID);

  const initialValues: IFormData | undefined = useMemo(
    () => activeTransport
      ? {
        contractorId: activeTransport.contractorId,
        autoparkId: activeTransport.autoparkId,
        locationAddress: activeTransport.location.locationAddress,
        parkingAddress: activeTransport.location.parkingAddress,
        comment: activeTransport.comment,
        accessiblePositionId: activeTransport.accessiblePositionId,
        exploitationStart: moment(activeTransport.location.exploitationStart),
        stateNumber: activeTransport.stateNumber,
        inventoryNumber: activeTransport.vehicle.inventoryNumber ?? undefined,
        assetNumber: activeTransport.vehicle.assetNumber,
        vinCode: activeTransport.vehicle.vinCode,
        telematicsId: activeTransport.general.telematicsId ?? undefined,
        currentMileage: activeTransport.currentMileage,
        bodyNumber: activeTransport.vehicle.bodyNumber ?? undefined,
        chassisNumber: activeTransport.vehicle.chassisNumber ?? undefined,
        certificateNumber: activeTransport.documents.certificateNumber,
        certificateIssuedDate: moment(activeTransport.documents.certificateIssuedDate),
        vehicleType: activeTransport.documents.vehicleType,
        balanceUnitNumber: activeTransport.balanceUnitNumber,
        facility: activeTransport.facility,
        equipmentUnitSystemNumber: activeTransport.equipmentUnitSystemNumber,
        type: activeTransport.vehicle.type,
        subtype: activeTransport.vehicle.subtype,
      }
      : undefined,
    [activeTransport]
  );

  const handleFinish = (data: IFormData) => {
    if (JSON.stringify(data) === JSON.stringify(initialValues)) {
      handleClose();
    } else {
      if (activeTransport) {
        const query: EditTransportRequest = {
          contractorId: data.contractorId,
          autoparkId: data.autoparkId,
          location: {
            exploitationStart: data.exploitationStart.valueOf(),
            locationAddress: data.locationAddress,
            parkingAddress: data.parkingAddress,
          },
          vehicle: {
            id: activeTransport.vehicle.id,
            stateNumber: data.stateNumber,
            inventoryNumber: data.inventoryNumber?.length ? data.inventoryNumber : null,
            assetNumber: data.assetNumber,
            vinCode: data.vinCode,
            telematicsId: data.telematicsId?.length ? data.telematicsId : null,
            currentMileage: data.currentMileage,
            bodyNumber: data.bodyNumber?.length ? data.bodyNumber : null,
            chassisNumber: data.chassisNumber?.length ? data.chassisNumber : null,
            subtypeId: data.subtype,
          },
          documents: {
            certificateNumber: data.certificateNumber,
            certificateIssuedDate: data.certificateIssuedDate.valueOf(),
            vehicleType: data.vehicleType,
          },
          comment: data.comment,
          accessiblePositionId: data.accessiblePositionId,
          balanceUnitNumber: data.balanceUnitNumber,
          facility: data.facility,
          equipmentUnitSystemNumber: data.equipmentUnitSystemNumber,
        } as EditTransportRequest;

        editTransport(query)
          .then(update)
          .then(handleClose)
          .catch(ignore);
      }
    }
  };

  return (
    <Modal
      visible={stateShowModal.isOpen && stateShowModal.type === 'editTransport'}
      destroyOnClose
      title={t.Transport.modal[stateShowModal.type]}
      className={styles.modal}
      okText={t.global.save}
      cancelText={t.global.cancel}
      okButtonProps={{ form: FORM_ID, htmlType: 'submit' }}
      cancelButtonProps={{ type: 'text' }}
      closeIcon={<Icon type="closeModal" className={styles.modalIcon} />}
      confirmLoading={isLoading}
      onCancel={handleClose}
    >
      <TransportEditForm initialValues={initialValues!} onFinish={handleFinish} />
    </Modal>
  );
};

export default TransportEditModal;
