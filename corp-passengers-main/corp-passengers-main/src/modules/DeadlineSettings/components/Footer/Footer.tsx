import { Button, Popconfirm } from 'antd';
import React, { Dispatch, FC, SetStateAction } from 'react';
import { FormInstance } from 'antd/es/form';
import { useTranslation } from 'i18n';
import styles from './Footer.module.scss';
import { useFormActions } from '../../hooks/useFormActions';
import { EmployeesHandbookTexts, EmployeesHandbookTextsCyrillic } from '../../../Employees/Employees.constants';

interface Props {
  form: FormInstance;
  setBusy: Dispatch<SetStateAction<boolean>>;
  settingsId?: string;
}

export const Footer: FC<Props> = ({
  form, setBusy, settingsId,
}) => {
  const { t } = useTranslation();
  const {
    onFinish, onCancel, onDefault,
  } = useFormActions(form, setBusy, settingsId);

  const getPopConfirm = (title: string, onConfirmFunc: () => void, getBtnFunc: () => JSX.Element): JSX.Element => (
    <Popconfirm
      placement="top"
      title={title}
      onConfirm={onConfirmFunc}
      okText={EmployeesHandbookTextsCyrillic[EmployeesHandbookTexts.confirm]}
      cancelText={EmployeesHandbookTextsCyrillic[EmployeesHandbookTexts.no]}
      style={{ width: 300 }}
    >
      {getBtnFunc()}
    </Popconfirm>
  );

  const getCancelBtn = (): JSX.Element => (
    <Button
      danger
      type="primary"
      className={styles.button}
    >
      {t.global.cancel}
    </Button>
  );

  const getBackDefaultBtn = (): JSX.Element => (
    <Button className={styles.backToDefaults} type="primary">
      {t.DeadlineSettings.resetToDefaults}
    </Button>
  );

  return (
    <footer className={styles.buttonsLayout}>
      <Button
        className={styles.button}
        type="primary"
        onClick={() => onFinish()}
      >
        {t.global.save}
      </Button>
      {getPopConfirm(t.DeadlineSettings.prompts.cancelChanges, onCancel, getCancelBtn)}
      {getPopConfirm(t.DeadlineSettings.prompts.resetToDefaults, onDefault, getBackDefaultBtn)}
    </footer>
  );
};
