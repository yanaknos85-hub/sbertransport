import React, { useState, FC } from 'react';
import { Modal } from 'antd';
import { useTranslation } from 'i18n';

import { useCreateTransport } from 'api/transport/transport.api';
import { CreateTransportRequest } from 'api/transport/transport.types';
import { ignore } from 'utils/utils';
import { Icon } from 'components/Icon/Icon';
import { Stepper } from 'components/Stepper';
import { Button } from 'components/Button';
import { Steps, StepsNames } from 'modules/Vehicles/constants/transport';

import { useModalForm } from '../../context/ModalForm';
import VehicleForm, { IFormData as IVehicleFormData } from './Vehicle/Form';
import DocumentsForm, { IFormData as IDocumentsFormData } from './Documents/Form';
import GeneralForm, { IFormData as IGeneralFormData } from './General/Form';

import styles from './ModalFormTransport.module.scss';

const ModalFormTransport: FC = () => {
  const { t } = useTranslation();
  const { stateShowModal, handleClose } = useModalForm();

  const [step, setStep] = useState(Steps.Vehicle);
  const [query, setQuery] = useState<Partial<CreateTransportRequest>>();

  const [vehicleData, setVehicleData] = useState<IVehicleFormData>();
  const [documentsData, setDocumentsData] = useState<IDocumentsFormData>();

  const [createTransport, { isLoading: isLoadingCreate }] = useCreateTransport();

  const isModalLoading = isLoadingCreate;

  const reset = () => {
    setStep(Steps.Vehicle);
    setQuery(undefined);
    setVehicleData(undefined);
    setDocumentsData(undefined);
  };

  const handleCancel = () => {
    reset();
    handleClose();
  };

  const handleVehicleFinish = (data: IVehicleFormData) => {
    setVehicleData(data);
    setQuery({ vehicle: { id: data.vehicleId, year: data.year } } as unknown as CreateTransportRequest);
    setStep(step => step + 1);
  };

  const handleDocumentsFinish = (data: IDocumentsFormData) => {
    if (query) {
      setDocumentsData(data);
      setQuery({
        documents: {
          passportNumber: data.passportNumber,
          passportIssuedDate: data.passportIssuedDate.valueOf(),
          brandByPassport: data.brandByPassport,
          modelByPassport: data.modelByPassport,
          certificateNumber: data.certificateNumber,
          certificateIssuedDate: data.certificateIssuedDate.valueOf(),
          vehicleType: data.vehicleType,
        },
        vehicle: {
          id: query.vehicle!.id,
          year: query.vehicle!.year,
          stateNumber: data.stateNumber,
          currentMileage: data.currentMileage,
          vinCode: data.vinCode,
          assetNumber: data.assetNumber,
          inventoryNumber: data.inventoryNumber?.length ? data.inventoryNumber : null,
          bodyNumber: data.bodyNumber?.length ? data.bodyNumber : null,
          chassisNumber: data.chassisNumber?.length ? data.chassisNumber : null,
        },
      } as unknown as CreateTransportRequest);
      setStep(step => step + 1);
    }
  };

  const handleGeneralFinish = (data: IGeneralFormData) => {
    if (query) {
      const finalQuery: CreateTransportRequest = {
        contractorId: data.contractorId,
        autoparkId: data.autoparkId,
        location: {
          exploitationStart: data.exploitationStart.valueOf(),
          locationAddress: data.locationAddress,
          parkingAddress: data.parkingAddress,
        },
        vehicle: {
          ...query.vehicle,
          bodyColor: data.bodyColor,
          subtypeId: data.subtypeId,
          telematicsId: data.telematicsId?.length ? data.telematicsId : null,
          year: query.vehicle!.year,
        },
        documents: query.documents,
        comment: data.comment,
        accessiblePositionId: data.accessiblePositionId,
        balanceUnitNumber: data.balanceUnitNumber,
        facility: data.facility,
        equipmentUnitSystemNumber: data.equipmentUnitSystemNumber,
      } as unknown as CreateTransportRequest;

      createTransport(finalQuery)
        .then(handleCancel)
        .catch(ignore);
    }
  };

  const chooseForm = () => {
    switch (step) {
      case Steps.Vehicle:
        return <VehicleForm initialValues={vehicleData} onFinish={handleVehicleFinish} />;
      case Steps.Documents:
        return <DocumentsForm initialValues={documentsData} onFinish={handleDocumentsFinish} />;
      case Steps.General:
        return <GeneralForm loading={isModalLoading} onFinish={handleGeneralFinish} />;
      default:
        return null;
    }
  };

  return (
    <Modal
      visible={stateShowModal.isOpen && stateShowModal.type === 'addTransport'}
      destroyOnClose
      title={t.Transport.modal[stateShowModal.type]}
      closeIcon={<Icon type="closeModal" className={styles.ModalIcon} />}
      footer={null}
      className={styles.modal}
      onCancel={handleCancel}
    >
      <div className={styles.content}>
        <Stepper
          currentStep={step}
          steps={Object.values(StepsNames)}
          className={styles.stepper}
        />

        {chooseForm()}

        <Button
          type="link"
          className={styles.cancelBtn}
          disabled={isModalLoading}
          hidden={!step}
          onClick={() => setStep(step - 1)}
        >
          Назад
        </Button>
      </div>
    </Modal>
  );
};

export default ModalFormTransport;
