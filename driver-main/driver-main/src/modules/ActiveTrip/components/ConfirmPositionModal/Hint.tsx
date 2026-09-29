import { FC } from 'react';
import { FormInstance } from 'antd-mobile/es/components/form';
import styles from './ConfirmPositionModal.module.scss';

interface HintProps {
  title: string;
  form: FormInstance;
}

const Hint: FC<HintProps> = ({
  title,
  form,
}) => {
  const onClick = () => {
    form.setFieldsValue({
      reason: title,
    });
  };

  return (
    <div className={styles.hint} onClick={onClick}>
      {title}
    </div>
  );
};

export default Hint;
