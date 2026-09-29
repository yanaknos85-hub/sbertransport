import React from 'react';
import { useTranslation } from 'i18n';
import { HelperTooltip } from 'shared/components/HelperTooltip';
import { AnalyticTypes } from '../../constants/charts.constants';
import { formatHeaderContent } from '../../utils/formaters.util';
import * as Styles from './Card.styled';

const Card = ({ type, children }: { type: AnalyticTypes; children: JSX.Element }): JSX.Element => {
  const { t } = useTranslation();
  const content = formatHeaderContent(t, type);
  const { tooltips } = t.Widgets.charts;

  return (
    <Styles.CardWrapper>
      <Styles.Header>
        <Styles.Title>
          {content.title}
          {' '}
          <HelperTooltip text={tooltips[type.toLowerCase() as keyof typeof tooltips]} />
        </Styles.Title>
        <Styles.Description>{content.description}</Styles.Description>
      </Styles.Header>
      {children}
    </Styles.CardWrapper>
  );
};

export default Card;
