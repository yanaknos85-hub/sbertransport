
import { FullscreenExitOutlined } from '@ant-design/icons';
import { useBoolean } from 'ahooks';
import {
  AutoComplete,
  Button,
  Col,
  Form,
  Row
} from 'antd';
import type { FormListFieldData } from 'antd/lib/form/FormList';
import { observer } from 'mobx-react';
import { BaseSelectRef } from 'rc-select';
import React, {
  useEffect, useReducer, useRef, useState
} from 'react';
import type { FC, RefObject } from 'react';
import cn from 'classnames';

import { ReactComponent as BookmarkDisabledIcon } from 'shared/components/Images/view/bookmark_disabled.svg';
import { ReactComponent as MinusIcon } from 'shared/components/Images/view/minus.svg';
import { ReactComponent as WaitingTimeIcon } from 'shared/components/Images/view/time/waiting_time.svg';
import { ReactComponent as UnionIcon } from 'shared/components/Images/view/union.svg';
import { ReactComponent as BurgerIcon } from 'shared/components/Images/burgerIcon.svg';
import { NumericInput } from 'shared/components/NumericInput';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useGeoPosition } from 'shared/hooks/usePosition';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { StoreNames } from 'stores/StoreNames.enum';

import { genCharArray } from 'utils/alphabet';

import AddFavoriteModal from '../../FavoriteAddressPage/AddFavoriteModal';
import useFavoriteAddressPicker from '../../FavoriteAddressPicker/useFavoriteAddressPicker';

import styles from '../styles/create.module.scss';
import { AutocompleteText, PointLetter, WrapperPointLetter } from '../styles/styled';
import { AddressViewItem, AddressViewList, WaypointsProps } from '../types/types';

export const startWaypointPlaceholder = 'Откуда?';
export const endWaypointPlaceholder = 'Куда?';
const waypointPlaceholder = 'Город, улица, дом';

const arrToString = (arr: (string | undefined)[]) => arr.filter(Boolean).join(', ');

// Убрана проверка по полю item.street, т.к. при поиске станции метро данное поле отсутствует
export const filterAutocomplete = (item: WaypointModel) => !!(item.country || item.region);

const addLabel = (waypointIndex: number, isLast: boolean) => waypointIndex === 0 ? 'Откуда' : isLast ? 'Куда?' : `Остановка №${waypointIndex}`;

export const WaypointOption: FC<{ item: WaypointModel }> = ({ item }) => (
  <>
    {item.name && (
    <>
      <span className={styles.searchAddressName}>{item.name}</span>
      <br />
    </>
    )}
    <span className={styles.searchAddressFirst}>{arrToString([item.street, item.house])}</span>
    <br />
    <span className={styles.searchAddressSecond}>
      {arrToString([
        item.place,
        item.livingArea,
        item.settlement,
        item.city !== item.region ? item.city : '',
        item.district,
        item.region,
        item.country,
      ])}
    </span>
  </>
);

interface AddressViewProps {
  index: number;
  item: AddressViewItem;
  isLast: boolean;
  setValue: (name: string, item: AddressViewItem) => void;
  refs: RefObject<BaseSelectRef[]>;
}

const AddressView: FC<AddressViewProps> = ({
  index,
  item,
  isLast,
  setValue,
  refs,
}) => {
  const { value, visible } = item;

  const clickHandler = () => {
    setValue(`${index}`, { value, visible: false });

    setTimeout(() => {
      if (refs && refs.current) {
        refs.current[index].focus();
      }
    }, 100);
  };

  if (!visible) {
    return null;
  }

  return (
    <div
      className={styles.addressView}
      onClick={clickHandler}
    >
      <span>{addLabel(index, isLast)}</span>
      <span>{value}</span>
    </div>
  );
};

