/* eslint-disable no-nested-ternary */
/* eslint-disable no-unused-expressions */
/* eslint-disable @typescript-eslint/no-empty-function */
import moment from 'moment';
import React, { FC, useEffect, useState } from 'react';
import { ServiceEnum, ServiceEnumTitles } from 'modules/EmployeeApp/EmployeeApp.constants';
import { ReactComponent as Star } from 'shared/components/Images/evaluationIcons/star.svg';
import TBar from 'shared/ui/Bar/Bar';
import TTab from 'shared/ui/Tab/Tab';
import { _cn } from 'utils';
import Waypoints from '../../../../../../shared/components/Waypoints';
import { IMood, IProduct } from '../../types';

import customStart from './customStart';
import styles from './start.module.scss';

const Start: FC<Partial<IProduct>> = ({
  setRating = () => {},
  rating = 1,
  isPositive,
  icons,
  iconTitlesMap = {},
  advantageTitles = {},
  drawbackTitles = {},

  setAdvantages = () => {},
  setDrawbacks = () => {},
  setIsPositive = () => {},
  clearAdvantages = () => {},
  clearDrawbacks = () => {},
  ratingComment,
  setRatingComment = () => {},

  advantages,
  drawbacks,
  request,
  isReadOnly,
}) => {
  const drawbacksIconsKeys = icons ? Object.keys(icons.drawbacks) : [];
  const advantagesIconsKeys = icons ? Object.keys(icons.advantages) : [];

  const [reasons, setReasons] = useState<IMood['reasons'] | undefined>(icons && icons.advantages);
  const [iconsKeys, setIconsKeys] = useState<IMood['reasonKeys']>(advantagesIconsKeys);

  // eslint-disable-next-line @typescript-eslint/no-shadow
  const setMood = ({
    reasons, reasonKeys, isPositive,
  }: IMood) => {
    isPositive ? clearDrawbacks() : clearAdvantages();

    setReasons(reasons);
    setIconsKeys(reasonKeys);
    setIsPositive(isPositive);
  };

  // eslint-disable-next-line @typescript-eslint/no-shadow
  const selectReason = (key: number, isPositive: boolean) => {
    isPositive ? setAdvantages(key) : setDrawbacks(key);
  };

  useEffect(() => {
    isPositive
    && icons
    && rating <= 3
    && setMood({
      reasons: icons?.drawbacks,
      reasonKeys: drawbacksIconsKeys,
      reasonTitles: drawbackTitles,
      isPositive: false,
    });

    !isPositive
    && icons
    && rating > 3
    && setMood({
      reasons: icons?.advantages,
      reasonKeys: advantagesIconsKeys,
      reasonTitles: advantageTitles,
      isPositive: true,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rating]);

  const TitleIcon = icons && (icons?.title as FC<Record<string, unknown>>);
  const isAvailableText = (e: React.ChangeEvent<HTMLTextAreaElement>) => e.target.value.length <= 180;

  return (
    <div className={styles.rateContainer}>
      <p className={styles.rateContainer__title}>
        {!isReadOnly ? 'Пожалуйста, оцените сервис использования услуги' : 'Вы уже оценили эту поездку'}
      </p>
      <div className={styles.rateContainer__frame}>
        {!isReadOnly && (
          <div className={styles.header}>
            <div className={styles.info}>
              <div className={styles.carContainer}>
                {TitleIcon && <TitleIcon />}
                {ServiceEnumTitles[ServiceEnum[request?.transportType?.toLowerCase() as unknown as ServiceEnum]]}
              </div>
              <p>
                <b>Поездка</b>
                {' '}
                {moment(request?.desiredDate).format('DD.MM.YYYY h:mm')}
              </p>
            </div>

            <Waypoints waypoints={request?.expected?.waypoints ?? []} />
          </div>
        )}
        <div>
          <div className={_cn([styles.stars, isReadOnly && styles.starsReadonly])}>
            <p>{rating ? (rating < 4 ? 'Плохо' : 'Хорошо') : 'Ваша оценка'}</p>
            <div className={_cn([styles.startIconsContainer, isReadOnly && styles.startIconsContainerReadonly])}>
              {[...Array(5)].map(
                (l: undefined, num: number): JSX.Element => (
                  <Star
                    title={`${num + 1}`}
                    key={`_${num ** 1}`}
                    onClick={() => !isReadOnly && setRating(num + 1)}
                    className={_cn([rating > num ? (rating > 3 ? styles.goodRate : styles.badRate) : ''])}
                  />
                )
              )}
            </div>
            <p>{rating ? (rating < 4 ? 'Что было не так?' : 'Что Вам особенно понравилось?') : ''}</p>
          </div>
        </div>

        <div>
          <TBar style={customStart['tBar']}>
            {iconsKeys?.map((value: string, key: number) => {
              const Icon = reasons && (reasons[value] as FC<Record<string, unknown>>);
              const reasonsTitles = isPositive ? advantageTitles : drawbackTitles;

              return (
                <TTab
                  style={{
                    ...customStart['tTab'],
                    cursor: isReadOnly ? 'default' : 'pointer',
                  }}
                  key={key}
                  onClick={() => !isReadOnly && selectReason(key, rating > 3)}
                  isActive={advantages?.includes(iconTitlesMap[value]) || drawbacks?.includes(iconTitlesMap[value])}
                  isPositive={rating >= 4}
                  isReadOnly={isReadOnly}
                >
                  {Icon && <Icon />}
                  <p style={customStart['p']}>{reasonsTitles[iconTitlesMap[value]]}</p>
                </TTab>
              );
            })}
          </TBar>
        </div>

        {!isReadOnly && (
          <div style={customStart['textFieldContainer']}>
            <textarea
              placeholder="Что бы вы отметили?"
              value={ratingComment}
              onChange={e => isAvailableText(e) && setRatingComment(e.target.value)}
              style={customStart['textField']}
            />

            <p style={customStart['symbolLimit']}>{`${ratingComment?.length ?? 0}/180`}</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Start;
