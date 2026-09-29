import { SaveOutlined, StopOutlined } from '@ant-design/icons';
import { carTypeDescriptions } from '@sber-sbertransport/mf-core';
import {
  Button, Col, Form, Input, InputNumber, PageHeader, Row, Select, Tooltip
} from 'antd';
import Checkbox from 'antd/lib/checkbox/Checkbox';
import { observer } from 'mobx-react';
import React, { useEffect, useState } from 'react';

import { GET_THIRD_PARTY_AGREEMENT_FOR_PERSONAL_TRANSPORT } from 'constants/constants.env';

import { ValidationRules } from 'shared/fieldValidationRules';
import { useDownloadUrl } from 'shared/hooks/useDownloadUrl';

import PersonalCarCardUpload from './Components/PersonalCarCardUpload';
import {
  PersonalCars,
  PersonalCarsCyrillic,
  PersonalCarsTexts,
  PersonalCarsTextsCyrillic,
  PersonalOwnerInformation,
  PersonalOwnerInformationCyrillic
} from './PersonalCars.constants';
import style from './personalCars.module.scss';
import { usePersonalCar } from './usePersonalCard';
import { isOwnerThirdParty } from './utils';

export const PersonalCarsCard: React.FC = observer((): JSX.Element => {
  const {
    onFinish, goToPersonalCarsList, form, osagoHandler, specificCar,
  } = usePersonalCar();
  const [isAccepted, setIsAccepted] = useState<boolean>(false);
  const [isThirdParty, setIsThirdParty] = useState(false);
  const [isThirdPartyAccepted, setIsThirdPartyAccepted] = useState(false);

  useEffect(() => {
    setIsAccepted(!!specificCar);
    setIsThirdParty(isOwnerThirdParty(form.getFieldValue(PersonalCars.ownerInfo)));
    setIsThirdPartyAccepted(form.getFieldValue(PersonalCars.persDataAccept));
  }, [form, specificCar]);

  const { download: downloadTemplate } = useDownloadUrl(GET_THIRD_PARTY_AGREEMENT_FOR_PERSONAL_TRANSPORT);

  return (
    <div className={style.personal_cars_card_wrapper}>
      <PageHeader title={PersonalCarsTextsCyrillic[PersonalCarsTexts.pageHeader]} onBack={goToPersonalCarsList} />
      <Row>
        <Col span={6}>
          <PersonalCarCardUpload updateWithOsago={osagoHandler} />
        </Col>
      </Row>
      <Form
        layout="vertical"
        form={form}
        name="create-request"
        size="middle"
        onFinish={onFinish}
      >
        <Form.Item name={PersonalCars.id} noStyle={true}>
          <Input type="hidden" />
        </Form.Item>
        <Form.Item
          name={PersonalCars.transportType}
          label={PersonalCarsCyrillic[PersonalCars.transportType]}
          rules={[ValidationRules.general.required]}
        >
          <Select
            placeholder={PersonalCarsCyrillic[PersonalCars.selectTransportType]}
            options={Object.entries(carTypeDescriptions).map(([key, value]) => ({
              value: key,
              label: value,
            }))}
          />
        </Form.Item>
        <Form.Item
          name={PersonalCars.brandName}
          label={PersonalCarsCyrillic[PersonalCars.brandName]}
          rules={[ValidationRules.general.required, ValidationRules.general.maxLength(13)]}
        >
          <Input type="string" required={true} />
        </Form.Item>
        <Form.Item
          name={PersonalCars.model}
          label={PersonalCarsCyrillic[PersonalCars.model]}
          rules={[ValidationRules.general.required, ValidationRules.general.maxLength(30)]}
        >
          <Input type="string" required={true} />
        </Form.Item>
        <Form.Item
          name={PersonalCars.engineVolume}
          label={PersonalCarsCyrillic[PersonalCars.engineVolume]}
          rules={[ValidationRules.general.required, ValidationRules.general.maxLength(5)]}
        >
          <InputNumber style={{ width: '100%' }} required={true} />
        </Form.Item>
        <Form.Item
          name={PersonalCars.registrationNumber}
          label={PersonalCarsCyrillic[PersonalCars.registrationNumber]}
          rules={[ValidationRules.general.required, ValidationRules.general.minMaxLength(7, 9)]}
        >
          <Input type="string" required={true} />
        </Form.Item>

        <Form.Item
          noStyle={true}
          shouldUpdate={(prevValues, currentValues): boolean => {
            return prevValues.transportType !== currentValues.transportType;
          }}
        >
          {({ getFieldValue }): JSX.Element | boolean => {
            return (
              getFieldValue('transportType') !== 'MOTORCYCLE' && (
                <Form.Item
                  name={PersonalCars.passengerSeatsCount}
                  label={PersonalCarsCyrillic[PersonalCars.passengerSeatsCount]}
                  rules={[ValidationRules.general.required, ValidationRules.general.maxLength(2)]}
                >
                  <Input type="number" required={true} />
                </Form.Item>
              )
            );
          }}
        </Form.Item>

        <Form.Item
          name={PersonalCars.registrationCertificate}
          label={PersonalCarsCyrillic[PersonalCars.registrationCertificate]}
          rules={[ValidationRules.general.required, ValidationRules.general.maxLength(10)]}
        >
          <Input type="string" required={true} />
        </Form.Item>
        <Form.Item
          name={PersonalCars.insuranceNumber}
          label={PersonalCarsCyrillic[PersonalCars.insuranceNumber]}
          rules={[ValidationRules.general.required, ValidationRules.general.maxLength(20)]}
        >
          <Input type="string" required={true} />
        </Form.Item>

        <Form.Item
          name={PersonalCars.color}
          label={PersonalCarsCyrillic[PersonalCars.color]}
          rules={[ValidationRules.general.maxLength(128)]}
        >
          <Input type="string" />
        </Form.Item>
        <Form.Item
          name={PersonalCars.ownerInfo}
          label={PersonalCarsCyrillic[PersonalCars.ownerInfo]}
          rules={[ValidationRules.general.required]}
        >
          <Select
            options={Object.values(PersonalOwnerInformation).map(title => ({
              value: title,
              label: PersonalOwnerInformationCyrillic[title],
            }))}
            onChange={(value?: string) => setIsThirdParty(isOwnerThirdParty(value))}
          />
        </Form.Item>

        {isThirdParty && (
          <>
            <Form.Item name={PersonalCars.persDataAccept} valuePropName="checked">
              <Checkbox className={style.required} onChange={e => setIsThirdPartyAccepted(e.target.checked)}>
                <Tooltip title={PersonalCarsCyrillic[PersonalCars.thirdPartyHint]}>
                  Обязуюсь по требованию проверяющих органов предоставить оригинал согласия ПДН (приложение)
                </Tooltip>
              </Checkbox>
            </Form.Item>
            <Button
              size="middle"
              htmlType="submit"
              onClick={downloadTemplate}
            >
              Скачать шаблон ПДН
            </Button>
          </>
        )}

        <Form.Item>
          <Checkbox
            checked={isAccepted}
            onChange={() => setIsAccepted(!isAccepted)}
            className={style.required}
          >
            <Tooltip title={PersonalCarsCyrillic[PersonalCars.hint]}>
              Достоверность введеных сведений об автомобиле и собственнике подтверждаю
            </Tooltip>
          </Checkbox>
        </Form.Item>

        <div className={style.buttons_wrapper}>
          <Button
            disabled={!isAccepted || (isThirdParty && !isThirdPartyAccepted)}
            icon={<SaveOutlined />}
            size="middle"
            htmlType="submit"
          >
            {PersonalCarsTextsCyrillic[PersonalCarsTexts.save]}
          </Button>

          <Button
            icon={<StopOutlined />}
            size="middle"
            danger={true}
            onClick={goToPersonalCarsList}
          >
            {PersonalCarsTextsCyrillic[PersonalCarsTexts.cancel]}
          </Button>
        </div>
      </Form>
    </div>
  );
});
