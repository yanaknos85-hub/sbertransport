import React, { FC, useCallback, useState } from 'react';
import { DeleteOutlined, EditOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Dropdown, Popconfirm } from 'antd';
import cn from 'classnames';
import { useTranslation } from 'i18n';

import { UUID } from 'utils/io-ts';
import { RowData, StatusesType } from '../../types';
import { ReactComponent as VerticalDotsOutlined } from './images/VerticalDotsOutlined.svg';
import styles from './Row.module.scss';

const Status: FC<{ type: StatusesType }> = ({ type }) => {
  const { t } = useTranslation();

  return (
    <div className={styles.status}>
      <div className={cn(styles.circle, styles[`status-circle_${type}`])} />
      <p className={styles.statusTitle}>{t.Requests.Status[type]}</p>
    </div>
  );
};

type Statuses =
  | {
    type: StatusesType;
    title?: never;
    description?: never;
  }
  | {
    type?: never;
    title: string;
    description: string;
  };

export interface Props<T = RowData> {
  rowData: T;
  activeRow: T | null;
  statuses: Statuses[];
  toggleActive: (row: T) => void;
  onEdit?: (data: T) => void;
  onDelete?: (id: UUID) => void;
}

export const Row: FC<Props> = ({
  rowData, activeRow, statuses, toggleActive, onEdit, onDelete,
}) => {
  const { t } = useTranslation();
  const [isFastActionVisible, setIsFastActionVisible] = useState(false);

  const isSelected = rowData?.id === activeRow?.id;

  const currentData = (isSelected ? { ...rowData, ...activeRow } : rowData) as typeof rowData;

  const title = [
    currentData.lastName,
    currentData.firstName,
    currentData.patronymic,
    currentData.stateNumber,
    currentData.name,
  ]
    .filter(Boolean)
    .join(' ');

  const showFastAction = useCallback(() => setIsFastActionVisible(true), []);
  const hideFastAction = () => setIsFastActionVisible(false);

  const handleSelect = () => {
    toggleActive(currentData);
  };

  const handleEditClick = () => {
    hideFastAction();
    onEdit?.(currentData);
  };

  const handleDeleteClick = () => {
    onDelete?.(currentData.id);
  };

  const handleFastActionSideClick = (e: { stopPropagation: () => void }) => {
    e.stopPropagation();
  };

  return (
    <div
      className={cn(styles.dispatcherRow, {
        [styles.dispatcherRow_selected]: isSelected,
      })}
      onClick={handleSelect}
      role="button"
      onKeyDown={handleSelect}
      tabIndex={-1}
    >
      {!currentData.name && (
      <Avatar
        className={styles.avatar}
        size="default"
        icon={<UserOutlined />}
      />
      )}

      <div className={styles.content}>
        <strong className={styles.title}>{title}</strong>
        <div className={styles.statusRow}>
          {statuses.map(status => {
            if (status.type) {
              return (
                <div className={styles.statusId} key={status?.type}>
                  <Status type={status.type} />
                </div>
              );
            }

            return (
              <div className={styles.statusId} key={status?.title + status?.description}>
                {status.title}
                {' '}
                <span>{status.description}</span>
              </div>
            );
          })}
        </div>
      </div>

      {(onEdit || onDelete) && (
        <div
          className={styles.sideFastAction}
          onClick={handleFastActionSideClick}
          onKeyDown={handleFastActionSideClick}
          tabIndex={-1}
          role="button"
        >
          <Dropdown
            visible={isFastActionVisible}
            onVisibleChange={setIsFastActionVisible}
            overlay={(
              <div className={styles.fastActions}>
                {onEdit && (
                  <button
                    type="button"
                    className={styles.fastAction}
                    onClick={handleEditClick}
                  >
                    <EditOutlined />
                    {' '}
                    {t.global.edit}
                  </button>
                )}
                {onDelete && (
                  <Popconfirm
                    placement="bottom"
                    title={t.global.deleteConfirm}
                    onConfirm={handleDeleteClick}
                    okText={t.global.delete}
                    cancelText={t.global.cancel}
                    overlayClassName={styles.tableEditTooltip}
                  >
                    <button type="button" className={styles.fastAction}>
                      <DeleteOutlined />
                      {' '}
                      {t.global.delete}
                    </button>
                  </Popconfirm>
                )}
              </div>
            )}
            overlayClassName={styles.dropDown}
          >
            <button
              type="button"
              className={styles.fastActionButton}
              onClick={showFastAction}
            >
              <VerticalDotsOutlined />
            </button>
          </Dropdown>
        </div>
      )}
    </div>
  );
};
