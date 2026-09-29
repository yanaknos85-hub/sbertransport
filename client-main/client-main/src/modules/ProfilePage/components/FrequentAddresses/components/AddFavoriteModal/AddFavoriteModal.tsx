import { Input } from 'antd';
import Modal from 'antd/lib/modal/Modal';
import { reaction } from 'mobx';
import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';
import './overwrite.scss';
import AddressAutoComplete from 'shared/components/AddressAutoComplete';
import { useGeoAutoComplete } from 'shared/hooks/geo';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { StoreNames } from 'stores/StoreNames.enum';
import Close from 'shared/components/Images/Close.svg';
import TButton from 'shared/ui/Button/Button';

interface TAddFavoriteModalProps {
  visible: boolean;
  onOk(): void;
  onCancel(): void;
  defaultWaypoint?: WaypointModel;
}

const AddFavoriteModal: FC<TAddFavoriteModalProps> = observer(({
  visible, onOk, onCancel, defaultWaypoint,
}) => {
  const [label, setLabel] = useState(defaultWaypoint?.addressString);
  const [waypoint, setWaypoint] = useState(defaultWaypoint);

  useEffect(() => {
    setWaypoint(defaultWaypoint);
  }, [defaultWaypoint]);

  const { [StoreNames.geoStore]: geo, [StoreNames.addressStore]: address } = useAppStoreContext();

  const { autoCompleteList, editSingleAddress } = useGeoAutoComplete();

  useEffect(() => {
    const favoriteAddDisposer = reaction(
      () => address.isAddressChanged === true,
      () => onOk()
    );
    return (): void => {
      favoriteAddDisposer();
    };
  }, [onOk, address.isAddressChanged]);

  const okHandler = (): void => {
    if (waypoint && label) {
      // @ts-ignore
      address.addFavoriteAddress({
        ...waypoint,
        label: label.trim(),
      });
      // @ts-ignore
      if (!address.isUnique({ ...waypoint })) {
        onCancel();
      }
    }
  };

  const cancelHandler = (): void => {
    onCancel();
  };

  const selectHandler = (point: WaypointModel): void => {
    setWaypoint(point);
    geo.clearCurrentState();
  };

  const clear = (): void => {
    setLabel('');
    setWaypoint(undefined);
  };

  return (
    <Modal
      visible={visible}
      onOk={okHandler}
      onCancel={cancelHandler}
      okText="Добавить"
      cancelText="Отмена"
      okButtonProps={{
        // disabled: !label || !waypoint,
        type: 'primary',
      }}
      destroyOnClose={true}
      afterClose={clear}
      className="modal"
      footer={null}
    >
      <div className="cardWrapper">
        <div className="header">
          <div className="numberApplication">
            Добавить адрес
          </div>
          <div className="cancel" onClick={cancelHandler}>
            <img alt="Close" src={Close} />
          </div>
        </div>
        <span className="phoneTitle">
          Адрес
        </span>
        <Input
          value={label}
          onChange={(e): void => setLabel(e.target.value)}
          placeholder="Введите название"
          style={{ marginBottom: 16 }}
        />
        <AddressAutoComplete
          defaultValue={defaultWaypoint?.addressString}
          value={defaultWaypoint?.addressString}
          list={autoCompleteList}
          onSearch={editSingleAddress}
          onSelect={selectHandler}
        />
        <div className="saveButton">
          <TButton
            htmlType="submit"
            $size="small"
            onClick={okHandler}
          >
            Сохранить
          </TButton>
        </div>
      </div>
    </Modal>
  );
});

export default AddFavoriteModal;
