import React, { Dispatch, SetStateAction } from 'react';
import { AutoComplete, Col, Form } from 'antd';
import { FormListFieldData } from 'antd/lib/form/FormList';
import { ReactComponent as BookmarkDisabledIcon } from 'shared/components/Images/view/bookmark_disabled.svg';
import { ReactComponent as ClockOutlineIcon } from 'shared/components/Images/view/time/clock_outline.svg';
import { useGeoAutoComplete } from 'shared/hooks/geo';
import { GeoWaypoints } from 'shared/hooks/geo/useGeoWaypoints';

export const startWaypointPlaceholder = 'Откуда?';
export const endWaypointPlaceholder = 'Куда?';

export const WaypointAutoComplete = ({
  index,
  setActiveWaypointIndex,
  geoWaypoints,
  field,
}: {
  index: number;
  setActiveWaypointIndex: Dispatch<SetStateAction<number>>;
  geoWaypoints: GeoWaypoints;
  field: FormListFieldData;
}): JSX.Element => {
  const { Option: OptionAutoComplete } = AutoComplete;
  const { autoCompleteList, editSingleAddress } = useGeoAutoComplete();

  const onSelect = (value: string): void => {
    const point = autoCompleteList.find(x => x.addressString === value);
    if (point) {
      geoWaypoints.editWaypoint(index, point);
    }
  };

  const onFocus = (): void => {
    setActiveWaypointIndex(index);
  };

  const actualPlaceholder = index === 0 ? startWaypointPlaceholder : endWaypointPlaceholder;

  return (
    <Col flex="auto">
      <Form.Item name={[field.name, 'waypoint']}>
        <AutoComplete
          placeholder={actualPlaceholder}
          onSelect={onSelect}
          onFocus={onFocus}
          onSearch={editSingleAddress}
        >
          {autoCompleteList.map((y, j) => (
            // eslint-disable-next-line react/no-array-index-key
            <OptionAutoComplete
              value={y.addressString}
              key={j}
              title={y.addressString}
            >
              {/* FIXME react/no-array-index-key */}
              <ClockOutlineIcon />
              {y.addressString}
              {' '}
              <BookmarkDisabledIcon />
            </OptionAutoComplete>
          ))}
        </AutoComplete>
      </Form.Item>
    </Col>
  );
};
