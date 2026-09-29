/* eslint-disable react-hooks/exhaustive-deps */
import React, { FC, useEffect, useState } from 'react';
import { Card, Form, FormInstance } from 'antd';

import { icons as iconsArray } from '../../constants';
import { IconBlock } from '../../static/styles';
import { IconData, Selected } from '../../types';
import { getImages } from '../../utils';

interface Props {
  rating: number;
  form: FormInstance;
  transportType?: string;
}

export const Icons: FC<Props> = ({
  rating, form, transportType,
}) => {
  const transportTypeImages = getImages(transportType, iconsArray);

  const initialState = [...transportTypeImages.positive, ...transportTypeImages.negative].reduce(
    (acc, curr) => ({ ...acc, [curr.type]: false }),
    {}
  );

  const [selected, setSelected] = useState<Selected>(initialState);
  const [imagesArray, setImagesArray] = useState<IconData[]>([]);

  const selectedIcons = Object.entries(selected).reduce((acc: any, [key, value]) => {
    if (value === true) {
      acc.push(key);
    }
    return acc;
  }, []);

  useEffect(() => {
    if (rating <= 3) {
      setSelected(initialState);
      setImagesArray([...transportTypeImages.negative]);
    }

    if (rating > 3) {
      setSelected(initialState);
      setImagesArray([...transportTypeImages.positive]);
    }
  }, [rating]);

  useEffect(() => {
    form.setFieldsValue({ reasons: selectedIcons });
  }, [selectedIcons]);

  const handleTypeSelect = (type: string) => {
    setSelected({ ...selected, [type]: !selected[type] });
  };

  return (
    <Form.Item name="reasons">
      <IconBlock>
        {[...imagesArray].map(({ type, icon }) => (
          <Card
            bordered={false}
            key={type}
            onClick={() => handleTypeSelect(type)}
            bodyStyle={{ padding: 0 }}
          >
            {icon({
              fillBackground: selected[type] ? '#10BF6A' : '#F2F3F6',
              fillFont: selected[type] ? 'white' : 'black',
              stroke: selected[type] ? 'white' : '#262626',
              fillOpacity: selected[type] ? 1 : 0.6,
            })}
          </Card>
        ))}
      </IconBlock>
    </Form.Item>
  );
};
