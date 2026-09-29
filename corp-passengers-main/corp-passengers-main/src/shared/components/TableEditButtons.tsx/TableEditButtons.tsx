import './override.scss';

import React from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'i18n';
import { Button as ButtonAnt, Popconfirm } from 'antd';
import { DeleteOutlined, EditOutlined } from '@ant-design/icons';
import classNames from 'classnames';

import { UUID } from 'utils/io-ts';
import { Button } from '../Button/Button';
import styles from './tableEditButtons.module.scss';

interface ITableEditButtons {
  theme?: 'primary' | 'secondary';
  deleteIconColor?: string;
  path?: string;
  id: string;
  onDelete?: (id: string | UUID) => void;
  isEdit?: boolean;
  onEdit?: (id: string | UUID) => void;
  title?: string;
  okText?: string;
  cancelText?: string;
}

export const TableEditButtons: React.FC<ITableEditButtons> = (props: ITableEditButtons): JSX.Element => {
  const { t } = useTranslation();
  const {
    path,
    id,
    onEdit,
    onDelete,
    title = t.global.deleteConfirm,
    okText = t.global.delete,
    cancelText = t.global.cancel,
    theme = 'primary',
    deleteIconColor = 'red',
    isEdit = true,
  } = props;

  return (
    <div className={classNames(styles.tableEditButtons)}>
      {isEdit && !onEdit && (
        <Link to={`${path}/${id}`}>
          <EditOutlined />
        </Link>
      )}
      {Boolean(onEdit) && isEdit && (
        <ButtonAnt onClick={() => onEdit?.(id)} style={{ border: 'none', background: 'none' }}>
          <EditOutlined />
        </ButtonAnt>
      )}
      {onDelete && (
        <div className={styles.tableEditButtonsDelete}>
          <Popconfirm
            placement="left"
            title={title}
            onConfirm={() => onDelete(id)}
            okText={okText}
            cancelText={cancelText}
            overlayClassName={styles.tableEditTooltip}
          >
            {theme === 'primary' ? (
              <DeleteOutlined color={deleteIconColor} />
            ) : (
              <Button>
                <DeleteOutlined />
              </Button>
            )}
          </Popconfirm>
        </div>
      )}
    </div>
  );
};