const Address: FC<{
  field: FormListFieldData;
  index: number;
  isLast: boolean;
  addToFavorite: (item: WaypointModel) => void;
  currentAddressInput: number;
  setCurrentAddressInput: (index: number) => void;
  addressViewRefs: RefObject<BaseSelectRef[]>;
  addressViewList: AddressViewList;
  setAddressViewList: React.Dispatch<React.SetStateAction<AddressViewList>>;
}> = observer(
  ({
    field,
    index,
    isLast,
    addToFavorite,
    currentAddressInput,
    setCurrentAddressInput,
    addressViewRefs,
    addressViewList,
    setAddressViewList,
  }) => {
    const { [StoreNames.geoStore]: geo } = useAppStoreContext();

    const addressAutocompleteList = geo.addressAutocompleteList.filter(filterAutocomplete);

    return (
      <div className={cn(
        styles.address,
        addressViewList[index]?.visible ? styles.address_hidden : styles.address_visible
      )}
      >
        <Form.Item name={[field.name, 'waypoint']} label={addLabel(index, isLast)}>
          <AutoComplete
            className={styles.addressAutoComplete}
            ref={ref => {
              if (ref && addressViewRefs.current) {
                // eslint-disable-next-line no-param-reassign
                addressViewRefs.current[index] = ref;
              }
            }}
            defaultOpen={false}
            placeholder={waypointPlaceholder}
            getPopupContainer={trigger => trigger.parentNode}
            // @ts-ignore
            onSelect={(value, option): void => {
              setAddressViewList({
                ...addressViewList,
                [index]: {
                  value,
                  visible: true,
                },
              });

              geo.onAddressSelect(value, option, index);
            }}
            onBlur={() => {
              if (addressViewList[index]) {
                setAddressViewList({
                  ...addressViewList,
                  [index]: {
                    value: addressViewList[index].value,
                    visible: true,
                  },
                });
              }
            }}
            onFocus={() => setCurrentAddressInput(index)}
            options={[
              ...(addressAutocompleteList.length
                ? [
                  {
                    label: 'Результаты поиска',
                    options: addressAutocompleteList.map(item => ({
                      value: item.addressString,
                      key: `${item.addressString} searching container`,
                      label: (
                        <React.Fragment key={`${item.addressString} from searching list`}>
                          <UnionIcon style={{ minWidth: 16 }} />
                          <AutocompleteText>
                            <WaypointOption item={item} />
                          </AutocompleteText>
                          <Button
                            type="dashed"
                            onClick={e => {
                              e.stopPropagation();
                              addToFavorite(item);
                            }}
                            style={{ width: 36 }}
                            icon={<BookmarkDisabledIcon />}
                          />
                        </React.Fragment>
                      ),
                    })),
                  },
                ]
                : []),
              ...useFavoriteAddressPicker(currentAddressInput),
            ]}
          />
        </Form.Item>
      </div>
    );
  }
);

const WaitTime: FC<{ field: FormListFieldData }> = ({ field }) => {
  const [numeric, setNumeric] = useState<number | undefined>(2);

  return (
    <div className={styles.waitTime}>
      <div className={styles.waitTime__icon}>
        <WaitingTimeIcon />
      </div>
      <div className={styles.waitTime__input}>
        <Form.Item name={[field.name, 'waitTime']}>
          <NumericInput
            suffix={<small>мин</small>}
            value={numeric}
            onChange={val => setNumeric(val)}
          />
        </Form.Item>
      </div>
    </div>
  );
};

const BtnRemoveAddress: FC<{
  index: number;
  setAddressViewList: React.Dispatch<React.SetStateAction<AddressViewList>>;
  addressViewList: AddressViewList;
}> = observer(({
  index, setAddressViewList, addressViewList,
}) => {
  const { [StoreNames.geoStore]: geo } = useAppStoreContext();

  const handleRemove = () => {
    const finalAddressViewList = Object.keys(addressViewList)
      .filter(key => key !== String(index))
      .reduce((acc, key, idx) => {
        acc[idx] = addressViewList[key];
        return acc;
      }, {});

    setAddressViewList(finalAddressViewList);
    geo.removeWaypoint(index);
  };

  return (
    <Button
      type="link"
      icon={<MinusIcon />}
      size="middle"
      block={true}
      className={styles.mrgnBtm}
      onClick={handleRemove}
    />
  );
});

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const BtnSetCurrentAddress: FC = observer(() => {
  const { [StoreNames.geoStore]: geo } = useAppStoreContext();

  const { position } = useGeoPosition();

  return (
    <div style={{
      position: 'absolute', top: 24, right: 24,
    }}
    >
      <div>
        <Button
          type="primary"
          icon={<FullscreenExitOutlined />}
          size="middle"
          onClick={() => geo.setCurrentAddressByCoordinates(position)}
          style={{ borderRadius: 20 }}
        />
      </div>
    </div>
  );
});

