import { ILogger } from '@sber-sbertransport/mf-core';

import { Col, Form, Row } from 'antd';
import { FormInstance } from 'antd/lib/form';
import React, { useEffect, useRef } from 'react';

import { ValidationRules } from 'shared/fieldValidationRules';

import { sizesToUnits, toMillimeters } from 'utils';
import { FieldType } from '../Field/Field';
import FormField from '../FormField/FormField';
import { Modal } from './CargoType.style';
import { CargoCreateType, CargoTypeCategoryNameEnum, CargoTypeNameEnum } from 'types/Cargo';

interface ModalFormProps {
  visible: boolean;
  onSave: () => void;
  onCancel: () => void;
  postCargoType: (data: CargoCreateType) => Promise<void>;
  logger: ILogger;
}

const useResetFormOnCloseModal = ({ form, visible }: { form: FormInstance; visible: boolean }) => {
  const prevVisibleRef = useRef<boolean>();

  useEffect(() => {
    prevVisibleRef.current = visible;
  }, [visible]);
  const prevVisible = prevVisibleRef.current;

  useEffect(() => {
    if (!visible && prevVisible) {
      form.resetFields();
    }
  }, [form, prevVisible, visible]);
};

const ModalForm: React.FC<ModalFormProps> = ({
  visible, onSave, onCancel, postCargoType, logger,
}) => {
  const [form] = Form.useForm();

  const initialValues = {
    checkboxSizes: false,
  };

  useResetFormOnCloseModal({
    form,
    visible,
  });

  const onOk = () => {
    form.submit();

    const {
      length, width, height,
    } = sizesToUnits(form.getFieldsValue(['length', 'width', 'height']));

    const request: Omit<CargoCreateType, 'id'> = {
      name: form.getFieldValue('name'),
      type: CargoTypeNameEnum.OTHER,
      category: CargoTypeCategoryNameEnum.OTHER,
      length,
      width,
      height,
      weight: form.getFieldValue('weight'),
      volume: toMillimeters(length * width * height),
      active: true,
    };

    postCargoType(request)
      .then(() => logger.toMessage('success', 'Данные успешно отправлены'))
      .catch(() => {
        logger.toMessage('error', 'Ошибка дублирования записи. Данный тип груза уже существует');
      });

    onSave();
  };

  const field = {
    name: {
      type: FieldType.input,
      label: 'Название',
      name: 'name',
      params: {
        rules: [ValidationRules.general.maxLength(50)],
        placeholder: 'Введите название груза',
      },
    },
    length: {
      type: FieldType.number,
      label: 'Длина, см',
      name: 'length',
      params: {
        rules: [ValidationRules.general.min(0)],
        controls: false,
        min: 0,
      },
    },
    width: {
      type: FieldType.number,
      label: 'Ширина, см',
      name: 'width',
      params: {
        rules: [ValidationRules.general.min(0)],
        controls: false,
        min: 0,
      },
    },
    height: {
      type: FieldType.number,
      label: 'Высота, см',
      name: 'height',
      params: {
        rules: [ValidationRules.general.min(0)],
        controls: false,
        min: 0,
      },
    },
    weight: {
      type: FieldType.number,
      label: 'Вес, кг',
      name: 'weight',
      params: {
        rules: [ValidationRules.general.min(0)],
        controls: false,
        min: 0,
      },
    },
  };

  return (
    <Modal
      title="Введите параметры груза"
      visible={visible}
      onOk={onOk}
      onCancel={onCancel}
      centered={true}
      closable={true}
      keyboard={true}
      width={620}
      okText="Сохранить"
      cancelText="Отмена"
    >
      <p>Если вы не нашли нужный груз в списке, вы можете вручную указать его габариты</p>
      <Form
        form={form}
        layout="vertical"
        name="cargoNewForm"
        initialValues={initialValues}
      >
        <FormField {...field.name} />
        <Row gutter={16}>
          <Col span={6}>
            <FormField {...field.length} />
          </Col>
          <Col span={6}>
            <FormField {...field.width} />
          </Col>
          <Col span={6}>
            <FormField {...field.height} />
          </Col>
          <Col span={6}>
            <FormField {...field.weight} />
          </Col>
        </Row>
      </Form>
    </Modal>
  );
};

export default ModalForm;
