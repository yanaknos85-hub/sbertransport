/* eslint-disable react/no-danger */
import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { ReactComponent as Time } from 'shared/images/cargo/time.svg';
import { useAppStore } from 'stores';
import { getTime, toRubles } from 'utils';

import { TariffCost, TariffType } from 'stores/CargoTariff/CargoTariff.interface';

import * as Styled from './CargoTariffStep.style';

interface Props {
  idx: number;
  id: TariffType;
  active: boolean;
  tariff: TariffCost | null;
  logger: any;
  title: string;
  description: string;
  image: string;
  onChange: (id: number) => void;
}

const TariffComponent: FC<Props> = observer(props => {
  const {
    idx, active, tariff, description, image, onChange,
  } = props;
  const { configStore } = useAppStore();
  const IS_SDO = configStore.env.IS_SDO;

  const cost = tariff?.cost;
  const deliveryTime = tariff?.deliveryTime;
  const contragent = tariff?.contragent;
  const name = tariff?.auto?.name;

  const isDisabledTariff = !cost;

  const handleChangeTariff = () => {
    if (!isDisabledTariff) {
      onChange(idx);
    }
  };

  if (isDisabledTariff) {
    return null;
  }

  return (
    <Styled.TariffCard
      role="button"
      active={active}
      disabled={isDisabledTariff}
      onClick={() => {
        if (!isDisabledTariff) {
          handleChangeTariff();
        }
      }}
    >
      <Styled.TariffIcon>
        <Styled.TariffIconImg src={image} alt="icon" />
      </Styled.TariffIcon>
      <Styled.TariffDescription>
        <Styled.TariffTitle>
          <div>
            {tariff?.transportType.nameRus}
            {isDisabledTariff && <Styled.TariffDisabled>Тариф недоступен</Styled.TariffDisabled>}
          </div>
          {!isDisabledTariff && (
            <div style={{ whiteSpace: 'nowrap' }}>
              {cost ? `${toRubles(cost, true).toFixed(2)} ₽` : '&nbsp;'}
            </div>
          )}
          {isDisabledTariff && (
            <Styled.Popover
              title="Тариф недоступен"
              placement="topRight"
              content={(
                <Styled.PopoverContent>
                  В настоящее время тариф недоступен, выберите один из доступных или обратитесь в
                  {' '}
                  <Styled.PopoverLink href="tel:88007074882">техподдержку</Styled.PopoverLink>
                </Styled.PopoverContent>
              )}
            >
              <Styled.DisabledIcon />
            </Styled.Popover>
          )}
        </Styled.TariffTitle>
        {!isDisabledTariff && (
          <Styled.TariffText>
            <div>{description}</div>
            <Styled.TariffTime>
              <Time />
              <Styled.TariffDay
                dangerouslySetInnerHTML={{
                  __html: deliveryTime ? `&asymp; ${getTime(deliveryTime)}` : '&nbsp;',
                }}
              />
            </Styled.TariffTime>
          </Styled.TariffText>
        )}
        {name && (
          <Styled.TariffText>
            <Styled.TariffTitle>Авто: </Styled.TariffTitle>
            {name}
          </Styled.TariffText>
        )}
        {contragent && !IS_SDO && (
          <Styled.TariffText>
            <Styled.TariffTitle>Контрагент: </Styled.TariffTitle>
            {contragent}
          </Styled.TariffText>
        )}
      </Styled.TariffDescription>
    </Styled.TariffCard>
  );
});

export default TariffComponent;
