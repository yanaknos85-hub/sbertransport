import React from 'react';
import { Link } from 'react-router-dom';
import { CloseOutlined, SearchOutlined } from '@ant-design/icons';
import { Popconfirm } from 'antd';

import { DelegatesTexts, DelegatesTextsCyrillic } from '../Delegates.constants';

import style from '../delegates.module.scss';

interface IDelegatesButtons {
  path: string;
  id: string;
  onDelete: (id: string) => void;
}

export const DelegatesButtons: React.FC<IDelegatesButtons> = ({
  path, id, onDelete,
}): JSX.Element => (
  <div className={style.personal_cars_pop_confirm}>
    <Link to={`${path}/${id}`}>
      <SearchOutlined />
    </Link>
    <div className={style.personal_cars_pop_confirm__delete}>
      <Popconfirm
        placement="left"
        title={DelegatesTextsCyrillic[DelegatesTexts.deleteConfirm]}
        onConfirm={(): void => onDelete(id)}
        okText={DelegatesTextsCyrillic[DelegatesTexts.remove]}
        cancelText={DelegatesTextsCyrillic[DelegatesTexts.cancel]}
        style={{ width: 300 }}
      >
        <CloseOutlined />
      </Popconfirm>
    </div>
  </div>
);
