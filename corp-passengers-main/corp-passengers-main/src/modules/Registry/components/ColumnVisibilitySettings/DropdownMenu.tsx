import {
  Button, Checkbox
} from 'antd';
import React, {
  Dispatch, FC, SetStateAction, useEffect, useState
} from 'react';
import cn from 'classnames';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import { useTranslation } from 'i18n';
import { ColumnsType } from 'antd/lib/table';

import { Props } from './ColumnVisibilitySettings';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { StoreNames } from 'stores';
import { ReactComponent as CloseModal } from 'shared/images/closeModal.svg';
import { TaxiRegistryColumnProps } from 'modules/TaxiRegistry/hooks/useColumns';
import { TripRegistryColumnProps } from 'modules/PublicRegistry/hooks/useColumns';
import { PersonalRegistryColumnProps } from 'modules/PersonalRegistry/hooks/useColumns';
import { CarSharingRegistryColumnProps } from 'modules/CarSharingRegistry/types';
import { GroupTransferReportItem } from 'stores/GroupTransferRegistry/GroupTransferRegistry';
import { OrderExecutionColumnProps } from 'modules/OrderExecution/components/OrderTable/Passengers/types';
import { IUiPreferences } from 'stores/Registry/Registry.interface';

import styles from './styles.module.scss';

interface DropdownMenuProps {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  localSetting: any;
  setLocalSetting: Dispatch<SetStateAction<(Props['setting'] & { default?: boolean }) | undefined>>;
  setVisible: Dispatch<SetStateAction<boolean>>;
  saveSettingChange: (key: Record<string, boolean | undefined> | undefined) => void;
  defaultStateOnCancel: () => void;
  // eslint-disable-next-line @stylistic/max-len
  defaultColumns?: ColumnsType<GroupTransferReportItem> | TaxiRegistryColumnProps[] | TripRegistryColumnProps[] | PersonalRegistryColumnProps[] | CarSharingRegistryColumnProps[] | OrderExecutionColumnProps[];
  transportType?: string;
  userId?: string;
  isOto?: boolean;
}

type SettingType = [string, { active: boolean; index: number }];

