import { DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { Popconfirm } from 'antd';
import classNames from 'classnames';
import React from 'react';
import { Link } from 'react-router-dom';

import './override.scss';
import style from './tableEditButtons.module.scss';

interface ITableEditButtons {
  path: string;
  id: string;
  onDelete: (id: string) => void;
  title: string;
  okText: string;
  cancelText: string;
}

export const TableEditButtons: React.FC<ITableEditButtons> = (props: ITableEditButtons): JSX.Element => {
  const {
    path, id, onDelete, title, okText, cancelText,
  } = props;

  return (
    <div className={classNames(style.table_edit_buttons_pop_confirm, 'table_edit_buttons')}>
      <Link to={`${path}/${id}`}>
        <EditOutlined />
      </Link>
      <div className={style.table_edit_buttons__delete}>
        <Popconfirm
          placement="left"
          title={title}
          onConfirm={(): void => onDelete(id)}
          okText={okText}
          cancelText={cancelText}
          style={{ width: 300 }}
        >
          <DeleteOutlined />
        </Popconfirm>
      </div>
    </div>
  );
};
