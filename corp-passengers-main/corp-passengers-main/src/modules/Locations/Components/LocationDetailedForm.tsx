import React, {
  FC, useEffect, useState
} from 'react';
import { Form, List, Popconfirm } from 'antd';
import * as R from 'ramda';
import { observer } from 'mobx-react';
import { useTranslation } from 'i18n';
import { SaveOutlined, RedoOutlined } from '@ant-design/icons';
import { useForm } from 'antd/lib/form/Form';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { WaypointModel } from 'stores/Geo/models/Waypoint.model';
import { formatAddress } from 'utils/formatAddress';

import { Coordinates } from 'shared/components/Map/MapComponent.types';
import AddressAutoComplete from 'shared/components/AddressAutoComplete/AddressAutoComplete';
import { preventDefault } from 'utils';
import { Button } from 'shared/components/Button/Button';
import Input from 'shared/components/Inputs/Input/Input';
import { LocationsTextsCyrillic, LocationsTexts } from '../Locations.constants';

import styles from './components.module.scss';

export interface LocationDetailedFormProps {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  initialValues: Dictionary<any>;
  inProgress?: boolean;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  handleSave: (values: any) => void;
  pageHeader?: string;
  deletePopupTitle?: string;
  deleteOkText?: string;
  deleteCancelText?: string;
  handleClose: () => void;
}

// Moscow coordinates
const initMapPosition = {
  longitude: 37.617423,
  latitude: 55.752693,
};

const stringMaxLength = 250;

export const LocationDetailedForm: FC<LocationDetailedFormProps> = observer(
  ({
    handleClose,
    initialValues,
    inProgress,
    handleSave,
    deleteOkText = LocationsTextsCyrillic[LocationsTexts.remove],
    deleteCancelText = LocationsTextsCyrillic[LocationsTexts.cancel],
    deletePopupTitle = LocationsTextsCyrillic[LocationsTexts.deleteConfirm],
  }) => {
    const { t } = useTranslation();

    const initWayPoint = !R.isEmpty(initialValues.address) ? initialValues.address : initMapPosition;
    const [wayPoint, setWayPoint] = useState(initWayPoint);
    const currentCoordinates: Coordinates = {
      latitude: Number(wayPoint.latitude || initMapPosition.latitude),
      longitude: Number(wayPoint.longitude || initMapPosition.longitude),
    };

    const [form] = useForm();

    const cancelButton = (
      <Popconfirm
        placement="top"
        title={deletePopupTitle}
        okText={deleteOkText}
        cancelText={deleteCancelText}
        style={{ width: 300 }}
        onConfirm={handleClose}
      >
        <Button icon={<RedoOutlined />} size="middle">
          {t.global.cancel}
        </Button>
      </Popconfirm>
    );

    useEffect(() => {
      if (inProgress) {
        return;
      }
      form.setFieldsValue(initialValues);
    }, [form, initialValues, inProgress]);

    const selectHandler = (point: WaypointModel): void => {
      const fields = form.getFieldsValue();
      const newFormValues = {
        ...fields,
        address: { ...point },
        fullAddress: formatAddress(point),
      };
      setWayPoint(point);
      form.setFieldsValue(newFormValues);
    };

    const renderMap = (): JSX.Element => (
      <div className={styles.mapWrapper}>
        <MapComponent
          position={currentCoordinates}
          markers={[wayPoint]}
          className={styles.map}
          dragging
          zoomControl
        />
      </div>
    );

    return (
      <div className={styles.location_detailed_form}>
        <List
          itemLayout="horizontal"
          className="model_detailed"
          split={false}
        >
          <Form
            layout="vertical"
            form={form}
            name="location-detail-form"
            size="middle"
            onFinish={handleSave}
          >
            {renderMap()}
            <Form.Item name="address">
              <Input type="hidden" />
            </Form.Item>
            <Form.Item
              name="label"
              label={t.Forms.locationDetailedForm.label}
              rules={[
                { required: true },
                {
                  max: stringMaxLength,
                  message: t.Forms.locationDetailedForm.messages.labelIncorrectLength({ max: stringMaxLength }),
                },
              ]}
            >
              <Input onPressEnter={preventDefault} />
            </Form.Item>
            <Form.Item
              name="fullAddress"
              label={t.Forms.locationDetailedForm.fullAddress}
              rules={[
                {
                  required: true,
                  message: t.Forms.locationDetailedForm.messages.addressRequired,
                },
                { max: stringMaxLength },
              ]}
            >
              <AddressAutoComplete onSelect={selectHandler} />
            </Form.Item>
            <div>
              <Button
                icon={<SaveOutlined />}
                size="middle"
                htmlType="submit"
              >
                {t.global.save}
              </Button>
              {cancelButton}
            </div>
          </Form>
        </List>
      </div>
    );
  }
);