export const DropDownMenu: FC<DropdownMenuProps> = ({
  localSetting,
  // setLocalSetting,
  setVisible,
  // saveSettingChange,
  // defaultStateOnCancel,
  defaultColumns,
  transportType,
  userId,
  isOto,
}) => {
  const { t } = useTranslation();
  // const [isDefaultState, setDefaultState] = useState(true);
  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  const [locSettings, setLocSettings] = useState();
  const [settings, setSettings] = useState<SettingType[]>([]);

  const mergeAndSortColumns = (defaultColumns, checkedColumns) => {
    const param = isOto ? 'key' : 'dataIndex';
    const checkedMap = new Map(checkedColumns.map(item => [item[param], item]));

    const present: TripRegistryColumnProps[] = [];
    const missing: TripRegistryColumnProps[] = [];

    let requestIdVisible: TripRegistryColumnProps | null = null;

    defaultColumns.forEach(col => {
      if (
        col[param] === 'requestIdVisible'
        || col[param] === 'humanReadableId'
      ) {
        requestIdVisible = { ...col, checked: checkedMap.has(col[param]) };
        present.unshift(requestIdVisible!);
        return;
      }

      if (checkedMap.has(col[param])) {
        present.push({ ...col, checked: true });
      } else {
        missing.push({ ...col, checked: false });
      }
    });

    const sortedPresent = checkedColumns
      .map(col => present.find(item => item[param] === col[param] && col[param] !== 'requestIdVisible' && col[param] !== 'humanReadableId'
      ))
      .filter(Boolean);

    sortedPresent.unshift(requestIdVisible);

    return [...sortedPresent, ...missing];
  };

  const defoultSettings = mergeAndSortColumns(defaultColumns, localSetting);

  const transformedDefaultColumns = defaultColumns && [
    ...defaultColumns.map((item, index) => [
      isOto ? item.key : item.dataIndex,
      {
        active: true,
        index: index,
      },
    ]),
  ];
  const transformedColumns = localSetting && [
    ...defoultSettings.map((item, index) => [
      isOto ? item.key : item.dataIndex,
      {
        active: item.checked ?? false,
        index: index,
      },
    ]),
  ];

  // useEffect(() => {
  //   setDefaultState(is => !is);
  // }, []);

  useEffect(() => {
    if (defaultColumns) {
      setSettings(transformedColumns as SettingType[]);
    }
  }, []);

  // hints for unchangable list of settings
  useEffect(() => {
    if (defaultColumns && isOto && JSON.stringify(localSetting) !== JSON.stringify(locSettings)) {
      setSettings(transformedColumns as SettingType[]);
      setLocSettings(localSetting);
    }
  }, [localSetting]);

  useEffect(() => {
    if (isOto) {
      register.setSaveSettings([]);
    }
  }, [transportType]);

  const onDragEnd = ({ source, destination }) => {
    if (!destination || destination.index === 0) return;

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

  const transformData = (data, userID, transportType) => {
    return {
      userID,
      nameForm: isOto ? `orderExecution_${transportType.toLowerCase()}` : `registry_${transportType}`,
      controls: [
        {
          typeControl: 'table',
          value: isOto ? `request_${transportType.toLowerCase()}` : `request_${transportType}`,
          settings: data
            .filter(([_, settings]) => settings.active)
            .map(([key, settings]) => ({
              nameSetting: 'column',
              valueSetting: key,
              sort: settings.index,
            })),
        },
      ],
    };
  };

  const handleSuccess = () => {
    const result = settings.map(([name, setting], idx) => [name, { ...setting, index: idx }]);
    register.setSaveSettings(result);
    const saveSettings = transformData(result, userId, transportType);
    if (isOto) {
      register.setUiPreferencesOto(userId, saveSettings as IUiPreferences);
    } else {
      register.setUiPreferences(userId, saveSettings as IUiPreferences);
    }
    setVisible(false);
  };

  const onDefaultSettings = () => {
    setSettings(transformedDefaultColumns as SettingType[]);
    register.setSaveSettings(transformedDefaultColumns as SettingType[]);
    const saveSettings = transformData(transformedDefaultColumns, userId, transportType);
    if (isOto) {
      register.setUiPreferencesOto(userId, saveSettings as IUiPreferences);
    } else {
      register.setUiPreferences(userId, saveSettings as IUiPreferences);
    }
    setVisible(false);
  };

  return (
    <div className={styles.menuSetting}>
      <div className={styles.itemHead}>
        <p className={styles.itemsHeader}>{t.Registry.settings.headerTitle}</p>
        <CloseModal onClick={() => setVisible(false)} />
      </div>
      <>
        <p className={styles.subtitle}>{t.Registry.settings.subtitle}</p>
        <div className={styles.list}>
          <DragDropContext onDragEnd={onDragEnd}>
            <Droppable droppableId="droppable-columns">
              {droppableProvided => (
                <div ref={droppableProvided.innerRef}>
                  {settings.map(([item, { active }], idx) => {
                    const settingName = Object.entries(
                      isOto ? t.Monitor : t.Forms.informationAttributesOfRegistries
                    ).find(
                      ([label]) => label === item
                    );

                    return (
                      <Draggable
                        key={item}
                        draggableId={item}
                        index={idx}
                        isDragDisabled={
                          item === 'requestIdVisible'
                          || item === 'humanReadableId'
                        }
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
                              <span>{settingName && settingName[1]}</span>
                            </Checkbox>
                            {/* <Icon type="drag" className={styles.dragIcon} /> */}
                          </div>
                        )}
                      </Draggable>
                    );
                  })}
                  {droppableProvided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
        </div>
      </>
      <div className={styles.buttons}>
        <Button
          type="text"
          onClick={onDefaultSettings}
        >
          {t.Registry.settings.buttons.default}
        </Button>
        <Button
          type="primary"
          // loading={isLoading}
          onClick={handleSuccess}
        >
          {t.Registry.settings.buttons.apply}
        </Button>
      </div>
    </div>
  );
};
