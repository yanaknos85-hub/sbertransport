import React, { FC, useState, useEffect } from 'react';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import Modal from 'antd/lib/modal/Modal';
import cn from 'classnames';
import { useTranslation } from 'i18n';

import { Icon } from 'components/Icon/Icon';
import Checkbox from 'components/Checkbox/Checkbox';
import { Button } from 'components/Button';
import { getDefaultTableSettings, SettingsColumnsType, TableSettingsType } from 'hooks/useTableSettings';
import { ignore } from 'utils/utils';

import styles from './TableSettings.module.scss';

interface TableSettingsProps {
  columns: SettingsColumnsType;
  tableSettings: TableSettingsType;
  onSaveTableSettings: (settings: TableSettingsType) => Promise<void>;
  iconType?: 'default' | 'bordered';
}

type SettingType = [string, { active: boolean; index: number }];

const TableSettings: FC<TableSettingsProps> = ({
  columns, tableSettings, onSaveTableSettings, iconType = 'default',
}) => {
  const { t } = useTranslation();

  const [isOpen, setOpen] = useState(false);
  const [isLoading, setLoading] = useState(false);
  const [settings, setSettings] = useState<SettingType[]>([]);

  useEffect(() => {
    const result = Object.entries(tableSettings).sort(
      ([_a, { index: indexA }], [_b, { index: indexB }]) => indexA - indexB
    );

    setSettings(result);
  }, [tableSettings]);

  const handleCancel = () => {
    setOpen(false);
  };

  const onDragEnd = ({ source, destination }) => {
    if (!destination) return;

    const result = JSON.parse(JSON.stringify(settings));
    const [removed] = result.splice(source.index, 1);
    result.splice(destination.index, 0, removed);

    setSettings(result);
  };

  const onChecked = (value: boolean, name: string) => {
    const result = settings.map(setting => {
      if (setting[0] === name) {
        return [name, { ...setting[1], active: value }];
      }

      return setting;
    });

    setSettings(result as SettingType[]);
  };

  const handleSuccess = () => {
    const result = settings.map(([name, setting], idx) => [name, { ...setting, index: idx }]);
    setLoading(true);
    onSaveTableSettings(Object.fromEntries(result) as TableSettingsType)
      .then(handleCancel)
      .catch(ignore)
      .finally(() => setLoading(false));
  };

  const onDefaultSettings = () => {
    const defaultSettings = getDefaultTableSettings(columns);
    setSettings(Object.entries(defaultSettings));
  };

  return (
    <>
      <Button
        type="text"
        className={cn(styles.icon, { [styles.bordered]: iconType === 'bordered' })}
        onClick={() => setOpen(true)}
      >
        <Icon className={styles.settings} type="settings" />
      </Button>
      <Modal
        title={t.Requests.Modal.SettingsTable.title}
        className={styles.modal}
        visible={isOpen}
        onCancel={handleCancel}
        closeIcon={<Icon type="closeModal" className={styles.modalIcon} />}
        destroyOnClose
        footer={[
          <>
            <Button
              type="text"
              onClick={onDefaultSettings}
            >
              {t.Requests.Modal.default}
            </Button>
            <Button
              type="primary"
              loading={isLoading}
              onClick={handleSuccess}
            >
              {t.Requests.Modal.apply}
            </Button>
          </>,
        ]}
      >
        <>
          <p className={styles.subtitle}>{t.Requests.Modal.SettingsTable.subtitle}</p>
          <div className={styles.list}>
            <DragDropContext onDragEnd={onDragEnd}>
              <Droppable droppableId="droppable-columns">
                {droppableProvided => (
                  <div ref={droppableProvided.innerRef}>
                    {settings.map(([item, { active }], idx) => (
                      <Draggable
                        key={item}
                        draggableId={item}
                        index={idx}
                      >
                        {(draggableProvided, draggableSnapshot) => (
                          <div
                            style={{ ...draggableProvided.draggableProps.style }}
                            className={cn(styles.item, {
                              [styles.isDragging]: draggableSnapshot.isDragging,
                            })}
                            ref={draggableProvided.innerRef}
                            {...draggableProvided.draggableProps}
                            {...draggableProvided.dragHandleProps}
                          >
                            <Checkbox checked={active} onChange={e => onChecked(e.target.checked, item)}>
                              <span>{columns[item] ?? item}</span>
                            </Checkbox>
                            <Icon type="drag" className={styles.dragIcon} />
                          </div>
                        )}
                      </Draggable>
                    ))}
                    {droppableProvided.placeholder}
                  </div>
                )}
              </Droppable>
            </DragDropContext>
          </div>
        </>
      </Modal>
    </>
  );
};

export default TableSettings;
