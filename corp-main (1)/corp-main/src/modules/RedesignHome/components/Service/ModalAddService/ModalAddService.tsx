import React, { FC, useEffect, useState } from 'react';
import {
  Button, Checkbox, Form, Input, Modal, Select, Tooltip
} from 'antd';
import { useForm } from 'antd/lib/form/Form';
import TextArea from 'antd/lib/input/TextArea';
import { observer } from 'mobx-react';

import { Contacts, SDOContacts } from 'constants/constants.app';

import { descriptionServicesTypes, ITypesService, servicesTypes } from 'modules/RedesignHome/types/Home.types';
import { Icon } from 'shared/components/Icon';
import DescriptionIcon from 'shared/images/descriptionIcon.svg';
import { typesServiceCargo, typesServiceCarService, typesServicePassengers } from 'modules/RedesignHome/constants/services';
import { OrganizationTransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import ChevronSmall from 'shared/images/menu 2.0/ChevronSmall';
import { LabeledValue } from 'utils';
import { useGetListRegions } from 'api/tariffs';
import { useProfile } from 'api/profile';
import { formatFullName } from 'utils/formatFullName';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { ValidationRules } from 'shared/fieldValidationRules';

import styles from './styles.module.scss';

interface ModalAddServiceProps {
  isVisible?: boolean;
  handleDone: () => void;
  selectedService: string;
  imageSrc?: string;
  organizationTransportTypes: OrganizationTransportTypes[];
}

const ModalAddService: FC<ModalAddServiceProps> = observer(({
  isVisible, handleDone, selectedService, imageSrc, organizationTransportTypes,
}) => {
  const {
    [StoreNames.homeStore]: homeStore, [StoreNames.configStore]: configStore, logger,
  } = useAppStoreContext();
  const IS_SDO = configStore.env.IS_SDO;
  const [typesService, setTypesService] = useState<ITypesService[] | undefined>();
  const [form] = useForm();
  const regionOptions: LabeledValue[] = useGetListRegions({
    suspense: false,
  }).data?.map(({ name, id }) => ({ label: name, value: id })) ?? [];
  const {
    mobilePhone, email, firstName, lastName, patronymic,
  } = useProfile().data;
  const fullName = formatFullName(firstName, lastName, patronymic);
  const processingActiveService = (typesService: ITypesService[]) => {
    return typesService?.map(item => ({
      ...item,
      checked: !!organizationTransportTypes.find(status => status.transportType === item.name)?.active,
    }));
  };
  const { required } = ValidationRules.general;

  const getTypesService = (selectedService: string) => {
    switch (selectedService) {
      case 'passengers':
      {
        return processingActiveService(typesServicePassengers);
      }
      case 'cargo':
      {
        return processingActiveService(typesServiceCargo);
      }
      case 'carService':
      {
        return typesServiceCarService;
      }
      default:
      {
        return [];
      }
    }
  };

  useEffect(() => {
    selectedService && setTypesService(getTypesService(selectedService));
  }, [selectedService]);

  const mapIdsToLabels = (idsArray, objectsArray) => {
    return idsArray.map(id => {
      const foundObject = objectsArray.find(obj => obj.value === id);
      return foundObject ? foundObject.label : null;
    }).filter(label => label !== null);
  };

  function getTitlesByCondition(configObj, servicesArray) {
    return servicesArray
      .filter(service => configObj[service.name] === true)
      .map(service => service.title);
  }

  const handleSave = () => {
    form.validateFields().then(
      () => {
        const serviceDate = form.getFieldsValue();
        const data = {
          clientName: serviceDate.fio,
          phoneNumber: serviceDate.phoneNumber,
          email: serviceDate.email,
          receiver: IS_SDO ? SDOContacts.mailSbertransport : Contacts.mailSbertranposrt,
          territory: mapIdsToLabels(serviceDate.territoryUse, regionOptions),
          comment: serviceDate.comment,
          service: servicesTypes[selectedService],
          serviceTypes: getTitlesByCondition(serviceDate.typesService, typesService),
        };

        homeStore.saveNotification(data);
        handleDone();
        logger.toMessage('success', 'Ваша заявка на подключение услуги отправлена менеджеру');
      },
      () => {
        logger.toMessage('error', 'Проверьте заполнение полей формы!');
      }
    );
  };

  return (
    <Modal
      visible={isVisible}
      onCancel={handleDone}
      className={styles.modalAddService}
      title={servicesTypes[selectedService]}
      closeIcon={<Icon type="closeModal" className={styles.ModalIcon} />}
      footer={[
        <Button
          key="done"
          onClick={handleDone}
          type="text"
        >
          Отменить
        </Button>,
        <Button
          key="done"
          onClick={handleSave}
          type="primary"
        >
          Отправить
        </Button>,
      ]}
    >
      <Form
        layout="vertical"
        form={form}
        name="create-request"
        size="middle"
        // onFinish={onFinish}
        // onValuesChange={onValuesChange}
      >
        <div className={styles.modalAddService__content}>
          <div className={styles.wrapper__title}>
            <span className={styles.content_title}>
              {descriptionServicesTypes[selectedService]}
            </span>
            <div className={styles.content_img}>
              {imageSrc && (
              <img
                src={imageSrc}
                alt={imageSrc}
                className={styles[selectedService]}
              />
              )}
            </div>
          </div>
          <div className={styles.typesService}>
            <span className={styles.typesService_title}>Виды сервиса</span>
            <div className={styles.typesService_choosing__wrapper}>
              <Form.List name="typesService">
                {() => (
                  typesService?.map((service, index) => (
                    <div key={index} className={styles.typesService_choosing}>
                      <Form.Item
                        key={service.name}
                        name={service.name}
                        style={{ margin: '0' }}
                        valuePropName="checked"
                        initialValue={service.checked}
                      >
                        <Checkbox
                          className={service.checked ? styles.activeCheck : styles.unActiveCheck}
                        />
                      </Form.Item>
                      <span className={styles.service_title}>{service.title}</span>
                      <Tooltip placement="top" title={service.description}>
                        <img src={DescriptionIcon} alt={service.title} />
                      </Tooltip>
                    </div>
                  ))
                )}
              </Form.List>
            </div>
          </div>
          <div className={styles.territoryUse}>
            <span className={styles.territoryUse_title}>Территория использования</span>
            <Form.Item
              key="territoryUse"
              name="territoryUse"
              style={{ margin: '0' }}
              rules={[required]}
            >
              <Select
                mode="multiple"
                options={regionOptions}
                showSearch
                showArrow
                placeholder="Выберите территорию использования"
                optionFilterProp="label"
                suffixIcon={(
                  <div style={{ borderLeft: '1px solod grey' }}>
                    <ChevronSmall />
                  </div>
              )}
              />
            </Form.Item>
          </div>
          <div className={styles.contactInformation}>
            <span className={styles.contactInformation_title}>Контактные данные</span>
            <div className={styles.contactInformation_main}>
              <div>
                <Form.Item
                  key="fio"
                  name="fio"
                  style={{ margin: '0', width: 306 }}
                  label="ФИО"
                  initialValue={fullName}
                  rules={[required]}
                >
                  <Input placeholder="Укажите ФИО контактного лица" />
                </Form.Item>
                <Form.Item
                  key="phoneNumber"
                  name="phoneNumber"
                  style={{ margin: '0', width: 306 }}
                  label="Номер телефона"
                  initialValue={mobilePhone}
                >
                  <Input placeholder="Укажите номер" />
                </Form.Item>
                <Form.Item
                  key="email"
                  name="email"
                  style={{ margin: '0', width: 306 }}
                  label="E-mail"
                  initialValue={email}
                  rules={[required]}
                >
                  <Input placeholder="Укажите почту" />
                </Form.Item>
              </div>
              <Form.Item
                key="comment"
                name="comment"
                style={{ margin: '0' }}
                label="Комментарий"
              >
                <TextArea
                  placeholder="Ваши пожелания"
                  autoSize={{ minRows: 3, maxRows: 5 }}
                  maxLength={255}
                />
              </Form.Item>
            </div>
          </div>
        </div>
      </Form>
    </Modal>
  );
});

export default ModalAddService;
