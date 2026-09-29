import React, { FC } from 'react';
import {
  DeleteOutlined, EditOutlined, StarFilled, UserOutlined
} from '@ant-design/icons';
import { Avatar, Popconfirm } from 'antd';
import { useTranslation } from 'i18n';

import { CarDefault, TruckDefault, CarSpecial } from 'components/Images/Images';
import { UUID } from 'utils/io-ts';
import { IconTypes, RowData } from '../../types';
import styles from './DetailedHeader.module.scss';

export interface Props<T = RowData> {
  activeRow: T | null;
  marks: { label: string; value: string }[];
  iconType?: string;
  onEdit?: (data: T) => void;
  onDelete?: (id: UUID) => void;
}

const Icon = {
  [IconTypes.PassengerCar]: CarDefault,
  [IconTypes.CargoTruck]: TruckDefault,
  [IconTypes.SpecialCar]: CarSpecial,
};

export const DetailedHeader: FC<Props> = ({
  activeRow, marks, iconType, onEdit, onDelete,
}) => {
  const { t } = useTranslation();

  const fullName = [activeRow?.lastName, activeRow?.firstName, activeRow?.patronymic, activeRow?.stateNumber]
    .filter(Boolean)
    .join(' ');

  const handleEditClick = () => {
    if (!activeRow) {
      return;
    }

    onEdit?.(activeRow);
  };

  const handleDeleteClick = () => {
    if (!activeRow) {
      return;
    }

    onDelete?.(activeRow.id);
  };

  const rating = ((activeRow?.rating ?? 0) / 100).toString().slice(0, 3).replace('.', ',');

  return (
    <div className={styles.container}>
      {iconType ? (
        iconType === IconTypes.Avatar ? (
          <Avatar
            className={styles.avatar}
            size="large"
            icon={<UserOutlined />}
          />
        ) : (
          <div className={styles.imgWrap}>
            <img src={Icon[iconType]} alt="icon" />
          </div>
        )
      ) : null}

      <div className={styles.contentSide}>
        <div className={styles.contentHeader}>
          <div className={styles.textSide}>
            <strong className={styles.fullName}>{fullName}</strong>
            {Number.isInteger(activeRow?.rating) && (
              <div className={styles.rating}>
                <StarFilled className={styles.star} />
                {' '}
                {rating}
              </div>
            )}
          </div>
          <div className={styles.controlGroup}>
            {onEdit && (
              <button
                type="button"
                className={styles.action}
                onClick={handleEditClick}
              >
                <EditOutlined />
              </button>
            )}
            {onDelete && (
              <Popconfirm
                placement="left"
                title={t.global.deleteConfirm}
                onConfirm={handleDeleteClick}
                okText={t.global.delete}
                cancelText={t.global.cancel}
                overlayClassName={styles.tableEditTooltip}
              >
                <button type="button" className={styles.action}>
                  <DeleteOutlined />
                </button>
              </Popconfirm>
            )}
          </div>
        </div>
        <div className={styles.uniqValues}>
          {marks.map(mark => (
            <div className={styles.uniqValue} key={mark.label}>
              <span className={styles.label}>{mark.label}</span>
              <span className={styles.value}>{mark.value}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
