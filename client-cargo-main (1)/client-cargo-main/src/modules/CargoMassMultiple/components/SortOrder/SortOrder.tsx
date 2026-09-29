/* eslint-disable jsx-a11y/interactive-supports-focus */
/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
/* eslint-disable jsx-a11y/click-events-have-key-events */
import React, {
  FC, RefObject, useEffect, useRef, useState
} from 'react';

import { ReactComponent as ClockIconSvg } from './images/clock.svg';
import { ReactComponent as DistanceIconSvg } from './images/distance.svg';
import { ReactComponent as VolumeIconSvg } from './images/volume.svg';
import { ReactComponent as WalletIconSvg } from './images/wallet.svg';
import * as S from './SortOrder.style';

export enum SortOrderName {
  CREATION_TIME = 'По дате создания',
  COST = 'По цене',
  TIME = 'По сроку доставки',
  VOLUME = 'По объему',
  DISTANCE = 'По расстоянию',
}

export enum SortType {
  CREATION_TIME = 'CREATION_TIME',
  TIME = 'TIME',
  DISTANCE = 'DISTANCE',
  VOLUME = 'VOLUME',
  COST = 'COST',
}

export enum SortTypeOrder {
  ASC = 'ASC',
  DESC = 'DESC',
}

const SortNameRusOrder = {
  [SortOrderName.CREATION_TIME]: {
    ASC: 'Сначала старые',
    DESC: 'Сначала новые',
  },
  [SortOrderName.COST]: {
    ASC: 'Сначала дорогие',
    DESC: 'Сначала дешевые',
  },
  [SortOrderName.TIME]: {
    ASC: 'Сначала долгие',
    DESC: 'Сначала быстрые',
  },
  [SortOrderName.VOLUME]: {
    ASC: 'Сначала крупные',
    DESC: 'Сначала мелкие',
  },
  [SortOrderName.DISTANCE]: {
    ASC: 'Сначала близкие',
    DESC: 'Сначала дальние',
  },
};

interface SorterProps {
  sort: { type: SortType; order: SortTypeOrder };
  onSort: (type: SortType, order: SortTypeOrder) => void;
}

const SortOrder: FC<SorterProps> = ({ sort, onSort }: SorterProps) => {
  const items = [
    {
      label: SortOrderName.CREATION_TIME,
      type: SortType.CREATION_TIME,
      icon: null,
      list: [
        {
          label: SortNameRusOrder[SortOrderName.CREATION_TIME].DESC,
          order: SortTypeOrder.DESC,
        },
        {
          label: SortNameRusOrder[SortOrderName.CREATION_TIME].ASC,
          order: SortTypeOrder.ASC,
        },
      ],
    },
    {
      label: SortOrderName.COST,
      type: SortType.COST,
      icon: <WalletIconSvg />,
      list: [
        {
          label: SortNameRusOrder[SortOrderName.COST].DESC,
          order: SortTypeOrder.DESC,
        },
        {
          label: SortNameRusOrder[SortOrderName.COST].ASC,
          order: SortTypeOrder.ASC,
        },
      ],
    },
    {
      label: SortOrderName.TIME,
      type: SortType.TIME,
      icon: <ClockIconSvg />,
      list: [
        {
          label: SortNameRusOrder[SortOrderName.TIME].DESC,
          order: SortTypeOrder.DESC,
        },
        {
          label: SortNameRusOrder[SortOrderName.TIME].ASC,
          order: SortTypeOrder.ASC,
        },
      ],
    },
    {
      label: SortOrderName.VOLUME,
      type: SortType.VOLUME,
      icon: <VolumeIconSvg />,
      list: [
        {
          label: SortNameRusOrder[SortOrderName.TIME].DESC,
          order: SortTypeOrder.DESC,
        },
        {
          label: SortNameRusOrder[SortOrderName.VOLUME].ASC,
          order: SortTypeOrder.ASC,
        },
      ],
    },
    {
      label: SortOrderName.DISTANCE,
      type: SortType.DISTANCE,
      icon: <DistanceIconSvg />,
      list: [
        {
          label: SortNameRusOrder[SortOrderName.DISTANCE].DESC,
          order: SortTypeOrder.DESC,
        },
        {
          label: SortNameRusOrder[SortOrderName.DISTANCE].ASC,
          order: SortTypeOrder.ASC,
        },
      ],
    },
  ];

  const sortByType = items.find(i => i.type === sort.type) || items[0];
  const sortByOrder = sortByType.list.find(i => i.order === sort.order) || sortByType.list[0];

  const [selected, setSelected] = useState({ typeText: sortByType.label, orderText: sortByOrder.label });
  const [open, setOpen] = useState(false);
  const sorterRef: RefObject<HTMLDivElement> = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event: any) => {
      if (!sorterRef?.current?.contains(event.target)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleSelected = (typeText: SortOrderName, orderText: string, type: SortType, order: SortTypeOrder) => {
    setSelected({ typeText, orderText });
    setOpen(false);
    onSort(type, order);
  };

  return (
    <S.Sorter ref={sorterRef}>
      <S.Selected onClick={() => setOpen(!open)}>
        {selected.typeText}
        {/* ToDo: почему warning в стайлед свг с использованием bool? */}
        <S.DropdownIcon down={(!open).toString()} />
      </S.Selected>
      <S.Dropdown open={open}>
        <S.DropdownSelected>
          {selected.orderText}
          <S.SelectedIcon />
        </S.DropdownSelected>
        {/* <DropdownItem>
          <List>
            <ListItem
              top={0}
              onClick={() => handleSelected(idItem.label, idItem.type, idItem.order)}
            >
              {idItem.label}
            </ListItem>
          </List>
        </DropdownItem> */}
        {items.map(({
          label: typeText, icon, type, list,
        }) => (
          <S.DropdownItem key={type}>
            <S.ListLabel>
              {icon && <S.ListLabelIcon>{icon}</S.ListLabelIcon>}
              <S.ListLabelText>{typeText}</S.ListLabelText>
            </S.ListLabel>
            <S.List>
              {list.map(({ label: orderText, order }) => (
                <S.ListItem key={order} onClick={() => handleSelected(typeText, orderText, type, order)}>
                  {orderText}
                </S.ListItem>
              ))}
            </S.List>
          </S.DropdownItem>
        ))}
      </S.Dropdown>
    </S.Sorter>
  );
};

export default SortOrder;
