import React, { FC, useReducer, useState } from 'react';
import { FullscreenExitOutlined } from '@ant-design/icons';
import { useBoolean } from 'ahooks';
import {
  AutoComplete, Button, Col, Form, Row
} from 'antd';
import { observer } from 'mobx-react';
import { ReactComponent as BookmarkDisabledIcon } from 'shared/components/Images/view/bookmark_disabled.svg';
import { ReactComponent as MinusIcon } from 'shared/components/Images/view/minus.svg';
import { ReactComponent as ClockOutlineIcon } from 'shared/components/Images/view/time/clock_outline.svg';
import { ReactComponent as WaitingTimeIcon } from 'shared/components/Images/view/time/waiting_time.svg';
import { NumericInput } from 'shared/components/NumericInput';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { StoreNames } from 'stores/StoreNames.enum';
import { genCharArray } from 'utils/alphabet';

import AddFavoriteModal from '../../FavoriteAddressPage/AddFavoriteModal';
import useFavoriteAddressPicker from '../../FavoriteAddressPicker/useFavoriteAddressPicker';
import { AutocompleteText, PointLetter } from '../styles/styled';
import { WaypointsProps } from '../types/types';

import styles from '../styles/create.module.scss';

export const startWaypointPlaceholder = 'Откуда?';
export const endWaypointPlaceholder = 'Куда?';

export const Waypoints: FC<WaypointsProps> = observer(
  ({
    setCurrentAddressInput, geo, getGeolocation, currentAddressInput,
  }) => {
    const { [StoreNames.addressStore]: address } = useAppStoreContext();

    const [numeric, setNumeric] = useState<number | undefined>(2);
    const alphabet = genCharArray('A', 'Z');

    const [visible, { setTrue, setFalse }] = useBoolean(false);

    const reducer = (
      state: { defaultFavoriteWaypoint: WaypointModel | undefined },
      action: WaypointModel | undefined
    ) => ({
      defaultFavoriteWaypoint: action,
    });

    const [state, dispatch] = useReducer(reducer, { defaultFavoriteWaypoint: undefined });

    return (
      <>
        <Form.List name="waypoints">
          {fields => (
            <div className={styles.waypoints}>
              {fields.map((field, index) => (
                <Row key={field.key}>
                  <Col flex="auto">
                    <Form.Item name={[field.name, 'waypoint']}>
                      <AutoComplete
                        placeholder={index === 0 ? startWaypointPlaceholder : endWaypointPlaceholder}
                        // @ts-ignore
                        onSelect={(x, y): void => geo.onAddressSelect(x, y, index)}
                        onFocus={(): void => setCurrentAddressInput(index)}
                        defaultOpen={false}
                        getPopupContainer={trigger => trigger.parentNode}
                        options={[
                          {
                            label: 'Результаты поиска',
                            options: geo.addressAutocompleteList.map(item => ({
                              value: item.addressString,
                              key: `${item.addressString} searching container`,
                              label: (
                                <React.Fragment key={`${item.addressString} from searching list`}>
                                  <ClockOutlineIcon />
                                  <AutocompleteText>{item.addressString}</AutocompleteText>
                                  <Button
                                    type="dashed"
                                    onClick={e => {
                                      e.stopPropagation();
                                      dispatch(item);
                                      setTrue();
                                    }}
                                    style={{ width: 36 }}
                                    icon={<BookmarkDisabledIcon />}
                                  />
                                </React.Fragment>
                              ),
                            })),
                          },
                          ...useFavoriteAddressPicker(address, geo, currentAddressInput),
                        ]}
                      />
                    </Form.Item>
                  </Col>

                  {fields.length > 2 && index !== 0 && index !== fields.length - 1 && (
                    <>
                      <div style={{
                        height: 60, display: 'flex', justifyContent: 'center', alignItems: 'center',
                      }}
                      >
                        {' '}
                        <WaitingTimeIcon />
                      </div>
                      <Col span={5}>
                        <Form.Item name={[field.name, 'waitTime']}>
                          <NumericInput
                            suffix={<small>мин</small>}
                            value={numeric}
                            onChange={val => setNumeric(val)}
                          />
                        </Form.Item>
                      </Col>
                    </>
                  )}

                  {index === 0 && (
                    <Col style={{
                      position: 'absolute', top: 13, right: 20,
                    }}
                    >
                      <div>
                        <Button
                          type="primary"
                          icon={<FullscreenExitOutlined />}
                          size="middle"
                          onClick={getGeolocation}
                          style={{ borderRadius: 20 }}
                        />
                      </div>
                    </Col>
                  )}

                  <PointLetter $backgroundColor={index === 0 ? '#19B150' : '#6979F7'}>{alphabet[index]}</PointLetter>

                  {fields.length > 2 && index !== 0 && (
                    <Col span={2}>
                      <Button
                        type="link"
                        icon={<MinusIcon />}
                        size="middle"
                        block={true}
                        className={styles.mrgnBtm}
                        onClick={(): void => geo.removeWaypoint(index)}
                        style={{ height: 60 }}
                      />
                    </Col>
                  )}
                </Row>
              ))}
            </div>
          )}
        </Form.List>
        <AddFavoriteModal
          visible={visible}
          onOk={setTrue}
          onCancel={setFalse}
          defaultWaypoint={state.defaultFavoriteWaypoint}
        />
      </>
    );
  }
);
