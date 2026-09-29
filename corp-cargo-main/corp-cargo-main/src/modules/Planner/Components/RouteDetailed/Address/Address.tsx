import React, { FC, ReactNode, DragEvent } from 'react';
import { useTranslation } from 'i18n';
import { Tooltip } from 'antd';
import { RouteWaypointType, RouteType } from '../../../types';

import { ReactComponent as Bucket } from '../../../images/bucketIcon.svg';
import { ReactComponent as MapIcon } from '../../../images/mapIcon.svg';

import { renderPointType } from '../../utils';

import { colors } from "shared/styles/styles";

import { ListIds } from '../Addresses/ListIds';
import { ArrowControls } from '../ArrowControls/ArrowControls';

import * as S from './Address.style';

interface Props {
  data: RouteType;
  onDragStart: (e: DragEvent<HTMLDivElement>, point: RouteWaypointType) => void;
  onDragOver: (e: DragEvent<HTMLDivElement>) => boolean;
  onDrop: (e: DragEvent<HTMLDivElement>, point: RouteWaypointType, idx: number) => Promise<void>;
  onDragEnd: () => void;
  point: RouteWaypointType;
  handleListItemUp: (point: RouteWaypointType, idx: number) => Promise<void>;
  handleListItemDown: (point: RouteWaypointType, idx: number) => Promise<void>;
  handleDeleteListItem: (id: string, idx: number) => Promise<void>;
  idx: number;
  lastIndex: number;
}

export const Address: FC<Props> = props => {
  const {
    data,
    onDragStart,
    onDragOver,
    onDrop,
    onDragEnd,
    point,
    handleListItemUp,
    handleListItemDown,
    handleDeleteListItem,
    idx,
    lastIndex,
  } = props;

  const { t } = useTranslation();

  const addressTitle = point?.addressStringRepresentation && point?.addressStringRepresentation?.length > 38 ?
    point.addressStringRepresentation : '';

  const getAddressLetter = (index: number): string | ReactNode => {
    if (index === 1) {
      return (
        <S.AddressCircleFirst>
          {String.fromCharCode(96 + index).toUpperCase()}
        </S.AddressCircleFirst>
      )
    }

    if (index === lastIndex) {
      return (
        <S.AddressCircleLast>
          {String.fromCharCode(96 + index).toUpperCase()}
        </S.AddressCircleLast>
      )
    }
    return (
      <S.AddressCircle>
        {String.fromCharCode(96 + index).toUpperCase()}
      </S.AddressCircle>
    );
  };

  return (
    <S.Card
      draggable={data.status === 'CARGO_PLANNING'}
      onDragStart={e => {
        onDragStart(e, point);
        e.currentTarget.style.cursor = 'grabbing';
      }}
      onDragEnd={(e) => {
        onDragEnd();
        e.currentTarget.style.cursor = 'grab';
        e.currentTarget.style.borderColor = colors.gray3;
      }}
      onDragOver={(e) => {
        const allowed = onDragOver(e);
        e.currentTarget.style.borderColor = allowed ? colors.green10 : colors.red10;
      }}
      onDragLeave={(e) => {
        e.preventDefault();
        e.currentTarget.style.borderColor = colors.gray3;
      }}
      onDrop={async (e) => {
        await onDrop(e, point, idx);
        e.currentTarget.style.cursor = 'grab';
      }}
    >
      <S.Content>
        <S.LeftSide>
          <S.InfoWrapper>
            {data.status === 'CARGO_PLANNING' ? (
              <ArrowControls
                handleListItemUp={() => handleListItemUp(point, idx)}
                handleListItemDown={() => handleListItemDown(point, idx)}
              />
            ) : (
              <S.Text2>{t.Planner.routePlanningFinished}</S.Text2>
            )}
          </S.InfoWrapper>
          <S.LetterElement>
            {getAddressLetter(point.orderingIndex)}
          </S.LetterElement>
        </S.LeftSide>
        <S.Info>
          <S.IdElement>
            <ListIds point={point} />
            <S.Type>
              {renderPointType(point.type)}
            </S.Type>
          </S.IdElement>
          <S.AddressTitle>
            <Tooltip title={addressTitle}>
              <S.Address>
                {point.addressStringRepresentation}
              </S.Address>
            </Tooltip>
            <S.Link href="#">
              <MapIcon />
            </S.Link>
          </S.AddressTitle>
        </S.Info>
        <S.Bucket>
          <Tooltip title={t.Planner.deleteAddressPoint} placement="top">
            <Bucket onClick={() => handleDeleteListItem(point.id, idx)} />
          </Tooltip>
        </S.Bucket>
      </S.Content>
    </S.Card>
  );
};
