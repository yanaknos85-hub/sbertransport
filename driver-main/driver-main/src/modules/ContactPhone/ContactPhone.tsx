import { FC, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Form } from 'antd-mobile';

import { useProfile, useEditProfile } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { routes } from 'constants/routes.constants';
import NavBar from 'components/NavBar';
import Button from 'components/Button/Button';
import FormField from 'components/Form/FormField/FormField';
import { FieldType } from 'components/Form/Field/Field';
import { validationPatterns, ValidationRules } from 'utils/fieldValidationRules';
import { formatPhoneNumber } from 'utils/formattors/formatPhoneNumber';

import styles from './ContactPhone.module.scss';

interface FormValues {
  contactPhone: string;
}

const ContactPhone: FC = () => {
  const navigate = useNavigate();
  const driver = useProfile().data;
  const { mutateAsync: editProfile, isPending } = useEditProfile();

  const [form] = Form.useForm();

  useEffect(() => {
    form.setFieldValue('contactPhone', formatPhoneNumber(driver.contactPhone));
  }, [form, driver.contactPhone]);

  const goToProfile = () => {
    setTimeout(() => navigate(routes.Profile), 500);
  };

  const onFinishHandler = (values: FormValues): void => {
    const contactPhone = values.contactPhone.replace(/\s/g, '');

    if (contactPhone) {
      editProfile({ ...driver, contactPhone }).then(goToProfile);
    }
  };

  return (
    <div className={styles.container}>
      <NavBar>Основной номер</NavBar>

      <Form
        name="contact-phone-form"
        form={form}
        onFinish={onFinishHandler}
        className={styles.form}
      >
        <FormField
          type={FieldType.Phone}
          name="contactPhone"
          rules={[
            { required: true, message: 'Пожалуйста, введите номер' },
            ValidationRules.checkingEditingPhoneNumber(driver.contactPhone),
            {
              pattern: validationPatterns.numberValidation,
              message: 'Неправильный формат телефона',
            },
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

export default ContactPhone;
