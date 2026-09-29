import { OrgStructureType } from '@sber-sbertransport/mf-core/dist/constants/constants';
import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';
import {
  Form, Upload, Switch, Checkbox, Modal
} from 'antd';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { EmployeeDetailedModel } from '@sber-sbertransport/mf-core';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import User from 'shared/components/Images/user.png';
import { beforeUpload } from 'utils/fileUtils/fileUploadUtils';
import CourierAgreementModal from '../CourierAgreementModal/CourierAgreementModal';
import {
  BasicInformation, BasicInformationWrapper, CourierButtonWrapper, CourierWrapper, PaddingText, Position, PostInformation, ServiceNumber, StyledHeaderProfile, SwitchWrapper, UserAvatar, UserFullNameWrapper, UserName
} from './styled';
import Courier from 'shared/images/cargo/courier.png';
import { useCourierRole, useDeleteCourierRole } from 'api/domestic-courier';
import { ReactComponent as CloseIcon } from 'components/Evaluation/static/icons/closeIcon.svg';
import { useRole } from 'utils/useRole';
import { courierRoleText } from './utils';
import { ROLE } from 'constants/constants.app';

import './overwrite.scss';
import styles from './HeaderProfile.module.scss';

const HeaderProfile: FC<{ selfDetailed: EmployeeDetailedModel }> = observer(({ selfDetailed }) => {
  const {
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.mappedStore]: mappedStore,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.authStore]: authStore,
    [StoreNames.selfStore]: { selfEmployee },
    logger,
  } = useAppStoreContext();

  const name = selfDetailed?.fullNameString?.split(' ')[0];
  const lastNameAndPatronymic = `${selfDetailed?.fullNameString?.split(' ')[1]} ${selfDetailed?.fullNameString?.split(' ')[2]}`;
  const checkingPosition = mappedStore.selfEmployeeDetailed.position !== employeeStore.selfEmployee.positionId;
  const roles = useRole();
  const courier = roles.find(role => role.includes(ROLE.DOMESTIC_COURIER));
  const [form] = Form.useForm();
  const [imageFile, setImageFile] = useState(null);

  const [isModalVisible, setIsModalVisible] = useState(false);
  const [agreed, setAgreed] = useState(false);
  const [isCheckboxEnabled, setIsCheckboxEnabled] = useState(false);
  const [showChildModal, setShowChildModal] = useState(false);

  const [addCourierRole] = useCourierRole();
  const [deleteCourierRole] = useDeleteCourierRole();

  const handleChange = () => {
    const file = form.getFieldValue('avatar');
    if (file.file.status !== 'uploading') {
      const reader = new FileReader();
      reader.onload = () => {
        const img = new Image();
        img.onload = () => {
          const canvas = document.createElement('canvas');
          const ctx = canvas.getContext('2d');
          canvas.width = 300;
          canvas.height = img.height * (canvas.width / img.width);
          ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
          canvas.toBlob(blob => {
            setImageFile(new File([blob], file.file.name, { type: 'image/jpeg' }));
          }, 'image/jpeg');
        };
        img.src = reader.result as string;
      };
      reader.readAsDataURL(file.file.originFileObj);
    }
  };

  const handleCourierClick = () => {
    setAgreed(false);
    setIsCheckboxEnabled(false);
    setIsModalVisible(true);
  };

  const handleOk = async () => {
    if (agreed) {
      try {
        await addCourierRole();
        setTimeout(() => {
          authStore.updateRefreshToken(authStore.refreshToken);
        }, 1000);
      } catch (error) {
        console.error('Ошибка при добавлении роли курьера:', error);
      } finally {
        setIsModalVisible(false);
      }
    }
  };

  const handleCancel = () => {
    setAgreed(false);
    setIsCheckboxEnabled(false);
    setIsModalVisible(false);
  };

  const handleChildAgree = () => {
    setAgreed(true);
    setIsCheckboxEnabled(true);
    setShowChildModal(false);
  };

  const handleChildCancel = () => {
    setShowChildModal(false);
  };

  const handleParentCheckboxChange = (e: CheckboxChangeEvent) => {
    if (isCheckboxEnabled && e.target.checked === false) {
      setAgreed(false);
      setIsCheckboxEnabled(false);
    }
  };

  const handleTextClick = () => {
    if (!agreed) {
      setShowChildModal(true);
    }
  };

  const handleSwitchChange = async (checked: boolean) => {
    if (checked) {
      setIsModalVisible(true);
    } else {
      try {
        await deleteCourierRole();
        setTimeout(() => {
          authStore.updateRefreshToken(authStore.refreshToken);
        }, 1000);
      } catch (error) {
        console.error('Ошибка при удалении роли курьера:', error);
      }
    }
  };

  useEffect(() => {
    if (imageFile) {
      tripStore.setUserAvatar(employeeStore.selfEmployee.userId, imageFile).then(() => tripStore.getUserAvatar(employeeStore.selfEmployee.userId).then(avatar => tripStore.addAvatar(avatar))
      );
    }
  }, [imageFile]);
  return (
    <StyledHeaderProfile>
      <BasicInformationWrapper>
        <Form
          layout="vertical"
          form={form}
          name="create-request"
          size="middle"
        >
          <Form.Item
            name="avatar"
          >
            <Upload
              name="avatar"
              showUploadList={false}
              onChange={handleChange}
              beforeUpload={(e): boolean => beforeUpload(e, logger)}
            >
              <div className="AvatarWrapper">
                <UserAvatar src={tripStore.avatar ? tripStore.avatar : User} />
              </div>
            </Upload>
          </Form.Item>
        </Form>
        <BasicInformation>
          <UserFullNameWrapper>
            <UserName>
              {name}
            </UserName>
            <UserName>
              {lastNameAndPatronymic}
            </UserName>
          </UserFullNameWrapper>
          <PostInformation>
            <Position>
              {checkingPosition && selfDetailed?.position}
            </Position>
            <ServiceNumber>
              {selfDetailed?.personnelNumber}
            </ServiceNumber>
          </PostInformation>
        </BasicInformation>
      </BasicInformationWrapper>
      {/*
        Проверка нужна чтобы тогл подключения роли не был виден внешним клиентам.
        Дублируется в SideMenu.tsx
        У основных тестовых пользователей тип структуры EXTERNAL.
        Для работы с разделом Биржа нужно заменить проверку на EXTERNAL.
        Для теста нужно сначала зайти как интернал на сайт и в ЛК, потом в бд влючить экстернал у пользака,
        далее включить роль и потом опять включить интернал.
      */}
      {selfEmployee.orgStructureType === OrgStructureType.INTERNAL && (
        <CourierWrapper>
          {courier ? (
            <SwitchWrapper>
              <span>Внутренний курьер</span>
              <Switch checked={!!courier} onChange={handleSwitchChange} />
            </SwitchWrapper>
          ) : (
            <CourierButtonWrapper
              onClick={handleCourierClick}
            >
              <img src={Courier} alt="Courier" />
            </CourierButtonWrapper>
          )}
        </CourierWrapper>
      )}
      <Modal
        title="Подключить роль курьера"
        className={styles.modal}
        open={isModalVisible}
        onOk={handleOk}
        onCancel={handleCancel}
        okText="Подключить"
        cancelText="Отмена"
        width={800}
        centered
        closeIcon={<CloseIcon />}
        okButtonProps={{ disabled: !agreed }}
      >
        <PaddingText>
          {courierRoleText}
        </PaddingText>
        <PaddingText>
          <Checkbox
            checked={agreed}
            disabled={!isCheckboxEnabled}
            onChange={handleParentCheckboxChange}
          >
            Я ознакомился и согласен с{' '}
            <span
              style={{ color: '#10BF6A', cursor: 'pointer' }}
              onClick={handleTextClick}
            >
              сервисом предоставления услуг
            </span>
          </Checkbox>
        </PaddingText>
      </Modal>
      <CourierAgreementModal
        visible={showChildModal}
        onAgree={handleChildAgree}
        onCancel={handleChildCancel}
      />
    </StyledHeaderProfile>
  );
});
export default HeaderProfile;
