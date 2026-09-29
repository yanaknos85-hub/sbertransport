import React, { FC } from 'react';
import { convertToRubles, formatRubles } from 'utils';
import { useTranslation } from 'i18n';
import * as S from './Price.styles';

interface Props { regular?: boolean; sum: number; contractorName?: string }

export const Price: FC<Props> = props => {
  const {
    regular, sum, contractorName,
  } = props;

  const { t } = useTranslation();
  /* TODO  ContractInfo это p, Contractor это div, соответственно из-за данного компонента две ошибки в консоли, исправить когда будем выводить планировщик многоточки */
  return (
    <S.Price>
      <S.ContractInfo>
        <p>
          Тип заявки:
          {' '}
          <span>{regular ? 'Регулярная' : 'Разовая'}</span>
        </p>
        <S.Contractor>
          <p>
            {t.Planner.contractorTitle}
            :
            {' '}
            <span>{contractorName ?? '-'}</span>
          </p>
        </S.Contractor>
      </S.ContractInfo>
      <S.Cost>
        <S.CostTitle>{t.Planner.orderCost}</S.CostTitle>
        <p>{formatRubles(convertToRubles(sum))}</p>
      </S.Cost>
    </S.Price>
  );
};
