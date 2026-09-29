import { FC, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Form } from 'antd-mobile';

import { useProfile, useEditProfile } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { routes } from 'constants/routes.constants';
import NavBar from 'components/NavBar';
import Button from 'components/Button/Button';
import FormField from 'components/Form/FormField/FormField';
import { FieldType } from 'components/Form/Field/Field';
import { ValidationRules } from 'utils/fieldValidationRules';
import { formatDriverLicense } from 'utils/formattors/formatDriverLicense';

import styles from './DriverLicense.module.scss';

interface FormValues {
  driverLicenseNumber: string;
}

const DriverLicense: FC = () => {
  const navigate = useNavigate();
  const driver = useProfile().data;
  const { mutateAsync: editProfile, isPending } = useEditProfile();

  const [form] = Form.useForm();

  useEffect(() => {
    form.setFieldValue('driverLicenseNumber', formatDriverLicense(driver.driverLicenseNumber));
  }, [form, driver.driverLicenseNumber]);

  const goToProfile = () => {
    setTimeout(() => navigate(routes.Profile), 500);
  };

  const onFinishHandler = ({ driverLicenseNumber }: FormValues): void => {
    if (driverLicenseNumber) {
      editProfile({ ...driver, driverLicenseNumber }).then(goToProfile);
    }
  };

  return (
    <div className={styles.container}>
      <NavBar>
        Водительское
        <br />
        удостоверение
      </NavBar>

      <Form
        name="driver-license-form"
        form={form}
        onFinish={onFinishHandler}
        className={styles.form}
      >
        <FormField
          type={FieldType.DriverLicense}
          name="driverLicenseNumber"
          rules={[
            { required: true, message: 'Пожалуйста, введите номер удостоверения' },
            ValidationRules.driverLicenseRule(driver.driverLicenseNumber),
          ]}
        />
        <Button
          block
          size="large"
          type="submit"
          loading={isPending}
          className={styles.btn_submit}
        >
          Сохранить
        </Button>
      </Form>
    </div>
  );
};

export default DriverLicense;