export const Waypoints: FC<WaypointsProps> = observer(({
  setCurrentAddressInput, currentAddressInput, style,
}) => {
  const { geoStore } = useAppStoreContext();

  const addressViewRefs = useRef<BaseSelectRef[]>([]);
  const [addressViewList, setAddressViewList] = useState<AddressViewList>({});
  const [visible, { setTrue, setFalse }] = useBoolean(false);

  const reducer = (
    state: { defaultFavoriteWaypoint: WaypointModel | undefined },
    action: WaypointModel | undefined
  ) => ({
    defaultFavoriteWaypoint: action,
  });

  const [state, dispatch] = useReducer(reducer, { defaultFavoriteWaypoint: undefined });

  const alphabet = genCharArray('A', 'Z');

  useEffect(() => {
    const newAddressViewList = { ...addressViewList };

    geoStore.waypoints.forEach((waypoint, index) => {
      // Если адрес пустой → сбрасываем addressViewList
      if (!waypoint.addressString) {
        delete newAddressViewList[index];
        return;
      }

      // Если адрес изменился → обновляем
      if (addressViewList[index]?.value !== waypoint.addressString) {
        newAddressViewList[index] = {
          value: waypoint.addressString,
          visible: true,
        };
      }
    });

    setAddressViewList(newAddressViewList);
  }, [geoStore.waypoints]);

  return (
    <>
      <Form.List name="waypoints">
        {fields => (
          <div className={styles.waypoints} style={style}>
            {fields.map((field, index) => {
              const isFirstRow = index === 0;
              const isLastRow = index === fields.length - 1;
              const withWaitTime = fields.length > 2 && !isFirstRow && !isLastRow;
              const withRemoveBtn = fields.length > 2 && !isFirstRow;

              return (
                <Row
                  key={field.key}
                  wrap={false}
                  className={isFirstRow ? styles.waypoint_first : styles.waypoint}
                >
                  <Col className={styles.wrapper_addressView} flex="auto">
                    <WrapperPointLetter>
                      <BurgerIcon />
                      <PointLetter $backgroundColor={isFirstRow ? '#19B150' : '#6979F7'}>{alphabet[index]}</PointLetter>
                    </WrapperPointLetter>

                    {addressViewList[index]?.visible ? (
                      <AddressView
                        index={index}
                        item={addressViewList[index]}
                        isLast={isLastRow}
                        refs={addressViewRefs}
                        // eslint-disable-next-line @typescript-eslint/no-shadow
                        setValue={(index: string, item: AddressViewItem) => {
                          setAddressViewList({
                            ...addressViewList,
                            [index]: item,
                          });
                        }}
                      />
                    ) : (
                      <Address
                        field={field}
                        index={index}
                        isLast={isLastRow}
                        addToFavorite={(item: WaypointModel) => {
                          dispatch(item);
                          setTrue();
                        }}
                        currentAddressInput={currentAddressInput}
                        setCurrentAddressInput={setCurrentAddressInput}
                        addressViewRefs={addressViewRefs}
                        addressViewList={addressViewList}
                        setAddressViewList={setAddressViewList}
                      />
                    )}
                  </Col>

                  {/* {isFirstRow && <BtnSetCurrentAddress />} */}

                  {withWaitTime && (
                    <Col>
                      <WaitTime field={field} />
                    </Col>
                  )}

                  {withRemoveBtn && (
                    <Col>
                      <BtnRemoveAddress
                        addressViewList={addressViewList}
                        setAddressViewList={setAddressViewList}
                        index={index}
                      />
                    </Col>
                  )}
                </Row>
              );
            })}
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
});
