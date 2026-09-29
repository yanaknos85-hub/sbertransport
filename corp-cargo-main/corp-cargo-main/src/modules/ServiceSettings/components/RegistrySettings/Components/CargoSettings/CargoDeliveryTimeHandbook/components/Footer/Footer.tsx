import { Button, Popconfirm } from 'antd';
import { useTranslation } from 'i18n';
import React, { Dispatch, FC, SetStateAction } from 'react';
import { FormInstance } from 'antd/es/form';
import styles from './Footer.module.scss';
import { useFormActions } from '../../hooks/useFormActions';

interface Props {
  form: FormInstance;
  setBusy: Dispatch<SetStateAction<boolean>>;
  settingsId?: string;
}

export const Footer: FC<Props> = ({ form, setBusy }) => {
  const { t } = useTranslation();
  const { onSave, onCancel } = useFormActions(form, setBusy);

  const getPopConfirm = (title: string, onConfirmFunc: () => void, getBtnFunc: () => JSX.Element): JSX.Element => (
    <Popconfirm
      placement="top"
      title={title}
      onConfirm={onConfirmFunc}
      okText={t.global.yes}
      cancelText={t.global.no}
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

  return (
    <footer className={styles.buttonsLayout}>
      <Button
        className={styles.button}
        type="primary"
        onClick={() => onSave()}
      >
        {t.global.save}
      </Button>
      {getPopConfirm('Отменить внесённые изменения?', onCancel, getCancelBtn)}
    </footer>
  );
};
