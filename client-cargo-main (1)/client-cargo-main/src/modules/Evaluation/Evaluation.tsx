import './static/modalStyle.scss';

import React, { FC } from 'react';
import { Modal } from 'antd';
import { FormInstance } from 'antd/es/form/hooks/useForm';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';

import { EvaluationForm } from './Components/EvaluationForm/EvaluationForm';
import { ReactComponent as CloseIcon } from './static/icons/closeIcon.svg';

interface Props {
  form: FormInstance;
  request: CargoRequestModel;
  requestStatus: number | undefined;
  onCancel: () => void;
  onSubmit: (values: any) => void;
  onClickDetailedHandler: (id: string) => void;
  title: string;
  subTitle: string;
  visible: boolean;
  disabling: boolean;
  setDisabling: (value: boolean) => void;
}

export const Evaluation: FC<Props> = props => {
  const {
    form,
    request,
    requestStatus,
    onCancel,
    onSubmit,
    onClickDetailedHandler,
    title,
    subTitle,
    visible,
    disabling,
    setDisabling,
  } = props;

  return (
    <Modal
      title={null}
      footer={null}
      open={visible}
      centered={true}
      closeIcon={<CloseIcon />}
      onCancel={onCancel}
      closable={!requestStatus}
    >

      {request.status === 'CARGO_SHIPMENT_FINISHED' && (
        <EvaluationForm
          title={title}
          subTitle={subTitle}
          request={request}
          onClickDetailedHandler={onClickDetailedHandler}
          form={form}
          onSubmit={onSubmit}
          disabling={disabling}
          setDisabling={setDisabling}
        />
      )}
    </Modal>
  );
};
