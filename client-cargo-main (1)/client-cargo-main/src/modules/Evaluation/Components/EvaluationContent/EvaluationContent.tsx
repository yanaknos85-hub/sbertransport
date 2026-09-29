import React, { FC } from 'react';
import { Rate } from 'antd';
import TextArea from 'shared/form/Textarea/Textarea';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { icons as renderedIcons } from '../../constants';
import { TEvaluationRequest } from '../../types';
import { renderIcons, renderRateTitle } from '../../utils';
import { HeaderComponent } from '../Header/HeaderComponent';
import {
  IconBlockRequest, RequestContent, Subtitle, TextAreaBlock, Title
} from './EvaluationContent.style';

// @deprecated
interface Props {
  transportType?: TransportTypeEnum;
  data: TEvaluationRequest | null;
}

export const EvaluationContent: FC<Props> = ({ data, transportType }) => {
  if (!data) {
    return null;
  }

  const {
    rating, reasons, comment,
  } = data;

  const { rateTitle, rateSubtitle } = renderRateTitle(rating);

  const icons = renderIcons(transportType, reasons, renderedIcons);

  return (
    <div>
      <HeaderComponent title="Ваш отзыв" />
      <RequestContent>
        <Title>{rateTitle}</Title>
        <Rate
          allowClear={true}
          style={{ fontSize: '36px', color: '#DFE2E4' }}
          value={rating}
          disabled={true}
        />
        {reasons.length > 0 && (
          <>
            <Subtitle>{rateSubtitle}</Subtitle>
            <IconBlockRequest comment={comment} rating={rating}>
              {[...icons].map(({ icon }) => icon({
                fillBackground: '#F2F3F6',
                fillFont: '#C2C2C2',
                stroke: '#C2C2C2',
              })
              )}
            </IconBlockRequest>
          </>
        )}
        {comment && (
          <TextAreaBlock>
            <TextArea value={comment} disabled={true} />
          </TextAreaBlock>
        )}
      </RequestContent>
    </div>
  );
};
